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
 * EmissionRecord is a subclass of the type of records that describe (or result)
 * in emissions.
 * @author Tero Karhunen.
 */
public abstract class EmissionRecord extends Record{
    /**
     * Get the emission energy (keV).
     * @return the emission energy.
     */
    public abstract Double getE();
    /**
     * Get the relative intensity of the emission.
     * @return the relative intensity.
     */
    public abstract Double getRI();
    /**
     * Get the uncertainty of the emission energy.
     * @return the uncertainty.
     */
    public abstract Uncertainty getDE();
    /**
     * Get the uncertainty of the relative intensity of the emission.
     * @return the uncertainty.
     */
    public abstract Uncertainty getDRI();
}
