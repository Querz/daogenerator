package net.dckg.daogenerator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface IDao {

    /**
     * Sets the sql connection.
     * @param conn
     * @return self
     */
    IDao withConnection(Connection conn);

    /**
     * Load this object's data under primary key.
     * @throws SQLException
     */
    void load() throws SQLException;

    /**
     * Load all columns of DB table into List.
     *
     * <p>This is very resource expensive.
     * <br>Do not use for large tables!
     * @return
     * @throws SQLException
     */
    List<IDao> loadAll() throws SQLException;

    /**
     * Create a new record in DB with this object's data.
     *
     * <p>PrimaryKeys can be left empty (null), if autoincrement enabled.
     * @throws SQLException
     */
    void create() throws SQLException;

    /**
     * Save contents of this object to DB.
     *
     * <p>Object must already exist (PrimaryKeys).
     * @throws SQLException
     */
    void save() throws SQLException;

    /**
     * Delete this object from DB.
     * @throws SQLException
     */
    void delete() throws SQLException;

    /**
     * Delete all rows in this object's table.
     * @throws SQLException
     */
    void deleteAll() throws SQLException;

    /**
     * Return number of rows in this object's table.
     * @return
     * @throws SQLException
     */
    int countAll() throws SQLException;

}
