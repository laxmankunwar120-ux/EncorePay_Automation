package com.encorepay.utilities;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable holder for everything required to render an Account Comparison
 * (Old vs New server data migration) PDF report.
 *
 * <p>Field values must already be normalized by the caller. {@code oldData}
 * and {@code newData} are keyed by {@code Scope_Field} (see
 * {@code ViewAccountPage}). {@code comparisonResults} maps the same keys to a
 * status of {@code MATCHED}, {@code MISMATCH} or {@code FIELD NOT FOUND}.</p>
 */
public class AccountComparisonReportData {

    private final String accountNumber;
    private final String oldServerUrl;
    private final String newServerUrl;
    private final String browser;
    private final String generatedBy;
    private final String oldScreenshotPath;
    private final String newScreenshotPath;
    private final Map<String, String> oldData;
    private final Map<String, String> newData;
    private final Map<String, String> comparisonResults;

    private AccountComparisonReportData(Builder b) {
        this.accountNumber = b.accountNumber;
        this.oldServerUrl = b.oldServerUrl;
        this.newServerUrl = b.newServerUrl;
        this.browser = b.browser;
        this.generatedBy = b.generatedBy;
        this.oldScreenshotPath = b.oldScreenshotPath;
        this.newScreenshotPath = b.newScreenshotPath;
        this.oldData = b.oldData != null ? b.oldData : new LinkedHashMap<>();
        this.newData = b.newData != null ? b.newData : new LinkedHashMap<>();
        this.comparisonResults = b.comparisonResults != null ? b.comparisonResults : new LinkedHashMap<>();
    }

    public String getAccountNumber() { return accountNumber; }
    public String getOldServerUrl() { return oldServerUrl; }
    public String getNewServerUrl() { return newServerUrl; }
    public String getBrowser() { return browser; }
    public String getGeneratedBy() { return generatedBy; }
    public String getOldScreenshotPath() { return oldScreenshotPath; }
    public String getNewScreenshotPath() { return newScreenshotPath; }
    public Map<String, String> getOldData() { return oldData; }
    public Map<String, String> getNewData() { return newData; }
    public Map<String, String> getComparisonResults() { return comparisonResults; }

    public static class Builder {
        private String accountNumber = "";
        private String oldServerUrl = "";
        private String newServerUrl = "";
        private String browser = "";
        private String generatedBy = "EncorePay Automation Framework";
        private String oldScreenshotPath = "";
        private String newScreenshotPath = "";
        private Map<String, String> oldData;
        private Map<String, String> newData;
        private Map<String, String> comparisonResults;

        public Builder accountNumber(String v) { this.accountNumber = v == null ? "" : v; return this; }
        public Builder oldServerUrl(String v) { this.oldServerUrl = v == null ? "" : v; return this; }
        public Builder newServerUrl(String v) { this.newServerUrl = v == null ? "" : v; return this; }
        public Builder browser(String v) { this.browser = v == null ? "" : v; return this; }
        public Builder generatedBy(String v) { this.generatedBy = v == null ? "" : v; return this; }
        public Builder oldScreenshotPath(String v) { this.oldScreenshotPath = v == null ? "" : v; return this; }
        public Builder newScreenshotPath(String v) { this.newScreenshotPath = v == null ? "" : v; return this; }
        public Builder oldData(Map<String, String> v) { this.oldData = v; return this; }
        public Builder newData(Map<String, String> v) { this.newData = v; return this; }
        public Builder comparisonResults(Map<String, String> v) { this.comparisonResults = v; return this; }

        public AccountComparisonReportData build() {
            return new AccountComparisonReportData(this);
        }
    }
}
