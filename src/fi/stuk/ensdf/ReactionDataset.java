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

import java.util.ArrayList;
import java.util.List;

/**
 The evaluated results of a single type of experiment, e.g., a radioactive decay
or a nuclear reaction for a given nuclide.<p>
The combined evaluated results of a number of experiments of the same
kind, e.g., (Heavy ion, xn), Coulomb excitation, etc. for a given nuclide.
 * @author Tero Karhunen
 */ 
public class ReactionDataset extends Dataset{
    /** The reactions in this dataset. */
    List<Reaction> reactions = new ArrayList<>();

    /**
     * Add a reaction to this dataset.
     * @param r the reaction to add.
     */
    public void add(Reaction r){
        reactions.add(r);
    }

    /**
     * Get the number of reactions in this dataset.
     * @return the number of reactions.
     */
    public int getSize(){
        return(reactions.size());
    }
}
