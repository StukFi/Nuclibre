/*
* Copyright (c) 2023 Radiation and Nuclear Safety Authority (STUK)
*
* Use of this source code is governed by an MIT-style
* license that can be found in the LICENSE file.
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
