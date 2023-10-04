package fi.stuk.nuclibre;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.util.ArrayList;

public class CSVOutputDialect implements OutputDialect {
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
        return null;
    }

    private void writeLine(Path outputFile, String line) throws Exception {
        lastOutputLine = outputFile + ":" + line;
        Files.writeString(outputFile, line + "\n", StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    }

    @Override
    public void insert(Connection c, String table, String... values) throws Exception {
        if (Main.testRun)
            return;
        ArrayList<String> data = new ArrayList<>();
        for (int i = 1; i < values.length; i += 2) {
            String val = values[i];
            if (val == null || val.equals("NULL") || val.equals("NaN")) {
                data.add("");
                continue;
            }
            data.add(val);
        }
        Path outputFile = outputDir.resolve(table + ".csv");
        if (!Files.exists(outputFile) || Files.size(outputFile) == 0) {
            ArrayList<String> headers = new ArrayList<>();
            for (int i = 0; i < values.length; i += 2)
                headers.add(values[i]);
            writeLine(outputFile, String.join(",", headers));
        }
        writeLine(outputFile, String.join(",", data));
    }

    @Override
    public String getLastSQL() {
        return lastOutputLine;
    }
}
