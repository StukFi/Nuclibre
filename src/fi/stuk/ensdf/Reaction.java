/*
* Copyright (c) 2023 Radiation and Nuclear Safety Authority (STUK)
*
* Use of this source code is governed by an MIT-style
* license that can be found in the LICENSE file.
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
