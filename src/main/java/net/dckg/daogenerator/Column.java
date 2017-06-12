package net.dckg.daogenerator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.sql.Types;

/**
 * Denotes a column in database.
 *
 * <p>Every DAO property must be public and have this annotation;
 * <br> - name is it's column name in database
 * <br> - type is it's column type in database
 * <br>
 * <b>The order of properties must be the same as in database table create script.</b>
 * @see java.sql.Types
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    String name() default "";

    /**
     * the SQL type (as defined in java.sql.Types) to be sent to the database
     */
    int type() default Types.INTEGER;
}
