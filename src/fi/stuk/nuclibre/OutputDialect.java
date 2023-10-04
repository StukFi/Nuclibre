package fi.stuk.nuclibre;

import java.io.File;
import java.sql.Connection;

/**
 * SQL dialect interface. Work around schema definition differences in RDBMS implementations.
 */
public interface OutputDialect {

    /**
     * Get JDBC connection object to a database.
     *
     * @param f Path on disk.
     * @return JDBC connection.
     * */
    Connection getDatabaseConnection(File f) throws Exception;

    /**
     * Get JDBC connection object to a database.
     *
     * The necessary tables will be created in the database.
     *
     * @param f Path on disk.
     * @return JDBC connection.
     * @throws Exception
     */
    Connection createNuclibDatabase(File f) throws Exception;

    /**
     * Insert a row of values into table using database connection c.
     *
     * @param c JDBC connection.
     * @param table Target table name.
     * @param values Even-sized array of (column, value) pairs.
     * @throws Exception
     */
    void insert(Connection c, String table, String... values) throws Exception;

    /**
     * Get the last executed SQL statement.
     *
     * @return Plain text SQL statement.
     * */
    String getLastSQL();

}
