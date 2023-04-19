/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.stuk.ensdf.record;

import fi.stuk.ensdf.type.Uncertainty;

/**
<b>The Gamma Record</b><p>
Must follow the LEVEL record for the level from which the
 ray decays.
Records for
 rays which are unassigned in a level scheme should precede the first
level of the data set.<p>
<b>Field (Col.) Name Description Reference</b><p>
1-5 NUCID Nuclide identification V.1<p>
6 Blank
Any alphanumeric character other than '1'
for continuation records<p>
7 Must be blank<p>
8 G Letter 'G' is required<p>
9 Must be blank<p>
10-19 E Energy of the
-ray in keV V.18
- Must not be blank<p>
20-21 DE Standard uncertainty in E V.11<p>
22-29 RI Relative photon intensity1 V.13<p>
30-31 DRI Standard uncertainty in RI V.11<p>
32-41 M Multipolarity of transition V.19<p>
42-49 MR Mixing ratio, . (Sign must be shown V.10
explicitly if known. If no sign is given,
it will be assumed to be unknown.)
50-55 DMR Standard uncertainty in MR V.12<p>
56-62 CC Total conversion coecient V.9<p>
63-64 DCC Standard uncertainty in CC V.11<p>
65-74 TI Relative total transition intensity1 V.13<p>
75-76 DTI Standard uncertainty in TI V.11<p>
77 C Comment FLAG used to refer to V.8
a particular comment record. The symbol
'*' denotes a multiply-placed
 ray.
The symbol '&' denotes a multiply-placed
transition with intensity not divided.
The symbol '@' denotes a multiply-placed
transition with intensity suitably divided.
The symbol '%' denotes that the intensity given as
RI is the % branching in the Super Deformed
Band.<p>
78 COIN Letter 'C' denotes placement conrmed by V.15
coincidence. Symbol '?' denotes questionable
coincidence.
1The intensity units are dened by the NORMALIZATION record.
 * @author Tero Karhunen
 */
public class GammaRecord extends EmissionRecord{
    protected String NUCID;
    protected String G;
    protected Double E;
    protected Uncertainty DE;
    protected Double RI;
    protected Uncertainty DRI;
    protected String M;
    protected Double MR;
    protected Uncertainty DMR;
    protected Double CC;
    protected Uncertainty DCC;
    protected Double TI;
    protected Uncertainty DTI;
    protected String C;
    protected String COIN;
    protected String Q;
    /** K shell internal conversion coefficient. */
    protected Double Kc;
    /** K shell internal conversion coefficient uncertainty. */
    protected Double dKc;
    /** L shell internal conversion coefficient. */
    protected Double Lc;
    /** L shell internal conversion coefficient uncertainty. */
    protected Double dLc;
    /** M shell internal conversion coefficient. */
    protected Double Mc;
    /** M shell internal conversion coefficient uncertainty. */
    protected Double dMc;
    
    
    @Override
    public void parse(){
        NUCID = field(1,5);
        G = field(8);
        E = dfield(10,19);     
        DE = ufield(20,21,E);
        RI = dfield(22,29);
        NormalizationRecord r = ds.getNormalizationRecord();
        if(r != null && RI != null)r.normalize(this);
        if(RI == null)RI = 0d;
        DRI = ufield(30,31,RI);
        M = field(32,41);
        MR = dfield(42,49);
        DMR = ufield(50,55,MR);
        CC = dfield(56,62);
        DCC = ufield(63,64,CC);
        TI = dfield(65,74);
        DTI = ufield(75,76,TI);
        C = field(77);
        COIN = field(78);
        Q = field(80);               
    }

    /**
     * @return the NUCID
     */
    public String getNUCID() {
        return NUCID;
    }

    /**
     * @param NUCID the NUCID to set
     */
    public void setNUCID(String NUCID) {
        this.NUCID = NUCID;
    }

    /**
     * @return the G
     */
    public String getG() {
        return G;
    }

    /**
     * @param G the G to set
     */
    public void setG(String G) {
        this.G = G;
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
     * @return the RI
     */
    public Double getRI() {
        return RI;
    }

    /**
     * @param RI the RI to set
     */
    public void setRI(Double RI) {
        this.RI = RI;
    }

    /**
     * @return the DRI
     */
    public Uncertainty getDRI() {
        return DRI;
    }

    /**
     * @param DRI the DRI to set
     */
    public void setDRI(Uncertainty DRI) {
        this.DRI = DRI;
    }

    /**
     * @return the M
     */
    public String getM() {
        return M;
    }

    /**
     * @param M the M to set
     */
    public void setM(String M) {
        this.M = M;
    }

    /**
     * @return the MR
     */
    public Double getMR() {
        return MR;
    }

    /**
     * @param MR the MR to set
     */
    public void setMR(Double MR) {
        this.MR = MR;
    }

    /**
     * @return the DMR
     */
    public Uncertainty getDMR() {
        return DMR;
    }

    /**
     * @param DMR the DMR to set
     */
    public void setDMR(Uncertainty DMR) {
        this.DMR = DMR;
    }

    /**
     * @return the CC
     */
    public Double getCC() {
        return CC;
    }

    /**
     * @param CC the CC to set
     */
    public void setCC(Double CC) {
        this.CC = CC;
    }

    /**
     * @return the DCC
     */
    public Uncertainty getDCC() {
        return DCC;
    }

    /**
     * @param DCC the DCC to set
     */
    public void setDCC(Uncertainty DCC) {
        this.DCC = DCC;
    }

    /**
     * @return the TI
     */
    public Double getTI() {
        return TI;
    }

    /**
     * @param TI the TI to set
     */
    public void setTI(Double TI) {
        this.TI = TI;
    }

    /**
     * @return the DTI
     */
    public Uncertainty getDTI() {
        return DTI;
    }

    /**
     * @param DTI the DTI to set
     */
    public void setDTI(Uncertainty DTI) {
        this.DTI = DTI;
    }

    /**
     * @return the C
     */
    public String getC() {
        return C;
    }

    /**
     * @param C the C to set
     */
    public void setC(String C) {
        this.C = C;
    }

    /**
     * @return the COIN
     */
    public String getCOIN() {
        return COIN;
    }

    /**
     * @param COIN the COIN to set
     */
    public void setCOIN(String COIN) {
        this.COIN = COIN;
    }

    /**
     * @return the Q
     */
    public String getQ() {
        return Q;
    }

    /**
     * @param Q the Q to set
     */
    public void setQ(String Q) {
        this.Q = Q;
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
                    if(tok[i].startsWith("KC=") || tok[i].startsWith("LC=") ||
                            tok[i].startsWith("MC=") ||
                            tok[i].startsWith("EKC=") || tok[i].startsWith("ELC=") ||
                            tok[i].startsWith("EMC=")
                            )parseConvCoeff(tok[i]);
                }
            }
        }
    }
    
    private void parseConvCoeff(String coeff){        
        String[] valueAndUnc = coeff.split("=");        
        String value[] = valueAndUnc[1].trim().split("\\s+");
        value[0] = value[0].replace("(", " ");
        value[0] = value[0].replace(")", " ");
        value[0] = value[0].trim();
        String[] vTok = value[0].split("\\s+");
        value[0] = vTok[0];
        if(coeff.startsWith("K")){
            if(Kc == null)Kc = Double.parseDouble(value[0]);            
        }
        if(coeff.startsWith("EK")){
            Kc = Double.parseDouble(value[0]);            
        }
        else if(coeff.startsWith("L")){
            if(Lc == null)Lc = Double.parseDouble(value[0]);
        }
        else if(coeff.startsWith("EL")){
            Lc = Double.parseDouble(value[0]);
        }
        else if(coeff.startsWith("M")){
            if(Mc == null)Mc = Double.parseDouble(value[0]);
        }
        else if(coeff.startsWith("EM")){
            Mc = Double.parseDouble(value[0]);
        }
    }

    /**
     * @return the Kc
     */
    public Double getKc() {
        return Kc;
    }

    /**
     * @return the dKc
     */
    public Double getdKc() {
        return dKc;
    }

    /**
     * @return the Lc
     */
    public Double getLc() {
        return Lc;
    }

    /**
     * @return the dLc
     */
    public Double getdLc() {
        return dLc;
    }

    /**
     * @return the Mc
     */
    public Double getMc() {
        return Mc;
    }

    /**
     * @return the dMc
     */
    public Double getdMc() {
        return dMc;
    }
    
    public static void main(String[] args){
        GammaRecord d = new GammaRecord();
        String str = " 22NAS G  KC=0.00338 5$LC=0.000205 3$MC=4.53E-6 7";
        d.setContent("a\n"+str);
        d.parseContinuation();
    }
}
