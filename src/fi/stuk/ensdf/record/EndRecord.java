/*
*(c) 2023 STUK - Finnish Radiation and Nuclear Safety Authority. 
*
* This source code is licensed under a
* Creative Commons Attribution 4.0 International License.
*
* You should have received a copy of the license along with this
* work.  If not, see <http://creativecommons.org/licenses/by/4.0/>. 
*/
package fi.stuk.ensdf.record;

/**
<p>The End Record</p>
Required for all data sets.
Must be the last record in a data set.<p>
<b>Field (Col.) Description<b>
1-80 All columns are blank<p>
 * @author Tero Karhunen
 */
public class EndRecord extends Record{
    /**
     * See if this is an end record.
     * @return <code> true</code> if it is, <code>false</code> otherwise.
     */
    public boolean isEndRecord(){
        String tc = this.content.trim();
        return(tc.isEmpty());
    }    
}
