package com.encorepay.utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    private static final String CONFIG_PATH = "src/main/resources/config.properties";
    private final Properties properties;

    public ConfigReader() {
        properties = new Properties();
        loadProperties();
    }

    private void loadProperties() {
        try (InputStream fis = new FileInputStream(CONFIG_PATH)) {
            properties.load(fis);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load config from " + CONFIG_PATH, e);
        }
    }

    public String getURL() {
        return getProperty("url");
    }

    public String getOldUrl() {
        return getProperty("old.url");
    }

    public String getNewUrl() {
        return getProperty("new.url");
    }

    public String getUsername() {
        return getProperty("username");
    }

    public String getPassword() {
        return getProperty("password");
    }

    public String getBrowser() {
        return getProperty("browser", "chrome");
    }

    public int getExplicitWait() {
        return getIntProperty("explicitWait", 15);
    }

    public int getImplicitWait() {
        return getIntProperty("implicitWait", 0);
    }

    public int getOverlayTimeout() {
        return getIntProperty("overlayTimeout", 8);
    }

    public int getServerCheckTimeout() {
        return getIntProperty("serverCheckTimeout", 10);
    }

    public int getServerCheckMaxRetries() {
        return getIntProperty("serverCheckMaxRetries", 3);
    }

    public long getServerCheckRetryDelay() {
        return getIntProperty("serverCheckRetryDelay", 5000);
    }

    public boolean isServerCheckEnabled() {
        return Boolean.parseBoolean(getProperty("serverCheckEnabled", "true"));
    }

    public String getServerDownAction() {
        return getProperty("serverDownAction", "skip").toLowerCase();
    }

    public String getClientName() {
        return getProperty("clientName", "Default Client");
    }

    public String getApplicationName() {
        return getProperty("applicationName", "EncorePay Automation");
    }

    public String getEnvironmentName() {
        return getProperty("environment", "QA");
    }

    public String getBuildVersion() {
        return getProperty("buildVersion", "Current QA Build");
    }

    public String getReleaseName() {
        return getProperty("releaseName", "QA Regression Cycle");
    }

    public String getExecutionOwner() {
        return getProperty("executionOwner", System.getProperty("user.name"));
    }

    public String getProperty(String key) {
        String override = System.getProperty(key);
        if (override != null) {
            return override;
        }
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        String override = System.getProperty(key);
        if (override != null) {
            return override;
        }
        return properties.getProperty(key, defaultValue);
    }

    public Properties getProperties() {
        return properties;
    }

    private int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}