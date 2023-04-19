/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.stuk.ensdf;

import fi.stuk.ensdf.record.IdentificationRecord;

/**
 * Decay is a data entry in a decay dataset.
 * @see DecayDataset
 * @author Tero Karhunen
 */
public class Decay extends Dataset{    
    /** Key for this decay. Lazy initialization in {@linkplain #getKey()}. */
    String sId = null;
    
    /** MS is the metastable symbol. If MS is not null, then this decay is
     * from a metastable state.
     */
    private String MS = null;
    
     /** MSDestination is the metastable symbol for the daughter. 
      * If MSDestionation is not null, then this decay is to a metastable state.
     */
    protected String MSDestination = null;
    
    /** The branching directly to MS state of the daughter. */
    protected double daughterMSBraching = 0d;
    
    /**
     * Create a new decay with given identification record.
     * @param id the identification record.
     */
    public Decay(IdentificationRecord id){
        this.identificationRecord = id;
    }
    
    @Override
    public String getKey(){
        if(sId == null){
            sId = this.identificationRecord.getDSID().split("\\s+")[0];
        }
        return(sId);
    }
    
    /** Get the MS (metastable symbol) of this decay.
     * @return the MS, or <code>null</code> if the decay is not from a metastable
     * state.
     */
    public String getMS() {
        return MS;
    }

    /** Set the MS (metastable symbol) of this decay.
     * @param MS the MS to set.
     */
    public void setMS(String MS) {
        this.MS = MS;
    }    

    /**
     * @return the MSDestination
     */
    public String getMSDestination() {
        return MSDestination;
    }

    /**
     * @param MSDestination the MSDestination to set
     */
    public void setMSDestination(String MSDestination) {
        this.MSDestination = MSDestination;
    }

    /**
     * @return the daughterMSBraching
     */
    public double getDaughterMSBraching() {
        return daughterMSBraching;
    }

    /**
     * @param daughterMSBraching the daughterMSBraching to set
     */
    public void setDaughterMSBraching(double daughterMSBraching) {
        this.daughterMSBraching = daughterMSBraching;
    }
}
