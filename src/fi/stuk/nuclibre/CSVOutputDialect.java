package fi.stuk.nuclibre;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class CSVOutputDialect implements OutputDialect {
    private static final Header DECAY = Header.fromText("parentNuclideId daughterNuclideId decayType qValue uncQValue branching uncBranching source");
    private static final Header LINE = Header.fromText("nuclideId lineType idLine daughterNuclideId initialIdStateP initialIdStateD finalIdState energy uncEnergy emissionProb uncEmissionProb designation source");
    private static final Header NUCLIDE = Header.fromText("nuclideId isomer halflife uncHalflife isStable qMinus uncQMinus sn uncSn sp uncSp qAlpha uncQAlpha qPlus uncQPlus qEc uncQEc source");
    private static final Header STATE = Header.fromText("nuclideId idState energy uncEnergy spinParity halflife uncHalflife isomer source");
    private static final Map<String, Header> TABLES = Map.of(
            "decays", DECAY,
            "nuclides", NUCLIDE,
            "states", STATE,
            "libLines", LINE
    );
    private Path outputDir;
    private String lastOutputLine;

    @Override
    public Connection getDatabaseConnection(File f) throws Exception {
        return null;
    }

    @Override
    public Connection createNuclibDatabase(File f) throws Exception {
        outputDir = Path.of(f.getPath());
        if (!Files.isDirectory(outputDir))
            Files.createDirectory(outputDir);
        for (Entry<String, Header> entry : TABLES.entrySet()) {
            String table = entry.getKey();
            Header header = entry.getValue();
            Files.writeString(getOutputFile(table), String.join(",", header.getColumns()) + "\n",
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
        return null;
    }

    private void appendLine(Path outputFile, String line) throws Exception {
        lastOutputLine = outputFile + ":" + line;
        Files.writeString(outputFile, line + "\n", StandardOpenOption.APPEND);
    }

    private Path getOutputFile(String table) {
        return outputDir.resolve(table + ".csv");
    }

    @Override
    public void insert(Connection c, String table, String... values) throws Exception {
        if (Main.testRun)
            return;
        Header header = TABLES.get(table);
        if (header == null)
            return;
        String[] data = new String[header.getColumnCount()];
        for (int i = 0; i < values.length; i += 2) {
            String column = values[i];
            String val = values[i + 1];
            if (val == null || val.equals("NULL") || val.equals("NaN"))
                continue;
            int index = header.getColumnPosition(column);
            if (index == -1)
                continue;
            data[index] = val;
        }
        for (int i = 0; i < data.length; i++) {
            if (data[i] != null)
                continue;
            data[i] = "";
        }
        Path outputFile = getOutputFile(table);
        appendLine(outputFile, String.join(",", data));
    }

    @Override
    public String getLastSQL() {
        return lastOutputLine;
    }

    private static class Header {
        private final String[] columns;
        private final Map<String, Integer> positions = new HashMap<>();

        private Header(String[] columns) {
            this.columns = columns;
            for (int i = 0; i < columns.length; i++)
                positions.put(columns[i].toLowerCase(), i);
        }

        static Header fromText(String text) {
            String[] columns = text.split("\\s+");
            return new Header(columns);
        }

        public String[] getColumns() {
            return columns;
        }

        public int getColumnCount() {
            return columns.length;
        }

        public int getColumnPosition(String column) {
            Integer val = positions.get(column.toLowerCase());
            if (val == null)
                return -1;
            return val;
        }
    }
}
