/*
*(c) 2023 STUK - Finnish Radiation and Nuclear Safety Authority. 
*
* This source code is licensed under a
* Creative Commons Attribution 4.0 International License.
*
* You should have received a copy of the license along with this
* work.  If not, see <http://creativecommons.org/licenses/by/4.0/>. 
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
