/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.stuk.ensdf;

import fi.stuk.ensdf.record.IdentificationRecord;

/**
 * Reaction is data entry in a reaction dataset.
 * @see ReactionDataset
 * @author Tero Karhunen
 */
public class Reaction extends Dataset{    
    
    /**
     * Create a new reaction with given identification record.
     * @param id the identification record.
     */
    public Reaction(IdentificationRecord id){
        this.identificationRecord = id;
    }        
    
    @Override
    public String getKey(){
        return(this.identificationRecord.getNUCID());
    }
}
