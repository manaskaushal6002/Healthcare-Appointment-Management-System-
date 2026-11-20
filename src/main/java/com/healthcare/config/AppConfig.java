package com.healthcare.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Loads and provides application configuration properties from db.properties.
 * Follows the Singleton pattern to ensure single-point property resolution.
 */
public class AppConfig {

    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());
    private static final String CONFIG_FILE = "db.properties";
    private static AppConfig instance;
    private final Properties properties = new Properties();

    private AppConfig() {
        loadProperties();
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                LOGGER.log(Level.WARNING, "Unable to find " + CONFIG_FILE + " on classpath. Using fallback defaults.");
                setFallbackDefaults();
                return;
            }
            properties.load(input);
            LOGGER.info("Configuration successfully loaded from " + CONFIG_FILE);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error reading " + CONFIG_FILE + ", applying defaults.", ex);
            setFallbackDefaults();
        }
    }

    private void setFallbackDefaults() {
        properties.setProperty("db.url", "jdbc:mysql://localhost:3306/healthcare_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        properties.setProperty("db.user", "root");
        properties.setProperty("db.password", "root");
    }

    public String getDbUrl() {
        return properties.getProperty("db.url");
    }

    public String getDbUser() {
        return properties.getProperty("db.user");
    }

    public String getDbPassword() {
        return properties.getProperty("db.password");
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
