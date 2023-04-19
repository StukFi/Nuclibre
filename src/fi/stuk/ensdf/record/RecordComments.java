/*
* Copyright (c) 2023 Radiation and Nuclear Safety Authority (STUK)
*
* Use of this source code is governed by an MIT-style
* license that can be found in the LICENSE file.
*/
package fi.stuk.ensdf.record;

/**
Record Comments<p>
Must follow the record to which the comment pertains.<p>
<b>Field (Col.) Name Description Reference</b><p>
1-5 NUCID Nuclide identification V.1<p>
6 Blank<p>
Any alphanumeric character other
than '1' for continuation records<p>
7 C Letter 'C' or 'D' is required
See notes 4 and 5 on General Comments<p>
8 RTYPE Record type being commented upon V.6
It can be blank for Particle records<p>
9 PSYM Blank , or symbol for a particle,
e.g., N, P, etc.<p>
10-80 SYM$ or SYM = type of data being commented upon V.8
SYM,SYM,: : : ,$ Specified SYMs must be followed by a '$'
except as in note 1 below.<p>
10-80 CTEXT Text of comment follows the '$' V.7
On continuation comment records,
CTEXT may start in col. 10, and
SYM or SYMs are not repeated.
[See ENSDF Translation Dictionary,
Appendix F]<p>
NOTES:<p>
1. The old format, where SYM were specified in col. 10-19, will be accepted
without the '$' delimiter as long as col. 19 is a blank. In this case comment
text begins in col. 20.<p>
2. Record comments placed following a record of the same RTYPE refer only to
that one record. (For example, a comment record with 'CL' in cols. 7-8 and
'T$' in col. 10-11 placed following the level record for the second-excited
state refers to the half-life of only the second-excited state.)
 * @author Tero Karhunen
 */
public class RecordComments extends Record{

    String NUCID;
    String RTYPE;
    String PSYM;
    String CTEXT;

    public void parse(){
        NUCID = field(1,5);
        RTYPE = field(8);
        PSYM = field(9);
        CTEXT = field(10,80);
    }
}
