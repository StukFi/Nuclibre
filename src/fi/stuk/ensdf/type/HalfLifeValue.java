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

/**
 * HalfLifeValue is an ENSDF type used for half lives.
 * <p>
 * A HalfLifeValue consists of a <i>value</i> and a <i>unit</i>. Additionally,
 * a halflife can be <i>unknown</i> (as found out by using {@linkplain #isUnknown()},
 * or <i>stable</i> as found out by using {@linkplain #isStable()};
 * @author Tero Karhunen.
 */
public class HalfLifeValue {
    /** The value. */
    double value = 0;
    
    /** Value unit, seconds, days, months etc. */
    String unit = null;
    
    /** Flag indicating that the half-life is unknown. */
    boolean isUnknown = false;
    
    /** Flag indicating that the nuclide is stable (i.e. no half life). */
    boolean isStable = false;

    /**
     * Get the half life in seconds
     * @return the half life in seconds
     */
    public double asSeconds(){
        double mul = getSecondsMultiplier();
        return(value*mul);
    }

    /**
     * See if the halflife is considered <i>stable</i>.
     * @return <code>true</code> if the halflife is considered stable, <code>
     * false</code> otherwise.
     */
    public boolean isStable(){
        return(isStable);
    }
    
    /**
     * See if the halflife is <i>unknown</i>.
     * @return <code>true</code> if the halflife is unknown, <code>
     * false</code> otherwise.
     */
    public boolean isUnknown(){
        return(isUnknown);
    }
    
    /**
     * Get the conversion factor to convert from {@linkplain #unit} to seconds.
     * @return the multiplication factor
     */
    private double getSecondsMultiplier(){
        if(this.unit == null)return(1);
        else if(this.unit.equalsIgnoreCase("S"))return (1);
        else if(this.unit.equalsIgnoreCase("M"))return (60);
        else if(this.unit.equalsIgnoreCase("H"))return (3600);
        else if(this.unit.equalsIgnoreCase("Y"))return (31556926);
        else if(this.unit.equalsIgnoreCase("D"))return (86400);
        else if(this.unit.equalsIgnoreCase("MS"))return (1e-3);
        else if(this.unit.equalsIgnoreCase("US"))return (1e-6);
        else if(this.unit.equalsIgnoreCase("NS"))return (1e-9);
        else if(this.unit.equalsIgnoreCase("PS"))return (1e-12);
        else if(this.unit.equalsIgnoreCase("FS"))return (1e-15);
        else if(this.unit.equalsIgnoreCase("AS"))return (1e-18);
        return(1d);
    }

    /**
     * Parse a half life value from ENSDF textual representation.
     * @param s the string holding the ENSDF textual representation.
     */
    public void parse(String s){
        if(s.equalsIgnoreCase("STABLE")){
            isStable = true;
            return;
        }
        String[] tok = s.split("\\s+");
        if(tok.length == 2){
            this.value = Double.parseDouble(tok[0]);
            this.unit = tok[1];
        }
        else isUnknown = true;
    }

    @Override
    public String toString(){
        if(isUnknown)return("unknown");
        if(isStable)return("stable");
        return(value+" "+unit);
    }
}
