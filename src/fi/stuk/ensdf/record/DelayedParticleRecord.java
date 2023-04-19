/*
* Copyright (c) 2023 Radiation and Nuclear Safety Authority (STUK)
*
* Use of this source code is governed by an MIT-style
* license that can be found in the LICENSE file.
*/
package fi.stuk.ensdf.record;

/**
The (Delayed-) Particle Record<p>
Must follow the LEVEL record for the level which is fed by the particle.
Records for particles which are unassigned in a level scheme should precede the first
level of the data set.<p>
<b>Field (Col.) Name Description Reference</b><p>
1-5 NUCID Nuclide identication V.1<p>
6 Blank<
Any alphanumeric character other than '1'
for continuation records<p>
7 Must be blank<p>
8 D Blank for prompt-, Letter 'D' for delayedparticle
emission<p>
9 Particle The symbol for the (delayed) particle
(N=neutron, P=proton, A=alpha particle) is
required)<p>
10-19 E Energy of the particle in keV V.18<p>
20-21 DE Standard uncertainty in E V.11<p>
22-29 IP Intensity of (delayed) particles in percent V.13
of the total (delayed-) particle emissions<p>
30-31 DIP Standard uncertainty in IP V.11<p>
32-39 EI Energy of the level in the V.13
'intermediate' (mass=A+1 for n, p;A+4 for )
nuclide in case of delayed particle<p>
40-49 T Width of the transition in keV V.14<p>
50-55 DT Uncertainty in T V.12<p>
56-64 L Angular-momentum transfer of the V.22
emitted particle<p>
65-76 Blank<p>
77 C Comment FLAG used to refer to V.8
a particular comment record.<p>
78 COIN Letter 'C' denotes placement conrmed by V.15
coincidence. Symbol '?' denotes probable
coincidence.<p>
79 Blank<p>
80 Q The character '?' denotes an uncertain
placement of the transition in the level
scheme
Letter 'S' denotes an expected, but as yet
unobserved, transition
 * @author Tero Karhunen
 */
public class DelayedParticleRecord extends Record{
    String NUCID;
    String D;
    String Particle;
    String E;
    String DE;
    String IP;
    String DIP;
    String EI;
    String T;
    String DT;
    String L;
    String C;
    String Q;

    @Override
    public void parse(){
        NUCID = field(1,5);
        D = field(8);
        Particle = field(9);
        E = field(10,19);
        DE = field(20,21);
        IP = field(22,29);
        DIP = field(30,31);
        EI = field(32,39);
        T = field(40,49);
        DT = field(50,55);
        L = field(56,64);
        C = field(77);
        Q= field(80);
    }
}
