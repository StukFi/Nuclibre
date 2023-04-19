/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.stuk.ensdf;

import fi.stuk.ensdf.type.HalfLifeValue;
import fi.stuk.nuclibre.Main;

/**
 The Evaluated Nuclear Structure Data File (ENSDF) is made up of a collection of
`data sets' which present one of the following kinds of information:
<ol>
<li> The summary information for a mass chain giving information, e.g., evaluators' names and aliations, cuto date, evaluators' remarks, and publication
details, etc.<p></li>
<li>The references used in all the data sets for the given mass number. This
data set is based upon reference codes (key numbers) used in various data
sets for a given mass number and is added to the file by the NNDC.<p></li>
<li>The adopted level and gamma-ray properties for each nuclide.<p></li>
<li>The evaluated results of a single type of experiment, e.g., a radioactive decay
or a nuclear reaction for a given nuclide.<p></li>
<li>The combined evaluated results of a number of experiments of the same
kind, e.g., (Heavy ion, xn), Coulomb excitation, etc. for a given nuclide.<p></li>
</ol>
 The data sets in ENSDF are organized by their mass number. Within a mass
number the data sets are of two kinds:
 <ul>
<li>Data sets which contain information pertaining to the complete mass chain.
These data sets contain information of the type (1) and (2) given above.<p></li>
<li>Data sets belonging to a given nuclide (Z-value).<p></li>
 * </ul>
Latter data sets, i.e. for a given nuclide (Z-value), consist of the following:
<ul>
<li>A Comments data set which gives abstract information for the nuclide. This
data set contains summary information as described in (1) above. This data
set exists only if the nuclide was evaluated or updated beyond the whole
mass chain was evaluated.<p></li>
<li>Adopted data set (only one per Z-value) giving adopted properties of the
levels and gamma rays seen in that nuclide.<p></li>
<li>Data sets giving information of the type (4) or (5) above.<p></li>
</ul>
 * @author Tero Karhunen
 */ 
public class NuclideDataset {
    /** The ID of this nuclide dataset (e.g. 137CS). */
    protected String NUCID;
    
    /** The line this dataset began at in the source file. */
    protected int lineNro = -1;       
    
    /**A Comments data set which gives abstract information for the nuclide. This
data set contains summary information as described in (1) above. This data
set exists only if the nuclide was evaluated or updated beyond the whole
mass chain was evaluated.  */
    private CommentsDataset commentsDataset;

    /** The adopted levels for this nuclide dataset. */
    private AdoptedDataset adoptedDataset = null;//new AdoptedDataset();
    
    /** The decays for this nuclide dataset. */
    private DecayDataset decayDataset = new DecayDataset();
    
    /** The reactions for this nuclide dataset. */
    private ReactionDataset reactionDataset = new ReactionDataset();

    protected boolean stored = false;
    
    public NuclideDataset(String NUCID){
        this.NUCID = NUCID;        
    }

    /**
     * Set a dataset to this dataset.
     * @param d the dataset to set.
     */
    public void set(Dataset d){
        if(d instanceof AdoptedDataset)setAdoptedDataset((AdoptedDataset)d);        
        //else if(d instanceof AdoptedDataset)adoptedDataset = ((AdoptedDataset)d);        
        else if(d instanceof ReactionDataset)setReactionDataset((ReactionDataset) d);
        else if(d instanceof Reaction)reactionDataset.add((Reaction)d);        
        else if(d instanceof DecayDataset)setDecayDataset((DecayDataset) d);
        else if(d instanceof Decay)decayDataset.add((Decay)d);        
    }  
    
    /**
     * Replace or add a dataset in this dataset.
     * @param d the dataset to replace or add.
     */
    public void replace(Dataset d){
        if(d instanceof AdoptedDataset)setAdoptedDataset((AdoptedDataset)d);                
        else if(d instanceof ReactionDataset)setReactionDataset((ReactionDataset) d);
        else if(d instanceof Reaction)reactionDataset.add((Reaction)d);
        else if(d instanceof DecayDataset)setDecayDataset((DecayDataset) d);
        else if(d instanceof Decay){
            String DSID = d.getIdentificationRecord().getDSID();    
            
            //Ugly workaround:
            //sometimes ENSDF B+ decay is represented by LARA EC decay
            if(DSID.contains("EC DECAY")){
                int ind = shouldReplace(DSID);
                if(ind == -1){
                    String tryDSID = DSID.replace("EC DECAY", "B+ DECAY");
                    ind = shouldReplace(tryDSID);                    
                }
                if(ind != -1){
                    if(!Main.silent) System.out.println("\tReplace "+decayDataset.decays.get(ind).getIdentificationRecord().getDSID()+" with "+DSID);
                    this.decayDataset.decays.remove(ind);
                }
                else if(!Main.silent) System.out.println("\tAdd new decay "+DSID);
                this.decayDataset.decays.add((Decay)d);
                return;
            }
            
            //See if an existing decay should be replaced, or a new one added.
            int ind = shouldReplace(DSID);
            boolean replaced = false;
            if(ind != -1){
                    if(!Main.silent) System.out.println("\tReplace "+decayDataset.decays.get(ind).getIdentificationRecord().getDSID()+" with "+DSID);
                    this.decayDataset.decays.remove(ind);                    
                    replaced = true;
                }
                else if(!Main.silent) System.out.println("\tAdd new decay "+DSID);
                this.decayDataset.decays.add((Decay)d);
                
            //Ugly workaround:
            //there may be same decay twice in ENSDF, must replace both
            if(replaced){
                ind = shouldReplace(DSID);
                if(ind != -1 && ind != decayDataset.decays.size()-1){
                    if(!Main.silent) System.out.println("\tReplace "+decayDataset.decays.get(ind).getIdentificationRecord().getDSID()+" with "+DSID);
                    this.decayDataset.decays.remove(ind);                    
                }        
            }            
        }
    }
    
    /**
     * See if a decay should be replaced with a decay with given DSID.
     * @param DSID the DSID to replace with.
     * @return the index of the decay to replace, or <code>-1</code> if there is
     * no decay to replace.
     */
    private int shouldReplace(String DSID){        
        for(int i = 0;i < decayDataset.decays.size();i++){   
            Decay dec = decayDataset.decays.get(i);
                if(compareNames(DSID, dec.getIdentificationRecord().getDSID())){                    
                    return(i);
                }
            }
        return(-1);
    }
    
    /**
     * Compare the DSID of two decays, in order to find out if a decay should
     * be replaced. 
     * @param patchDSID the patching data DSID.
     * @param existingDSID the existing data DSID.
     * @return <code>true</code> if the DSIDs match, <code>false</code> otherwise.
     */
    private boolean compareNames(String patchDSID, String existingDSID){        
        if(patchDSID.startsWith(existingDSID))return(true);
        if(existingDSID.contains("(") && existingDSID.contains(")") && firstToksMatch(patchDSID, existingDSID,3)){
            HalfLifeValue hl1 = getHalfLifeFromDSID(existingDSID);
            HalfLifeValue hl2 = getHalfLifeFromDSID(patchDSID);
            double r = hl1.asSeconds() / hl2.asSeconds();            
            if(r < 1.1 && r > 0.9)return(true);
        }
        return(false);
    }
    
    /**
     * Compare first tokens of given strings.
     * @param s1 the first string.
     * @param s2 the second string.
     * @param n the number of tokens that must match.
     * @return <code>true</code> if the given number of first tokens match, <code>
     * false</code> otherwise.
     */
    private boolean firstToksMatch(String s1, String s2, int n){
        String[] t1 = s1.split("\\s+");
        String[] t2 = s2.split("\\s+");
        for(int i = 0;i < n;i++){
            if(!t1[i].equals(t2[i]))return(false);
        }
        return(true);
    }
    
    /**
     * Get halflife from DSID.
     * @param dsid the DSID to get the halflife from.
     * @return the half life or <code>null</code> if it cannot be determined.
     */
    private HalfLifeValue getHalfLifeFromDSID(String dsid){
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
                return(hl);
            }
        else return(null);
    }
    
    /** Get the adopted dataset.
     * @return the adoptedDataset
     */
    public AdoptedDataset getAdoptedDataset() {
        return adoptedDataset;
    }

    /** Set the adopted dataset.
     * @param adoptedDataset the adoptedDataset to set
     */
    public void setAdoptedDataset(AdoptedDataset adoptedDataset) {
        this.adoptedDataset = adoptedDataset;
    }

    /** get the decay dataset
     * @return the decayDataset
     */
    public DecayDataset getDecayDataset() {
        return decayDataset;
    }

    /** Set the decay dataset
     * @param decayDataset the decayDataset to set
     */
    public void setDecayDataset(DecayDataset decayDataset) {
        this.decayDataset = decayDataset;
    }

    /** Get the reaction dataset
     * @return the reactionDataset
     */
    public ReactionDataset getReactionDataset() {
        return reactionDataset;
    }

    /** set the reaction dataset
     * @param reactionDataset the reactionDataset to set
     */
    public void setReactionDataset(ReactionDataset reactionDataset) {
        this.reactionDataset = reactionDataset;
    }

    /** Get the NUCID of this dataset.
     * @return the NUCID.
     */
    public String getNUCID() {
        return NUCID;
    }
    
    /**
     * Get the line number this dataset started at in the source data.
     * @return the line number.
     */
    public int getLineNro(){
        return(lineNro);
    }
    
    /**
     * Set the line number this dataset started at in the source data.
     * @param lineNo the line number this dataset started at.
     */
    public void setLineNro(int lineNo){
        this.lineNro = lineNo;
    }   

    /**
     * @return the stored
     */
    public boolean isStored() {
        return stored;
    }

    /**
     * @param stored the stored to set
     */
    public void setStored(boolean stored) {
        this.stored = stored;
    }
}
