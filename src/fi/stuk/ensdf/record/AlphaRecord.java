/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.stuk.ensdf.record;

import fi.stuk.ensdf.type.Uncertainty;

/**
The Alpha Record<p>
Must follow the LEVEL record for the level being populated in the decay.<p>
<b>Field (Col.) Name Description Reference</b><p>
1-5 NUCID Nuclide identication V.1<p>
6 Blank<p>
7 Must be blank<p>
8 A Letter 'A' is required<p>
9 Must be blank<p>
10-19 E Alpha energy in keV V.18<p>
20-21 DE Standard uncertainty in E V.11<p>
22-29 IA Intensity of -decay branch in percent of V.13
the total  decay<p>
30-31 DIA Standard uncertainty in IA V.11<p>
32-39 HF Hindrance factor for  decay V.9<p>
40-41 DHF Standard uncertainty in HF V.11<p>
42-76 Must be blank<p>
77 C Comment FLAG (Letter 'C' denotes V.8
coincidence with a following radiation.
A '?' denotes probable coincidence with a
following radiation.)<p>
78-79 Must be blank<p>
80 Q The character '?' denotes uncertain or
questionable  branch
Letter 'S' denotes an expected or predicted
 branch<p>
 * @author Tero Karhunen
 */
public class AlphaRecord extends EmissionRecord{    
    String NUCID;    
    String A;
    Double E;
    Uncertainty DE;
    Double IA;
    Uncertainty DIA;
    Double HF;
    Uncertainty DHF;
    String C;
    String Q;

    @Override
    public void parse(){
        NUCID = field(1,5);
        A = field(8);
        E = dfield(10,19);
        DE = ufield(20,21, E);
        IA = dfield(22,29);
        DIA = ufield(30,31, IA);
        HF = dfield(32,39);
        DHF = ufield(40,41, HF);
        C = field(77);
        Q = field(80);
    }

    @Override
    public Double getE() {
        return(E);
    }

    @Override
    public Double getRI() {
        return(IA);
    }

    @Override
    public Uncertainty getDE() {
        return(DE);
    }

    @Override
    public Uncertainty getDRI() {
        return(DIA);
    }
}
