package net.dckg.daogenerator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Dao implements IDao {

    private Boolean mDebugAbortAfterSql = false;
    private String mDebugIntermediateSql;

    protected TableInfo mTable = new TableInfo(this);
    protected Connection mConn;

    public Dao() {
        mTable.setColumns(new ArrayList<>());
        getColumnInfo();
        getTableMetadata();
    }

    private void debugIntermediateSql(String pSql) {
        mDebugIntermediateSql = pSql;
    }

    private void getColumnInfo() {
        int pks = 0;
        int cols = 0;

        Field[] publicFields = this.getClass().getFields();
        for(Field f : publicFields) {
            if(f.isAnnotationPresent(Column.class)) {
                cols++;
                ColumnInfo columnInfo = new ColumnInfo().withField(f);
                mTable.getRawColumns().add(columnInfo);
                if(columnInfo.isPrimaryKey())
                    pks++;
            }
        }
        // Collections.sort(mTable.getColumns()); // index must be same as in DB table
        if(pks >= cols || pks == 0 )
            throw new RuntimeException("invalid DAO definition: cols: " + cols + ", primary keys: " + pks);
    }

    private void getTableMetadata() {
        mTable.setTableName(((Table) getAnnotation(this.getClass(), Table.class)).name());
    }

    private static Annotation getAnnotation(Class clazz, Class<? extends Annotation> annotationClass) {
        Annotation[] annotations = clazz.getAnnotations();
        for(Annotation a : annotations) {
            if(a.annotationType().equals(annotationClass))
                return a;
        }
        return null;
    }

    @Override
    public IDao withConnection(Connection pConn) {
        this.mConn = pConn;
        return this;
    }

    @Override
    public void load() throws SQLException {
        String sql = "SELECT * FROM " + mTable.getTableName() + " " +
                "WHERE (" + TableInfo.implode(TableInfo.columnsToName(mTable.getKeyColumns()), " = ? AND ") + " = ?);";
        debugIntermediateSql(sql);
        if(mDebugAbortAfterSql) return;

        PreparedStatement stmt = mConn.prepareStatement(sql);
        mTable.setKeys(this, stmt);
        mTable.singleQuery(this, stmt);
    }

    @Override
    public List<IDao> loadAll() throws SQLException {
        String sql = "SELECT * FROM " + mTable.getTableName() + ";";
        debugIntermediateSql(sql);
        if(mDebugAbortAfterSql) return null;

        PreparedStatement stmt = mConn.prepareStatement(sql);
        return mTable.listQuery(this.getClass(), stmt);
    }

    @Override
    public void create() throws SQLException {
        String sql = "INSERT INTO " + mTable.getTableName() + " " +
                "(" + TableInfo.implode(TableInfo.columnsToName(mTable.getColumns()), ", ") + ") " +
                "VALUES (" + TableInfo.implode(mTable.getColumnsPlaceholder(), ", ") + ");";

        debugIntermediateSql(sql);
        if(mDebugAbortAfterSql) return;

        PreparedStatement stmt = mConn.prepareStatement(sql);

        mTable.setColumns(this, stmt);
        mTable.databaseUpdate(stmt);
    }

    @Override
    public void save() throws SQLException {
        String sql = "UPDATE " + mTable.getTableName() + " " +
                "SET " + TableInfo.implode(TableInfo.columnsToName(mTable.getNonKeyColumns()), " = ?, ") + " = ? " +
                "WHERE (" + TableInfo.implode(TableInfo.columnsToName(mTable.getKeyColumns()), " = ? AND ") + " = ?);";
        debugIntermediateSql(sql);
        if(mDebugAbortAfterSql) return;

        PreparedStatement stmt = mConn.prepareStatement(sql);

        mTable.setNonKeys(this, stmt);
        mTable.setKeys(this, stmt, mTable.getNonKeyColumns().size() + 1);
        switch(mTable.databaseUpdate(stmt)) {
            case 0:
                throw new NotFoundException("Object could not be saved! (PrimaryKey: not found)!");
            case 1:
                return;
            default:
                throw new SQLException("PrimaryKey Error when updating DB (Many objects were affected)!");
        }
    }

    @Override
    public void delete() throws SQLException {
        String sql = "DELETE FROM " + mTable.getTableName() +
                " WHERE (" + TableInfo.implode(TableInfo.columnsToName(mTable.getKeyColumns()), " = ? AND ") + " = ?);";
        debugIntermediateSql(sql);
        if(mDebugAbortAfterSql) return;

        try(PreparedStatement stmt = mConn.prepareStatement(sql)) {
            mTable.setKeys(this, stmt);
            switch (mTable.databaseUpdate(stmt)) {
                case 0:
                    throw new NotFoundException("Object could not be deleted (PrimaryKey not found)!");
                case 1:
                    break;
                default:
                    throw new SQLException("PrimaryKey error when updating DB (Many rows were deleted)!");
            }
        }
    }

    @Override
    public void deleteAll() throws SQLException {
        String sql = "DELETE FROM " + mTable.getTableName() + ";";
        debugIntermediateSql(sql);
        if(mDebugAbortAfterSql) return;

        try(PreparedStatement stmt = mConn.prepareStatement(sql)) {
            mTable.databaseUpdate(stmt);
        }
    }

    @Override
    public int countAll() throws SQLException {
        String sql = "SELECT Count(*) FROM " + mTable.getTableName() + ";";
        debugIntermediateSql(sql);
        if(mDebugAbortAfterSql) return 0;

        int rows = 0;
        try(PreparedStatement stmt = mConn.prepareStatement(sql)) {
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            if(rs.next())
                rows = rs.getInt(1);
        }

        return rows;
    }

}
