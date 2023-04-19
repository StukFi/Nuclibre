/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.stuk.ensdf.record;

/**
The History Record<p>
The history records follow the Identification record and should appear in
reverse-chronological order, most recent being the first<p>
<b>Field (Col.) Name Description Reference</b><p>
1-5 NUCID Nuclide identification V.1<p>
6 Blank
Any alphanumeric character other than '1'
for continuation records<p>
7 Must be blank<p>
8 H Letter 'H' is required<p>
9 Must be blank<p>
10-80 History Dataset history consisting of various
eld descriptors and their values in cols V.25<p>
10-80 continued on any number of continuation
records. Field descriptor is followed by an '='
(without spaces before or after '=')and the value
and a terminator '$' ('$' is not needed for the
last eld descriptor).<p>
 * @author Tero Karhunen
 */
public class HistoryRecord extends Record{
    String NUCID;
    String DATASET;

    public void parse(){
        NUCID = field(1,5);
        DATASET = field(10,80);
    }
}
