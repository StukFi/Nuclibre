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

import fi.stuk.ensdf.type.Uncertainty;

/**
The EC (or EC + +) Record<p>
Must follow the LEVEL record for the level being populated in the decay.<p>
<b>Field (Col.) Name Description Reference</b><p>
1-5 NUCID Nuclide identication V.1<p>
6 Blank
Any alphanumeric character other than '1'
for continuation records<p>
7 Must be blank<p>
8 E Letter 'E' is required<p>
9 Must be blank<p>
10-19 E Energy for electron capture to the level V.18
Given only if measured or deduced from measured +
end-point energy<p>
20-21 DE Standard uncertainty in E V.11<p>
22-29 IB Intensity of +-decay branch1 V.13<p>
30-31 DIB Standard uncertainty in IB V.11<p>
32-39 IE Intensity of electron capture branch1 V.13<p>
40-41 DIE Standard uncertainty in IE V.11<p>
42-49 LOGFT The log ft for ( + +) transition V.9
for uniqueness given in col. 78-79<p>
50-55 DFT Standard uncertainty in LOGFT V.12<p>
65-74 TI Total ( + +) decay intensity1 V.13<p>
75-76 DTI Standard uncertainty in TI V.11<p>
77 C Comment FLAG (Letter 'C' denotes V.8
coincidence with a following radiation.
A '?' denotes probable coincidence with a
following radiation.)<p>
78-79 UN Forbiddenness classication for ; + decay, V.16
e.g., '1U', '2U'for rst, second unique forbidden.
(A blank signies an allowed or a nonunique forbidden
transition. Nonunique forbiddenness can be
indicated in col 78, with col 79 blank)
80 Q The character '?' denotes an uncertain or
questionable , + branch
Letter 'S' denotes an expected or predicted
transition<p>
1IE, IB and TI must be in the same units (see also NB in NORMALIZATION
record).
 * @author Tero Karhunen
 */
public class ECRecord extends EmissionRecord{
    String NUCID;
    String _E;
    Double E;
    Uncertainty DE;
    protected Double IB;
    protected Uncertainty DIB;
    protected Double IE;
    Uncertainty DIE;
    String LOGFT;
    String DFT;
    String TI;
    String DTI;
    String C;
    String UN;
    String Q;
    protected Double CK = null;
    protected Double CL = null;
    protected Double CM = null;
    
    @Override
    public void parse(){
        NUCID = field(1,5);
        _E = field(8);
        E = dfield(10,19);
        DE = ufield(20,21,E);
        IB = dfield(22,29);
        DIB = ufield(30,31,IB);
        IE = dfield(32,39);
        DIE = ufield(40,41,IE);
        LOGFT = field(42,49);
        DFT = field(50,55);
        TI = field(65,74);
        DTI = field(75,76);
        C = field(77);
        UN = field(78,79);
        Q = field(80);
    }

    public String getNUCID(){
        return(NUCID);
    }
    
    @Override
    public Double getE() {
        return(511.0);
    }

    @Override
    public Double getRI() {
        //return(IB);
        //System.out.println("Get RI changed to return IE instead of IB");
        if(IB != null && IE != null)return(IB+IE);
        else if (IB == null)return(IE);
        else if(IE == null)return(IB);
        else return(null);
    }

    @Override
    public Uncertainty getDE() {
        return(null);
    }

    @Override
    public Uncertainty getDRI() {
        return(DIB);
    }
    
    public void parseContinuation(){
        String[] lines = content.split("\n");
        if(lines.length > 1){
            for(int j = 1;j < lines.length;j++){
                String line = lines[j].substring(8);
                line = line.replaceAll("=", "= ");
                    line = line.trim();
                String[] tok = line.split("\\$");
                for(int i = 0;i < tok.length;i++){
                    if(tok[i].startsWith("CK=") || tok[i].startsWith("CL=") ||
                            tok[i].startsWith("CM="))parseCoeff(tok[i]);
                }
            }
        }
    }

    private void parseCoeff(String coeff){        
        String[] valueAndUnc = coeff.split("=");        
        String value[] = valueAndUnc[1].trim().split("\\s+");
        if(coeff.charAt(1) == 'K'){
            CK = Double.parseDouble(value[0]);
            
        }
        else if(coeff.charAt(1) == 'L')CL = Double.parseDouble(value[0]);
        else if(coeff.charAt(1) == 'M')CM = Double.parseDouble(value[0]);
    }

    /**
     * @return the CK
     */
    public Double getCK() {
        return CK;
    }

    /**
     * @return the CL
     */
    public Double getCL() {
        return CL;
    }

    /**
     * @return the CM
     */
    public Double getCM() {
        return CM;
    }

    /**
     * @return the IE
     */
    public Double getIE() {
        return IE;
    }

    /**
     * @return the DIB
     */
    public Uncertainty getDIB() {
        return DIB;
    }

    /**
     * @return the IB
     */
    public Double getIB() {
        return IB;
    }
}
