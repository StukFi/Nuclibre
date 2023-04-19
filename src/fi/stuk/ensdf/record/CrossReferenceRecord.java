/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.stuk.ensdf.record;

/**
The Cross-Reference Record<p>
Given only in adopted data sets.<p>
Must precede L, G, B, E, A, DP records.<p>
<b>Field (Col.) Name Description Reference</b><p>
1-5 NUCID Nuclide identification V.1<p>
6 Blank<p>
7 Must be blank<p>
8 X Letter 'X' is required<p>
9 DSSYM Any ASCII character that uniquely
identifies the data set whose DSID is
given in col. 10-39.
10-39 DSID Must exactly match one of the DSID's used V.2<p>
40-80 Blank<p>
NOTES:<p>
1. In the Nuclear Data Sheets the DSID on the first 'X' record in the data
set will be identified with character 'A' and second DSID with 'B' and so
on irrespective of DSSYM on the X card. Only the first 14 DSID's on 'X'
records are given difierent symbols. All the rest are given the symbol 'O'
(for others). By merely reshuffling the X-records, evaluators can ascertain
the DSID's that will be identified individually. This has no effect on the file
and affects only the published output.<p>
2. If the DSID for the data set is continued on to a second card, the DSID
on XREF record must match the DSID on the the first card, including the
terminating ',' which will be translated into ellipses in the cross-reference
table in the output.<p>
3. There must be a data set corresponding every given X-record.
 * @author Tero Karhunen
 */
public class CrossReferenceRecord extends Record{
    String NUCID;
    String DSSYM;
    String DSID;

    @Override
    public void parse(){
        NUCID = field(1,5);
        DSSYM = field(9);
        DSID = field(10,39);
    }
}
