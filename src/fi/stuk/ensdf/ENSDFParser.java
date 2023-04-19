/*
*(c) 2023 STUK - Finnish Radiation and Nuclear Safety Authority. 
*
* This source code is licensed under a
* Creative Commons Attribution 4.0 International License.
*
* You should have received a copy of the license along with this
* work.  If not, see <http://creativecommons.org/licenses/by/4.0/>. 
*/
package fi.stuk.ensdf;

import fi.stuk.ensdf.record.CommentRecord;
import fi.stuk.ensdf.record.EndRecord;
import fi.stuk.ensdf.record.IdentificationRecord;
import fi.stuk.ensdf.record.Record;
import fi.stuk.nuclibre.Main;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parser is a parser for ENSDF format nuclear datasets.
 * <p>
 * Running the parser on provided ENSDF format data will result in a
 * number of {@linkplain NuclideDataset} objects to be created. The nuclides 
 * described by the datasets are mapped by their IDs (e.g. 137CS) to the datasets
 * in {@linkplain #nuclideData}.
 * <p>
 * The data is assumed to consist of ENSDF {@linkplain Dataset}s, which in turn
 * consist of one line {@linkplain Record}s.
 * <p>
 * Once parsed, the datasets for given nuclide can be accessed by using
 * {@linkplain #getNuclideDataset(java.lang.String) } and {@linkplain #getNuclideData() }
 * <p>
 * The parsed datasets can be patched (i.e. their decays replaced or new decays added)
 * by subsequent calls to {@linkplain #patch(java.lang.String, java.io.Reader, int) }.
 * @see Dataset
 * @see Record
 * @author Tero Karhunen
 */
public class ENSDFParser {
    /** Mapping of nuclideId:s (e.g. 137CS) to their datasets.
     @see Dataset
     */
    HashMap<String, NuclideDataset> nuclideData = new HashMap<String, NuclideDataset>();
    
    /** Current dataset being parsed. */
    Dataset currentDataset = null;
    
    /** Current identification record. */
    IdentificationRecord id = new IdentificationRecord();
    
    /** Current end record. */
    EndRecord end = new EndRecord();
    
    /** Current line number in the data. */
    int ln = 1;
    
    /** Size of the current input. */
    double size = 1;
    
    /** Flag indicating that we are patching instead of initial parsing. */
    boolean patching = false;
    
    /** The origin of the data. */
    String origin;
    
    /**
     * Parse ENSDF format data using a given reader.
     * @param name name for the data to parse.
     * @param r the reader.
     * @param size input size in bytes for printing progress.
     * A size of <code>0</code> or <code>1</code> will cause the number of
     * lines processed to be printed instead of percentage.
     * @param origin the data origin.
     * @throws IOException 
     */
    public void parse(String name, Reader r, int size, String origin) throws IOException{  
        this.size = size;
        this.origin = origin;
        if(!Main.silent)System.out.println("Start parsing ENSDF "+name);
        StringBuilder buf = new StringBuilder();
        ln = 1;
        int in = -1;
        String line = null;
        while ( (in = r.read()) != -1){
            char c = (char)in;
            if(c == '\n'){
                buf.append(c);
                line = buf.toString();
                buf.delete(0, buf.length());
            }
            else buf.append(c);

            if(line != null){
                parseLine(line);
                ln++;
                line = null;
            }
        }        
        if(!Main.silent)System.out.println("Parsing done.");
    }

    /**
     * Patch the parsed data with additional data from a given reader.
     * @param name name for the data to patch with.
     * @param r the reader.
     * @param size patch data size.
     * @param origin the data origin.
     * @throws IOException 
     */
    public void patch(String name, Reader r, int size, String origin) throws IOException{  
        currentDataset = null;
        patching = true;     
        parse(name,r,size, origin);
    }      
    
    /**
     * Parse a line read from the data with the reader. The linnes should be
     * standard one line records.
     * @see Record
     * @param line the line to parse.
     */
    private void parseLine(String line){         
        if(!Main.silent){
            if(ln % 200000 == 0 && size > 1)System.out.println("\tParsed "+Math.round(((ln*80d)/size)*100)+" % so far.");
            else if(ln % 200000 == 0)System.out.println("\tParsed "+ln+" lines so far.");
        }
       
        /** Create dataset, if current dataset doesn't exist.*/         
        if(currentDataset == null && !line.trim().isEmpty()){
            int offs = id.accumulate(line);
            if(offs == 80){
                id.lineNro = ln;
                id.parse();
                currentDataset = Dataset.createDataset(id, origin);
                currentDataset.lineNro = this.ln;
                if(!patching)getNuclideDataset(currentDataset.getKey()).set(currentDataset);
                else{                    
                    NuclideDataset ds = getNuclideDataset(currentDataset.getKey());
                    ds.replace(currentDataset);
                }
                id = new IdentificationRecord();
            }
        }
        /** Parse the record of the input line to the current dataset.*/
        else{
            int offs = end.accumulate(line);
            if(end.isEndRecord()){
                currentDataset = null;
                end.clear();
            }
            else{
                char code = end.getRecordID();
                char code2 = end.getRecordID2();                
                boolean comment = end.isComment();
                end.clear();
                try {
                    Record r = null;                   
                    if(!comment){
                        r = currentDataset.createRecord(code,code2,line);
                        r.ds = currentDataset;
                    }
                    else r = new CommentRecord(code);                    
                    r.accumulate(line);
                    if(!r.isContinuation()){                       
                        r.lineNro = ln;                       
                        r.parse();                              
                        currentDataset.addRecord(r);
                    }
                    else{
                        
                        currentDataset.continueRecord(r);
                    }

                } catch (Exception ex) {
                    if(currentDataset != null && !(currentDataset instanceof Reaction)){
                        if(Main.printExceptions){
                            line = line.replaceAll("\n", "");
                            Logger.getLogger(ENSDFParser.class.getName()).log(Level.WARNING, "ENSDF parser exception at line: "+ln+"\n"+line+"\nCurrent dataset: "+currentDataset.getIdentificationRecord().getDSID(), ex);
                        }
                    }
                }
            }
        }
    }

    /**
     * Get a nuclide dataset for given nuclide ID (e.g. Cs-137). If the dataset
     * does not exist, a new one will be created.
     * @param NUCID the nuclide id (e.g. Cs-137)
     * @return the nuclide dataset
     */
    private NuclideDataset getNuclideDataset(String NUCID){
        NuclideDataset d = nuclideData.get(NUCID);
        if(d == null){
            d = new NuclideDataset(NUCID);
            d.lineNro = ln;
            nuclideData.put(NUCID, d);
        }
        return(d);
    }
    
    /**
     * Get the map from nuclide IDs to parsed nuclide datasets.
     * @return the map
     */
    public Map<String, NuclideDataset> getNuclideData(){
        return(this.nuclideData);
    }
}

