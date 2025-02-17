/*
* Copyright (c) 2023 Radiation and Nuclear Safety Authority (STUK)
*
* Use of this source code is governed by an MIT-style
* license that can be found in the LICENSE file.
*/
package fi.stuk.nuclibre;

import fi.stuk.ensdf.AdoptedDataset;
import fi.stuk.ensdf.Dataset;
import fi.stuk.ensdf.NuclideDataset;
import fi.stuk.ensdf.PeriodicTable;
import fi.stuk.ensdf.record.AlphaRecord;
import fi.stuk.ensdf.record.BetaRecord;
import fi.stuk.ensdf.record.ECRecord;
import fi.stuk.ensdf.record.GammaRecord;
import fi.stuk.ensdf.record.LevelRecord;
import fi.stuk.ensdf.record.Record;
import fi.stuk.ensdf.type.HalfLifeValue;
import java.util.List;

/**
 * EncoderUtil class contains static re-entrant utitlity methods used by the 
 * {@linkplain ENSDFNuclireEncoder}.
 * <p>
 * The methods are mostly used to look up some information from the ENSDF data, 
 * or to convert between ENSDF and nuclib conventions.
 * @author Tero Karhunen
 */
public class EncoderUtil {
    
    /**
      * Get the index of ground state level in the records of a given adopted dataset.
      * @param l the adopted dataset.
      * @return the index of the level record in the records list of the dataset.
      * If no ground state level is found, returns <code>-1</code>.
      */
    public static int getGroundStateIndex(AdoptedDataset l){        
        List<Record> records = l.getRecords();
        for(int i = 0;i < records.size();i++){
            Record r = records.get(i);
            if(r instanceof LevelRecord){
                LevelRecord lr = (LevelRecord)r;
                if(lr.getE() != null && lr.getE() == 0.0 && lr.getT() != null){
                    return(i);
                }   
            }
        }        
        return(-1);
    }
    
    /**
     * Get halflife from adopted levels.
     * @param l the adopted dataset.
     * @return the halflife.
     */
    public static HalfLifeValue getHalflife(AdoptedDataset l){        
        int gsIndex = getGroundStateIndex(l);
        if(gsIndex == -1)return(null);        
        return(((LevelRecord)l.getRecords().get(gsIndex)).getT());
    }
    
    /**
     * Check if a given decay is from a metastable state according to the DSID.
     * @param dsid the decay dataset id (ENSDF).
     * @param d the parent nuclide dataset.     
     * @return the halflife of the metastable state or <code>null</code> if
     * the decay is not from a metastable state.
     */
    public static HalfLifeValue getMetastableSymbol(String dsid, NuclideDataset d){
        String[] tok = dsid.split("\\s+");
        int start = -1;
        if(tok.length > 4  && dsid.contains("(") && dsid.contains(")")){
            for(int j = 3;j < tok.length;j++){
                String num = tok[j].replace("(", "");
                if(num.length() > 0 && Character.isDigit(num.charAt(0))){
                    start = j;
                    break;
                }
            }
            if(start == -1)return(null);
        //if(tok.length > 4  && tok[3].contains("(") && tok[4].contains(")")){        
                String sHalf = tok[start].replace("(","")+" "+tok[start+1].replace(")","");
                HalfLifeValue hl = new HalfLifeValue();                
                hl.parse(sHalf);
                HalfLifeValue hl2 = EncoderUtil.getHalflife(d.getAdoptedDataset());               
                if(hl2 == null)return(hl);
                if(hl2.isStable() && !hl.isStable())return(hl);
                double r = hl.asSeconds() / hl2.asSeconds();                
                if(r < 1.05 && r > 0.95)return(null);
                //if( r > 0.955 && r < 1.055 && s < 1)return(null);
                if(Math.abs(hl.asSeconds() / hl2.asSeconds() - 1) < 0.001d)return(null);
                return(hl);
            }
        else return(null);
    }
    
    /**
     * Get a level record with given energy from an adopted dataset.
     * @param energy the energy of the level to retrieve.
     * @param a the adopted levels dataset.
     * @return the level or <code>null</code> if no such level is found.
     */
    public static LevelRecord getLevelFromAdoptedDataset(double energy, Dataset a){
        LevelRecord ret = null;
        List<LevelRecord> g = a.getRecordsOfType(LevelRecord.class);        
        double dist = Double.MAX_VALUE;
        for(int i = 0;i < g.size();i++){
            LevelRecord r = g.get(i);
                if(r != null && r.getE() != null){
                double de = Math.abs(r.getE()-energy);
                if(de < dist){
                    dist = de;
                    ret = r;
                }            
            }
        } 
        if(dist < 1)return(ret);
        else return(null);
    }
    
        /**
     * Get the nuclibre line type corresponding to an ENSDF record.
     * @param r the emission record for the line type.
     * @return the line type.
     */
    public static String getLineType(Record r){
        if(r instanceof GammaRecord)return("G");
        else if(r instanceof BetaRecord)return("B");
        else if(r instanceof AlphaRecord)return("A");
        else if(r instanceof ECRecord)return("E");
        return(null);
    }
    
    /**
     * Get the nuclibre decay type corresponding to a ENSDF decay type.
     * @param ensdfType the ensdf decay type.
     * @return the nuclibre decay type.
     */
    public static String getNuclibreDecayType(String ensdfType){        
        return(ensdfType);
    }

        /**
     * Get the element name portion from an ENSDF NUCID (e.g. 137CS).
     * @param NUCID the NUCID to extract the element name from.
     * @return the element name.
     */
    public static String getElementName(String NUCID){
        String elem = "";
        for(int i = 0;i < NUCID.length();i++){
            char c = NUCID.charAt(i);       
            if(!Character.isDigit(c))elem += c;
        }
        return(elem);
    }
        
    /**
     * Get the isotope number (mass) from an ENSDF NUCID (e.g. 137CS).
     * @param NUCID the NUCID to extract the element name from.
     * @return the isotope number.
     */
    public static Integer getIsotope(String NUCID){
        String sIso = "";
        for(int i = 0;i < NUCID.length();i++){
            char c = NUCID.charAt(i);       
            if(Character.isDigit(c))sIso += c;
        }
        return(Integer.parseInt(sIso));
    }

    
    /**
     * Get a nuclib style nuclide id (e.g.<code> Cs-137</code>) for given ENSDF 
     * nuclide id.
     * @param NUCID the ENSDF style nuclide id (e.g. 137CS).
     * @return the nuclib style id.
     */    
    public static String getNuclibNuclideId(String NUCID){
        //String NUCID = d.getNUCID();
        Integer isotope = getIsotope(NUCID);
        String elem = getElementName(NUCID);
        elem = elem.toLowerCase();
        if(elem.length() == 0){          
            return(isotope+"");
        }
        if(elem.length() > 1)
            elem = elem.substring(0, 1).toUpperCase() + elem.substring(1);
        else elem = (elem.charAt(0)+"").toUpperCase();        
        return(elem+"-"+isotope);
    }

    /**
     * Get Q value for beta+ decay.
     * @param motherNUCID the mother nuclide NUCID (ENSDF)
     * @return the Q value in keV.
     * @throws java.lang.Exception
     */
    public static double getQPlus(String motherNUCID) throws Exception{//, String daughterNUCID){
        double m_electron =  0.00054858e6; //micro Daltons
        double amuToMeV = 931.494028;  
        Double m_mother = PeriodicTable.getMass(motherNUCID);
        if(m_mother == null)return(0);
        Integer Z = PeriodicTable.getZ(EncoderUtil.getElementName(motherNUCID));
        if(Z == null)return(0);
        int A = EncoderUtil.getIsotope(motherNUCID);
        String sym = PeriodicTable.getElement(Z-1);
        if(sym == null)return(0);
        String daughterNUCID = A+sym;
        Double m_daughter = PeriodicTable.getMass(daughterNUCID);       
        if(m_daughter == null)return(0);
        double QBplus = (m_mother - m_daughter -2*m_electron)*1e-3*amuToMeV;
        return(QBplus);
    }
    
        /**
     * Get uncertainty of Q value for beta+ decay.
     * @param motherNUCID the mother nuclide NUCID (ENSDF)
     * @return the 1-sigma absolute uncertainty Q value in keV.
     * @throws java.lang.Exception
     */
    public static double getQPlusUncertainty(String motherNUCID) throws Exception{//, String daughterNUCID){
        //double m_electron =  0.00054858e6;  //negligible uncertainty
        double amuToMeV = 931.494028;  
        Double m_mother = PeriodicTable.getMass(motherNUCID);
        if(m_mother == null)return(0);
        Double u_m_mother = PeriodicTable.getMassUncertainty(motherNUCID);
        Integer Z = PeriodicTable.getZ(EncoderUtil.getElementName(motherNUCID));
        if(Z == null)return(0);
        int A = EncoderUtil.getIsotope(motherNUCID);
        String sym = PeriodicTable.getElement(Z-1);
        if(sym == null)return(0);
        String daughterNUCID = A+sym;
        Double m_daughter = PeriodicTable.getMass(daughterNUCID);       
        if(m_daughter == null)return(0);
        Double u_m_daughter = PeriodicTable.getMassUncertainty(daughterNUCID);
        double QBplusUnc = Math.sqrt(u_m_mother*u_m_mother - u_m_daughter*u_m_daughter) *1e-3*amuToMeV;
        return(QBplusUnc);
    }
    
    /**
     * Get Q value for EC decay.
     * @param motherNUCID the mother nuclide NUCID (ENSDF)
     * @return the Q value in keV.
     * @throws java.lang.Exception
     */
    public static double getQEC(String motherNUCID) throws Exception{//, String daughterNUCID){        
        double amuToMeV = 931.494028;
        Double m_mother = PeriodicTable.getMass(motherNUCID);                
        if(m_mother == null)return(0);
        Integer Z = PeriodicTable.getZ(EncoderUtil.getElementName(motherNUCID));
        if(Z == null)return(0);
        int A = EncoderUtil.getIsotope(motherNUCID);
        String sym = PeriodicTable.getElement(Z-1);
        if(sym == null)return(0);
        String daughterNUCID = A+sym;
        Double m_daughter = PeriodicTable.getMass(daughterNUCID);        
        if(m_daughter == null)return(0);
        double Qec = (m_mother - m_daughter)*1e-3*amuToMeV;        
        return(Qec);
    }
    
    /**
     * Get uncertainty of Q value for EC decay.
     * @param motherNUCID the mother nuclide NUCID (ENSDF)
     * @return the 1-sigma absolute uncertainty of Q value in keV.
     * @throws java.lang.Exception
     */
    public static double getQECUncertainty(String motherNUCID) throws Exception{//, String daughterNUCID){        
        double amuToMeV = 931.494028;
        Double m_mother = PeriodicTable.getMass(motherNUCID);                
        if(m_mother == null)return(0);
        Double u_m_mother = PeriodicTable.getMassUncertainty(motherNUCID);                
        Integer Z = PeriodicTable.getZ(EncoderUtil.getElementName(motherNUCID));
        if(Z == null)return(0);
        int A = EncoderUtil.getIsotope(motherNUCID);
        String sym = PeriodicTable.getElement(Z-1);
        if(sym == null)return(0);
        String daughterNUCID = A+sym;
        Double m_daughter = PeriodicTable.getMass(daughterNUCID);        
        if(m_daughter == null)return(0);
        Double u_m_daughter = PeriodicTable.getMassUncertainty(daughterNUCID);
        double QecUnc = Math.sqrt(u_m_mother*u_m_mother - u_m_daughter*u_m_daughter)*1e-3*amuToMeV;      
        return(QecUnc);
    }
    
    /**
     * Round given value to given significant digits.
     * @param value the value to round.
     * @param nSigDig the number of significant digits.
     * @param dir the direction of rounding (1 = up).
     * @return the rounded value.
     */
    public static double roundToSigDigits(double value, int nSigDig, int dir) {
        double intermediate = value / Math.pow(10, Math.floor(Math.log10(Math.abs(value))) - (nSigDig - 1));
        if (dir > 0) {
            intermediate = Math.ceil(intermediate);
        } else if (dir < 0) {
            intermediate = Math.floor(intermediate);
        } else {
            intermediate = Math.round(intermediate);
        }
        double result = intermediate * Math.pow(10, Math.floor(Math.log10(Math.abs(value))) - (nSigDig - 1));
        return (result);
    }
}
