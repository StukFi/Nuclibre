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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Periodic table contains element data for different elements.
 * @author Tero Karhunen
 */
public class PeriodicTable {
    /** Map of element names to elemental number. */
    final static HashMap<String, Integer> namesToZ = new HashMap<String, Integer>();
    
    /** Map of element names to atomic mass. */
    final static HashMap<String, Double> namesToMasses =  new HashMap<String, Double>();
    /**
     * Get the element number for given element name.
     * @param elemSym the element name (ENSDF).
     * @return the number, or <code>null</code> if no number is found.
     */
    public static Integer getZ(String elemSym){
        return(namesToZ.get(elemSym));
    }
    /**
     * Get the element symbol for given element number.
     * @param Z the element number.
     * @return the symbol, or <code>null</code> if no name is found.
     */
    public static String getElement(int Z){
        Iterator<Map.Entry<String, Integer>> it = namesToZ.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String, Integer> e = it.next();
            if(e.getValue() ==Z)return(e.getKey());
        }
        return(null);
    }
    /**
     * Get the atomic mass for given element name.
     * @param elemSym the element name (ENSDF).
     * @return the atomic mass in microdaltons, or <code>null</code> if no number is found.
     */
    public static Double getMass(String elemSym){
        return(namesToMasses.get(elemSym));
    }
    
    /**
     * Load mass data from table3.
     */
    private static void loadMasses(){
        File file = new File("Table3.txt");
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line = null;
            int n = 1;
            while((line = br.readLine()) != null){ 
                if(n >= 37){
                    boolean offs = false;
                    if(!Character.isWhitespace(line.charAt(0)))offs = true;
                    String[] tok = line.trim().split("\\s+");
                    int nTok = tok.length;
                    int Aind = 3;                    
                    int massInd = nTok-2;
                    if(offs){
                        Aind++;                                                
                    }                    
                    String A = tok[Aind];
                    String name = tok[Aind+1].toUpperCase();
                    String mass = tok[massInd];
                    mass = mass.replace("#", "");
                    Double dMass = Double.parseDouble(mass);
                    namesToMasses.put(A+name, dMass);                   
                }
                n++;
            }
            br.close();
            fr.close();
        } catch (Exception ex) {
            Logger.getLogger(PeriodicTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Initialize the periodic table.
     */
    static{
        namesToZ.put("H", 1);
        namesToZ.put("HE", 2);
        namesToZ.put("LI", 3);
        namesToZ.put("BE", 4);
        namesToZ.put("B", 5);
        namesToZ.put("C", 6);
        namesToZ.put("N", 7);
        namesToZ.put("O", 8);
        namesToZ.put("F", 9);
        namesToZ.put("NE", 10);
        namesToZ.put("NA", 11);
        namesToZ.put("MG", 12);
        namesToZ.put("AL", 13);
        namesToZ.put("SI", 14);
        namesToZ.put("P", 15);
        namesToZ.put("S", 16);
        namesToZ.put("CL", 17);
        namesToZ.put("AR", 18);
        namesToZ.put("K", 19);
        namesToZ.put("CA", 20);
        namesToZ.put("SC", 21);
        namesToZ.put("TI", 22);
        namesToZ.put("V", 23);
        namesToZ.put("CR", 24);
        namesToZ.put("MN", 25);
        namesToZ.put("FE", 26);
        namesToZ.put("CO", 27);
        namesToZ.put("NI", 28);
        namesToZ.put("CU", 29);
        namesToZ.put("ZN", 30);
        namesToZ.put("GA", 31);
        namesToZ.put("GE", 32);
        namesToZ.put("AS", 33);
        namesToZ.put("SE", 34);
        namesToZ.put("BR", 35);
        namesToZ.put("KR", 36);
        namesToZ.put("RB", 37);
        namesToZ.put("SR", 38);
        namesToZ.put("Y", 39);
        namesToZ.put("ZR", 40);
        namesToZ.put("NB", 41);
        namesToZ.put("MO", 42);
        namesToZ.put("TC", 43);
        namesToZ.put("RU", 44);
        namesToZ.put("RH", 45);
        namesToZ.put("PD", 46);
        namesToZ.put("AG", 47);
        namesToZ.put("CD", 48);
        namesToZ.put("IN", 49);
        namesToZ.put("SN", 50);
        namesToZ.put("SB", 51);
        namesToZ.put("TE", 52);
        namesToZ.put("I", 53);
        namesToZ.put("XE", 54);
        namesToZ.put("CS", 55);
        namesToZ.put("BA", 56);
        namesToZ.put("LA", 57);
        namesToZ.put("CE", 58);
        namesToZ.put("PR", 59);
        namesToZ.put("ND", 60);
        namesToZ.put("PM", 61);
        namesToZ.put("SM", 62);
        namesToZ.put("EU", 63);
        namesToZ.put("GD", 64);
        namesToZ.put("TB", 65);
        namesToZ.put("DY", 66);
        namesToZ.put("HO", 67);
        namesToZ.put("ER", 68);
        namesToZ.put("TM", 69);
        namesToZ.put("YB", 70);
        namesToZ.put("LU", 71);
        namesToZ.put("HF", 72);
        namesToZ.put("TA", 73);
        namesToZ.put("W", 74);
        namesToZ.put("RE", 75);
        namesToZ.put("OS", 76);
        namesToZ.put("IR", 77);
        namesToZ.put("PT", 78);
        namesToZ.put("AU", 79);
        namesToZ.put("HG", 80);
        namesToZ.put("TL", 81);
        namesToZ.put("PB", 82);
        namesToZ.put("BI", 83);
        namesToZ.put("PO", 84);
        namesToZ.put("AT", 85);
        namesToZ.put("RN", 86);
        namesToZ.put("FR", 87);
        namesToZ.put("RA", 88);
        namesToZ.put("AC", 89);
        namesToZ.put("TH", 90);
        namesToZ.put("PA", 91);
        namesToZ.put("U", 92);
        namesToZ.put("NP", 93);
        namesToZ.put("PU", 94);
        namesToZ.put("AM", 95);
        namesToZ.put("CM", 96);
        namesToZ.put("BK", 97);
        namesToZ.put("CF", 98);
        namesToZ.put("ES", 99);
        namesToZ.put("FM", 100);
        namesToZ.put("MD", 101);
        namesToZ.put("NO", 102);
        namesToZ.put("LR", 103);
        namesToZ.put("RF", 104);
        namesToZ.put("DB", 105);
        namesToZ.put("SG", 106);
        namesToZ.put("BH", 107);
        namesToZ.put("HS", 108);
        namesToZ.put("MT", 109);
        namesToZ.put("DS", 110);
        namesToZ.put("RG", 111);
        namesToZ.put("CN", 112);
        namesToZ.put("NH", 113);
        namesToZ.put("UUQ", 114);
        loadMasses();
    }
}
