package net.dckg.daogenerator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Denotes a table in database.
 *
 * <p>Every DAO class has to declare a table name.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
    String name() default "";
}