/*
* Copyright (c) 2023 Radiation and Nuclear Safety Authority (STUK)
*
* Use of this source code is governed by an MIT-style
* license that can be found in the LICENSE file.
*/
package fi.stuk.ensdf;

import java.util.ArrayList;
import java.util.List;

/**
<b>Decay Data Set</b>
<ol>
<li>ID record<p>
The ionization state of the atom would be in square brackets following the
nuclide symbol in the DSID field.<p>
</li>
<li>Parent record
* <ul>
<li>Energy field: level energy of the parent nucleus</li>
<li>Half-life field: half-life for the decay of the ionized itom</li>
<li>Q-value field: nuclear ground-state to ground-state value</li>
<li>New field (77-80): ionization state</li>
* </ul>
</li>
<li>Level records
<ul>
<li>Energy field: level energy of the daughter nucleus</li>
<li>MS field: atomic electron shell or subshell in which the emitted betaparticle</li>
is captured.
<li> A new quantity, "ION", giving the ionization state would be required</li>
on an "S L" record following the level record.
</ul>
</li>
<li>Daughter Adopted Levels, Gammas<p>
The adopted levels would be cross-referenced to the observed states in the
ionized atom decay dataset.<p>
</li>
<li>Parent Adopted Levels, Gammas<p>
The half-life and decay branching of the ionized atom decay would be given
as comments (analagous to the current practice for half-lifes which dier due
to chemical eects). This should be regarded as an interim solution; after
more experience is gained, methods of giving this data on level continuation
records should be derived.
</li> 
* </ol>
 * @author Tero Karhunen
 */
public class DecayDataset extends Dataset{    
    /** The decays in this dataset. */
    List<Decay> decays = new ArrayList<>();

    /**
     * Add a decay into this dataset.
     * @param d the decay to add.
     */
    public void add(Decay d){
        decays.add(d);
    }

    /**
     * Get the number of decays in this dataset.
     * @return the number of decays.
     */
    public int getSize(){
        return(decays.size());
    }        
    
    /**
     * Get the decays in this dataset.
     * @return the decays.
     */
    public List<Decay> getDecays(){
        return(decays);
    }
}
