/*
* Copyright (c) 2023 Radiation and Nuclear Safety Authority (STUK)
*
* Use of this source code is governed by an MIT-style
* license that can be found in the LICENSE file.
*/
package fi.stuk.ensdf.record;

/**
The Reference Record<p>
Record can occur only in Reference data set.
The NNDC provides the Reference data set.<p>
<b>Field (Col.) Name Description Reference</b><p>
1-3 MASS Mass Number<p>
4-7 Must be blank<p>
8 R Letter 'R' is required<p>
9 Must be blank<p>
10-17 KEYNUM Reference key number V.3<p>
18-80 REFERENCE Abbreviated reference
(from NSR file)<p>
 * @author Tero Karhunen
 */
public class ReferenceRecord extends Record{
    String MASS;
    String R;
    String KEYNUM;
    String REFERENCE;

    @Override
    public void parse(){
        MASS = field(1,3);
        R = field(8);
        KEYNUM = field(10,17);
        REFERENCE = field(18,80);
    }
}
