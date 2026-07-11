package com.encorepay.config;

import com.encorepay.utilities.ConfigReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Singleton used by AdminJobsTest and JobMonitoringPdfGenerator.
 * Wraps ConfigReader (utilities package) — single source of truth.
 */
public class ConfigLoader {

    private static ConfigLoader  instance;
    private final  ConfigReader  reader;
    private final  List<ClientConfig> clients;
    private        String        activeUrl;

    // ── Singleton ──────────────────────────────────────────────────────────────
    public static synchronized ConfigLoader getInstance() {
        if (instance == null) {
            instance = new ConfigLoader();
        }
        return instance;
    }

    private ConfigLoader() {
        reader  = new ConfigReader();
        clients = parseClients();
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    public String getURL() {
        return activeUrl != null ? activeUrl : reader.getURL();
    }

    public String getClientName() {
        return reader.getClientName();
    }

    public boolean isMultiClientEnabled() {
        return Boolean.parseBoolean(
            reader.getProperty("enableMultiClientReporting", "false"));
    }

    public List<ClientConfig> getClients() {
        return Collections.unmodifiableList(clients);
    }

    /** Override URL per-client during multi-client run. Pass null to reset. */
    public void setActiveUrl(String url) {
        this.activeUrl = url;
    }

    public String getProperty(String key) {
        return reader.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return reader.getProperty(key, defaultValue);
    }

    // Common passthroughs to ConfigReader for convenience
    public int getExplicitWait() { return reader.getExplicitWait(); }
    public int getImplicitWait() { return reader.getImplicitWait(); }
    public int getOverlayTimeout() { return reader.getOverlayTimeout(); }
    public String getUsername() { return reader.getUsername(); }
    public String getPassword() { return reader.getPassword(); }
    public String getApplicationName() { return reader.getApplicationName(); }
    public String getEnvironmentName() { return reader.getEnvironmentName(); }
    public String getBuildVersion() { return reader.getBuildVersion(); }
    public String getReleaseName() { return reader.getReleaseName(); }
    public String getExecutionOwner() { return reader.getExecutionOwner(); }

    public Properties getProperties() {
        return reader.getProperties();
    }

    // ── Internal ───────────────────────────────────────────────────────────────

    private List<ClientConfig> parseClients() {
        List<ClientConfig> list = new ArrayList<>();
        if (!isMultiClientEnabled()) return list;

        Properties props = reader.getProperties();
        for (int i = 1; i <= 100; i++) {
            String name = props.getProperty("client." + i + ".name", "").trim();
            String url  = props.getProperty("client." + i + ".url",  "").trim();
            if (name.isEmpty() && url.isEmpty()) break;
            if (name.isEmpty()) name = "Client-" + i;
            if (url.isEmpty()) continue;
            list.add(new ClientConfig(i, name, url));
        }
        return list;
    }
}