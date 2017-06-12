package net.dckg.daogenerator;

import java.lang.reflect.Field;

/**
 * Container class containing mapping of DAO properties to database column name, type and if it's a PrimaryKey.
 */
public class ColumnInfo implements Comparable<ColumnInfo> {

    private Field mField;

    /**
     * Set Field for reflection.
     * @param pField Field of property
     * @return self
     */
    ColumnInfo withField(Field pField) {
        this.mField = pField;
        pField.setAccessible(true);
        return this;
    }

    /**
     * Returns whether this property is a PrimaryKey or not.
     * @return true if this property is a PrimaryKey.
     */
    public boolean isPrimaryKey() {
        return mField.isAnnotationPresent(Key.class);
    }

    /**
     * Returns this property's corresponding column name in database.
     * @return table name.
     */
    public String getDbName() {
        return ((Column) mField.getAnnotation(Column.class)).name();
    }

    /**
     * Returns this property's name in DAO class.
     * @return name.
     */
    public String getFieldName() {
        return mField.getName();
    }

    /**
     * Returns this property's corresponding type in database.
     * @return type.
     * @See java.sql.Types
     */
    public int getType() {
        return ((Column) mField.getAnnotation(Column.class)).type();
    }

    /**
     * Get property field for getting value via reflection.
     * @return Field.
     */
    public Field getField() {
        return mField;
    }

    @Override
    public int compareTo(ColumnInfo o) {
        return getDbName().compareTo(o.getDbName());
    }
}
