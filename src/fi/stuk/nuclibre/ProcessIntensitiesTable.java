/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.stuk.nuclibre;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author 03054231
 */
public class ProcessIntensitiesTable {
    public static void main(String[] args){
        ShellData sd[];
        try {
            sd = ShellData.parse();
               System.out.println(sd[60].element);
            System.out.println(sd[60].lines.size());            
            ShellData.LineRecord lr = sd[60].lines.get(1);
         
        } catch (Exception ex) {
            Logger.getLogger(ProcessIntensitiesTable.class.getName()).log(Level.SEVERE, null, ex);
        }
         
//        IntensityRecord[] r = new IntensityRecord[100];
//        for(int i = 0;i < r.length;i++)r[i] = new IntensityRecord(i);
//        
//        File file = new File("X-ray_relative_intensities.txt");
//        try {
//            FileReader fr = new FileReader(file);
//            BufferedReader br = new BufferedReader(fr);
//            String line = null;
//            while((line = br.readLine()) != null){
//                String[] tok = line.split("\\s+");
//                String sEn = tok[0];
//                if(sEn != null && !sEn.isEmpty()){
//                    char c = sEn.charAt(0);
//                    if(Character.isDigit(c)){
//                        double enKev = Double.parseDouble(sEn.replace(",", ""));
//                        int Z = Integer.parseInt(tok[1]);
//                        String elem = tok[2];
//                        String sLine = tok[3];
//                        int relInt = Integer.parseInt(tok[4]);
//                        LineRecord l = new LineRecord(sLine, enKev, relInt);
//                        r[Z].addLine(l);
//                        r[Z].setElement(elem);
//                    }
//                }
//            }
//        } catch (Exception ex) {
//            Logger.getLogger(ProcessIntensitiesTable.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        
//        DecimalFormat df = new DecimalFormat();
//        df.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
//        df.setMaximumFractionDigits(3);
//        for(int i = 0;i < r.length;i++){
//            IntensityRecord ir = r[i];
//            if(ir.element != null){
//                for(int j = 0;j < ir.lines.size();j++){
//                    LineRecord lr = ir.lines.get(j);
//                    System.out.println(rp(ir.Z+" "+ir.element,6)+" "+rp(lr.line,8)+" "+rp(df.format(lr.en/1000d),7)+"   "+lr.relInt+" ");
//                }
//            }
//        }
    }
    
    private static String rp(String text, int length) {
    return String.format("%-" + length + "." + length + "s", text);
    }
    
    private static class IntensityRecord{
        int Z = 0;
        String element = null;
        List<LineRecord> lines = new ArrayList<LineRecord>();
        
        public IntensityRecord(int Z){
            this.Z = Z;            
        }
        
        public void setElement(String elem){
            this.element = elem;
        }                
        
        public String getElement(){
            return(element);
        }
        
        public int getZ(){
            return(Z);
        }
        
        public void addLine(LineRecord r){
            lines.add(r);
        }
    }
    
    private static class LineRecord{
        String line = null;
        double en = 0;
        int relInt = 0;
        
        public LineRecord(String line, double en, int relInt){
            this.line = line;
            this.en = en;
            this.relInt = relInt;            
        }
    }
}
