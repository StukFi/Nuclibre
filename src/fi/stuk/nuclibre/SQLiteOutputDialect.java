package fi.stuk.nuclibre;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SQLiteOutputDialect implements OutputDialect {
    /** Last SQL statement preformed. */
    public static String lastSQL = null;

    /**
     * Get an sqlite database connection for a given file.
     * @param f the file.
     * @return the sqlite database connection for the file.
     * @throws java.lang.Exception if something goes wrong.
     */
    public Connection getDatabaseConnection(File f) throws Exception{
        String urlStr ="jdbc:sqlite:"+ f.getAbsolutePath();
        //Obtain a connection instance using the driver via DriverManager
        Connection c = DriverManager.getConnection (urlStr, null, null);
        return c;
    }

    /**
     * Creates an empty nuclibre sqlite database to a given file.
     * <p>
     * If the file doesn't exist, a new file is created with the database
     * structure. If the file already exists, an exception will be thrown (use
     * {@linkplain #getDatabaseConnection(java.io.File)} instead).
     * @param f the database file.
     * @return the database connection used to create the database.<p>
     * Note that the connection remains open, and should be closed by the
     * user when appropriate using {@linkplain Connection#close()}.
     * @throws java.lang.Exception if something goes wrong.
     */
    @Override
    public Connection createNuclibDatabase(File f) throws Exception{
        Connection c = getDatabaseConnection(f);
        c.setAutoCommit(false);
        createTables(c);
        return(c);
    }
    @Override
    public void insert(Connection c, String table, String... values) throws Exception {
        if(Main.testRun)return;
        String columnStr = "";
        String valueStr = "";
        List<String> add = new ArrayList<>();
        for(int i = 0;i < values.length;i+=2){
            String col = values[i];
            String val = values[i+1];
            if(col != null && val != null && !val.equals("NULL") && !val.equals("null") && !val.equals("NaN")){
                add.add(col);
                add.add(val);
            }
        }
        for(int i = 0;i < add.size();i+=2){
            String col = add.get(i);
            String val = add.get(i+1);
            columnStr += col;
            valueStr += "'"+val+"'";
            if(i != add.size()-2){
                columnStr += ",";
                valueStr += ",";
            }
        }
        String sql = "INSERT INTO "+table+" ("+columnStr+") VALUES ("+valueStr+");";
        lastSQL = sql;
        Statement s = c.createStatement();
        s.execute(sql);
        s.close();

    }

    @Override
    public String getLastSQL() {
        return lastSQL;
    }

    /**
     * Create a given table structure using a given database connection.
     * @param c the database connection.
     * @param tableSql the table structure creation SQL.
     * @throws Exception if something goes wrong.
     */
    public void createTable(Connection c, String tableSql) throws Exception{
        Statement s = c.createStatement();
        s.execute(tableSql);
        s.closeOnCompletion();
    }

    /**
     * Create nuclibre table structure using a given database connection.
     * @param c the database connection
     * @throws Exception if something goes wrong.
     */
    public void createTables(Connection c) throws Exception{
        createTable(c, NuclibreSQL.CREATE_DECAYS_STRING);
        createTable(c, NuclibreSQL.CREATE_NUCLIDES_STRING);
        createTable(c, NuclibreSQL.CREATE_STATES_STRING);
        createTable(c, NuclibreSQL.CREATE_LIBLINES_STRING);
        c.commit();
    }

}
