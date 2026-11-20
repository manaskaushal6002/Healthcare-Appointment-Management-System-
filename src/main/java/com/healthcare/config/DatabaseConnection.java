package com.healthcare.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton database connection manager using JDBC.
 * Manages active Connection lifecycle and provides safe reconnection logic.
 */
public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static DatabaseConnection instance;
    private Connection connection;

    static {
        try {
            Class.forName(MYSQL_DRIVER);
            LOGGER.info("MySQL JDBC Driver registered successfully.");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL Driver not found on classpath. Ensure mysql-connector-j is included in pom.xml", e);
        }
    }

    private DatabaseConnection() {
        // Private constructor for Singleton pattern
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Retrieves an active JDBC Connection.
     * Automatically reconnects if the connection was previously closed or disconnected.
     *
     * @return active java.sql.Connection
     * @throws SQLException if a database access error occurs or credentials fail
     */
    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed() || !connection.isValid(2)) {
            AppConfig config = AppConfig.getInstance();
            String url = config.getDbUrl();
            String user = config.getDbUser();
            String password = config.getDbPassword();

            try {
                LOGGER.info("Opening new database connection to: " + url.split("\\?")[0]);
                connection = DriverManager.getConnection(url, user, password);
                LOGGER.info("Database connection established successfully.");
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to connect to MySQL database at " + url + " as user: " + user, e);
                throw new SQLException("Could not connect to MySQL database. Please verify MySQL service is running and db.properties credentials are correct. Error: " + e.getMessage(), e);
            }
        }
        return connection;
    }

    /**
     * Safely closes the underlying database connection.
     */
    public synchronized void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    LOGGER.info("Database connection closed cleanly.");
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing database connection", e);
            } finally {
                connection = null;
            }
        }
    }
}
