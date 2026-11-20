package com.healthcare.util;

/**
 * Custom unchecked exception for database and JDBC access failures.
 * Encapsulates low-level SQLExceptions to keep Service and UI layers decoupled from SQL details.
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
