/*
* Copyright (c) 2023 Radiation and Nuclear Safety Authority (STUK)
*
* Use of this source code is governed by an MIT-style
* license that can be found in the LICENSE file.
*/
package fi.stuk.ensdf.record;

import fi.stuk.ensdf.type.Uncertainty;

/**
The Normalization Record<p>
Must precede L, G, B, E, A, DP records.<p>
Required if an absolute normalization is possible;
used mainly with decay and (n;
) reaction data sets.<p>
<b>Field (Col.) Name Description Reference</b><p>
1-5 NUCID Nuclide (Daughter/Product) identication V.1<p>
6 Must be blank<p>
7 Must be blank<p>
8 N Letter 'N' is required<p>
9 Blank or an integer in case of multiple
P records in the data set.
It should correspond to the designator on the
P record.<p>
10-19 NR Multiplier for converting relative photon V.9
intensity (RI in the GAMMA record) to
photons per 100 decays of the parent
through the decay branch or to photons per
100 neutron captures in an (n,
) reaction.
Required if the absolute photon intensity
can be calculated.<p>
20-21 DNR Standard uncertainty in NR V.11<p>
22-29 NT Multiplier for converting relative transition V.9
intensity (including conversion electrons)
[TI in the GAMMA record] to transitions
per 100 decays of the parent through this
decay branch or per 100 neutron captures in
an (n;
) reaction.
Required if TI are given in the GAMMA
record and the normalization is known.<p>
30-31 DNT standard uncertainty in NT V.11<p>
32-39 BR Branching ratio multiplier for converting V.9
intensity per 100 decays through this decay
branch to intensity per 100 decays of the
parent nuclide.
Required if known.<p>
40-41 DBR Standard uncertainty in BR V.11<p>
42-49 NB Multiplier for converting relative 􀀀 and  V.9
intensities (IB in the B- record; IB, IE,
TI in the EC record) to intensities per
100 decays through this decay branch.
Required if known.<p>
50-55 DNB Standard uncertainty in NB V.11<p>
56-62 NP Multiplier for converting per hundred delayed- V.9
transition intensities to per hundred decays of
precursor<p>
63-64 DNP standard uncertainty in NP V.11<p>
65-80 Must be blank<p>
Note: Normally 􀀀 and  intensities are given as per 100 parent
decays. One should remember that the multiplier for conversion to
per 100 decays is NBBR and, therefore, NB = 1=BR. Also,
the uncertainties in I(􀀀) will be calculated from addition of three
quantities (I(􀀀)), DBR and DNB in quadrature. Unless the
uncertainties are precisely known it is recommended that NB be
given without uncertainty. See PN record.
If more than one P records exist in the data set then there
should be corresponding N records giving the respective branching
ratios.
 * @author Tero Karhunen
 */
public class NormalizationRecord extends Record{

    protected String NUCID;
    protected Double NR;
    protected Uncertainty DNR;
    protected Double NT;
    protected Uncertainty DNT;
    protected Double BR;
    protected Uncertainty DBR;
    protected Double NB;
    protected Uncertainty DNB;
    protected Double NP;
    protected Uncertainty DNP;
    
    @Override
    public void parse(){
        NUCID = field(1,5);
        NR = dfield(10,19);
        DNR = ufield(20,21,NR);
        NT = dfield(22,29);
        DNT = ufield(30,31,NT);
        BR = dfield(32,39);
        DBR = ufield(40,41,BR);
        NB = dfield(42,49);
        DNB = ufield(50,55,NB);
        NP = dfield(56,62);
        DNP = ufield(63,64,NP);        
    }
    
    public void normalize(GammaRecord r){
        double d = r.getRI();
        if(NR != null)r.setRI(d*NR);
        else if(NT != null)r.setRI(d*NT);
        else if(BR != null)r.setRI(d*BR);
        else if(NB != null)r.setRI(d*NB);
        else if(NP != null)r.setRI(d*NP);
        if(BR != null && NR != null)r.setRI(d*NR*BR);
       // else throw new IllegalStateException("Cant normalize gamma record.");
    }

    /**
     * @return the NUCID
     */
    public String getNUCID() {
        return NUCID;
    }

    /**
     * @return the NR
     */
    public Double getNR() {
        return NR;
    }

    /**
     * @return the DNR
     */
    public Uncertainty getDNR() {
        return DNR;
    }

    /**
     * @return the NT
     */
    public Double getNT() {
        return NT;
    }

    /**
     * @return the DNT
     */
    public Uncertainty getDNT() {
        return DNT;
    }

    /**
     * @return the BR
     */
    public Double getBR() {
        return BR;
    }

    /**
     * @return the DBR
     */
    public Uncertainty getDBR() {
        return DBR;
    }

    /**
     * @return the NB
     */
    public Double getNB() {
        return NB;
    }

    /**
     * @return the DNB
     */
    public Uncertainty getDNB() {
        return DNB;
    }

    /**
     * @return the NP
     */
    public Double getNP() {
        return NP;
    }

    /**
     * @return the DNP
     */
    public Uncertainty getDNP() {
        return DNP;
    }
}
