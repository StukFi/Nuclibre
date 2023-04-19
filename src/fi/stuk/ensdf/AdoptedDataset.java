/*
* Copyright (c) 2023 Radiation and Nuclear Safety Authority (STUK)
*
* Use of this source code is governed by an MIT-style
* license that can be found in the LICENSE file.
*/
package fi.stuk.ensdf;

import fi.stuk.ensdf.record.IdentificationRecord;
import fi.stuk.ensdf.record.LevelRecord;
import fi.stuk.ensdf.record.Record;

/**
 The Evaluated Nuclear Structure Data File (ENSDF) is made up of a collection of
'data sets' which present one of the following kinds of information:
 <ul>
 <li>(4) The evaluated results of a single type of experiment, e.g., a radioactive decay
or a nuclear reaction for a given nuclide.</li>
<li>(5) The combined evaluated results of a number of experiments of the same
kind, e.g., (Heavy ion, xn
), Coulomb excitation, etc. for a given nuclide.
* </li>
* </ul>
<p> 
If there is more than one data set of type (4) or (5) for a given nuclide, then
an adopted data set is required for that nuclide. If there is only one data set for
a given nuclide and no gamma-rays have been seen, then that data set is assumed
also to present the adopted properties for that nuclide. If, however, there is gamma
information known for the nuclide then a separate Adopted Levels, Gammas data set
must be given even if all the information comes from only one experiment (data set).
 * @author Tero Karhunen
 */
public class AdoptedDataset extends Dataset{
    /** An identifier for a level. The identifier is assigned to a level as 
     soon as it is parsed and added through {@linkplain #addRecord(fi.stuk.ensdf.record.Record) } */
    int idLevel = 0;
    
    /**
     * Create new adopted levels dataset for given identification record
     * @param id the identification record
     */
    public AdoptedDataset(IdentificationRecord id){
        this.identificationRecord = id;
    }
    
    @Override
    public void addRecord(Record r){
        if(r instanceof LevelRecord)((LevelRecord)r).setIdLevel(idLevel++);
        super.addRecord(r);
    }
}
