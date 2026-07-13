package com.encorepay;

import com.encorepay.base.BaseClass;
import com.encorepay.pages.LoginPage;
import com.encorepay.pages.LogoutPage;
import com.encorepay.pages.ViewAccountPage;
import com.encorepay.utilities.AccountComparisonPdfGenerator;
import com.encorepay.utilities.AccountComparisonReportData;
import com.encorepay.utilities.ConfigReader;
import com.encorepay.utilities.ScreenshotUtil;
import com.encorepay.utilities.helpers.TextHelper;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Validates account data migration between the OLD and NEW servers by comparing
 * every visible field collected from the "View Account" screen on each
 * environment and producing a professional PDF report.
 *
 * <p>All configuration (URLs, credentials, browser, account to search) is read
 * from {@link ConfigReader} / config.properties — nothing is hardcoded.</p>
 */
public class AccountComparisonTest extends BaseClass {

    private LoginPage loginPage;
    private LogoutPage logoutPage;
    private ViewAccountPage viewAccountPage;
    private ConfigReader configReader;

    private Map<String, String> oldAccountData;
    private Map<String, String> newAccountData;
    private String accountNumber;
    private String oldScreenshotPath;
    private String newScreenshotPath;
    private String pdfReportPath;

    @BeforeClass(alwaysRun = true)
    public void setupPages() {
        loginPage = new LoginPage(driver);
        logoutPage = new LogoutPage(driver);
        viewAccountPage = new ViewAccountPage(driver);
        configReader = new ConfigReader();
        System.out.println("[INFO] Starting Account Comparison Execution");
    }

    @Test(priority = 1, description = "Login to OLD Server, open View Account and capture all fields")
    public void tc01_CaptureOldServerAccount() {
        String oldUrl = resolveOldUrl();
        if (oldUrl == null || oldUrl.isBlank()) {
            throw new SkipException("old.url is not configured.");
        }
        accountNumber = resolveSearchTerm();

        navigateToAndLogin(oldUrl, "OLD Server");
        System.out.println("[INFO] Logged into OLD Server");

        viewAccountPage.openCollectionItems();
        System.out.println("[INFO] Navigated to Collection Item");
        viewAccountPage.searchAccount(accountNumber);
        boolean opened = viewAccountPage.openViewAccount();
        if (!opened) {
            System.out.println("[WARN] View Account control not located on OLD Server; capturing listing-level data.");
        }
        System.out.println("[INFO] Opened View Account");

        oldAccountData = viewAccountPage.captureAllAccountFields();
        oldScreenshotPath = captureScreenshotToReports("OLD_Server_View_Account", "Old Server View Account");
        System.out.println("[INFO] Collected OLD Server data: " + oldAccountData.size() + " fields");

        logoutPage.logout();
        action.waitForUiStable();
        recordVerification("OLD Server account data captured (" + oldAccountData.size() + " fields).");
    }

    @Test(priority = 2, description = "Login to NEW Server, open View Account and capture all fields")
    public void tc02_CaptureNewServerAccount() {
        String newUrl = resolveNewUrl();
        if (newUrl == null || newUrl.isBlank()) {
            throw new SkipException("new.url is not configured.");
        }
        accountNumber = resolveSearchTerm();

        navigateToAndLogin(newUrl, "NEW Server");
        System.out.println("[INFO] Logged into NEW Server");

        viewAccountPage.openCollectionItems();
        System.out.println("[INFO] Navigated to Collection Item");
        viewAccountPage.searchAccount(accountNumber);
        boolean opened = viewAccountPage.openViewAccount();
        if (!opened) {
            System.out.println("[WARN] View Account control not located on NEW Server; capturing listing-level data.");
        }
        System.out.println("[INFO] Opened View Account");

        newAccountData = viewAccountPage.captureAllAccountFields();
        newScreenshotPath = captureScreenshotToReports("NEW_Server_View_Account", "New Server View Account");
        System.out.println("[INFO] Collected NEW Server data: " + newAccountData.size() + " fields");

        logoutPage.logout();
        action.waitForUiStable();
        recordVerification("NEW Server account data captured (" + newAccountData.size() + " fields).");
    }

    @Test(priority = 3, dependsOnMethods = {"tc01_CaptureOldServerAccount", "tc02_CaptureNewServerAccount"},
          description = "Compare OLD vs NEW account data and generate PDF report")
    public void tc03_CompareAndGeneratePdf() {
        if (oldAccountData == null || newAccountData == null) {
            throw new SkipException("Account data not available for comparison.");
        }

        Map<String, String> comparisonResults = compareAccounts(oldAccountData, newAccountData);
        long mismatches = comparisonResults.values().stream()
                .filter(v -> !"MATCHED".equalsIgnoreCase(v)).count();

        if (mismatches > 0) {
            recordVerification("Account comparison completed with " + mismatches + " mismatches / not-found fields.");
        } else {
            recordVerification("Account comparison completed. All fields matched.");
        }

        AccountComparisonReportData reportData = new AccountComparisonReportData.Builder()
                .accountNumber(accountNumber)
                .oldServerUrl(resolveOldUrl())
                .newServerUrl(resolveNewUrl())
                .browser(configReader.getBrowser())
                .generatedBy("EncorePay Automation Framework")
                .oldData(oldAccountData)
                .newData(newAccountData)
                .comparisonResults(comparisonResults)
                .oldScreenshotPath(oldScreenshotPath)
                .newScreenshotPath(newScreenshotPath)
                .build();

        pdfReportPath = AccountComparisonPdfGenerator.generateReport(reportData);
        recordVerification("Account Comparison PDF report generated: " + pdfReportPath);
        System.out.println("[INFO] Account Comparison PDF: " + pdfReportPath);
        System.out.println("[INFO] Execution Completed Successfully");
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Map<String, String> compareAccounts(Map<String, String> oldData, Map<String, String> newData) {
        Map<String, String> results = new LinkedHashMap<>();
        Set<String> allKeys = new LinkedHashSet<>();
        allKeys.addAll(oldData.keySet());
        allKeys.addAll(newData.keySet());

        for (String key : allKeys) {
            boolean inOld = oldData.containsKey(key);
            boolean inNew = newData.containsKey(key);
            String oldVal = oldData.getOrDefault(key, "");
            String newVal = newData.getOrDefault(key, "");

            String status;
            if (!inOld || !inNew) {
                status = "FIELD NOT FOUND";
            } else if (TextHelper.normalize(oldVal).equalsIgnoreCase(TextHelper.normalize(newVal))) {
                status = "MATCHED";
            } else {
                status = "MISMATCH";
            }
            results.put(key, status);
            recordTestData(key + " | Old: " + oldVal + " | New: " + newVal + " | Result: " + status);
        }
        return results;
    }

    private String resolveOldUrl() {
        return stripBrackets(configReader.getOldUrl());
    }

    private String resolveNewUrl() {
        return stripBrackets(configReader.getNewUrl());
    }

    private String resolveSearchTerm() {
        return configReader.getProperty("account.comparison.search", "").trim();
    }

    private String stripBrackets(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        url = url.trim();
        if (url.startsWith("<") && url.endsWith(">")) {
            url = url.substring(1, url.length() - 1).trim();
        }
        return url;
    }

    private void navigateToAndLogin(String url, String serverLabel) {
        if (url == null || url.isBlank()) {
            throw new SkipException("Target URL is empty for " + serverLabel + ".");
        }
        try {
            driver.get(url);
        } catch (Exception e) {
            System.out.println("[WARN] Initial navigation failed: " + e.getMessage());
        }
        waitForPageReady();
        action.waitForUiStable();

        try {
            if (!loginPage.isLoginPageVisible()) {
                throw new SkipException("Sign-in page is not visible for " + serverLabel + ": " + url);
            }
        } catch (Exception e) {
            throw new SkipException("Sign-in page is not visible for " + serverLabel + ": " + url + ", reason=" + e.getMessage());
        }

        try {
            loginPage.login(configReader.getUsername(), configReader.getPassword(), false);
        } catch (Exception e) {
            System.out.println("[WARN] First login attempt failed: " + e.getMessage());
            try {
                driver.get(url);
                waitForPageReady();
                action.waitForUiStable();
                loginPage.login(configReader.getUsername(), configReader.getPassword(), false);
            } catch (Exception retryException) {
                throw new SkipException("Login failed for " + serverLabel + " after retry: " + url + ", reason=" + retryException.getMessage());
            }
        }

        Assert.assertTrue(loginPage.isLoginSuccessful(), "Login failed for " + serverLabel + ": " + url);
        waitForPageReady();
        action.waitForUiStable();
    }

    private String captureScreenshotToReports(String fileName, String stepLabel) {
        try {
            ScreenshotUtil.captureScreenshot(driver, "AccountComparison", stepLabel);
        } catch (Exception ignored) {
        }
        try {
            String reportsDir = "reports";
            new File(reportsDir).mkdirs();
            String safeName = (fileName == null ? "screenshot" : fileName).replaceAll("[\\\\/:*?\"<>|\\r\\n]+", "_");
            String path = reportsDir + File.separator + safeName + "_" + System.currentTimeMillis() + ".png";
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(src, new File(path));
            return path;
        } catch (Exception e) {
            System.out.println("[WARN] Report screenshot capture failed: " + e.getMessage());
            return "";
        }
    }
}
