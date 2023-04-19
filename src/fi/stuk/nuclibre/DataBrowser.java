/*
* Copyright (c) 2023 Radiation and Nuclear Safety Authority (STUK)
*
* Use of this source code is governed by an MIT-style
* license that can be found in the LICENSE file.
*/
package fi.stuk.nuclibre;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;

/**
 * A simple data browser for nuclibre data.
 * @author Tero Karhunen
 */
public class DataBrowser {
    /** The connection to the nuclibre datase. */
    Connection c = null;
    
    /**
     * Create a new data browser with a given connection.
     * @param c the connection.
     */
    public DataBrowser(Connection c){
        this.c = c;
    }
    
    /**
     * Utility method to print results in a <code>field=value</code> format.
     * @param s the result set to print.
     * @param pre a preamble to include
     * @param br row limit to include a break.
     * @return the number of rows printed.
     * @throws Exception 
     */
    private static int printResults(ResultSet s, String pre, int br) throws Exception{
        int ind = 0;
        int nRows = 0;
        while(s.next()){
            int n = s.getMetaData().getColumnCount();
            for(int i = 1;i < n+1;i++){
                String str = s.getString(i);
                if(str == null || str.equals("null"))str = "";
                System.out.println(pre + s.getMetaData().getColumnName(i)+"="+str);
            }            
            ind++;
            if(ind == br){
                System.out.println("");
                ind = 0;
            }
            nRows++;
        }       
        return(nRows);
    }
    
    /**
     * Get a fixed length formatted string.
     * @param string the string to format
     * @param length the length
     * @return the formatted string.
     */
    public static String pr(String string, int length) {
        return String.format("%-"+length+ "s", string);
    }
    
    /**
     * Get a fixed length formatted string with right padding.
     * @param string the string to format
     * @param length the length
     * @return the formatted string.
     */
    private static String rp(String text, int length) {
    return String.format("%-" + length + "." + length + "s", text);
    }
    
     /**
     * Utility method to print results in a tabular format.
     * @param s the result set to print.
     * @throws Exception 
     */
    private static void printResults2(ResultSet s) throws Exception{
        int ind = 0;
        while(s.next()){
            int n = s.getMetaData().getColumnCount();            
            if(ind == 0){
                for(int i = 1;i < n+1;i++){
                    System.out.print(rp(s.getMetaData().getColumnName(i),12)+"  ");
                }            
            }            
            System.out.println("");
            for(int i = 1;i < n+1;i++){
                String str = s.getString(i);
                if(str == null || str.equals("null"))str = "";
                System.out.print(rp(str,12)+"  ");
            }                        
            ind++;
        }       
    }
    
    /**
     * Utility method to print results for a given nuclib nuclide ID.
     * @param id the nuclib nuclide id.
     * @param c the connection to use to query results.
     * @throws Exception 
     */
    private static int printNuclides(String id, Connection c) throws Exception{        
        String sql = "SELECT * FROM nuclides WHERE nuclideId = '"+id+"';";        
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery(sql);        
        int n = printResults(rs,"",100);
        if(n == 0)return(0);
        System.out.println("");
        rs.close();
        s.close();
        sql = "SELECT * FROM decays WHERE parentNuclideId = '"+id+"';";
        s = c.createStatement();
        rs = s.executeQuery(sql);
        System.out.println("Decays:");
        printResults(rs,"\t",1);
        
        sql = "SELECT energy, emissionProb, daughterNuclideId, lineType, designation, source FROM libLines WHERE nuclideId = '"+id+"' AND (lineType = 'G' OR lineType = 'X' OR lineType = 'A') ORDER BY emissionProb DESC;";
        //sql = "SELECT energy, emissionProb, daughterNuclideId, lineType FROM libLines WHERE nuclideId = '"+id+"' AND (lineType = 'G' OR lineType = 'X' OR lineType = 'A') ORDER BY emissionProb DESC;";
        s = c.createStatement();
        rs = s.executeQuery(sql);
        System.out.println("Emissions:");
        printResults2(rs);
        return(1);
    }
    
    public void browse(String id) throws Exception{
        int n = printNuclides(id, c);
        if(n == 0){
            printNuclides(EncoderUtil.getNuclibNuclideId(id), c);
        }
    }         
}
