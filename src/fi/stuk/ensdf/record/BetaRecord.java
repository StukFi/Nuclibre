/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.stuk.ensdf.record;

import fi.stuk.ensdf.type.Uncertainty;

/**
The Beta (B-) Record<p>
Must follow the LEVEL record for the level which is fed by the B-.<p>
<b>Field (Col.) Name Description Reference</b><p>
1-5 NUCID Nuclide identication V.1<p>
6 Blank
Any alphanumeric character other than '1'
for continuation records
7 Must be blank<p>
8 B Letter 'B' is required<p>
9 Must be blank<p>
10-19 E Endpoint energy of the 􀀀 in keV V.18
Given only if measured<p>
20-21 DE Standard uncertainty in E V.11<p>
22-29 IB Intensity of the 􀀀-decay branch1 V.13<p>
30-31 DIB Standard uncertainty in IB V.11<p>
42-49 LOGFT The log ft for the 􀀀 transition V.9
for uniqueness given in col. 78-79<p>
50-55 DFT Standard uncertainty in LOGFT V.12<p>
56-76 Must be blank<p>
77 C Comment FLAG (Letter 'C' denotes V.8
coincidence with a following radiation.
A '?' denotes probable coincidence with a
following radiation.)<p>
78-79 UN Forbiddenness classication for the 􀀀 decay, V.16
e.g., '1U', '2U' for rst-, second-unique forbidden.
(A blank eld signies an allowed
transition. Nonunique forbiddenness can be
indicated in col 78, with col 79 blank)
80 Q The character '?' denotes an uncertain or
questionable B- decay.
Letter 'S' denotes an expected or predicted
transition<p>
1The intensity units are dened by the NORMALIZATION record.<p>
 * @author Tero Karhunen
 */
public class BetaRecord extends EmissionRecord{
    String NUCID;
    String B;
    Double E;
    Uncertainty DE;
    Double IB;
    Uncertainty DIB;
    String LOGFT;
    String DFT;
    String C;
    String UN;
    String Q;
    Double EAV;
    
    @Override
    public void parse(){
        NUCID = field(1,5);
        B = field(8);
        E = dfield(10,19);
        DE = ufield(20,21,E);
        IB = dfield(22,29);
        DIB = ufield(30,31,IB);
        LOGFT = field(42,49);
        DFT = field(50,55);
        C = field(77);
        UN = field(78,79);
        Q = field(80);
    }

    @Override
    public Double getE() {
        if(E == null && EAV != null)return(EAV);
        return(E);
    }

    @Override
    public Double getRI() {
        return(IB);
    }

    @Override
    public Uncertainty getDE() {
        return(DE);
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
                    if(tok[i].startsWith("EAV="))parseAvgEnergy(tok[i]);
                }
            }
        }
    }
    
    private void parseAvgEnergy(String tok){
        String[] valueAndUnc = tok.split("=");        
        String value[] = valueAndUnc[1].trim().split("\\s+");
        if(tok.charAt(0) == 'E'){
            EAV = Double.parseDouble(value[0]);
            
        }        
    }
}
