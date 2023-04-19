/*
* Copyright (c) 2023 Radiation and Nuclear Safety Authority (STUK)
*
* Use of this source code is governed by an MIT-style
* license that can be found in the LICENSE file.
*/
package fi.stuk.nuclibre;

import fi.stuk.ensdf.AdoptedDataset;
import fi.stuk.ensdf.Decay;
import fi.stuk.ensdf.DecayDataset;
import fi.stuk.ensdf.ENSDFParser;
import fi.stuk.ensdf.NuclideDataset;
import fi.stuk.ensdf.PeriodicTable;
import fi.stuk.ensdf.record.AlphaRecord;
import fi.stuk.ensdf.record.BetaRecord;
import fi.stuk.ensdf.record.ECRecord;
import fi.stuk.ensdf.record.EmissionRecord;
import fi.stuk.ensdf.record.GammaRecord;
import fi.stuk.ensdf.record.IdentificationRecord;
import fi.stuk.ensdf.record.LevelRecord;
import fi.stuk.ensdf.record.NormalizationRecord;
import fi.stuk.ensdf.record.ParentRecord;
import fi.stuk.ensdf.record.QValueRecord;
import fi.stuk.ensdf.record.Record;
import fi.stuk.ensdf.type.HalfLifeValue;
import fi.stuk.ensdf.type.Uncertainty;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ENSDFNuclibreEncoder encodes the data parsed from an ENSDF file into the
 * nuclibre sqlite database.
 * <p>
 * The encoding starts from the method {@linkplain #store(fi.stuk.ensdf.ENSDFParser, java.sql.Connection)}.
 * The storing of different nuclib tables is then delegated to various methods.
 * 
 * @author Tero Karhunen
 */
public class ENSDFNuclibreEncoder {    
    /** Keep track of the metastable states represented as isomers. */
    HashMap<String, HashMap<String,HalfLifeValue>> isomers = new HashMap<>();
    
    /** Keep track of stored isomers.*/
    HashSet<String> storedIsomers = new HashSet<>();
    
    /** Keep track of nuclides, where the half-life is not evident on the nuclide dataset. It may 
     be encountered as a parent record later.*/
    HashMap<String, NuclideDataset> deferred = new HashMap<>();
    
    /** Element electron shell data. */
    ShellData[] shellData;
    
    /** Last SQL statement preformed. */
    public static String lastSQL = null;
    
     /**
     * Insert data to given table using a given connection.
     * @param c the connection to use.
     * @param table the table to insert data to.
     * @param values the values to insert.
     * @throws Exception 
     */
    private void insert(Connection c, String table, String... values) throws Exception{
        if(Main.testRun)return;
        String columnStr = "";
        String valueStr = "";
        List<String> add = new ArrayList<>();        
        for(int i = 0;i < values.length;i+=2){
            String col = values[i];
            String val = values[i+1];
            if(col != null && val != null && !val.equals("NULL") && !val.equals("null")){
                add.add(col);
                add.add(val);
            }
        }
        for(int i = 0;i < add.size();i+=2){
            String col = add.get(i);
            String val = add.get(i+1);
            columnStr += col;
            valueStr += "'"+val+"'";
            if(i != add.size()-2){
                columnStr += ",";
                valueStr += ",";
            }
        }
        String sql = "INSERT INTO "+table+" ("+columnStr+") VALUES ("+valueStr+");";        
        lastSQL = sql;
        Statement s = c.createStatement();
        s.execute(sql);
        s.close();
    }
  
    /**
     * Store nuclide information from an ENSDF nuclide dataset into nuclib.
     * @param d the dataset.
     * @param c the database connection to use.
     */
    private boolean storeNuclide(NuclideDataset d, Connection c) throws Exception{       
       String NUCID = d.getNUCID();       
       String nuclideId = EncoderUtil.getNuclibNuclideId(NUCID);       
       String Z = PeriodicTable.getZ(EncoderUtil.getElementName(NUCID))+"";
       String A = EncoderUtil.getIsotope(nuclideId)+"";
       AdoptedDataset ad = d.getAdoptedDataset();
       //If no adopted levels, bail
       if(ad == null)return(false);              
       HalfLifeValue hl = null;
       Uncertainty dhl = null;
       int gsIndex = EncoderUtil.getGroundStateIndex(ad);
       if(gsIndex != -1){
           hl = ((LevelRecord)ad.getRecords().get(gsIndex)).getT();
           dhl = ((LevelRecord)ad.getRecords().get(gsIndex)).getDT();
       }
       //If the nuclide dataset does not contain a level with halflife, place 
       //the nuclide into deferred nuclides. The halflife might later be found
       //in decays.
       if(hl == null){
           deferred.put(d.getNUCID(), d);
           return(false);
       }
       String halfLifeSeconds = hl.asSeconds()+"";
       String uncHalfLifeSeconds = null;
       if(dhl != null)uncHalfLifeSeconds = dhl.getValue()+"";
       
       QValueRecord r = d.getAdoptedDataset().getqValueRecord();
            //beta- decay energy (Q-value)
       String qMinus = "NULL";
       if(r != null && r.getQm() != null)qMinus = r.getQm()+"";            
       String uncQMinus = "NULL";
       if(r != null && r.getDQm() != null)uncQMinus = r.getDQm().getValue()+"";
            //alpha decay energy
       String qAlpha = "NULL";
       if(r != null && r.getQA() != null)qAlpha = r.getQA()+"";            
       String uncQAlpha = "NULL";
       if(r != null && r.getDQA() != null)uncQAlpha = r.getDQA().getValue()+"";
            //proton separation energy
       String SP = "NULL";
       if(r != null && r.getSP() != null)SP = r.getSP()+"";            
       String uncSP = "NULL";
       if(r != null && r.getDSP() != null)uncSP = r.getDSP().getValue()+"";
            //neutron separation energy
       String SN = "NULL";
       if(r != null && r.getSN() != null)SN = r.getSN()+"";            
       String uncSN = "NULL";
       if(r != null && r.getDSN() != null)uncSN = r.getDSN().getValue()+"";       
            //beta+ and EC decay energy
       String qPlus = EncoderUtil.roundToSigDigits(EncoderUtil.getQPlus(NUCID),1,1)+"";
       String uncQPlus = null;         
       
       String qEc = EncoderUtil.roundToSigDigits(EncoderUtil.getQEC(NUCID),1,1)+"";
       String uncQEc = null;         
            //Flag indicating this nuclide is stable
       String isStable = hl.isStable() ? "1" : "0";       
       String source = "ENSDF";       
       try{
          
       insert(c, "nuclides", 
               "nuclideId", nuclideId, "z", Z, "a", A, "halflife", halfLifeSeconds , "uncHalflife", uncHalfLifeSeconds,
               "isStable", isStable, "qMinus", qMinus, "uncQMinus", uncQMinus, "sn", SN, "uncSn",uncSN, "sp", SP, "uncSP", uncSP, 
               "qAlpha", qAlpha, "uncQAlpha", uncQAlpha, "qPlus", qPlus, "uncQPlus", uncQPlus, "qEc",qEc, "uncQEc",uncQEc,  "source", source);
       }
       catch(Exception exe){
           if(Main.printExceptions){
                String str = ("Exception during SQL:\n"+lastSQL);
                str += ("\nNuclide data originated on line: "+d.getLineNro());
                Logger.getLogger(ENSDFNuclibreEncoder.class.getName()).log(Level.WARNING, "Failed to store nuclide "+nuclideId+"\n"+str, exe);
                return(false);
           }
       }
       return(true);
    }               
    
     /**
     * Store isomer from an ENSDF nuclide dataset into nuclib.
     * @param nuclideId the nuclib nuclideId (without MS).
     * @param MS the metastable symbol.
     * @param msHalfLife the halflife of the metastable state.
     * @param d the dataset.
     * @param c the database connection to use.
     */
    private void storeIsomer(String nuclideId, String MS, HalfLifeValue msHalflife, NuclideDataset d, Connection c) throws Exception{               
       String isomerId = nuclideId+MS;
       String Z = PeriodicTable.getZ(EncoderUtil.getElementName(d.getNUCID()))+"";            
       String A = EncoderUtil.getIsotope(nuclideId)+"";                   
       String halfLifeSeconds = msHalflife.asSeconds()+"";                               
       QValueRecord r = d.getAdoptedDataset().getqValueRecord();
            //beta- decay energy (Q-value)
       String qMinus = r.getQm()+"";            
       String uncQMinus = r.getDQm().getValue()+"";       
            //alpha decay energy
       String qAlpha = r.getQA()+"";            
       String uncQAlpha = r.getDQA().getValue()+"";
            //proton separation energy
       String SP = r.getSP()+"";            
       String uncSP = r.getDSP().getValue()+"";
            //neutron separation energy
       String SN = r.getSN()+"";            
       String uncSN = r.getDSN().getValue()+"";       
            //beta+ and EC decay energy
       String qPlus = null;
       String uncQPlus = null;                     
       String isStable = "0";       
       String source = "ENSDF";
       
       insert(c, "nuclides", 
               "nuclideId", isomerId, "z",Z, "a",A, "isomer",MS, "halflife", halfLifeSeconds , 
               "isStable", isStable, "category", null, "qMinus", qMinus, "uncQMinus", uncQMinus, "sn", SN, "uncSn",uncSN, "sp", SP, "uncSP", uncSP, 
               "qAlpha", qAlpha, "uncQAlpha", uncQAlpha, "qPlus", qPlus, "uncQPlus", uncQPlus, "source", source);       
       putToIsomers(nuclideId, MS, msHalflife);
    }  
    
    /**
     * Get the assigned metastable symbol from {@linkplain #isomers} for given nuclide id and halflife.
     * @param nuclideId the nuclib nuclid id.
     * @param h the halflife.
     * @return the metastable symbol.
     */
    private String getMSForHalflife(String nuclideId, HalfLifeValue h){
        HashMap<String, HalfLifeValue> m = isomers.get(nuclideId);
        if(m == null)return("m");
        Iterator<String> i = m.keySet().iterator();
        int max = 1;
        while(i.hasNext()){
            String ms = i.next();
            HalfLifeValue hl = m.get(ms);
            double r = hl.asSeconds() / h.asSeconds();                        
            if(r > 0.9995 && r < 1.0005)return(ms);
            if(Character.isDigit(ms.charAt(ms.length()-1))){
                int n = Integer.parseInt(""+ms.charAt(ms.length()-1));
                if(n > max)max = n;
            }
        }
        max++;
        return("m"+max);
    }
    
     /**
     * Get the assigned metastable symbol from {@linkplain #isomers} for given nuclide id and halflife.
     * @param nuclideId the nuclib nuclid id.
     * @param h the halflife.
     * @return the metastable symbol.
     */
    private String getStoredMSForHalflife(String nuclideId, HalfLifeValue h){
        HashMap<String, HalfLifeValue> m = isomers.get(nuclideId);
        if(m == null)return(null);
        Iterator<String> i = m.keySet().iterator();        
        while(i.hasNext()){
            String ms = i.next();
            HalfLifeValue hl = m.get(ms);
            double r = hl.asSeconds() / h.asSeconds();                        
            if(r > 0.95 && r < 1.05)return(ms);            
        }
        return(null);
    }
    
    /**
     * See if a given nuclide has an isomer in {@linkplain #isomers} with given
     * metastable symbol.
     * @param nuclideId the nuclib nuclide id.
     * @param MS the metastable symbol.
     * @return <code>true</code> if the nuclide has that isomer, <code>false</code>
     * otherwise.
     */
    private boolean hasIsomer(String nuclideId, String MS){
        HashMap<String, HalfLifeValue> m = isomers.get(nuclideId);
        if(m == null)return(false);
        if(m.get(MS) != null)return(true);
        return(false);
    }
    
    /**
     * Put an isomer with given nuclibre nuclide id, metastable symbol and 
     * halflife into {@linkplain #isomers}.
     * @param nuclideId the nuclibre nuclide id.
     * @param MS the metastable symbol.
     * @param hl the halflife.
     */
    private void putToIsomers(String nuclideId, String MS, HalfLifeValue hl){
        HashMap<String, HalfLifeValue> m = isomers.get(nuclideId);
        if(m == null){
            m = new HashMap<>();                        
            isomers.put(nuclideId, m);
        }
        m.put(MS, hl);
    }
    
    /**
     * Store nuclide energy states (levels) to database.
     * @param d the nuclide dataset to store the states for.
     * @param c the SQL connection to use in storing.
     * @throws Exception 
     */
    private void storeStates(NuclideDataset d, Connection c) throws Exception{
        String nuclideId = EncoderUtil.getNuclibNuclideId(d.getNUCID());
        List<LevelRecord> lvls = d.getAdoptedDataset().getRecordsOfType(LevelRecord.class);
        for(int i = 0;i < lvls.size();i++){
            LevelRecord l = lvls.get(i);            
            String idState = l.getIdLevel()+"";            
            String energy = l.getE()+"";            
            String uncEnergy = "NULL";
            if(l.getDE() != null)uncEnergy = l.getDE().getValue()+"";
            String spinParity = l.getJ();
            String halflife = l.getT().asSeconds()+"";
            String uncHalflife = "NULL";
            if(l.getDT() != null)uncHalflife = l.getDT().getValue()+"";
            String isomer = l.MS;
            String s = "";
            if(l.getE() != null)
            insert(c,  "states", "nuclideId", nuclideId, 
"idState", idState, "energy", energy, "uncEnergy", uncEnergy, "spinParity", spinParity, "halflife", halflife, "uncHalflife", uncHalflife, "isomer", isomer);
        }
    }       
    
    /**
     * Store a nuclide which was added to the deferred map. A nuclide would be
     * added to the deferred map, if its halflife was not known. A nuclide should
     * be stored as deferred if the halflife becomes known via a parent record
     * being encountered.
     * @param parentNUCID The parent NUCID of the nucide
     * @param pr the parent record encountered
     * @param map the map of nuclides parsed from ENSDF.
     * @param c the database connection to use.
     * @throws Exception 
     */
    private void storeDeferred(String parentNUCID, ParentRecord pr, Map<String, NuclideDataset> map, Connection c) throws Exception{    
        NuclideDataset dpds = deferred.get(parentNUCID);
        LevelRecord gs = new LevelRecord();
        gs.setIdLevel(0);
        gs.setE((Double) 0d);
        gs.setT(pr.getT());              
        dpds.getAdoptedDataset().getRecords().add(0,gs);
        storeNuclide(dpds, c);
        storeStates(dpds, c);
//        storeDecays(dpds, c, map);
//        storeLibLines(dpds, c, map);
        deferred.remove(parentNUCID);
    }        
    
    /**
     * Check if a given decaying level with given energy is metastable. If it is, 
     * store an isomer corresponding to the level.
     * @param decayingLevel the decaying level.
     * @param dsid dataset id for the decay
     * @param parentNUCID parent ENSDF style NUCID.
     * @param decayingLevelEnergy the energy of the decaying level (keV).
     * @param parentDataset parent dataset.
     * @param c connection to database.
     * @return the metastable symbol, if the level is metastable. An empty string
     * otherwise.
     * @throws Exception 
     */
    private String checkMetastableLevel(boolean isIT, HalfLifeValue prHalf, LevelRecord decayingLevel, String dsid, String parentNUCID, double decayingLevelEnergy, NuclideDataset parentDataset, Connection c)
    throws Exception{
            //See if the decaying level is metastable
            String MS = "";            
            if(decayingLevelEnergy != -1){                
                if(decayingLevel != null && decayingLevel.MS != null)MS = decayingLevel.MS;
                MS = MS.toLowerCase();
            }
            HalfLifeValue dbcHl = EncoderUtil.getMetastableSymbol(dsid, parentDataset);
            if(isIT){                
                if(decayingLevel != null && decayingLevel.getT() != null && decayingLevel.getT().asSeconds() != 0)dbcHl = decayingLevel.getT();
                if(dbcHl == null)dbcHl = prHalf;
            }
            if(dbcHl != null){
                String nid = EncoderUtil.getNuclibNuclideId(parentNUCID);
                if(!(decayingLevel != null && decayingLevel.getE() != null && decayingLevel.getE() == 0.0))
                    MS = getMSForHalflife(nid, dbcHl);     
                else MS = "";
            }         
            
            //if the decay is from a metastable state, store appropriate isomer
            if(MS != null && MS.length() > 0){                
                MS = MS.toLowerCase();               
                String nid = EncoderUtil.getNuclibNuclideId(parentNUCID);                 
                if(!hasIsomer(nid, MS)){
                    LevelRecord l = null;
                    if(decayingLevelEnergy > -1)l = EncoderUtil.getLevelFromAdoptedDataset(decayingLevelEnergy, parentDataset.getAdoptedDataset());
                    HalfLifeValue isomerHl = null;
                    if(l != null && l.getT() != null && l.getT().asSeconds() != 0)isomerHl = l.getT();
                    else if(dbcHl != null)isomerHl = dbcHl;   
                    if(decayingLevel != null && decayingLevel.getT() != null && decayingLevel.getT().asSeconds() != 0)isomerHl = decayingLevel.getT();
                    if(!(decayingLevel != null && decayingLevel.getE() != null && decayingLevel.getE() == 0.0)){
                        storeIsomer(EncoderUtil.getNuclibNuclideId(parentNUCID), MS, isomerHl, parentDataset, c); 
                        if(decayingLevel != null)decayingLevel.MS = MS;
                    }
                    else MS = "";
                }
            }
            return(MS);
    }
    
    /** Tolerance for energy level search. */
    double TOL = 0.0002;
    
    /**
     * Get the total transition intensity from a given energy level.
     * @param levelEn the level energy.
     * @param d the decay to get the transitions for.
     * @return the total transition intensity.
     */
    private double getTotalTransInt(double levelEn, Decay d){
             List<Record> records = d.getRecords();
        Record prev = null;        
        double total = 0d;        
        for(Record r: records){
            if(r instanceof EmissionRecord){
                EmissionRecord er = (EmissionRecord)r;
                String lineType = EncoderUtil.getLineType(er);
                if(!lineType.equalsIgnoreCase("SF")){                              
                    Double en = er.getE();                     
                    LevelRecord fromLevel = null;
                    if(lineType.equals("G")){                       
                        if(prev instanceof LevelRecord){
                            fromLevel = (LevelRecord)prev;                                                                          
                        }
                        if (fromLevel != null && en != null && fromLevel.getE() != null) {
                            double dFromEn = fromLevel.getE();                            
                            if( Math.abs(dFromEn - levelEn) < TOL){  
                                if(er.getRI() != null){
                                        total += er.getRI();
                                }                                    
                            }
                        }
                    }                    
                }
            }            
            if(r instanceof LevelRecord){
                prev = r;                
            }            
        }
        return(total);
    }
    
    /**
     * Get transitions to a given energy level.
     * @param levelEn the energy level.
     * @param d the decay to find the transitions from.
     * @param add list to receive the transitions.
     * @param depth current depth in depth-first search.
     * @param iDepth current depth in depth-first search as an integer.
     * @param checkedEn set of already checked energies.
     */
    private void getTransitionsToLevel(double levelEn, Decay d, List<Transition> add, double depth, int iDepth, HashSet<Double> checkedEn){
        if(checkedEn.contains(levelEn))return;        
         List<Record> records = d.getRecords();
        Record prev = null;
        EmissionRecord prevEm = null;
        double lastAddedLevelEn = -1;
        if(dPrint)System.out.println("Find transitions for "+d.getKey()+" "+levelEn+" keV depth:"+iDepth);
        for(Record r: records){
            if(r instanceof EmissionRecord){
                EmissionRecord er = (EmissionRecord)r;
                String lineType = EncoderUtil.getLineType(er);
                if(!lineType.equalsIgnoreCase("SF")){                              
                    Double en = er.getE();                     
                    LevelRecord fromLevel = null;
                    if(lineType.equals("G")){
                        if(prev instanceof LevelRecord){
                            fromLevel = (LevelRecord)prev;                                                
                        }
                        if (fromLevel != null && en != null && fromLevel.getE() != null && fromLevel.getE() >= levelEn) {
                            double dFromEn = fromLevel.getE();
                            if(dPrint)System.out.print(iDepth+" "+d.getKey()+ " Consider level "+fromLevel.getE()+"  with emission "+en+" yield:"+er.getRI());
                            double toLevelEn = dFromEn - en;
                            double ratio = levelEn / toLevelEn;
                            if(dPrint)System.out.print(" tr. en. is "+toLevelEn+" / "+levelEn);
                            if(dPrint)System.out.print(" r: "+ratio);
                            if(iDepth == 1 && Math.abs(dFromEn - levelEn) < 0.01){
                                if( prevEm != null && prevEm.getRI() != null
                                    && fromLevel.getE() != lastAddedLevelEn){
                                    double feedingProb = prevEm.getRI()/100d;
                                    if(dPrint)System.out.print(" <-- fed directly "+(feedingProb*100d));
                                    Transition t = new Transition(fromLevel.getE(), toLevelEn, 1, feedingProb);
                                    lastAddedLevelEn = fromLevel.getE();
                                    t.isDirect = true;
                                    add.add(t);
                                }
                                else if(dPrint)System.out.print(" <-- not fed");
                            }
                            if(fromLevel.getE() > levelEn && Math.abs(ratio - 1) < TOL){  
                                if(dPrint)System.out.print(" - YES ");
                                if(dPrint && prevEm != null && prevEm.getRI() != null)System.out.print(" feed pr:"+prevEm.getRI());
                                if(dPrint && er.getRI() != null)System.out.print(" em.int:"+er.getRI());
                                if(er.getRI() != null){
                                    double feedingProb = -1;
                                    if(prevEm != null && prevEm.getRI() != null)feedingProb = prevEm.getRI();
                                    double ti = getTotalTransInt(fromLevel.getE(),d);
                                    double transitionProb = er.getRI() / ti;
                                    if(dPrint)System.out.print(" trans.prob:"+transitionProb+" (total int: "+ti);
                                    double pr = (feedingProb/100d*depth);                                    
                                    pr *= transitionProb;
                                    if(feedingProb > 0){
                                        if(dPrint)System.out.println(" < ADD");
                                        Transition t = new Transition(fromLevel.getE(), toLevelEn, transitionProb, pr);
                                        add.add(t);
                                        getTransitionsToLevel(t.fromLevelEn, d, add, transitionProb*depth, iDepth +1, checkedEn);
                                    }
                                    else{
                                        if(dPrint)System.out.println(" <-- Reachable");                                        
                                        getTransitionsToLevel(fromLevel.getE(), d, add, transitionProb*depth, iDepth +1, checkedEn);
                                    }                                                                        
                                }                                    
                            }
                            if(dPrint)System.out.println("");
                        }                        
                    }
                    else if(r instanceof BetaRecord
                            || r instanceof ECRecord)prevEm = (EmissionRecord)r;
                }
            }            
            if(r instanceof LevelRecord){
                prev = r;
                prevEm = null;
            }            
        }
        checkedEn.add(levelEn);
    }
    
    /**
     * Store an alpha, beta or gamma emission line for a given emission record.
     * @param r the emission record.
     * @param lineNum lines stored thusfar.
     * @param isDecayFromIsomer flag indicating that the decay producing the emission
     * is from an isomer.
     * @param br branching ratio.
     * @param gammaTrans receiver for gamma transitions.
     * @param parentNuclideId parent nuclib nuclide id for the decay producing the emission.
     * @param daughterNUCID daughter NUCID (ENSDF).
     * @param feedingLevel the level on the parent feeding the decay.
     * @param prev previous level record encountered.
     * @param map parsed nuclide datasets.
     * @param daughterNuclideId daughter nuclib nuclide id for the decay producing the
     * emission.
     * @param c the connection used to store the emission.
     * @return the number of lines stored thus far.
     */
    private String checkMetastableDestLevel(Decay d, String daughterNUCID,
            Map<String, NuclideDataset> map, 
            String daughterNuclideId){
        
        List<Record> records = d.getRecords();
        Record prev = null;
        TOL = 0.005;
//        if (d.getIdentificationRecord().getDSID().startsWith("234TH B- DECAY"))dPrint = true;
//        if(d.getIdentificationRecord().getDSID().startsWith("131SB B- DECAY"))dPrint = true;        
//        if (d.getIdentificationRecord().getDSID().startsWith("131I B- DECAY"))dPrint = true;
//        if (d.getIdentificationRecord().getDSID().startsWith("133SB B- DECAY"))dPrint = true;
//        if (d.getIdentificationRecord().getDSID().startsWith("133I B- DECAY"))dPrint = true;        
//        if (d.getIdentificationRecord().getDSID().startsWith("135I B- DECAY"))dPrint = true;        
//        if (d.getIdentificationRecord().getDSID().startsWith("89SR B- DECAY"))dPrint = true;  
//        if (d.getIdentificationRecord().getDSID().startsWith("99MO B- DECAY"))dPrint = true;
//        if (d.getIdentificationRecord().getDSID().startsWith("81RB EC DECAY"))dPrint = true;
//        if (d.getIdentificationRecord().getDSID().startsWith("111IN EC DECAY"))dPrint = true;
//        if (d.getIdentificationRecord().getDSID().startsWith("96PD EC DECAY"))dPrint = true;
//        else dPrint = false;
        for(Record r: records){            
            if(r instanceof EmissionRecord){
                EmissionRecord er = (EmissionRecord)r;
                String lineType = EncoderUtil.getLineType(er);
                if(!lineType.equalsIgnoreCase("SF")){                              
                    Double en = er.getE(); 
                    LevelRecord fromLevel = null;
                    if(lineType.equals("G")){
                        if(prev instanceof LevelRecord){
                            fromLevel = (LevelRecord)prev;                    
                            if(fromLevel.getE() != null)fromLevel = EncoderUtil.getLevelFromAdoptedDataset(fromLevel.getE(),d);
                        }
                        if (fromLevel != null && en != null && fromLevel.getE() != null) {
                            double toLevelEn = fromLevel.getE() - en;
                            LevelRecord toLevel = EncoderUtil.getLevelFromAdoptedDataset(toLevelEn, d);
                            if(  (toLevel != null && isMetastableLevel(toLevel, map.get(daughterNUCID)) && toLevel.getT() != null)
                               ||(fromLevel != null && isMetastableLevel(fromLevel, map.get(daughterNUCID)) && fromLevel.getT() != null)
                                    ){                                
                                LevelRecord hlRec = fromLevel;
                                if(hlRec.getT() == null || hlRec.getT().asSeconds() == 0)hlRec = fromLevel;
                                boolean isDecayToMetastable = isMetastableLevel(hlRec, map.get(daughterNUCID));
                                String MS = getStoredMSForHalflife(daughterNuclideId, hlRec.getT());                                
                                if(MS != null && !MS.isEmpty() && isDecayToMetastable){
                                    double branch = 0;       
                                    List<Transition> toIsomer = new ArrayList<>();
                                    HashSet<Double> checked = new HashSet<>();
                                     getTransitionsToLevel(hlRec.getE(), d, toIsomer, 1d, 1, checked);
                                    for(Transition t : toIsomer)branch += (t.prob);
                                    d.setDaughterMSBraching(branch);
                                    d.setMSDestination(MS);
                                    if(dPrint){
                                        System.out.println(" "+d.getIdentificationRecord().getDSID() + " IS DECAY TO ISOMER "+daughterNuclideId+MS+" With "+toIsomer.size()+" levels and branching "+(branch*100)+" %");
                                        for(Transition t : toIsomer){
                                            if(t.isDirect)System.out.println("-- directly to "+t.fromLevelEn+" ("+t.prob*100+" %)");
                                            else System.out.println("-- "+t.fromLevelEn+" -> "+t.toLevelEn+" ("+t.prob*100+" %)");
                                        }
                                    }
                                    return(MS);
                                }
                                
                            }
                        }                        
                    }
                }
            }
            if(r instanceof LevelRecord)prev = r;           
        }
        return(null);
    }    
    
    /** Flag to enable debug prints. Will be removed.*/
    boolean dPrint = false;
    
    /**
     * Store a decay into the database.
     * @param decay the decay to store.
     * @param d the nuclide 
     * @param map the map of nuclides parsed from ENSDF.
     * @param c the database connection.
     * @return <code>true</code> if the decay was stored, <code>false</code> 
     * otherwise.
     * @throws Exception 
     */
    private boolean storeDecay(Decay decay, Map<String, NuclideDataset> map, Connection c, String prevParent)
    throws Exception{
        IdentificationRecord r = decay.getIdentificationRecord();
        String daughterNUCID = r.getNUCID();        
        String dsid = r.getDSID();        
        if(!dsid.contains(":")){
            ParentRecord pr = decay.getParentRecord();            
            //If no parent record, bail.
            if(pr == null)return(false);            
            NormalizationRecord nr = decay.getNormalizationRecord();
            //If no normalization record, bail.
            if(nr == null)return(false);
            String[] tok = dsid.split("\\s+");
            // if DSID is of form 216BI B- DECAY (x H+Y H), bail
            if(tok.length > 5)return(false);
            String parentNUCID = tok[0]; 
            
            //make sure the daughters decays are stored.
            if(!daughterNUCID.equals(parentNUCID) && !daughterNUCID.equals(prevParent)){
                NuclideDataset daughter = map.get(daughterNUCID);
                if(daughter.getDecayDataset() != null && !daughter.getDecayDataset().isStored()                
                    ){
                    //System.out.print(" -> ");                    
                    storeDecays(daughter, c, map, parentNUCID);
                }
            }
            
            
            String type = tok[1];
            type = EncoderUtil.getNuclibreDecayType(type);             
            //If decay type is spontaneous fission, bail.
            if(type.equals("SF"))return(false);            
                        
            //See if the deferred nuclides map contains the parent, if so, 
            //fill its halflife and store it.
            if(deferred.containsKey(parentNUCID) && !parentNUCID.equals(daughterNUCID))storeDeferred(parentNUCID, pr, map, c);
            NuclideDataset parentDataset = map.get(parentNUCID);
            //If no parent dataset, bail.
            if(parentDataset == null)return(false);
                        
            String qValue = null;
            String uncQValue = null;
            double decayingLevelEnergy = -1;
            if(pr.getE() != null)decayingLevelEnergy = pr.getE();
            LevelRecord decayingLevel = null;
            if(parentDataset.getAdoptedDataset() != null){                
                decayingLevel = EncoderUtil.getLevelFromAdoptedDataset(decayingLevelEnergy, decay);
                if(decayingLevel == null)decayingLevel = EncoderUtil.getLevelFromAdoptedDataset(decayingLevelEnergy, parentDataset.getAdoptedDataset());
            }
            boolean isIT = type.equals("IT");            
            HalfLifeValue prHalf = pr.getT();            
            String MS = checkMetastableLevel(isIT, prHalf, decayingLevel, dsid, parentNUCID, decayingLevelEnergy, parentDataset, c);
            boolean isDecayFromIsomer = (MS != null && MS.length() > 0);
            
            Double Q = pr.getQP();            
            if(Q != null){
                qValue = Q+"";
                uncQValue = pr.getDQP().getValue()+"";
            }
            
            String uncBranching = "NULL";
            if(!nr.getDBR().isUnknown())uncBranching = nr.getDBR().getValue()+"";
            parentNUCID = EncoderUtil.getNuclibNuclideId(parentNUCID);
            if(isDecayFromIsomer){
                parentNUCID+=MS;
                decay.setMS(MS);                
            }            
            String daughterId = EncoderUtil.getNuclibNuclideId(daughterNUCID);
            String daughterMS = checkMetastableDestLevel(decay,daughterNUCID,map, daughterId);
            if(daughterMS != null && !daughterMS.isEmpty()){
                double br = decay.getDaughterMSBraching();
                if(br > 1)br = 1;
                String branching = br+"";                
                try{
                    //For B-, require that the decaying level is known.
                    if(!(type.equals("B-") && decayingLevel == null && !isDecayFromIsomer)){
                        insert(c, "decays", 
                                "parentNuclideId", parentNUCID, "daughterNuclideId", daughterId+daughterMS, "decayType", type, "qValue", qValue, "uncQValue", uncQValue, 
                                "branching", branching, "uncBranching", uncBranching, "source", decay.getOrigin());
                    }
                }
                catch(Exception exe){
                    if(Main.printExceptions){
                        String str = ("Exception during SQL:\n"+lastSQL);
                        str += ("\nDecay originated on line: "+decay.getLineNro());                    
                        Logger.getLogger(ENSDFNuclibreEncoder.class.getName()).log(Level.WARNING, "Failed to store decay "+dsid+"\n"+str, exe);
                    }               
                }
            }            
            try{             
                double br = nr.getBR()-decay.getDaughterMSBraching();
                if(br > 0){
                    String branching = br+"";                
                    //For B-, require that the decaying level is known.
                    if(!(type.equals("B-") && decayingLevel == null && !isDecayFromIsomer)){
                        insert(c, "decays", 
                                "parentNuclideId", parentNUCID, "daughterNuclideId", daughterId, "decayType", type, "qValue", qValue, "uncQValue", uncQValue, 
                                "branching", branching, "uncBranching", uncBranching, "source", decay.getOrigin());
                    }
                }
            }
            catch(Exception exe){
                if(Main.printExceptions){
                    String str = ("Exception during SQL:\n"+lastSQL);
                    str += ("\nDecay originated on line: "+decay.getLineNro());                    
                    Logger.getLogger(ENSDFNuclibreEncoder.class.getName()).log(Level.WARNING, "Failed to store decay "+dsid+"\n"+str, exe);
                }               
            }
        }
        return(true);
    }
    
    /**
     * Store decays into the nuclibre database.
     * @param d the dataset whose decays should be stored.
     * @param c the database connection used to write into the database.
     * @param map the parsed datasets.
     * @return <code>true</code> if the decays were stored fully, <code>false</code>
     * otherwise.
     * @throws Exception 
     */
    private boolean storeDecays(NuclideDataset d, Connection c, Map<String, NuclideDataset> map, String prevParent) throws Exception{        
        DecayDataset ds = d.getDecayDataset();
        List<Decay> decays = ds.getDecays();
        for(Decay decay : decays){
            if(!decay.isStored()){
                boolean stored = storeDecay(decay, map, c, prevParent);
                decay.setStored(stored);
            }
        }
        ds.setStored(true);
        return(true);
    }
    
    /**
     * Store x-rays into the database.
     * @param x the x-rays to store.
     * @param parentId the parent nuclibre id of the decay producing the x-rays.
     * @param daughterId the daughter nuclibre id of the decay producing the x-rays.      
     * @param lineNum current line identifier.
     * @param c the connection to store the x-rays with.
     * @throws Exception 
     */
    private void storeXRays(double br, XRay[] x, String parentId, String daughterId, int lineNum, Connection c) throws Exception{        
        for(int i = 0;i < x.length;i++){
            XRay r = x[i];
            if(r.lineInt > 0){                
                 insert(c, "libLines",                            
                                "nuclideId", parentId, "lineType", "X", "idLine", lineNum+"", "daughterNuclideId", daughterId,                            
                                "energy", r.energy+"", "emissionProb", 
                                ""+EncoderUtil.roundToSigDigits(r.lineInt*br,4,1), "designation", r.line);
                 lineNum++;
            }
        }
    }
    
    /**
     * Store x-rays from electron conversion and electron capture into the database.
     * @param ic the internal conversion x-rays.
     * @param ec the electron capture x-rays.
      * @param parentId the parent nuclibre id of the decay producing the x-rays.
     * @param daughterId the daughter nuclibre id of the decay producing the x-rays.      
     * @param lineNum current line identifier.
     * @param c the connection to store the x-rays with.
     * @throws Exception 
     */
    private void storeXRays(double br, XRay[] ic, XRay[] ec,  String parentId, String daughterId, int lineNum, Connection c) throws Exception{
        if(ic == null){
            storeXRays(br,ec,parentId, daughterId,lineNum, c);
            return;
        }
        else if(ec == null){
            storeXRays(br,ic, parentId, daughterId, lineNum, c);
            return;
        }
        for(int i = 0;i < ic.length;i++){
            ic[i].lineInt += ec[i].lineInt;
        }
        storeXRays(br,ic, parentId, daughterId, lineNum, c);
    }
    
    /**
     * Get x-rays resulting from electron capture.
     * @param captures the EC records corresponding to the electron captures
     * @param parentId the parent nuclibre id of the decay producing the x-rays.
     * @param daughterId the daughter nuclibre id of the decay producing the x-rays.      
     * @return the x-rays.
     */
    private XRay[] getECXRays(List<ECRecord> captures, String daughter){
        double kCaps = 0;        
        double lCaps = 0;    
        String[] elemAndWeight = daughter.split("-");
        int Z = PeriodicTable.getZ(elemAndWeight[0].toUpperCase());
        XRay[] ret = new XRay[shellData[Z].lines.size()];
        double kFluorYield = shellData[Z].Wk;
        double lFluorYield = shellData[Z].Wl;
        double totalKInt = 0;
        double totalLInt = 0;
        
        for(int i = 0;i < captures.size();i++){
            ECRecord er = captures.get(i);            
            Double KCapProb = er.getCK();
            Double LCapProb = er.getCL();
            Double IE = er.getIE();
            Double IB = er.getIB();
            if(IE == null)IE = 0d;
            if(IB == null)IB = 0d;
            IE += IB;
            if(KCapProb == null)KCapProb = 0d;
            if(LCapProb == null)LCapProb = 0d;
            kCaps += KCapProb * IE;
            lCaps += LCapProb * IE;
        }
        
        for(int i = 0;i < shellData[Z].lines.size();i++){
            ShellData.LineRecord r = shellData[Z].lines.get(i);            
            if(r.line.startsWith("K")){
                totalKInt += r.relInt ;
            }
            else if(r.line.startsWith("L")){
                totalLInt += r.relInt;
            }            
        }
        totalKInt = totalKInt / 100d;
        totalLInt = totalLInt / 100d;        
        
        for(int i = 0;i < shellData[Z].lines.size();i++){
            ShellData.LineRecord r = shellData[Z].lines.get(i);
            if(r.line.startsWith("K")){
                double lineInt = kFluorYield * kCaps * r.relInt / totalKInt / 100d ;
                ret[i] = new XRay(r.line,r.energy,lineInt);
            }
            else if(r.line.startsWith("L")){
                double lineInt = lFluorYield * lCaps * r.relInt / totalLInt / 100d;
                ret[i] = new XRay(r.line,r.energy,lineInt);
            }
            else ret[i] = new XRay(r.line,r.energy,0d);
        }
        return(ret);
    }
    
     /**
     * Get x-rays resulting from electron conversion in a gamma transition.
     * @param captures the gamma records corresponding to the gamma transitions.
     * @param parentId the parent nuclibre id of the decay producing the x-rays.
     * @param daughterId the daughter nuclibre id of the decay producing the x-rays.      
     * @return the x-rays.
     */
    private XRay[] getICXRays(List<GammaRecord> gammaTrans, String daughter){
        double kConvs = 0;        
        double lConvs = 0;                
        String[] elemAndWeight = daughter.split("-");        
        Integer Z = PeriodicTable.getZ(elemAndWeight[0].toUpperCase()); 
        if(Z == null)return(null);
        if(Z > shellData.length-1)Z = shellData.length-1;        
        XRay[] ret = new XRay[shellData[Z].lines.size()];
        double kFluorYield = shellData[Z].Wk;
        double lFluorYield = shellData[Z].Wl;
        double nKL = shellData[Z].nKL;
        
        for(int i = 0;i < gammaTrans.size();i++){
            GammaRecord gr = gammaTrans.get(i);
            Double KconvCoeff = gr.getKc();
            Double LconvCoeff = gr.getLc();           
            if(KconvCoeff == null)KconvCoeff = 0d;
            if(LconvCoeff == null)LconvCoeff = 0d;            
            double apKL = KconvCoeff;// / (1 + totalConv);
            kConvs += apKL * gr.getRI();
            lConvs += (apKL*nKL + LconvCoeff) * gr.getRI();            
        }          
        
        kConvs *= kFluorYield;        
        lConvs *= lFluorYield;
        double totalKInt = 0;
        double totalLInt = 0;        
        
        for(int i = 0;i < shellData[Z].lines.size();i++){
            ShellData.LineRecord r = shellData[Z].lines.get(i);            
            if(r.line.startsWith("K")){
                totalKInt += r.relInt ;
            }
            else if(r.line.startsWith("L")){
                totalLInt += r.relInt;
            }
        }
        totalKInt = totalKInt / 100d;
        totalLInt = totalLInt / 100d;        
        
        for(int i = 0;i < shellData[Z].lines.size();i++){
            ShellData.LineRecord r = shellData[Z].lines.get(i);
            if(r.line.startsWith("K")){
                double lineInt = kConvs * r.relInt / totalKInt / 100d;
                ret[i] = new XRay(r.line,r.energy,lineInt);
            }
            else if(r.line.startsWith("L")){
                double lineInt = lConvs * r.relInt / totalLInt / 100d;
                ret[i] = new XRay(r.line,r.energy,lineInt);
            }
            else ret[i] = new XRay(r.line,r.energy,0d);
        }
        return(ret);
    }    
    
    /**
     * Store annihilation photon and X-ray emissions from a given decay into the database.    
     * @param gammaTrans gamma transitions of the decay.
     * @param parentNuclideId the parent nuclib nuclide id of the decay.
     * @param daughterNuclideId the daughter nuclib nuclide id of the decay.
     * @param captures electron captures of the decay.
     * @param cumIb cumulative yield of annihilation photons.
     * @param lineNum number of lines stored thus far.
     * @param c the connection to store with.
     * @return the number of lines stored this far.
     * @throws Exception 
     */
    private int storeAnnihilationsAndXrays(Decay d, double br, List<GammaRecord> gammaTrans, String parentNuclideId, String daughterNuclideId,
            List<ECRecord> captures, double cumIb, int lineNum, Connection c)
                throws Exception{        
            cumIb = 0;        
            XRay[] ic = null;
            XRay[] ec = null;
            if(d.getMSDestination() != null)daughterNuclideId+= d.getMSDestination();
            if(!gammaTrans.isEmpty())ic = getICXRays(gammaTrans, daughterNuclideId);            
            if(!captures.isEmpty()){
                for(int i = 0;i < captures.size();i++){
                    ECRecord capture = captures.get(i);
                    if(capture.getIB() != null)
                        cumIb += captures.get(i).getIB();
                }
                if(cumIb != 0){
                    double intens = EncoderUtil.roundToSigDigits((2*cumIb),4,1);                    
                    if(!Double.isNaN(intens)){                       
                                insert(c, "libLines",                            
                                "nuclideId", parentNuclideId,"lineType", "G", "idLine", lineNum+"", "daughterNuclideId", daughterNuclideId,
                                "initialIdStateP", "NULL", "initialIdStateD", "NULL", 
                                "finalIdState", "NULL", "energy", "511", "uncEnergy", "NULL",
                                "emissionProb", ""+intens, "uncEmissionProb", "NULL", "designation", "annihilation");
                        lineNum++;
                    }
                }
                ec = getECXRays(captures, daughterNuclideId);                   
            }
            if(ic != null || ec != null)storeXRays(br,ic,ec, parentNuclideId, daughterNuclideId, ++lineNum, c);            
            return(lineNum);
    }
    
    boolean isMetastableLevel(LevelRecord r, NuclideDataset d){
        if(r == null)return(false);
        if(r.MS != null && !r.MS.isEmpty())return(true);
        HalfLifeValue hl = r.getT();
        if(hl != null){
            HalfLifeValue hl2 = EncoderUtil.getHalflife(d.getAdoptedDataset());
            if(hl2 != null){
                if(hl2.isStable() != hl.isStable())return(true);
                double rat = hl2.asSeconds() / hl.asSeconds();
                if(rat > 0.95 && rat < 1.05)return(false);                
                else return(true);
            }
        }
        return(false);
    }
    
    /**
     * Store an alpha, beta or gamma emission line for a given emission record.
     * @param r the emission record.
     * @param lineNum lines stored thusfar.
     * @param isDecayFromIsomer flag indicating that the decay producing the emission
     * is from an isomer.
     * @param br branching ratio.
     * @param gammaTrans receiver for gamma transitions.
     * @param parentNuclideId parent nuclib nuclide id for the decay producing the emission.
     * @param daughterNUCID daughter NUCID (ENSDF).
     * @param feedingLevel the level on the parent feeding the decay.
     * @param prev previous level record encountered.
     * @param map parsed nuclide datasets.
     * @param daughterNuclideId daughter nuclib nuclide id for the decay producing the
     * emission.
     * @param c the connection used to store the emission.
     * @return the number of lines stored thus far.
     */
    private int storeAlphaGammaOrBetaEmission(Decay d, String source, EmissionRecord er, int lineNum, boolean isDecayFromIsomer,
            double br, List<GammaRecord> gammaTrans, String parentNuclideId, String daughterNUCID,
            LevelRecord feedingLevel, LevelRecord prev, Map<String, NuclideDataset> map, 
            String daughterNuclideId, Connection c){
        
        String lineType = EncoderUtil.getLineType(er);
        if(!lineType.equalsIgnoreCase("SF")){                              
            String idLine = lineNum+"";
            String initialIdStateP = null;                    
            String initialIdStateD = null;                                                
            String finalIdState = null;                    
            Double en = er.getE();
            Uncertainty uEn = er.getDE();
            String energy = "NULL";
            String uncEnergy = "NULL";                    
            if(en != null)energy = en+"";
            if(uEn != null && en != null)uncEnergy = uEn.getValue()+"";
            Double emProb = er.getRI();                    
            String emissionProb = er.getRI()+"";
            String uncEmissionProb = "NULL";                    
            if(er.getDRI() != null && er.getRI() != null)uncEmissionProb = er.getDRI().getValue()+"";
            if(isDecayFromIsomer){
                if(er.getRI() != null)
                    if(!isDecayFromIsomer)emissionProb = (er.getRI()*br)+"";
                    else emissionProb = (er.getRI())+"";
            }
            if(er instanceof AlphaRecord && er != null && er.getRI() != null){
                emissionProb = (er.getRI()*br)+"";
            }
            LevelRecord fromLevel = null;
            LevelRecord toLevel = null;            
            if(lineType.equals("G")){
                gammaTrans.add((GammaRecord)er);
                if(prev instanceof LevelRecord){
                    fromLevel = (LevelRecord)prev;                    
                    if(fromLevel.getE() != null)fromLevel = EncoderUtil.getLevelFromAdoptedDataset(fromLevel.getE(),d);
                }
                if (fromLevel != null && en != null && fromLevel.getE() != null) {
                    double toLevelEn = fromLevel.getE() - en;
                    toLevel = EncoderUtil.getLevelFromAdoptedDataset(toLevelEn, d);//                 
                }
                if(feedingLevel != null)initialIdStateP = feedingLevel.getIdLevel()+"";
                if(fromLevel != null)initialIdStateD = fromLevel.getIdLevel()+"";
                if(toLevel != null)finalIdState = toLevel.getIdLevel()+"";
            }
            if(d.getMSDestination() != null )daughterNuclideId += d.getMSDestination();
            if(en != null && en != 0.0 && emProb != null && emProb != 0.0 && !Double.isNaN(emProb)){
                try{  
                    insert(c, "libLines",                            
                            "nuclideId", parentNuclideId,"lineType", lineType, "idLine", idLine, "daughterNuclideId", daughterNuclideId,
                            "initialIdStateP", initialIdStateP, "initialIdStateD", initialIdStateD, 
                            "finalIdState", finalIdState, "energy", energy, "uncEnergy", uncEnergy,
                            "emissionProb", emissionProb, "uncEmissionProb", uncEmissionProb, "source", source);
                    lineNum++;
                }
                catch(Exception ex){
                    if(Main.printExceptions){
                        String str = ("Exception during SQL for liblines:\n"+lastSQL);
                        Logger.getLogger(ENSDFNuclibreEncoder.class.getName()).log(Level.WARNING, "Failed to store libLine:\n"+str, ex);
                    }
                }
            }
        }     
        return(lineNum);
    }       
    
    /**
     * Store lines resulting from a given decay.
     * @param daughterNUCID the daughter NUCID (ENSDF)
     * @param daughterNuclideId the daughter nuclide id.
     * @param decay the decay.
     * @param lineNum number of lines stored thusfar.
     * @param map parsed datasets.
     * @param c the connection used to store with.
     * @return the number of lines stored thusfar.
     * @throws Exception 
     */
    private int storeLibLines(String daughterNUCID, String daughterNuclideId, Decay decay, int lineNum, Map<String, NuclideDataset> map, 
             List<ECRecord> captures, List<GammaRecord> gammaTrans,
            Connection c) throws Exception{        
            String DSID = decay.getIdentificationRecord().getDSID();            
            String parentNUCID = DSID.split("\\s+")[0];
            String parentNuclideId = EncoderUtil.getNuclibNuclideId(parentNUCID);
            ParentRecord pr = decay.getParentRecord();
            double feedingLevelEnergy = -1;
            if(pr != null && pr.getE() != null)feedingLevelEnergy = pr.getE();
            boolean reaction = false;
            if(!map.containsKey(parentNUCID))reaction = true;
            AdoptedDataset parentDataset = null;
            if(!reaction)parentDataset = map.get(parentNUCID).getAdoptedDataset();            
            LevelRecord feedingLevel = null;
            if(feedingLevelEnergy != -1 && !reaction){
                if(parentDataset != null)
                    feedingLevel = EncoderUtil.getLevelFromAdoptedDataset(feedingLevelEnergy, decay);
            }
            NormalizationRecord nr = decay.getNormalizationRecord();
            if(nr == null)return(lineNum);            
            double br = 1;
            if(nr.getBR() != null)br = nr.getBR();
            String MS = null;
            if(decay.getMS() != null)MS = decay.getMS();
            boolean isDecayFromIsomer = (MS != null && MS.length() > 0);
            if(isDecayFromIsomer)parentNuclideId+=MS.toLowerCase();
            if(isDecayFromIsomer && storedIsomers.contains(parentNuclideId+daughterNuclideId))return(lineNum);
            List<Record> recs = decay.getRecords();
            LevelRecord previousLevel = null;            
            for(Record r : recs){
                if(r instanceof EmissionRecord)
                    lineNum = storeAlphaGammaOrBetaEmission(decay, decay.getOrigin(),(EmissionRecord)r, lineNum, isDecayFromIsomer, br, gammaTrans, parentNuclideId, daughterNUCID, feedingLevel, previousLevel, map, daughterNuclideId, c);
                else if(r instanceof LevelRecord)previousLevel = (LevelRecord)r;
                if(r instanceof ECRecord){
                    ECRecord ec = ((ECRecord)r);
                    captures.add(ec);
                }
            }            
            lineNum = storeAnnihilationsAndXrays(decay,1,gammaTrans, parentNuclideId, daughterNuclideId, captures, 1d, lineNum, c);
            if(isDecayFromIsomer)storedIsomers.add(parentNuclideId+daughterNuclideId);
            return(lineNum);
    }
    
    /**
     * Store emission lines for a given nuclide dataset into the database.
     * @param d the dataset.
     * @param c the connection to store with.
     * @param map the parsed nuclide datasets.
     * @return <code>true</code> if the lines were stored, <code>false</code>
     * if the code had to bail.
     * @throws Exception 
     */
    private boolean storeLibLines(NuclideDataset d, Connection c, Map<String, NuclideDataset> map) throws Exception{
        DecayDataset ds = d.getDecayDataset();
        List<Decay> decays = ds.getDecays();
        String NUCID = d.getNUCID();        
        int lineNum = 0;        
        List<ECRecord> captures = new ArrayList<>();
        List<GammaRecord> gammaTrans = new ArrayList<>();      
        for(Decay decay : decays){             
            if(decay.isStored()){
                String daughterNuclideId = EncoderUtil.getNuclibNuclideId(decay.getIdentificationRecord().getNUCID());                   
                lineNum = storeLibLines(NUCID, daughterNuclideId, decay, lineNum, map, captures, gammaTrans, c);                               
                captures.clear();
                gammaTrans.clear();
            }
        }
        return(true);
    }
    
    /**
     * Store an ENSDF nuclide dataset into nuclib.
     * @param d the dataset to store.
     * @param c the database connection to use.
     */
    private void storeNuclidesStatesAndDecays(NuclideDataset d, Connection c, Map<String, NuclideDataset> map) throws Exception{        
        if(shellData == null)shellData = ShellData.parse();
        boolean stored = storeNuclide(d, c);
        if(stored){
            d.setStored(true);
            storeStates(d,c);
            storeDecays(d,c, map, null);
            //storeLibLines(d,c, map);
        }
    }
    
    /**
     * Store an ENSDF nuclide dataset into nuclib.
     * @param d the dataset to store.
     * @param c the database connection to use.
     */
    private void storeLines(NuclideDataset d, Connection c, Map<String, NuclideDataset> map) throws Exception{                
        if(d.isStored()){
            //d.setStored(true);
            //storeStates(d,c);
            //storeDecays(d,c, map);
            storeLibLines(d,c, map);
        }
    }
           
    /**
     * Store contents of a given ENSDF parser into a nuclib database using
     * given database connection.
     * @param p the parser whose contents to store.
     * @param nuclibConnection the database connection to use.
     * @throws java.lang.Exception if something goes wrong.
     */
    public void store(ENSDFParser p, Connection nuclibConnection) throws Exception{
        Map<String, NuclideDataset> datasets = p.getNuclideData();                
        List<NuclideDataset> dsList = new ArrayList<>();
        dsList.addAll(datasets.values());
        dsList.sort(new Comparator<NuclideDataset>(){
            @Override
            public int compare(NuclideDataset o1, NuclideDataset o2) {
                String n1 = o1.getNUCID();                
                String n2 = o2.getNUCID();
                int n1l = n1.length();
                int n2l = n2.length();
                if(n1l == n2l)                
                    return(String.CASE_INSENSITIVE_ORDER.compare(n1, n2));
                else{
                    return(Integer.compare(n1l, n2l));
                }
            }
        });
        
        Iterator<NuclideDataset> nd = dsList.iterator();//datasets.values().iterator();
        int size = datasets.size();
        int stored = 0;
        if(!Main.silent)System.out.println("Start storing to database.");
        while(nd.hasNext()){
            storeNuclidesStatesAndDecays(nd.next(),nuclibConnection,datasets);
            stored++;
            double perc = Math.round(((double)stored/2d)/((double)size)*100d);
            if(!Main.silent){
                if(stored % 300 == 0)System.out.println("\tStored "+perc+" % so far.");
            }
        }
        nd = datasets.values().iterator();
        while(nd.hasNext()){
            storeLines(nd.next(),nuclibConnection,datasets);
            stored++;
            double perc = Math.round(((double)stored/2d)/((double)size)*100d);
            if(!Main.silent){
                if(stored % 300 == 0)System.out.println("\tStored "+perc+" % so far.");
            }
        }
        if(!Main.silent)System.out.println("Storing done.");
    }
   
    /** Utility class to store intermediate X-ray information. */
    private class XRay{
        /** Line designation. */
        String line;
        /** Line energy (keV). */
        double energy;
        /** Line intensity (%). */
        double lineInt;
        
        /**
         * Create a new XRay with given line designation, energy and intensity.
         * @param line the designation.
         * @param en the energy (keV).
         * @param inte the intensity (%).
         */
        public XRay(String line, double en, double inte){
            this.line = line;
            this.energy = en;
            this.lineInt = inte;
        }
    }
    
    private class Transition{
        /** Source level en*/
        double fromLevelEn = 0;
        /** Destination level en*/
        double toLevelEn = 0;
        /** Transition intensity*/
        double intens = 0;
        /** Probability that this transition occurs*/
        double prob = 0;
        boolean isDirect = false;
        
        public Transition(double fromLevelEn, double toLevelEn, double intens, double prob){
            this.fromLevelEn = fromLevelEn;
            this.toLevelEn = toLevelEn;
            this.intens = intens;
            this.prob = prob;
        }
    }
}
