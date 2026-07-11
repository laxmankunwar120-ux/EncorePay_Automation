package com.encorepay;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.encorepay.base.BaseClass;
import com.encorepay.config.ConfigLoader;
import com.encorepay.config.ClientConfig;
import com.encorepay.models.JobStatus;
import com.encorepay.pages.AdminJobsPage;
import com.encorepay.pages.LoginPage;
import com.encorepay.pages.LogoutPage;
import com.encorepay.utilities.EmailSender;
import com.encorepay.utilities.GoogleChatBotSender;
import com.encorepay.utilities.GoogleChatWebhookSender;
import com.encorepay.utilities.GoogleDriveUploader;
import com.encorepay.utilities.JobMonitoringPdfGenerator;
import com.encorepay.utilities.JobMonitoringSummaryFormatter;

public class AdminJobsTest extends BaseClass {

    // ─── Page Objects ─────────────────────────────────────────────────────────────

    private LoginPage loginPage;
    private AdminJobsPage adminJobsPage;

    // ─── Shared Test State ────────────────────────────────────────────────────────

    private List<JobStatus> jobStatuses;
    private String pdfReportPath;
    private String googleChatReport;

    // ─── Setup / Teardown ─────────────────────────────────────────────────────────

    @BeforeClass(alwaysRun = true)
    public void setupPages() {
        loginPage     = new LoginPage(driver);
        adminJobsPage = new AdminJobsPage(driver);
    }

    @AfterMethod(alwaysRun = true)
    public void logResult(ITestResult result) {
        super.logTestResult(result);
    }

    // ─── TC01: Login ──────────────────────────────────────────────────────────────

    @Test(
        priority    = 1,
        description = "Confirm configured user can log in before monitoring Admin Jobs"
    )
    public void tc01_LoginForAdminJobs() {
        recordTestData("Credential set: configured valid QA admin user");

        loginPage.login(
            prop.getProperty("username"),
            prop.getProperty("password")
        );

        Assert.assertTrue(
            loginPage.isLoginSuccessful(),
            "Login should succeed before Admin Jobs monitoring starts."
        );
        recordVerification("Admin Jobs user authentication verified successfully.");
    }

    // ─── TC02: Navigate ───────────────────────────────────────────────────────────

    @Test(
        priority          = 2,
        dependsOnMethods  = "tc01_LoginForAdminJobs",
        description       = "Confirm Admin > Jobs page loads"
    )
    public void tc02_NavigateToAdminJobs() {
        adminJobsPage.navigateToAdminJobs();

        Assert.assertTrue(
            adminJobsPage.isJobsPageLoaded(),
            "Admin Jobs page should load successfully."
        );
        recordVerification("Admin Jobs page loaded successfully.");
        captureStep("Admin Jobs List");
    }

    // ─── TC03: Capture ────────────────────────────────────────────────────────────

    @Test(
        priority          = 3,
        dependsOnMethods  = "tc02_NavigateToAdminJobs",
        description       = "Capture Admin Job Monitoring data for Post Receipts, "
                          + "Collection Items, and Upcoming Demands"
    )
    public void tc03_CaptureAdminJobMonitoringData() {
        jobStatuses = new ArrayList<>();

        if (!ConfigLoader.getInstance().isMultiClientEnabled()) {
            // ── Single-client path ──────────────────────────────────────────────
            recordTestData(
                "Single test instance mode — capturing all clients from Admin Jobs table"
                + " | URL: " + config.getURL()
            );

            List<JobStatus> statuses = adminJobsPage.captureAdminJobMonitoringData(
                resolveClientNameFromUrl(config.getURL()));
            jobStatuses.addAll(statuses);

            recordVerification(
                "Job Monitoring data captured dynamically for all visible clients."
            );

        } else {
            // ── Multi-client path ───────────────────────────────────────────────
            // FIX: getClients() takes no argument — the undefined variable 'c' is removed.
            List<ClientConfig> clients = ConfigLoader.getInstance().getClients();

            if (clients == null || clients.isEmpty()) {
                throw new IllegalStateException(
                    "enableMultiClientReporting=true but no client.N.name/url entries are "
                    + "configured in config.properties. Add client entries or set "
                    + "enableMultiClientReporting=false."
                );
            }

            for (int i = 0; i < clients.size(); i++) {
                ClientConfig client = clients.get(i);

                recordTestData(
                    "Multi-client mode — monitoring: " + client.getName()
                    + " | URL: " + client.getUrl()
                );

                try {
                    if (i == 0) {
                        // First client: reuse existing authenticated session when possible
                        ConfigLoader.getInstance().setActiveUrl(client.getUrl());

                        String currentUrl = driver.getCurrentUrl();
                        boolean alreadyOnClientSite = currentUrl != null
                            && currentUrl.toLowerCase().contains(
                                extractHost(client.getUrl()));

                        if (!alreadyOnClientSite) {
                            navigateAndLogin(client);
                        }

                    } else {
                        ConfigLoader.getInstance().setActiveUrl(client.getUrl());
                        navigateAndLogin(client);
                    }

                    List<JobStatus> clientStatuses =
                        adminJobsPage.captureAdminJobMonitoringData(
                            resolveClientNameFromUrl(client.getUrl(), client.getName()));
                    jobStatuses.addAll(clientStatuses);

                } catch (Exception e) {
                    recordVerification(
                        "Failed to monitor jobs for client " + client.getName()
                        + ": " + e.getMessage()
                    );
                    addFailedPlaceholders(jobStatuses, client.getName(), e.getMessage());
                }
            }

            // Reset active URL after multi-client run
            ConfigLoader.getInstance().setActiveUrl(null);
        }

        // Logout only after all jobs for all clients have completed.
        safeLogout();

        Assert.assertNotNull(jobStatuses,
            "Admin job monitoring data should not be null.");
        Assert.assertFalse(jobStatuses.isEmpty(),
            "Admin job monitoring data should contain captured rows.");

        googleChatReport =
            JobMonitoringSummaryFormatter.formatAdminJobMonitoringReport(jobStatuses);

        recordTestData(googleChatReport);
        recordVerification("Admin Job Monitoring UI data captured for configured jobs.");
        captureStep("Admin Job Monitoring Data Captured");
    }

    // ─── TC04: Google Chat Summary ────────────────────────────────────────────────

    @Test(
        priority          = 4,
        dependsOnMethods  = "tc03_CaptureAdminJobMonitoringData",
        description       = "Send Google Chat summary for Admin Job Monitoring"
    )
    public void tc04_GenerateAdminJobMonitoringReports() {
        Assert.assertNotNull(jobStatuses,
            "Job statuses must have been captured by tc03 before reports can be generated.");

        String webhookUrl = resolveGoogleChatWebhookUrl();
        if (webhookUrl.isBlank()) {
            recordVerification(
                "Google Chat webhook URL not configured; chat notification skipped.");
            return;
        }

        GoogleChatWebhookSender.sendSummary(webhookUrl, googleChatReport);
        recordVerification("Admin Job Monitoring report sent to Google Chat.");
    }

    // ─── TC05: PDF ────────────────────────────────────────────────────────────────

    @Test(
        priority          = 5,
        dependsOnMethods  = "tc04_GenerateAdminJobMonitoringReports",
        description       = "Generate Daily Job Status PDF report"
    )
    public void tc05_GenerateDailyJobStatusPdfReport() {
        Assert.assertNotNull(jobStatuses,
            "Job statuses must have been captured by tc03 before PDF report can be generated.");

        pdfReportPath = JobMonitoringPdfGenerator.generatePdfReport(jobStatuses);

        Assert.assertNotNull(pdfReportPath,
            "PDF report path should not be null after generation.");
        Assert.assertFalse(pdfReportPath.isBlank(),
            "PDF report path should not be blank.");

        recordVerification("Daily Job Status PDF report generated: " + pdfReportPath);
        System.out.println("[INFO] Daily Job Status PDF report: " + pdfReportPath);

        sendEmailReportIfConfigured(pdfReportPath);
        sendChatReportFileMessageIfConfigured(pdfReportPath);
    }

    // ─── Private Helpers ──────────────────────────────────────────────────────────

    /**
     * Navigates to the given client's URL, waits for the page, logs in, then
     * confirms the Admin Jobs list is reachable.
     */
    private void navigateAndLogin(ClientConfig client) {
        driver.get(client.getUrl());
        waitForPageLoad();  // BaseClass page-load wait; replaces undefined waitForPageReady()

        if (loginPage.isLoginPageVisible()) {
            loginPage.login(
                prop.getProperty("username"),
                prop.getProperty("password")
            );

            Assert.assertTrue(
                loginPage.isLoginSuccessful(),
                "Login should succeed for client: " + client.getName()
            );
        }
        adminJobsPage.navigateToAdminJobs();

        Assert.assertTrue(
            adminJobsPage.isJobsPageLoaded(),
            "Admin Jobs page should load for client: " + client.getName()
        );
    }

    private String resolveClientNameFromUrl(String url) {
        return resolveClientNameFromUrl(url, null);
    }

    private String resolveClientNameFromUrl(String url, String fallbackName) {
        if (url == null || url.trim().isEmpty()) {
            return fallbackName == null || fallbackName.trim().isEmpty() ? config.getClientName() : fallbackName.trim();
        }

        String host = url.replaceFirst("https?://", "");
        int slash = host.indexOf('/');
        if (slash >= 0) {
            host = host.substring(0, slash);
        }
        int hash = host.indexOf('#');
        if (hash >= 0) {
            host = host.substring(0, hash);
        }

        String[] parts = host.split("\\.");
        for (int i = parts.length - 2; i >= 0; i--) {
            String part = parts[i].trim();
            if (part.isEmpty()) {
                continue;
            }
            if (part.equalsIgnoreCase("www")
                    || part.equalsIgnoreCase("uat")
                    || part.equalsIgnoreCase("qa")
                    || part.equalsIgnoreCase("dev")
                    || part.equalsIgnoreCase("test")
                    || part.equalsIgnoreCase("demo")
                    || part.equalsIgnoreCase("preprod")
                    || part.equalsIgnoreCase("prod")
                    || part.equalsIgnoreCase("encore")
                    || part.equalsIgnoreCase("crsp")) {
                continue;
            }
            return part.substring(0, 1).toUpperCase() + part.substring(1);
        }

        if (fallbackName != null && !fallbackName.trim().isEmpty()) {
            return fallbackName.trim();
        }
        return config.getClientName();
    }

    /**
     * Attempts logout; swallows any failure so that multi-client iteration
     * can continue even if the logout flow is broken.
     */
    private void safeLogout() {
        try {
            new LogoutPage(driver).logout();
        } catch (Exception ignored) {
            // Non-fatal — continue to next client
        }
    }

    /**
     * Extracts a normalised hostname fragment from a URL for same-site detection.
     * E.g. "https://app.client.com/#/home" → "app.client.com"
     */
    private String extractHost(String url) {
        if (url == null || url.isBlank()) return "";
        return url.replaceAll("https?://", "")
                  .replaceAll("[/#?].*", "")
                  .toLowerCase();
    }

    /**
     * Resolves the Google Chat webhook URL from (in priority order):
     *   1. System property  googleChatWebhookUrl
     *   2. System property  google.chat.webhook.url
     *   3. config.properties key  googleChatWebhookUrl
     *   4. config.properties key  google.chat.webhook.url
     */
    private String resolveGoogleChatWebhookUrl() {
        String value = System.getProperty("googleChatWebhookUrl", "").trim();
        if (!value.isBlank()) return value;

        value = System.getProperty("google.chat.webhook.url", "").trim();
        if (!value.isBlank()) return value;

        value = prop.getProperty("googleChatWebhookUrl", "").trim();
        if (!value.isBlank()) return value;

        return prop.getProperty("google.chat.webhook.url", "").trim();
    }

    private void sendEmailReportIfConfigured(String pdfPath) {
        try {
            String smtpHost = prop.getProperty("smtp.host", "").trim();
            String smtpPort = prop.getProperty("smtp.port", "").trim();
            String smtpUser = prop.getProperty("smtp.username", "").trim();
            String smtpPassword = resolveSmtpPassword();
            String fromEmail = prop.getProperty("mail.from", "").trim();
            String toEmail = prop.getProperty("mail.to", "").trim();

            if (smtpHost.isBlank() || smtpPort.isBlank() || smtpUser.isBlank()
                    || smtpPassword.isBlank() || fromEmail.isBlank() || toEmail.isBlank()) {
                recordVerification("Email report skipped: SMTP/mail config is incomplete.");
                return;
            }

            EmailSender.sendEmailWithAttachment(
                smtpHost, smtpPort, smtpUser, smtpPassword, fromEmail, toEmail, pdfPath);
            recordVerification("Email report sent successfully with PDF attachment.");
        } catch (Exception e) {
            recordVerification("Email report send failed: " + e.getMessage());
        }
    }
    private void sendChatReportFileMessageIfConfigured(String pdfPath) {
        try {
            String message = "Daily Job Monitoring Reports Generated\n"
                + "Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy"))
                + "\nPDF: Daily Job Status Report";

            if (sendPdfToGoogleChatByBotIfConfigured(message, pdfPath)) {
                recordVerification("Google Chat bot report message sent with PDF attachment.");
                return;
            }

            String webhookUrl = resolveGoogleChatWebhookUrl();
            if (webhookUrl.isBlank()) {
                recordVerification("Google Chat report message skipped: bot/webhook config unavailable.");
                return;
            }
            String pdfReference = buildShareablePdfReference(pdfPath);
            String fallbackMessage = "Daily Job Monitoring Reports Generated\n"
                + "PDF: " + pdfReference;
            GoogleChatWebhookSender.sendSummary(webhookUrl, fallbackMessage);
            recordVerification("Google Chat webhook report file message sent.");
        } catch (Exception e) {
            recordVerification("Google Chat report file message failed: " + e.getMessage());
        }
    }

    private boolean sendPdfToGoogleChatByBotIfConfigured(String message, String pdfPath) {
        String botSpace = resolveGoogleChatBotSpace();
        String botCredentialsPath = resolveGoogleChatBotCredentialsPath();
        if (botSpace.isBlank() || botCredentialsPath.isBlank()) {
            return false;
        }

        try {
            GoogleChatBotSender.sendMessageWithAttachment(
                botSpace,
                message,
                pdfPath,
                botCredentialsPath);
            return true;
        } catch (Exception e) {
            recordVerification("Google Chat bot send failed, switching to webhook fallback: " + e.getMessage());
            return false;
        }
    }

    private String buildShareablePdfReference(String pdfPath) {
        String credentialsPath = prop.getProperty("google.drive.credentials.path", "").trim();
        String folderId = prop.getProperty("google.drive.folder.id", "").trim();
        if (credentialsPath.isBlank()) {
            return pdfPath;
        }

        try {
            return GoogleDriveUploader.uploadFileAndGetShareableUrl(pdfPath, folderId, credentialsPath);
        } catch (Exception e) {
            return pdfPath;
        }
    }

    private String resolveSmtpPassword() {
        String value = prop.getProperty("smtp.password", "").trim();
        if (!value.isBlank()) {
            return value;
        }
        value = prop.getProperty("smtp.appPassword", "").trim();
        if (!value.isBlank()) {
            return value;
        }
        return prop.getProperty("mail.password", "").trim();
    }

    private String resolveGoogleChatBotSpace() {
        String value = System.getProperty("google.chat.bot.space", "").trim();
        if (!value.isBlank()) {
            return value;
        }
        value = prop.getProperty("google.chat.bot.space", "").trim();
        if (!value.isBlank()) {
            return value;
        }
        return prop.getProperty("google.chat.space", "").trim();
    }

    private String resolveGoogleChatBotCredentialsPath() {
        String value = System.getProperty("google.chat.bot.credentials.path", "").trim();
        if (!value.isBlank()) {
            return value;
        }
        return prop.getProperty("google.chat.bot.credentials.path", "").trim();
    }

    /**
     * Inserts placeholder {@link JobStatus} records for all monitored jobs when
     * a client-level exception prevents real data from being captured.
     */
    private void addFailedPlaceholders(
            List<JobStatus> target,
            String clientName,
            String errorMessage) {

        String today = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        for (String jobName : AdminJobsPage.MONITORED_JOB_NAMES) {
            JobStatus placeholder = new JobStatus();
            placeholder.setClientName(clientName);
            placeholder.setJobName(jobName);
            placeholder.setJobStatus("FAILED");
            placeholder.setReceiptStatus("N/A");
            placeholder.setReportStatus("FAILED");
            placeholder.setStatus("FAILED");
            placeholder.setCurrentStatus("FAILED");
            placeholder.setErrorMessage("Automation Error: " + errorMessage);
            placeholder.setStartTime(today);
            target.add(placeholder);
        }
    }
    }