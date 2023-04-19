/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.stuk.nuclibre;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ShellData is used to parse electron shell data for elements contained in the
 * data files <code>Table1.txt</code> and <code>Table2.txt</code>. The data files
 * should reside in the working directory.
 * <p>
 * Shell data for different elements is obtained by using the static method 
 * {@linkplain #parse() }.
 * @author Tero Karhunen
 */
public class ShellData {
    /** The atomic number. */
    int Z = 0;
    
    /** The element name. */
    String element = null;
    
    /** The x-ray lines of this element. */
    List<LineRecord> lines = new ArrayList<>();
    
    /** The K-shell fluorescence yield of this element. */
    double Wk = 0;
    
    /** The mean L-shell fluorescence yield of this element. */
    double Wl = 0;
    
    /** The K-L vacancy transfer coefficient. */
    double nKL = 0;
    
    /** Current parser line. */
    static int curLine = 0;
        
    /**
     * Create a new instance of ShellData with given atomic number.
     * @param z the atomic number.
     */
    private ShellData(int z){
        this.Z = z;
    }
    
    /** 
     * Parse the x-ray energies and relative intensities from a given data file.
     * @param file the data file.
     * @param data the shell data to fill with the parsed data.
     * @throws Exception If the parsing goes wrong.
     */
    private static void parseEnergiesAndYields(File file, ShellData[] data) throws Exception{
        curLine = 1;
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String line = null;
        while((line = br.readLine()) != null){
            if(!line.startsWith("#")){
                String[] tok = line.split("\\s+");
                tok[0]= tok[0].replace("*", "").replace("%", "");
                int Z = Integer.parseInt(tok[0]);
                String elem = tok[1];
                String xline = tok[2];
                double enKev = Double.parseDouble(tok[3]);
                int relInt = Integer.parseInt(tok[4]);
                LineRecord l = new LineRecord(xline,enKev, relInt);
                data[Z].addLine(l);
                data[Z].element = elem;
            }
            curLine++;
        }
        br.close();
        fr.close();
    }
    
    /**
     * Parse a double value with uncertainty specified.
     * @param str the string to parse.
     * @return the double.
     */
    private static double parseDoubleWithUnc(String str){
        int ind = str.indexOf('(');
        return(Double.parseDouble(str.substring(0,ind)));
    }        
    
    /**
     * Parse the fluorescence yields and vacancy transfer coefficients from a given
     * data file.    
     * @param file the data file
     * @param data the shell data to fill with the parsed data.
     * @throws Exception If the parsing goes wrong.
     */
    private static void parseFluorescenceYields(File file, ShellData[] data) throws Exception{
        curLine = 1;
        try (FileReader fr = new FileReader(file); BufferedReader br = new BufferedReader(fr)) {
            String line = null;
            while((line = br.readLine()) != null){
                if(!line.startsWith("#")){
                    String[] tok = line.split("\\s+");
                    int Z = Integer.parseInt(tok[0]);
                    String elem = tok[1];
                    double Wk = parseDoubleWithUnc(tok[2]);
                    double nKL = parseDoubleWithUnc(tok[7]);
                    double Wl = 0;
                    if(!tok[8].equals("-"))Wl = parseDoubleWithUnc(tok[8]);
                    data[Z].Wk = Wk;
                    data[Z].Wl = Wl;
                    data[Z].nKL = nKL;
                    data[Z].element = elem;
                }
                curLine++;
            }
        }
    }
    
    /** Add a line to this shell data. */
    private void addLine(LineRecord r){
            lines.add(r);
    }
    
    /**
     * Parse the data files <code>Table1.txt</code> and <code>Table2.txt</code>.
     * <p>
     * The data files are assumed to be located in the working directory.
     * @return the parsed element data.
     * @throws java.io.FileNotFoundException if the data files <code>Table1.txt</code>
     * and/or <code>Table2.txt</code> are not found in the working directory.      
     * @throws Exception If anything else goes wrong.
     */
    public static ShellData[] parse() throws FileNotFoundException, Exception{
        File file1 = new File("Table1.txt");                
        File file2 = new File("Table2.txt");
        //int nData = countRows(file1,file2);
        ShellData[] data = new ShellData[120];
        for(int i = 0;i < data.length;i++)data[i] = new ShellData(i);        
        parseFluorescenceYields(file1, data);        
        parseEnergiesAndYields(file2, data);
        return(data);
    }                 
    
    public static void main(String[] args){
        
        try {
            ShellData sd[] = ShellData.parse();
            System.out.println(sd[60].element);
            System.out.println(sd[60].lines.size());            
        } catch (FileNotFoundException ex) {            
            Logger.getLogger(ShellData.class.getName()).log(Level.SEVERE, "Data file not found.", ex);        
        } catch (Exception ex) {            
            Logger.getLogger(ShellData.class.getName()).log(Level.SEVERE, "Exception parsing line "+ShellData.curLine, ex);
        }
    }        
    
    /**
     * LineRecord is an utility class representing an x-ray line of an element.
     */
    public static class LineRecord{
        /** Line designation. */
        String line = null;
        
        /** Energy in keV. */
        double energy = 0;
        
        /** Relative intensity in %. */
        int relInt = 0;
        
        /**
         * Create a new line record of given designation with given energy and
         * relative yield.
         * 
         * @param line the line designation.
         * @param en the line energy in keV.
         * @param relInt the relative intensity in %.
         */
        public LineRecord(String line, double en, int relInt){
            this.line = line;
            this.energy = en;
            this.relInt = relInt;            
        }
    }
}

 
