package com.encorepay;

import com.encorepay.base.BaseClass;
import com.encorepay.pages.*;
import com.encorepay.utilities.*;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.*;

import java.util.*;

public class DashboardComparisonTest extends BaseClass {

    private LoginPage loginPage;
    private DashboardPage dashboardPage;
    private LogoutPage logoutPage;
    private ConfigReader configReader;

    private Map<String, String> oldDashboardData;
    private Map<String, String> newDashboardData;
    private Map<String, String> oldPerformanceData;
    private Map<String, String> newPerformanceData;
    private String pdfReportPath;

    @BeforeClass(alwaysRun = true)
    public void setupPages() {
        loginPage = new LoginPage(driver);
        dashboardPage = new DashboardPage(driver);
        logoutPage = new LogoutPage(driver);
        configReader = new ConfigReader();
    }

    @Test(priority = 1, description = "Login to Old Server and capture complete dashboard data")
    public void tc01_CaptureOldDashboard() {
        String oldUrl = resolveOldUrl();
        if (oldUrl == null || oldUrl.isBlank()) {
            throw new SkipException("old.url is not configured.");
        }

        navigateToAndLogin(oldUrl);
        dashboardPage.openDashboard();
        oldDashboardData = dashboardPage.captureFullDashboardData();
        oldPerformanceData = dashboardPage.capturePerformanceTiming();

        logoutPage.logout();
        action.waitForUiStable();

        recordVerification("Old Server dashboard data captured.");
        recordTestData("Old Server URL: " + oldUrl);
        System.out.println("[URL] Old server active URL: " + driver.getCurrentUrl());
    }

    @Test(priority = 2, description = "Login to New Server and capture complete dashboard data")
    public void tc02_CaptureNewDashboard() {
        String newUrl = resolveNewUrl();
        if (newUrl == null || newUrl.isBlank()) {
            throw new SkipException("new.url is not configured.");
        }

        navigateToAndLogin(newUrl);
        dashboardPage.openDashboard();
        newDashboardData = dashboardPage.captureFullDashboardData();
        newPerformanceData = dashboardPage.capturePerformanceTiming();

        logoutPage.logout();
        action.waitForUiStable();

        recordVerification("New Server dashboard data captured.");
        recordTestData("New Server URL: " + newUrl);
        System.out.println("[URL] New server active URL: " + driver.getCurrentUrl());
    }

    @Test(priority = 3, dependsOnMethods = {"tc01_CaptureOldDashboard", "tc02_CaptureNewDashboard"},
          description = "Compare Old vs New Dashboard and generate PDF report")
    public void tc03_CompareDashboardsAndGeneratePdf() {
        if (oldDashboardData == null || newDashboardData == null) {
            throw new SkipException("Dashboard data not available for comparison.");
        }

        Map<String, String> comparisonResults = compareDashboards(oldDashboardData, newDashboardData);
        long mismatches = comparisonResults.values().stream()
            .filter(v -> v.equalsIgnoreCase("Mismatch") || v.equalsIgnoreCase("Missing"))
            .count();

        if (mismatches > 0) {
            recordVerification("Dashboard comparison completed with " + mismatches + " mismatches.");
        } else {
            recordVerification("Dashboard comparison completed. All sections matched.");
        }

        pdfReportPath = DashboardComparisonPdfGenerator.generateComparisonReport(
            oldDashboardData, newDashboardData, oldPerformanceData, newPerformanceData, comparisonResults);

        recordVerification("Dashboard Comparison PDF report generated: " + pdfReportPath);
        System.out.println("[INFO] Dashboard Comparison PDF: " + pdfReportPath);
    }

    private String resolveOldUrl() {
        String url = configReader.getOldUrl();
        if (url == null || url.isBlank()) {
            return null;
        }
        url = url.trim();
        if (url.startsWith("<") && url.endsWith(">")) {
            url = url.substring(1, url.length() - 1).trim();
        }
        return url;
    }

    private String resolveNewUrl() {
        String url = configReader.getNewUrl();
        if (url == null || url.isBlank()) {
            return null;
        }
        url = url.trim();
        if (url.startsWith("<") && url.endsWith(">")) {
            url = url.substring(1, url.length() - 1).trim();
        }
        return url;
    }

    private void navigateToAndLogin(String url) {
        if (url == null || url.isBlank()) {
            throw new SkipException("Target URL is empty.");
        }

        try {
            driver.get(url);
        } catch (Exception e) {
            System.out.println("[WARN] Initial navigation failed: " + e.getMessage());
        }

        waitForPageReady();
        action.waitForUiStable();
        System.out.println("[URL] Navigated to: " + url + " | Active URL: " + driver.getCurrentUrl());

        try {
            if (!loginPage.isLoginPageVisible()) {
                throw new SkipException("Sign-in page is not visible for URL: " + url);
            }
        } catch (Exception e) {
            throw new SkipException("Sign-in page is not visible for URL: " + url + ", reason=" + e.getMessage());
        }

        try {
            loginPage.login(configReader.getUsername(), configReader.getPassword(), false);
        } catch (Exception e) {
            System.out.println("[WARN] First login attempt failed: " + e.getMessage());
            try {
                driver.get(url);
                waitForPageReady();
                action.waitForUiStable();
                System.out.println("[URL] Retry navigated to: " + url + " | Active URL: " + driver.getCurrentUrl());
                loginPage.login(configReader.getUsername(), configReader.getPassword(), false);
            } catch (Exception retryException) {
                throw new SkipException("Login failed for URL after retry: " + url + ", reason=" + retryException.getMessage());
            }
        }

        Assert.assertTrue(loginPage.isLoginSuccessful(), "Login failed for URL: " + url);
        waitForPageReady();
        action.waitForUiStable();
    }

    private Map<String, String> compareDashboards(Map<String, String> oldData, Map<String, String> newData) {
        Map<String, String> results = new LinkedHashMap<>();

        Set<String> allKeys = new LinkedHashSet<>();
        allKeys.addAll(oldData.keySet());
        allKeys.addAll(newData.keySet());

        for (String key : allKeys) {
            String oldVal = oldData.getOrDefault(key, "Missing");
            String newVal = newData.getOrDefault(key, "Missing");

            if (oldVal.equalsIgnoreCase("Missing") || newVal.equalsIgnoreCase("Missing")) {
                results.put(key, "Missing");
            } else if (oldVal.equalsIgnoreCase(newVal)) {
                results.put(key, "Matched");
            } else {
                results.put(key, "Mismatch");
            }

            recordTestData(key + " | Old: " + oldVal + " | New: " + newVal + " | Result: " + results.get(key));
        }

        return results;
    }
}
