/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.stuk.ensdf.record;

/**
The Identification Record<p>
Required for all data sets.<p>
Must precede all other records.<p>
<b>Field (Col.) Name Description Reference</b><p>
1-5 NUCID Nuclide Identification V.1<p>
6-9 Must be blank<p>
10-39 DSID Data set identification V.2<p>
40-65 DSREF References to main supporting publications
and analyses V.3<p>
66-74 PUB Publication Information V.4<p>
75-80 DATE The date (year/month) when the data
set was placed in ENSDF (entered
automatically by computer) V.5
 <p>
Note: In the rare case when DSID eld is insufficient for dataset
identification it may be continued on a second identification record
with col 1-39 defined as above except that col. 6 will contain an
alphanumeric character and columns 40-80 will be blank. If there
is a continuation record, the DSID field on the first IDENTIFICATION
record must end with a ',' (comma).
 * @author Tero Karhunen
 */
public class IdentificationRecord extends Record{
    int offs = 0;
    private String NUCID;
    String DSID;
    String DSREF;
    String PUB;
    protected String DATE;

    @Override
    public void parse() {
        if(NUCID != null && isContinuation()){
            NUCID += field(1,5);
        }
        else{
            NUCID = field(1,5);
            DSID = field(9,39);
            DSREF = field(40,65);
            PUB = field(66,74);
            DATE = field(75,80);
        }
    }

    /**
     * @return the DSID
     */
    public String getDSID() {
        return DSID;
    }

    /**
     * @return the NUCID
     */
    public String getNUCID() {
        return NUCID;
    }
    
    public boolean isContinuation(){
        return(this.DSID.endsWith(","));
    }

    /**
     * @return the DATE
     */
    public String getDATE() {
        return DATE;
    }
}
