package fi.stuk.nuclibre;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Postgres output dialect. Drops camel case naming of tables and columns in favor of underscore style
 * preferred in Postgres.
 */
public class PostgresOutputDialect implements OutputDialect {
    /**
     * SQL string to create 'decays' table.
     */
    public static final String MY_STYLE_CREATE_TABLE_DECAYS = "create table \"decays\" "
            + "("
            + "\"parentNuclideId\" varchar, "
            + "\"daughterNuclideId\" varchar, "
            + "\"decayType\" varchar, "
            + "\"qValue\" float, "
            + "\"uncQValue\" float, "
            + "\"branching\" float,"
            + "\"uncBranching\" float, "
            + "\"source\" varchar, "
            + "PRIMARY KEY(\"parentNuclideId\", \"daughterNuclideId\", \"decayType\")"
            + ");";

    /**
     * SQL string to create 'nuclides' table.
     */
    public static final String MY_STYLE_CREATE_TABLE_NUCLIDES = "create table \"nuclides\" "
            + "("
            + "\"nuclideId\" varchar, "
            + "\"z\" integer, "
            + "\"a\" integer, "
            + "\"isomer\" varchar, "
            + "\"halflife\" float, "
            + "\"uncHalflife\" float,"
            + "\"isStable\" boolean, "
            + "\"qMinus\" float, "
            + "\"uncQMinus\" float, "
            + "\"sn\" float, "
            + "\"uncSn\" float, "
            + "\"sp\" float,"
            + "\"uncSp\" float,"
            + "\"qAlpha\" float,"
            + "\"uncQAlpha\" float, "
            + "\"qPlus\" float, "
            + "\"uncQPlus\" float, "
            + "\"qEc\" float, "
            + "\"uncQEc\" float, "
            + "\"source\" varchar, "
            + "PRIMARY KEY(\"nuclideId\")"
            + ");";

    /**
     * SQL string to create 'states' table.
     */
    public static final String MY_STYLE_CREATE_TABLE_STATES = "create table \"states\" "
            + "("
            + "\"nuclideId\" varchar, "
            + "\"idState\" integer, "
            + "\"energy\" float, "
            + "\"uncEnergy\" float, "
            + "\"spinParity\" varchar, "
            + "\"halflife\" float, "
            + "\"uncHalflife\" float, "
            + "\"isomer\" varchar, "
            + "\"source\" varchar, "
            + "PRIMARY KEY(\"nuclideId\", \"idState\")"
            + ");";

    /**
     * SQL string to create 'libLines' table.
     */
    public static final String MY_STYLE_CREATE_TABLE_LIBLINES = "create table \"libLines\" "
            + "("
            + "\"nuclideId\" varchar, "
            + "\"lineType\" char,"
            + "\"idLine\" integer,"
            + "\"daughterNuclideId\" varchar,"
            + "\"initialIdStateP\" integer,"
            + "\"initialIdStateD\" integer,"
            + "\"finalIdState\" integer,"
            + "\"energy\" float,"
            + "\"uncEnergy\" float,"
            + "\"emissionProb\" float,"
            + "\"uncEmissionProb\" float,"
            + "\"designation\" varchar,"
            + "\"source\" varchar, "
            + "PRIMARY KEY(\"nuclideId\", \"daughterNuclideId\", \"lineType\", \"idLine\")"
            + ");";

    public static final String PG_STYLE_CREATE_TABLE_DECAY = "create table decay "
            + "("
            + "parent_nuclide_id varchar, "
            + "daughter_nuclide_id varchar, "
            + "decay_type varchar, "
            + "q_value float, "
            + "unc_q_value float, "
            + "branching float,"
            + "unc_branching float, "
            + "source varchar, "
            + "PRIMARY KEY (parent_nuclide_id, daughter_nuclide_id, decay_type)"
            + ");";

    /**
     * SQL string to create 'nuclides' table.
     */
    public static final String PG_STYLE_CREATE_TABLE_NUCLIDE = "create table nuclide "
            + "("
            + "nuclide_id varchar, "
            + "z integer, "
            + "a integer, "
            + "isomer varchar, "
            + "half_life float, "
            + "unc_half_life float,"
            + "is_stable boolean, "
            + "q_minus float, "
            + "unc_q_minus float, "
            + "sn float, "
            + "unc_sn float, "
            + "sp float,"
            + "unc_sp float,"
            + "q_alpha float,"
            + "unc_q_alpha float, "
            + "q_plus float, "
            + "unc_q_plus float, "
            + "q_ec float, "
            + "unc_q_ec float, "
            + "source varchar, "
            + "PRIMARY KEY (nuclide_id)"
            + ");";

    /**
     * SQL string to create 'states' table.
     */
    public static final String PG_STYLE_CREATE_TABLE_STATE = "create table state "
            + "("
            + "nuclide_id varchar, "
            + "id_state integer, "
            + "energy float, "
            + "unc_energy float, "
            + "spin_parity varchar, "
            + "half_life float, "
            + "unc_half_life float, "
            + "isomer varchar, "
            + "source varchar, "
            + "PRIMARY KEY(nuclide_id, id_state)"
            + ");";

    /**
     * SQL string to create 'libLines' table.
     */
    public static final String PG_STYLE_CREATE_TABLE_LINE = "create table line "
            + "("
            + "nuclide_id varchar,"
            + "line_type char,"
            + "id_line integer,"
            + "daughter_nuclide_id varchar,"
            + "initial_id_state_p integer,"
            + "initial_id_state_d integer,"
            + "final_id_state integer,"
            + "energy float,"
            + "unc_energy float,"
            + "emission_prob float,"
            + "unc_emission_prob float,"
            + "designation varchar,"
            + "source varchar, "
            + "PRIMARY KEY(nuclide_id, daughter_nuclide_id, line_type, id_line)"
            + ");";

    private static final String columnNameTranslations = "a\n" +
            "branching\t\n" +
            "daughter_nuclide_id\tdaughterNuclideId\n" +
            "decay_type\t\tdecayType\n" +
            "designation\n" +
            "emission_prob\t\temissionProb\n" +
            "energy\n" +
            "final_id_state\t\tfinalIdState\n" +
            "half_life\t\thalflife\n" +
            "id_line\t\t\tidLine\n" +
            "id_state\t\tidState\n" +
            "initial_id_state_d\tinitialIdStateD\n" +
            "initial_id_state_p\tinitialIdStateP\n" +
            "is_stable\t\tisStable\n" +
            "isomer\n" +
            "line_type\t\tlineType\n" +
            "nuclide_id\t\tnuclideId\n" +
            "parent_nuclide_id\tparentNuclideId\n" +
            "q_alpha\t\t\tqAlpha\n" +
            "q_ec\t\t\tqEc\n" +
            "q_minus\t\t\tqMinus\n" +
            "q_plus\t\t\tqPlus\n" +
            "q_value\t\t\tqValue\n" +
            "sn\n" +
            "source\n" +
            "sp\n" +
            "spin_parity\t\tspinParity\n" +
            "unc_branching\t\tuncBranching\n" +
            "unc_emission_prob\tuncEmissionProb\n" +
            "unc_energy\t\tuncEnergy\n" +
            "unc_half_life\t\tuncHalflife\n" +
            "unc_q_alpha\t\tuncQAlpha\n" +
            "unc_q_ec\t\tuncQEc\n" +
            "unc_q_minus\t\tuncQMinus\n" +
            "unc_q_plus\t\tuncQPlus\n" +
            "unc_q_value\t\tuncQValue\n" +
            "unc_sn\t\t\tuncSn\n" +
            "unc_sp\t\t\tuncSP\n" +
            "z\n";

    private static final Map<String, String> pgColumnNames = new HashMap<>();
    private static final Map<String, String> pgTableNames = Map.of(
            "decays", "decay",
            "libLines", "line",
            "nuclides", "nuclide",
            "states", "state"
    );
    private static final Map<String, String> pgStyleCreateStatements = Map.of(
            "decay", PG_STYLE_CREATE_TABLE_DECAY,
            "line", PG_STYLE_CREATE_TABLE_LINE,
            "nuclide", PG_STYLE_CREATE_TABLE_NUCLIDE,
            "state", PG_STYLE_CREATE_TABLE_STATE
    );
    private static final Map<String, String> myStyleCreateStatements = Map.of(
            "decays", MY_STYLE_CREATE_TABLE_DECAYS,
            "libLines", MY_STYLE_CREATE_TABLE_LIBLINES,
            "nuclides", MY_STYLE_CREATE_TABLE_NUCLIDES,
            "states", MY_STYLE_CREATE_TABLE_STATES
    );
    private static final Set<String> ignoredColumns = Set.of("category");
    private static final Set<String> booleanColumns = Set.of("is_stable");
    private static final Set<String> suppressDuplicates = Set.of("decay");
    /**
     * Let psql insert rows in batches to speed up data import. Set to zero to disable.
     */
    private static final int batchSize = 10_000;

    static {
        columnNameTranslations.lines()
                .map(String::toLowerCase)
                .map(line -> line.split("\\s+"))
                .forEach(PostgresOutputDialect::addColumnTranslation);
    }

    private final Map<String, Integer> tableRowCounts = new HashMap<>();
    private Path outputDir;
    private SchemaStyle schemaStyle = SchemaStyle.MYSQL_STYLE;

    private static void addColumnTranslation(String[] keys) {
        if (keys.length < 2)
            return;
        pgColumnNames.put(keys[1], keys[0]);
    }

    @Override
    public Connection getDatabaseConnection(File f) throws Exception {
        return null;
    }

    public Path getTablePath(String tableName) {
        return outputDir.resolve(tableName + ".sql");
    }

    public Map<String, String> getCreateTableMap() {
        return schemaStyle == SchemaStyle.MYSQL_STYLE ? myStyleCreateStatements : pgStyleCreateStatements;
    }

    @Override
    public Connection createNuclibDatabase(File f) throws Exception {
        outputDir = Path.of(f.getPath());
        if (!Files.isDirectory(outputDir))
            Files.createDirectory(outputDir);
        for (var entry : getCreateTableMap().entrySet()) {
            String tableName = entry.getKey();
            String sql = entry.getValue();
            Files.writeString(getTablePath(tableName), sql + "\n", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
        return null;
    }

    @Override
    public void insert(Connection c, String inputTableName, String... values) throws Exception {
        if (Main.testRun)
            return;
        ArrayList<String> columns = new ArrayList<>();
        ArrayList<String> data = new ArrayList<>();
        for (int i = 0; i < values.length; i += 2) {
            String inputColumnName = values[i];
            String lookupColumnName = values[i].toLowerCase();
            String val = values[i+1];
            String pgColumnName = pgColumnNames.getOrDefault(lookupColumnName, inputColumnName);
            if (ignoredColumns.contains(pgColumnName))
                continue;
            switch (schemaStyle) {
                case MYSQL_STYLE:
                    columns.add("\"" + inputColumnName + "\"");
                    break;
                case POSTGRES_STYLE:
                    columns.add(pgColumnName);
                    break;
            }
            if (val == null || val.equals("NULL") || val.equals("null") || val.equals("NaN")) {
                data.add("null");
                continue;
            }
            if (booleanColumns.contains(pgColumnName)) {
                data.add(val.matches("1") ? "true"  : "false");
            }
            else {
                try {
                    Double.parseDouble(val);
                    data.add(val);
                } catch (NumberFormatException e) {
                    data.add("'" + val + "'");
                }
            }
        }
        String pgTableName = pgTableNames.getOrDefault(inputTableName, inputTableName);
        String outputTableName = schemaStyle == SchemaStyle.MYSQL_STYLE ? inputTableName : pgTableName;
        Path outputFile = getTablePath(outputTableName);
        Integer rowCount = tableRowCounts.merge(outputTableName, 0, (k, v) -> v + 1);
        if (batchSize > 0) {
            if ((rowCount % batchSize) == 0)
                Files.writeString(outputFile, "COMMIT; BEGIN;\n", StandardOpenOption.APPEND);
        }
        Files.writeString(outputFile, String.format("INSERT INTO \"%s\" (%s) VALUES (%s)%s;\n",
                        outputTableName,
                        String.join(",", columns),
                        String.join(",", data),
                        suppressDuplicates.contains(pgTableName) ? " ON CONFLICT DO NOTHING" : ""),
                StandardOpenOption.APPEND);
    }

    @Override
    public void close() throws Exception {
        for (String tableName : tableRowCounts.keySet()) {
            Path outputFile = getTablePath(tableName);
            Files.writeString(outputFile, "COMMIT;\n", StandardOpenOption.APPEND);
        }
    }

    @Override
    public String getLastSQL() {
        return null;
    }

    public OutputDialect setSchemaStyle(SchemaStyle schemaStyle) {
        this.schemaStyle = schemaStyle;
        return this;
    }

    public enum SchemaStyle {
        MYSQL_STYLE,
        POSTGRES_STYLE,
        ;
    }
}


