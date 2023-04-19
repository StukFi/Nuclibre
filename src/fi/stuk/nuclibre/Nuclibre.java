/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.stuk.nuclibre;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Nuclibre class contains static utility methods to create and access a nuclibre
 database.
 * @author Tero Karhunen
 */
public class Nuclibre {    
    /**
     * Create a given table structure using a given database connection.
     * @param c the database connection.
     * @param tableSql the table structure creation SQL.
     * @throws Exception if something goes wrong.
     */
    private static void createTable(Connection c, String tableSql) throws Exception{
        Statement s = c.createStatement();
        s.execute(tableSql);
        s.closeOnCompletion();    
    }
    
    /**
     * Create nuclibre table structure using a given database connection.
     * @param c the database connection
     * @throws Exception if something goes wrong.
     */
    private static void createTables(Connection c) throws Exception{        
        createTable(c, NuclibreSQL.CREATE_DECAYS_STRING);
        createTable(c, NuclibreSQL.CREATE_NUCLIDES_STRING);
        createTable(c, NuclibreSQL.CREATE_STATES_STRING);
        createTable(c, NuclibreSQL.CREATE_LIBLINES_STRING);
        c.commit();
    }
    
    /**
     * Get an sqlite database connection for a given file.
     * @param f the file.
     * @return the sqlite database connection for the file.
     * @throws java.lang.Exception if something goes wrong.
     */
    public static Connection getDatabaseConnection(File f) throws Exception{
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
    public static Connection createNuclibDatabase(File f) throws Exception{
        Connection c = getDatabaseConnection(f);
        c.setAutoCommit(false);
        createTables(c);
        return(c);
    }
}
