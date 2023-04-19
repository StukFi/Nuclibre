/*
*(c) 2023 STUK - Finnish Radiation and Nuclear Safety Authority. 
*
* This source code is licensed under a
* Creative Commons Attribution 4.0 International License.
*
* You should have received a copy of the license along with this
* work.  If not, see <http://creativecommons.org/licenses/by/4.0/>. 
*/
package fi.stuk.ensdf.type;

import java.math.BigDecimal;

/**
 * Uncertainty is an ENSDF type to used to represent uncertainties.
 * <p>
<b>DBR,DCC,DE,DHF,DIA,DIB,DIE,DIP,DNB</b>
* <p>
These two character fields, represent uncertainty in the 'standard' form in the
given quantity. The 'standard' numeric uncertainty denotes an uncertainty in the
last significant figure(s), for example, NR=0.873, DNR=11 represent a normalization
factor of 0:873  0:011, similarly QP=2.3E6, DQP=10 stand for a Q-value of
(2:3  1:0)  106 (see also General Policies given in Appendix H). The non-numeric
uncertainty, e.g, <; >; or , etc. is denoted by expressions LT, GT, and GE, etc.
The allowed forms for these fields are summarized below:
* <ol>
<li>Blank</li>
<li> An integer < 99, preferably < 25, (left or right justified)</li>
<li> One of the following expressions:<p>
LT, GT, LE, GE, AP, CA, SY
for <; >;;;, calculated, and from systematics, respectively.
</ol>
<p>
*<b> DFT,DMR,DT,DNB,DQA</b><p>
These fields allow for the specication of 'standard' asymmetric uncertainty. For
example, T=4.2 S, DT=+8-10, represent a half-life=4:2+0:8
􀀀1 s, similarly MR=-3,
DMR=+1-4 represent mixing ratio=􀀀3+1
􀀀4 meaning a range from -7 to -2. (Note:
asymmetric uncertainties add algebraically.) When the +=􀀀 construction is missing
from this field, the digits or the expressions given in this field represent either the
numeric 'standard' symmetric or the non-numeric uncertainty as described in V.11
above.
* <p>
To summarize this field, there are two cases:
<ol>
<li>Symmetric uncertainty - the field consists of an integer number or an expression
of the type described in V.11 above.</li>
<li>Asymmetric uncertainty - the field is of the form +x􀀀y, where x and y are integers</li>
</ol> 
 * @author Tero Karhunen
 */
public class Uncertainty {
    
    /** Expressions, Lower Than, Greater Than, Greater or equal etc. */
    static String[] exp = {
        "LT","GT","LE","GE","AP","CA","SY"
    };
    
    int index = -1;
    
    /** The uncertainty value. */
    double value = 0;
    
    /** Flag indicating whether the uncertainty is unknown. */
    boolean unknown = false;
    
    /** Flag indicating whether the uncertainty is asymmetric. */
    boolean asymmetric = false;
    
    /** Min value for asymmetric uncertainty ({@linkplain #value} is then the
     max value). */
    double minValue = 0;

    /**
     * Get the uncertainty value.
     * @return the value.
     */
    public Double getValue(){
        return(value);
    }
    
    /**
     * Parse an uncertainty from an ENSDF textual representation.
     * @param s the string holding the ENSDF textual representation.
     */
    public void parse(String s, double base){
        if(s.isEmpty()){
            unknown = true;
            return;
        }
        asymmetric = isAsymmetric(s);
        if(asymmetric){
            parseAsymmetric(s);
            return;
        }
        for(int i = 0;i < exp.length;i++){
            if(exp[i].equals(s)){
                index = i;
                return;
            }
        }
        value = Double.parseDouble(s);
        int scale = BigDecimal.valueOf(base).scale();
        if(scale > 0)
            value = value * Math.pow(10,-scale);
    }

    /**
     * See if the uncertainty is asymmetric interval (as opposed to Gaussian).
     * @param s the uncertainty textual representation
     * @return <code>true</code> id the uncertainty is asymmetric, <code>false</code>
     * otherwise.
     */
    private boolean isAsymmetric(String s){
        if(s.contains("+") || s.contains("-"))return(true);
        return(false);
    }

    /**
     * Parse asymmetric uncertainty from ENSDF textual representation.
     * @param s the string holding the textual representation.
     */
    private void parseAsymmetric(String s){
        
    }
    
    /**
     * See if the uncertainty is <i>unknown</i>.
     * @return <code>true</code> if the uncertainty is unknown, <code>
     * false</code> otherwise.
     */
    public boolean isUnknown(){
        return(unknown);
    }                
    
    public static void main(String[] args){
        double d = 1.2;
        Uncertainty u = new Uncertainty();
        u.parse("2", d);
        
    }
}
