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

import fi.stuk.ensdf.Dataset;
import fi.stuk.ensdf.type.HalfLifeValue;
import fi.stuk.ensdf.type.SValue;
import fi.stuk.ensdf.type.Uncertainty;

/**
 * Record is a base class for one line record formats.
 * <p>
 * subclasses of this class will implement the different record formats.
 * <p>
In most cases, all information for a record can be placed on a single 80-column (byte)
card (record)1. A 'standard' format has been defined for each one-card record, such
that the most commonly used quantities can be placed on a single card. The standard
formats are described in this section for each record. If a needed quantity is not
included in the standard format or if a value will not fit within the field defined for
the value by the standard format, or if a record cannot be contained on a single card,
then additional cards can be prepared as described in Chapter IV (for examples,
see Appendix C and D). Note that many of the analysis programs may not process
standard fields when placed on the continuation records.
 * @author Tero Karhunen
 */
public abstract class Record {
    /** The dataset this record is added to. */
    public Dataset ds;
    
    /** The line on the input file this record was parsed from. */
    public int lineNro = 0;
    
    /** The textual content of this record. */
    protected String content = "";

    /**
     * Get the textual content of this record.
     * @return the content as string.
     */
    public String getContent(){
        return(content);
    }
    
    /**
     * Accumulate content in this record from a given fragment.
     * @param fragment the fragment to accumulate
     * @return <code>true</code> if the record is complete, <code>false</code>
     * otherwise.
     */
    public int accumulate(String fragment){
        int cl = content.length();
        int fl = fragment.length();
        int nNeeded = 80 - cl;
        if(nNeeded > fl)nNeeded = fl;
        String app = fragment.substring(0,nNeeded);
        content += app;
        return(nNeeded);
    }
    
    /** Clear this record    
     */
    public void clear(){
        this.content = "";
    }

    /**
     * Extranct an ENSDF field from a textual representation at given end and
     * start positions
     * @param s the textual representation
     * @param start start position
     * @param end end position
     * @return the field
     */
    protected String getENSDFField(String s, int start, int end){
        return(s.substring(start-1,end));
    }

    /**
     * Get a text value from a textual representation at given end and
     * start positions
     * @param s start position
     * @param e end position
     * @return the text
     */
    protected String field(int s, int e){
        return(getENSDFField(content, s, e).trim());
    }

    /**
     * Get an double value from a textual representation at given end and
     * start positions
     * @param s start position
     * @param e end position
     * @return the double
     */
    protected Double dfield(int s, int e){
        String str = getENSDFField(content, s, e).trim();
        if(str.isEmpty())return(null);        
        str = str.replace("(", "");
        str = str.replace(")", "");
        //The implicit values below are neutron and proton separation energies, 
        // they are available in the QValueRecord, should maybe handle them
        if(str.equals("WEAK"))return(0d);
        if(str.startsWith("SP+"))return(null);
        if(str.startsWith("SN+"))return(null);        
        if(str.length() > 2 && str.charAt(str.length()-2) == '+' && !Character.isDigit(str.charAt(str.length()-1)))return(null);
        if(str.length() > 2 && str.charAt(1) == '+')return(null);        
        if(str.length() == 1 && !Character.isDigit(str.charAt(0)))return(null);
        if(str.endsWith("AP"))str = str.replaceAll("AP", "");
        return(Double.parseDouble(str));
    }

    /**
     * Get an S-value from a textual representation at given end and
     * start positions
     * @param s start position
     * @param e end position
     * @return the S-value
     */
    protected SValue sfield(int s, int e){
        String str = getENSDFField(content, s, e).trim();
        if(str.isEmpty())return(null);
        return(new SValue(str));
    }

    /**
     * Get an half life value from a textual representation at given end and
     * start positions
     * @param s start position
     * @param e end position
     * @return the half life
     */
    protected HalfLifeValue hlfield(int s, int e){
        String str = getENSDFField(content, s, e).trim();        
        HalfLifeValue value = new HalfLifeValue();
        value.parse(str);
        return(value);
    }

    /**
     * Get an uncertainty value from a textual representation at given end and
     * start positions
     * @param s start position
     * @param e end position
     * @return the uncertainty
     */
    protected Uncertainty ufield(int s, int e, Double base){
        if(base == null)base = 1d;
        String str = getENSDFField(content, s, e).trim();
        Uncertainty u = new Uncertainty();
        u.parse(str, base);
        return(u);
    }
    
    /**
     * Get an uncertainty value from a textual representation at given end and
     * start positions
     * @param s start position
     * @param e end position
     * @return the uncertainty
     */
    protected Uncertainty ufield(int s, int e, HalfLifeValue base){
        if(base == null)return(null);
        String str = getENSDFField(content, s, e).trim();
        Uncertainty u = new Uncertainty();
        u.parse(str, base.asSeconds());
        return(u);
    }

    /**
     * Extranct a single char ENSDF field from a textual representation at given start position.
     * @param s the start position     
     * @return the field
     */
    protected String field(int s){
        return(field(s,s));
    }

    /**
     * Parse this record from textual content set unto this.
     */
    public void parse(){};

    /**
     * See if this record is a comment record.
     * @return <code>true</code> if this record is a comment record, <code>false
     * </code> otherwise.
     */
    public boolean isComment(){
        boolean comment = false;
        char c = this.content.charAt(6);
        comment = (c == 'c' || c == 't' || c == 'D' || c == 'd' || c == 'C');
        return(comment);
//        boolean comment2 = false;
//        c = this.content.charAt(5);
//        comment2 = (c == 'c' || c == 't' || c == 'D' || c == 'd');
//        return(comment || comment2);
    }

    /**
     * Get the record ID code for this record.
     * @return the code character
     */
    public char getRecordID(){
        char code = this.content.charAt(7);
        if(code == ' ')code = this.content.charAt(6);
        return(code);
    }
    
    /**
     * Get the second record ID code for this record.
     * @return the second code character
     */
    public char getRecordID2(){
        char code = this.content.charAt(6);
        return(code);
    }

    /**
     * See if this record is a continuation record.
     * @return <code>true</code> if this record is a continuation record, <code>false
     * </code> otherwise.
     */
    public boolean isContinuation(){
        return(!Character.isWhitespace(this.content.charAt(5)));
    }

    /**
     * Set the textual content of this record.
     * @param s the content.
     */
    public void setContent(String s){
        this.content = s;
    }
}
