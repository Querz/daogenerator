package net.dckg.daogenerator;

import java.sql.SQLException;

/**
 * Object's PrimaryKey could not be found.
 */
public class NotFoundException extends SQLException {

    public NotFoundException(String reason) {
        super(reason);
    }

    public NotFoundException(Throwable cause) {
        super(cause);
    }

    public NotFoundException(String reason, Throwable cause) {
        super(reason, cause);
    }

    public NotFoundException() {

    }
}
