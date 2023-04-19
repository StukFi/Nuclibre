/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.stuk.ensdf.record;

import fi.stuk.ensdf.type.HalfLifeValue;
import fi.stuk.ensdf.type.SValue;
import fi.stuk.ensdf.type.Uncertainty;

/**
 The Level Record<p>
Optional, although a data set usually has at least one.<p>
<b>Field (Col.) Name Description Reference</b><p>
1-5 NUCID Nuclide identification V.1<p>
6 Blank<p>
Any alphanumeric character other than '1'
for continuation records<p>
7 Must be blank<p>
8 L Letter 'L' is required<p>
9 Must be blank<p>
10-19 E Level energy in keV - Must not be blank V.18<p>
20-21 DE Standard uncertainty in E V.11<p>
22-39 J Spin and parity V.20<p>
40-49 T Half-life of the level; units must be given. V.14
Mean-life expressed as the width of a level,
in units of energy, may also be used<p>
50-55 DT Standard uncertainty in T V.12<p>
56-64 L Angular momentum transfer in the reaction V.22
determining the data set. (Whether it
is Ln, Lp, L, etc., is determined from
the DSID eld of the IDENTIFICATION
record.)<p>
65-74 S Spectroscopic strength for this level as deter- V.21
mined from the reaction in the IDENTI-
FICATION record. (Spectroscopic factor for
particle-exchange reactions;  for inelastic
scattering.)
Note: If a quantity other than spectroscopic factor
is given in this eld, a footnote relabelling the
eld is required.<p>
75-76 DS Standard uncertainty in S V.11<p>
77 C Comment FLAG used V.8
to refer to a particular comment record<p>
78-79 MS Metastable state is denoted by 'M ' or V.17
'M1' for the rst (lowest energy) isomer;
'M2', for the second isomer, etc.
For Ionized Atom Decay eld gives the atomic
electron shell or subshell in which 􀀀
particle is captured<p>
80 Q The character '?' denotes an uncertain or
questionable level
Letter 'S' denotes neutron, proton, alpha
separation energy or a level expected
but not observed
 * @author Tero Karhunen
 */
public class LevelRecord extends Record{
    String NUCID;
    String L;
    protected Double E;
    protected Uncertainty DE;
    protected String J;
    protected HalfLifeValue T;
    protected Uncertainty DT;
    String L2;
    SValue S;
    String DS;
    String C;
    public String MS;
    String Q;
    protected int idLevel = -1;
    
     @Override
    public void parse(){
        NUCID = field(1,5);
        L = field(8);
        E = dfield(10,19);
        DE = ufield(20,21, E);
        J = field(22,39);
        T = hlfield(40,49);
        DT = ufield(50,55, T);
        L2 = field(56,64);
        S = sfield(65,74);
        DS = field(75,76);
        C = field(77);
        MS = field(78,79);
        Q = field(80);        
    }

    /**
     * @return the idLevel
     */
    public int getIdLevel() {
        return idLevel;
    }

    /**
     * @param idLevel the idLevel to set
     */
    public void setIdLevel(int idLevel) {
        this.idLevel = idLevel;
    }

    /**
     * @return the E
     */
    public Double getE() {
        return E;
    }

    /**
     * @param E the E to set
     */
    public void setE(Double E) {
        this.E = E;
    }

    /**
     * @return the DE
     */
    public Uncertainty getDE() {
        return DE;
    }

    /**
     * @param DE the DE to set
     */
    public void setDE(Uncertainty DE) {
        this.DE = DE;
    }

    /**
     * @return the J
     */
    public String getJ() {
        return J;
    }

    /**
     * @param J the J to set
     */
    public void setJ(String J) {
        this.J = J;
    }

    /**
     * @return the T
     */
    public HalfLifeValue getT() {
        return T;
    }

    /**
     * @param T the T to set
     */
    public void setT(HalfLifeValue T) {
        this.T = T;
    }

    /**
     * @return the DT
     */
    public Uncertainty getDT() {
        return DT;
    }

    /**
     * @param DT the DT to set
     */
    public void setDT(Uncertainty DT) {
        this.DT = DT;
    }
}
