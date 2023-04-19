/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.stuk.ensdf.record;

import fi.stuk.ensdf.type.HalfLifeValue;
import fi.stuk.ensdf.type.Uncertainty;

/**
The Parent Record<p>
Required for all decay data sets.<p>
Must precede L, G, B, E, A, DP records.<p>
<b>Field (Col.) Name Description Reference</b><p>
1-5 NUCID Parent Nuclide identification V.1<p>
6 Must be blank<p>
7 Must be blank<p>
8 P Letter 'P' is required<p>
9 Blank or an integer in case of multiple
P records in the data set<p>
10-19 E Energy of the decaying level in keV V.18
(0.0 for g.s.)<p>
20-21 DE Standard uncertainty in E V.11<p>
22-39 J Spin and parity V.20<p>
40-49 T Half-life; units must be given V.14<p>
50-55 DT Standard uncertainty in T V.12<p>
56-64 Must be blank<p>
65-74 QP Ground-state Q-value in keV (total energy V.9
available for g:s: ! g:s: transition); it will
always be a positive number.
Not needed for IT and SF decay.<p>
75-76 DQP Standard uncertainty in QP V.11<p>
77-80 ION Ionization State (for Ionized Atom decay),
blank otherwise<p>
NOTES:<p>
1. More than one parent card is allowed in a data set. If the decay scheme is
due to more than one parent then separate P records should be given for
each parent level.<p>
2. Currently, publication program allows maximum of two parent cards.<p>
3. Parent information, namely, E, J, T, QP must be identical to their values
given in its Adopted Levels data set.<p>
 * @author Tero Karhunen
 */
public class ParentRecord extends Record{

    protected String NUCID;
    protected Double E;
    protected Uncertainty DE;
    protected String J;
    protected HalfLifeValue T;
    protected String DT;
    protected Double QP;
    protected Uncertainty DQP;
    protected String ION;

    @Override
    public void parse(){
        NUCID = field(1,5);
        E = dfield(10,19);
        DE = ufield(20,21,E);
        J = field(22,39);
        T = hlfield(40,49);
        DT = field(50,55);
        QP = dfield(65,74);
        DQP = ufield(75,76,QP);
        ION = field(77,80);        
    }

    /**
     * @return the NUCID
     */
    public String getNUCID() {
        return NUCID;
    }

    /**
     * @return the E
     */
    public Double getE() {
        return E;
    }

    /**
     * @return the DE
     */
    public Uncertainty getDE() {
        return DE;
    }

    /**
     * @return the J
     */
    public String getJ() {
        return J;
    }

    /**
     * @return the T
     */
    public HalfLifeValue getT() {
        return T;
    }

    /**
     * @return the DT
     */
    public String getDT() {
        return DT;
    }

    /**
     * @return the QP
     */
    public Double getQP() {
        return QP;
    }

    /**
     * @return the DQP
     */
    public Uncertainty getDQP() {
        return DQP;
    }

    /**
     * @return the ION
     */
    public String getION() {
        return ION;
    }
}
