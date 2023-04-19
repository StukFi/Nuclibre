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
            + "source string, "
            + "PRIMARY KEY(parentNuclideId, daughterNuclideId, decayType)"
            + ");";

    /**
     * SQL string to create 'nuclides' table.
     */
    public static final String CREATE_NUCLIDES_STRING = "create table nuclides "
            + "("
            + "nuclideId varchar(9), "
            + "z integer(11), "
            + "a integer(11), "
            + "isomer varchar(1), "
            + "halflife float, "
            + "uncHalflife float,"
            + "isStable tinyint(1), "
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
            + "source string,"
            + "PRIMARY KEY(nuclideId)"
            + ");";

    /**
     * SQL string to create 'states' table. 
     */
    public static final String CREATE_STATES_STRING = "create table states "
            + "("
            + "nuclideId varchar(9), "
            + "idState smallint(6), "
            + "energy float, "
            + "uncEnergy float, "
            + "spinParity float, "
            + "halflife float, "
            + "uncHalflife float, "
            + "isomer varchar(1), "
            + "source string, "
            + "PRIMARY KEY(nuclideId, idState)"
            + ");";
    
    /**
     * SQL string to create 'libLines' table.
     */
    public static final String CREATE_LIBLINES_STRING = "create table libLines "
            + "("
            + "nuclideId varchar(9), "
            + "lineType char(1),"
            + "idLine smallint(6),"
            + "daughterNuclideId varchar(9),"
            + "initialIdStateP smallint(6),"
            + "initialIdStateD smallint(6),"
            + "finalIdState smallint(6),"
            + "energy float,"
            + "uncEnergy float,"
            + "emissionProb float,"
            + "uncEmissionProb float,"
            + "designation varchar,"
            + "source string, "
            + "PRIMARY KEY(nuclideId, daughterNuclideId, lineType, idLine)"
            + ");";
}
