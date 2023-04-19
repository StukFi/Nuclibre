/*
* Copyright (c) 2023 Radiation and Nuclear Safety Authority (STUK)
*
* Use of this source code is governed by an MIT-style
* license that can be found in the LICENSE file.
*/
package fi.stuk.ensdf.record;

import fi.stuk.ensdf.type.Uncertainty;

/**
The Production Normalization Record<p>
Must follow N record, if N record present.<p>
It should be given when G records with intensities are present.<p>
<b>Field Name Description</b>
1-5 NUCID Nuclide (Daughter/Product) identication<p>
6 Blank<p>
7 P Letter 'P' (for production) is required<p>
8 N Letter 'N' is required<p>
9 Must be blank<p>
10-19 NRBR Multiplier for converting relative photon
intensity (RI in the GAMMA record) to
photons per 100 decays of the parent.
(Normally NRBR).
If left blank (NR DNR)(BR DBR) from N record will
be used for normalization.<p>
20-21 UNC1 Standard uncertainty in NRBR<p>
22-29 NTBR Multiplier for converting relative transition
intensity (including conversion electrons)
[TI in the GAMMA record] to transitions
per 100 decays of the parent. (Normally NTBR)
If left blank (NT DNT)(BR DBR) from N record will
be used for normalization.<p>
30-31 UNC1 standard uncertainty in NTBR<p>
42-49 NBBR Multiplier for converting relative 􀀀 and 
intensities (IB in the B- record; IB, IE,
TI in the EC record) to intensities per
100 decays.
If left blank (NB DNB)(BR DBR) from N record will be
used for normalization.<p>
50-55 UNC1 Standard uncertainty in (NB DNT)(BR DBR)<p>
56-62 NP Same as in 'N' record<p>
63-64 UNC1 standard uncertainty in NP<p>
77 COM Blank or 'C' (for comment)<p>
If blank, comment associated with the intensity
option will appear in the drawing in the Nuclear Data Sheets.
If letter 'C' is given, the desired comment to appear in the
drawing should be given on the continuation ('nPN') record(s),
col. 10-80.
1If left blank no uncertainty will appear in the publication.<p>
78 OPT Intensity Option. Option as to what intensity to
display in the drawings in the Nuclear Data Sheets. The
available options are given below (default option 3).<p>
<b>Option Intensity displayed Comment in drawing</b><p>
1 TI or RI(1 + ) Relative I(
 + ce)<p>
2 TINT or RINR(1 + ) I(
 + ce) per 100 (mode) decays<p>
3 TINTBR or
RIBRNR(1+ ) I(
 + ce) per 100 parent decays<p>
4 RINTBR I(
) per 100 parent decays<p>
5 RI Relative I(
)<p>
6 RI Relative photon branching from each level<p>
7 RI % photon branching from each level<p>
 * @author Tero Karhunen
 */
public class ProductionNormalizationRecord extends NormalizationRecord{

    String NUCID;
    Double NRxNB;
    Uncertainty UNC_NRxNB;
    Double NTxBR;
    Uncertainty UNC_NTxBR;
    Double NBxBR;
    Uncertainty UNC_NBxBR;
    Double NP;
    Uncertainty UNC_NP;
    String COM;
    String OPT;

    @Override
    public void parse(){
        NUCID = field(1,5);
        NRxNB = dfield(10,19);
        UNC_NRxNB = ufield(20,21,NRxNB);
        NTxBR = dfield(22,29);
        UNC_NTxBR = ufield(30,31,NTxBR);
        NBxBR = dfield(42,49);
        UNC_NBxBR = ufield(50,55,NBxBR);
        NP = dfield(56,62);
        UNC_NP = ufield(63,64,NP);
        COM = field(77);
        OPT = field(78);
    }
    
    public void normalize(GammaRecord r){
        double d = r.getRI();
        r.setRI(d);
    }
}
