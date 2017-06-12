package net.dckg.daogenerator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Denotes a column in database table.
 *
 * <p>Every DAO property which is a PrimaryKey in database must have this annotation.
 * <br>
 * <b>The order of properties must be the same as in database table create script.</b>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Key {
}
