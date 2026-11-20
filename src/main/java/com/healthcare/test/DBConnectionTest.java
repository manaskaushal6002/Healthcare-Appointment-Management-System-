package com.healthcare.test;

import com.healthcare.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Diagnostic utility to test MySQL database connectivity and inspect tables.
 * Can be executed directly in IntelliJ IDEA via Run -> Run 'DBConnectionTest.main()'.
 */
public class DBConnectionTest {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("Healthcare System - Database Connectivity Test");
        System.out.println("=================================================");

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            if (conn != null && !conn.isClosed()) {
                DatabaseMetaData metaData = conn.getMetaData();
                System.out.println("SUCCESS: Database connection established!");
                System.out.println("Database Product: " + metaData.getDatabaseProductName());
                System.out.println("Database Version: " + metaData.getDatabaseProductVersion());
                System.out.println("Driver Version:   " + metaData.getDriverVersion());
                System.out.println("Connected URL:    " + metaData.getURL());
                System.out.println("User:             " + metaData.getUserName());

                System.out.println("\nChecking system tables in 'healthcare_db'...");
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SHOW TABLES")) {
                    int count = 0;
                    while (rs.next()) {
                        count++;
                        System.out.println("  [" + count + "] Table: " + rs.getString(1));
                    }
                    if (count == 0) {
                        System.out.println("WARNING: Database is connected, but no tables found! Please execute schema.sql in MySQL.");
                    } else {
                        System.out.println("SUCCESS: Found " + count + " tables in database.");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("\nCONNECTION FAILED!");
            System.err.println("Details: " + e.getMessage());
            System.err.println("\nTroubleshooting tips:");
            System.err.println("1. Make sure MySQL service is running on your computer.");
            System.err.println("2. Check src/main/resources/db.properties (verify port, username, password).");
            System.err.println("3. Ensure the 'healthcare_db' database is created using schema.sql.");
        } finally {
            DatabaseConnection.getInstance().closeConnection();
        }
    }
}
