package net.dckg.daogenerator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class containing table metadata.
 */
public class TableInfo {

    private String mTableName;
    private List<ColumnInfo> mColumns;
    private Dao mDao;

    TableInfo(Dao dao) {
        this.mDao = dao;
    }

    /**
     * Returns this object's corresponding table name in database.
     * @return name
     */
    public String getTableName() {
        System.out.println("table name: " + mTableName);
        return mTableName;
    }

    /**
     * Return information about table columns.
     * @return A List of ColumnInfos for all columns.
     * @see ColumnInfo
     */
    public List<ColumnInfo> getColumns() {
        return mColumns;
    }

    /**
     * Return information about PrimaryKey table columns.
     * @return A List of ColumnInfos for PrimaryKey columns.
     */
    public List<ColumnInfo> getKeyColumns() {
        return mColumns.stream().filter(e -> e.isPrimaryKey()).collect(Collectors.toList());
    }

    /**
     * Return information about ordinary (non PrimaryKey) table columns.
     * @return A List of ColumnInfos for ordinary (non PrimaryKey) columns.
     */
    public List<ColumnInfo> getNonKeyColumns() {
        return mColumns.stream().filter(e -> !e.isPrimaryKey()).collect(Collectors.toList());
    }

    /**
     * Returns data of column name.
     * @param pName column name
     * @return data
     */
    public Object getFieldDataByColumnName(String pName) {
        try {
            return getColumnInfoByColumnName(pName).getField().get(mDao);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private ColumnInfo getColumnInfoByColumnName(String pName) {
        return mColumns.stream().filter(e -> e.getDbName().equals(pName)).findAny().get();
    }

    /**
     * Sets this object's corresponding table name in database.
     * @param pTableName name
     */
    void setTableName(String pTableName) {
        this.mTableName = pTableName;
    }

    List<ColumnInfo> getRawColumns() {
        return mColumns;
    }

    /**
     * Creates a List of '?' placeholders for SQL PreparedStatement
     * @param pList any List, only item size is required.
     * @return List
     */
    List<String> getColumnsPlaceholder(List pList) {
        List<String> ret = new ArrayList<>();
        for (int i = 0; i < pList.size(); i++)
            ret.add("?");
        return ret;
    }

    List<String> getColumnsPlaceholder() {
        return getColumnsPlaceholder(mColumns);
    }

    /**
     * Converts a List into a string, separated by a delimiter.
     * @param pList
     * @param pDelimiter
     * @return
     */
    static String implode(List<String> pList, String pDelimiter) {
        StringBuilder sb = new StringBuilder();
        pList.stream().forEach(e -> sb.append(e + pDelimiter));
        sb.setLength(sb.length() - pDelimiter.length());
        return sb.toString();
    }

    static List<String> columnsToName(List<ColumnInfo> pList) {
        List<String> res = new ArrayList<>();
        pList.stream().forEach(e -> res.add(e.getDbName()));
        return res;
    }

    void setKeys(Dao pDao, PreparedStatement pStmt) throws SQLException {
        setColumns(pDao, pStmt, getKeyColumns());
    }

    void setKeys(Dao pDao, PreparedStatement pStmt, int pOffset) throws SQLException {
        setColumns(pDao, pStmt, getKeyColumns(), pOffset);
    }

    void setNonKeys(Dao pDao, PreparedStatement pStmt) throws SQLException {
        setColumns(pDao, pStmt, getNonKeyColumns());
    }

    void setColumns(Dao pDao, PreparedStatement pStmt) throws SQLException {
        setColumns(pDao, pStmt, mColumns);
    }

    void setColumns(Dao pDao, PreparedStatement pStmt, List<ColumnInfo> pCols) throws SQLException {
        setColumns(pDao, pStmt, pCols, 1);
    }

    void setColumns(Dao pDao, PreparedStatement pStmt, List<ColumnInfo> pCols, int pFirstOffset) throws SQLException {
        for (int offset = 0; offset < pCols.size(); offset++) {
            try {
                ColumnInfo e = pCols.get(offset);
                System.out.println("bind param " + (offset + pFirstOffset) + " to value of " + e.getField().getName());
                pStmt.setObject((offset + pFirstOffset), e.getField().get(pDao), e.getType());
            } catch (IllegalAccessException e1) {
                throw new RuntimeException(e1);
            }
        }
        System.out.println("-----");
    }

    /**
     * Helper method for queries which return a single row.
     * @param pDao
     * @param pStmt
     * @throws NotFoundException
     */
    void singleQuery(Dao pDao, PreparedStatement pStmt) throws NotFoundException {
        try (ResultSet rs = pStmt.executeQuery()) {
            if (rs.next()) {
                for (ColumnInfo e : mColumns)
                    try {
                        e.getField().set(pDao, rs.getObject(mColumns.indexOf(e) + 1, e.getField().getType()));
                    } catch (IllegalAccessException e1) {
                        throw new RuntimeException(e1);
                    }
            } else {
                throw new NotFoundException();
            }
        } catch (SQLException e) {
            throw new NotFoundException(e);
        }
    }

    /**
     * Helper method for queries which return multiple rows.
     * @param pDaoClass
     * @param pStmt
     * @return
     */
    List<IDao> listQuery(Class<? extends Dao> pDaoClass, PreparedStatement pStmt) {

        List<IDao> list = new ArrayList<>();

        try (ResultSet rs = pStmt.executeQuery()) {
            while (rs.next()) {
                Dao dao;
                try {
                    dao = pDaoClass.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                for (ColumnInfo e : mColumns) {
                    try {
                        Object obj = rs.getObject(mColumns.indexOf(e) + 1, e.getField().getType());
                        e.getField().set(dao, obj);
                    } catch (IllegalAccessException e1) {
                        throw new RuntimeException(e1);
                    } catch (SQLException e1) {
                        throw new RuntimeException("DAO does not match DB table schema.");
                    }
                }
                list.add(dao);
                dao = null;
            }
        } catch (SQLException e) {
            // nothing, return empty list
        }
        return list;
    }

    /**
     * Helper method.
     * @param pStmt
     * @return
     * @throws SQLException
     */
    public int databaseUpdate(PreparedStatement pStmt) throws SQLException {
        return pStmt.executeUpdate();
    }

    public void setColumns(List<ColumnInfo> pColumns) {
        this.mColumns = pColumns;
    }

}
