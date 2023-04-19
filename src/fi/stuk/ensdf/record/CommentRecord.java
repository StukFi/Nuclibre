/*
* Copyright (c) 2023 Radiation and Nuclear Safety Authority (STUK)
*
* Use of this source code is governed by an MIT-style
* license that can be found in the LICENSE file.
*/
package fi.stuk.ensdf.record;

/**
The Comment Record<p>
General Comments<p>
Must precede all L, G, B, E, A, DP records.<p>
<b>Field (Col.) Name Description Reference</b><p>
1-5 NUCID Nuclide identification V.1<p>
6 Blank<p>
Any alphanumeric character other
than '1' for continuation records<p>
7 C Letter 'C', 'D', or 'T' is required
See notes 3 - 5 below<p>
8 RTYPE Blank or record type of records to V.6
which the comment pertains<p>
9 PSYM Blank , or symbol for a (delayed-)particle,
e.g., N, P, etc.<p>
10-80 CTEXT Text of the comment. V.7<p>
[See ENSDF Translation Dictionary
(Appendix F)]<p>
NOTES:<p>
1. The comment refers only to records of specified RTYPE given in that data
set. The comment will normally appear only in the table for that RTYPE
in the output. For example, if the comment is on levels ('L' in col. 8) it will
appear only in the level properties table.<p>
2. If col. 8 and 9 are blank then the comment refers to the whole data set.
These general comments precede formatted level or the radiation records.
See Appendix B for use of comment records in COMMENTS data set.<p>
3. Letter 'T' in place of 'C' in col. 7 of a comment record indicates to the output
programs that this record should be reproduced 'as is' and the blanks in the
record should not be squeezed out.<p>
4. Letter 'D' in place of 'C' in col. 7 of a comment record indicates to the
output programs that this is a documentation record and can be ignored.
This record will also be ignored by the various analysis programs.<p>
5. Lower case letters 'c' and 't' in col. 7 of a comment record indicate to the
output programs that CTEXT in these records should not be translated.
These will appear as written in the Nuclear Data Sheets. In this mode one
must write special characters directly, for example, 'jg' for
, 'f+238gPu'
for 238Pu. See Appendix A for list of special characters.<p>
 * @author Tero Karhunen
 */
public class CommentRecord extends Record{
    char code = ' ';
    String NUCID;
    String RTYPE;
    String PSYM;
    String CTEXT;

    public CommentRecord(char code){
        this.code = code;
    }

    public char getCode(){
        return(code);
    }

    public void parse(){
        NUCID = field(1,5);
        RTYPE = field(8);
        PSYM = field(9);
        CTEXT = field(10,80);
    }

    public String getCTEXT(){
        return(CTEXT);
    }

    public void setCTEXT(String s){
        this.CTEXT = s;
    }
}
