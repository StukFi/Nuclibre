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
    public static final String CREATE_TABLE_DECAY = "create table decay "
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
    public static final String CREATE_TABLE_NUCLIDE = "create table nuclide "
            + "("
            + "nuclide_id varchar, "
            + "z integer, "
            + "a integer, "
            + "isomer varchar, "
            + "half_life float, "
            + "unc_half_life float,"
            + "is_stable smallint, "
            + "category varchar, "
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
    public static final String CREATE_TABLE_STATE = "create table state "
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
    public static final String CREATE_TABLE_LINE = "create table line "
            + "("
            + "nuclide_id varchar,"
            + "line_type varchar,"
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
    private static final Map<String, String> createStatements = Map.of(
            "decay", CREATE_TABLE_DECAY,
            "line", CREATE_TABLE_LINE,
            "nuclide", CREATE_TABLE_NUCLIDE,
            "state", CREATE_TABLE_STATE
    );
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

    @Override
    public Connection createNuclibDatabase(File f) throws Exception {
        outputDir = Path.of(f.getPath());
        if (!Files.isDirectory(outputDir))
            Files.createDirectory(outputDir);
        for (var entry : createStatements.entrySet()) {
            String tableName = entry.getKey();
            String sql = entry.getValue();
            Files.writeString(getTablePath(tableName), sql + "\n", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
        return null;
    }

    @Override
    public void insert(Connection c, String table, String... values) throws Exception {
        if (Main.testRun)
            return;
        ArrayList<String> columns = new ArrayList<>();
        for (int i = 0; i < values.length; i += 2) {
            String columnName = values[i].toLowerCase();
            columns.add(pgColumnNames.getOrDefault(columnName, columnName));
        }
        ArrayList<String> data = new ArrayList<>();
        for (int i = 1; i < values.length; i += 2) {
            String val = values[i];
            if (val == null || val.equals("NULL") || val.equals("null") || val.equals("NaN")) {
                data.add("null");
                continue;
            }
            try {
                Double.parseDouble(val);
                data.add(val);
            } catch (NumberFormatException e) {
                data.add("'" + val + "'");
            }
        }
        String pgTableName = pgTableNames.getOrDefault(table, table);
        Path outputFile = getTablePath(pgTableName);
        Integer rowCount = tableRowCounts.merge(pgTableName, 0, (k, v) -> v + 1);
        if (batchSize > 0) {
            if ((rowCount % batchSize) == 0)
                Files.writeString(outputFile, "COMMIT; BEGIN;\n", StandardOpenOption.APPEND);
        }
        Files.writeString(outputFile, String.format("INSERT INTO %s (%s) VALUES (%s)%s;\n",
                        pgTableName,
                        String.join(",", columns),
                        String.join(",", data),
                        suppressDuplicates.contains(pgTableName) ? " ON CONFLICT DO NOTHING" : ""),
                StandardOpenOption.APPEND);
    }

    @Override
    public void close() throws Exception {
        for (String pgTableName : tableRowCounts.keySet()) {
            Path outputFile = getTablePath(pgTableName);
            Files.writeString(outputFile, "COMMIT;\n", StandardOpenOption.APPEND);
        }
    }

    @Override
    public String getLastSQL() {
        return null;
    }
}


