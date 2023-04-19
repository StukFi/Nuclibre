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

import fi.stuk.ensdf.type.Uncertainty;

/**
The Q-value Record<p>
Required for adopted data sets.<p>
If there is only one data set for the nuclide then the Q-value record should
be given in that data set.<p>
Must precede L, G, B, E, A, DP records.<p>
If signs are not given, they will be assumed to be +.<p>
<b>Field (Col.) Name Description Reference</b><p>
1-5 NUCID Nuclide identification V.1<p>
6 Blank<p>
7 Must be blank<p>
8 Q Letter 'Q' is required<p>
9 Must be blank<p>
10-19 Q- Total energy (keV) available for b- decay V.10
of the ground state. (Q- &gt 0 if decay is energetically possible.
Q- &lt 0 represents the Qeps energy of the Z+1 (Z = proton number) isobar.)<p>
20-21 DQ- Standard uncertainty in Q- V.11<p>
22-29 SN Neutron separation energy in keV V.10<p>
30-31 DSN Standard uncertainty in SN V.11<p>
32-39 SP Proton separation energy in keV V.10<p>
40-41 DSP Standard uncertainty in SP V.11<p>
42-49 QA Total energy (keV) available for  decay V.10
of the ground state<p>
50-55 DQA Standard uncertainty in QA V.12<p>
56-80 QREF Reference citation(s) for the Q-values V.3<p>

 * @author Tero Karhunen
 */
public class QValueRecord extends Record{
    protected String NUCID;
    protected Double Qm;
    protected Uncertainty DQm;
    protected Double SN;
    protected Uncertainty DSN;
    protected Double SP;
    protected Uncertainty DSP;
    protected Double QA;
    protected Uncertainty DQA;
    protected String QREF;

    public void parse(){
        NUCID = field(1,5);
        Qm = dfield(10,19);
        DQm = ufield(20,21,Qm);
        SN = dfield(22,29);
        DSN = ufield(30,31,SN);
        SP = dfield(32,39);
        DSP = ufield(40,41,SP);
        QA = dfield(42,49);
        DQA = ufield(50,55,QA);
        QREF = field(56,80);        
    }

    /**
     * @return the NUCID
     */
    public String getNUCID() {
        return NUCID;
    }

    /**
     * @return the Qm
     */
    public Double getQm() {
        return Qm;
    }

    /**
     * @return the DQm
     */
    public Uncertainty getDQm() {
        return DQm;
    }

    /**
     * @return the SN
     */
    public Double getSN() {
        return SN;
    }

    /**
     * @return the DSN
     */
    public Uncertainty getDSN() {
        return DSN;
    }

    /**
     * @return the SP
     */
    public Double getSP() {
        return SP;
    }

    /**
     * @return the DSP
     */
    public Uncertainty getDSP() {
        return DSP;
    }

    /**
     * @return the QA
     */
    public Double getQA() {
        return QA;
    }

    /**
     * @return the DQA
     */
    public Uncertainty getDQA() {
        return DQA;
    }

    /**
     * @return the QREF
     */
    public String getQREF() {
        return QREF;
    }
}
