/*
* Copyright (c) 2023 Radiation and Nuclear Safety Authority (STUK)
*
* Use of this source code is governed by an MIT-style
* license that can be found in the LICENSE file.
*/
package fi.stuk.nuclibre;

/**
 * NuclibreSQL contains SQL command strings to create different nuclibre tables.
 * @author Tero Karhunen
 */
public class NuclibreSQL {
    /**
     * SQL string to create 'decays' table.
     */
    public static final String CREATE_DECAYS_STRING = "create table decays "
            + "("
            + "parentNuclideId varchar(9), "
            + "daughterNuclideId varchar(9), "
            + "decayType varchar(3), "
            + "qValue float, "
            + "uncQValue float, "
            + "branching float,"
            + "uncBranching float, "
            + "source varchar(9), "
            + "PRIMARY KEY(parentNuclideId, daughterNuclideId, decayType)"
            + ");";

    /**
     * SQL string to create 'nuclides' table.
     */
    public static final String CREATE_NUCLIDES_STRING = "create table nuclides "
            + "("
            + "nuclideId varchar(9), "
            + "z integer, "
            + "a integer, "
            + "isomer varchar(3), "
            + "halflife float, "
            + "uncHalflife float,"
            + "isStable tinyint, "
            + "qMinus float, "
            + "uncQMinus float, "
            + "sn float, "
            + "uncSn float, "
            + "sp float,"
            + "uncSp float,"
            + "qAlpha float,"
            + "uncQAlpha float, "
            + "qPlus float, "
            + "uncQPlus float, "
            + "qEc float, "
            + "uncQEc float, "
            + "source varchar(9), "
            + "PRIMARY KEY(nuclideId)"
            + ");";

    /**
     * SQL string to create 'states' table. 
     */
    public static final String CREATE_STATES_STRING = "create table states "
            + "("
            + "nuclideId varchar(9), "
            + "idState integer, "
            + "energy float, "
            + "uncEnergy float, "
            + "spinParity varchar(18), "
            + "halflife float, "
            + "uncHalflife float, "
            + "isomer varchar(1), "
            + "source varchar(9), "
            + "PRIMARY KEY(nuclideId, idState)"
            + ");";
    
    /**
     * SQL string to create 'libLines' table.
     */
    public static final String CREATE_LIBLINES_STRING = "create table libLines "
            + "("
            + "nuclideId varchar(9), "
            + "lineType char(1),"
            + "idLine integer,"
            + "daughterNuclideId varchar(9),"
            + "initialIdStateP integer,"
            + "initialIdStateD integer,"
            + "finalIdState integer,"
            + "energy float,"
            + "uncEnergy float,"
            + "emissionProb float,"
            + "uncEmissionProb float,"
            + "designation varchar,"
            + "source varchar(9), "
            + "PRIMARY KEY(nuclideId, daughterNuclideId, lineType, idLine)"
            + ");";
}
