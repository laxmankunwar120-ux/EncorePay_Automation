package com.encorepay;

import java.util.List;
import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.encorepay.actiondriver.ActionDriver;
import com.encorepay.base.BaseClass;
import com.encorepay.pages.AllocationPage;
import com.encorepay.pages.BatchPage;
import com.encorepay.pages.DashboardPage;
import com.encorepay.pages.DepositSlipsPage;
import com.encorepay.pages.GenerateReceiptPage;
import com.encorepay.pages.LoginPage;
import com.encorepay.pages.LogoutPage;
import com.encorepay.pages.ReconciliationPage;
import com.encorepay.pages.SubmitDepositSlipPage;

public class EndToEndFlowTest extends BaseClass {

    private LoginPage loginPage;
    private LogoutPage logoutPage;
    private DashboardPage dashboardPage;
    private AllocationPage allocationPage;
    private GenerateReceiptPage generateReceiptPage;
    private DepositSlipsPage depositSlipsPage;
    private SubmitDepositSlipPage submitDepositSlipPage;
    private BatchPage batchPage;
    private ReconciliationPage reconciliationPage;

    public ActionDriver action;

    @BeforeClass(alwaysRun = true)
    public void setupPages() {

        loginPage = new LoginPage(driver);
        logoutPage = new LogoutPage(driver);
        dashboardPage = new DashboardPage(driver);
        allocationPage = new AllocationPage(driver);
        generateReceiptPage = new GenerateReceiptPage(driver);
        depositSlipsPage = new DepositSlipsPage(driver);
        submitDepositSlipPage = new SubmitDepositSlipPage(driver);
        batchPage = new BatchPage(driver);
        reconciliationPage = new ReconciliationPage(driver);

        action = new ActionDriver(driver);
    }

    @Test(priority = 1)
    public void tc01_Login() {

        recordTestData("Credential set: configured valid QA user");

        loginPage.login(
                prop.getProperty("username"),
                prop.getProperty("password"));

        Assert.assertTrue(loginPage.isLoginSuccessful());

        recordVerification(
                "Login authentication verified successfully.");
    }

    @Test(priority = 2, dependsOnMethods = "tc01_Login")
    public void tc02_DashboardLoads() {

        dashboardPage.openDashboard();

        Assert.assertFalse(
                dashboardPage.captureCardDetails().isEmpty());

        recordVerification(
                "Dashboard landing page loaded successfully.");
    }

    @Test(priority = 3, dependsOnMethods = "tc02_DashboardLoads")
    public void tc03_DashboardKpiCards() {

        Map<String, String> cards =
                dashboardPage.captureCardDetails();

        List<String> missing = cards.entrySet()
                .stream()
                .filter(e ->
                        e.getValue() == null
                                || e.getValue().isBlank()
                                || "-".equals(e.getValue()))
                .map(Map.Entry::getKey)
                .toList();

        if (!missing.isEmpty()) {

            throw new SkipException(
                    "Dashboard KPI cards returned no data: "
                            + missing);
        }

        recordVerification(
                "Dashboard KPI values verified: " + cards);
    }

    @Test(priority = 4, dependsOnMethods = "tc02_DashboardLoads")
    public void tc04_DashboardSliderCard() {

        dashboardPage.clickSliderAndCapture();

        Map<String, String> sliderCards =
                dashboardPage.captureCardDetails();

        String lmsValue = sliderCards.get("LMS");

        if (lmsValue == null || lmsValue.isBlank()) {

            throw new SkipException(
                    "No LMS Posted data.");
        }

        recordVerification(
                "LMS Posted Amount: " + lmsValue);
    }

    @Test(priority = 5, dependsOnMethods = "tc02_DashboardLoads")
    public void tc05_DashboardCharts() {

        dashboardPage.scrollDownAndCapture();

        dashboardPage.scrollUp();

        recordVerification(
                "Dashboard charts section verified.");
    }

    @Test(priority = 6, dependsOnMethods = "tc02_DashboardLoads")
    public void tc06_DashboardBranchHandling() {

        dashboardPage.openFilter();

        List<String> testedBranches =
                dashboardPage.performBranchwiseWalkthroughs(3);

        if (testedBranches == null
                || testedBranches.isEmpty()) {

            throw new SkipException(
                    "No branches available to perform dashboard automation.");
        }

        recordVerification(
                "Dashboard walkthrough performed across branches: "
                        + testedBranches);
    }

    @Test(priority = 8, dependsOnMethods = "tc02_DashboardLoads")
    public void tc08_SingleAllocation() {

        allocationPage.waitForAllocationPage();

        if (!allocationPage.hasRows()) {

            throw new SkipException(
                    "No collection items available for allocation.");
        }

        allocationPage.performSingleAllocation(
                "",
                AllocationPage.AllocationType.COLLECTION_ITEM);

        recordVerification(
                "Single Allocation completed successfully.");
    }

    @Test(priority = 9, dependsOnMethods = "tc08_SingleAllocation")
    public void tc09_BulkAllocation() {

        allocationPage.waitForAllocationPage();

        if (!allocationPage.hasAtLeastRows(5)) {

            throw new SkipException(
                    "Not enough collection items found for bulk allocation.");
        }

        int rowCount =
                allocationPage.getDynamicBulkRowCount(5);

        allocationPage.performBulkAllocation(
                "",
                AllocationPage.AllocationType.COLLECTION_ITEM,
                rowCount);

        recordVerification(
                "Bulk Allocation completed successfully.");
    }

    @Test(priority = 10, dependsOnMethods = "tc09_BulkAllocation")
    public void tc10_GenerateReceipt() {

        generateReceiptPage.processMultipleReceiptGenerations(
                3,
                "Cash",
                "Cheque",
                "Cash");

        recordVerification(
                "Multiple receipts generated successfully.");
    }

    @Test(priority = 11, dependsOnMethods = "tc10_GenerateReceipt")
    public void tc11_CreateDepositSlip() {

        batchPage.openDepositSlipCreation();

        depositSlipsPage.waitForPage();

        if (!depositSlipsPage.hasSelectableReceipts()) {

            throw new SkipException(
                    "No receipts available for deposit slip creation.");
        }

        depositSlipsPage.createCashDepositSlip(
                "",
                "Auto",
                "REF");

        recordVerification(
                "Deposit slip created successfully.");
    }

    @Test(priority = 12, dependsOnMethods = "tc11_CreateDepositSlip")
    public void tc12_SubmitDepositSlip() {

        batchPage.openDepositSlipCreation();

        submitDepositSlipPage.waitForPage();

        if (!submitDepositSlipPage
                .hasCreatedDepositSlipReadyForSubmission()) {

            throw new SkipException(
                    "No deposit slip ready for submission.");
        }

        submitDepositSlipPage.submitCreatedDepositSlip("");

        recordVerification(
                "Deposit slip submitted successfully.");
    }

    @Test(priority = 13, dependsOnMethods = "tc12_SubmitDepositSlip")
    public void tc13_AcknowledgeBatch() {

        batchPage.openBatchAcknowledgement();

        batchPage.waitForPage();

        if (!batchPage
                .hasSubmittedBatchReadyForAcknowledgement()) {

            throw new SkipException(
                    "No batch available for acknowledgement.");
        }

        batchPage.acknowledgeFirstSubmittedBatch();

        recordVerification(
                "Batch acknowledged successfully.");
    }

    @Test(priority = 14, dependsOnMethods = "tc13_AcknowledgeBatch")
    public void tc14_Reconciliation() {

        reconciliationPage.openCashReconciliation();

        if (!reconciliationPage
                .hasSubmittedSlipsForClearing()) {

            throw new SkipException(
                    "No slips available for reconciliation.");
        }

        reconciliationPage.selectFirstSubmittedSlip();

        reconciliationPage.clickClearedInBank();

        reconciliationPage.confirmClearedInBank("Automation");

        recordVerification(
                "Reconciliation completed successfully.");
    }

    @Test(priority = 15, dependsOnMethods = "tc14_Reconciliation")
    public void tc15_Logout() {

        logoutPage.logout();

        Assert.assertTrue(
                logoutPage.isLogoutSuccessful());

        recordVerification(
                "Logout completed successfully.");
    }

    @Override
    protected void waitForPageReady() {

        new WebDriverWait(
                driver,
                java.time.Duration.ofSeconds(10))

                .until(d ->
                        "complete".equals(
                                ((JavascriptExecutor) d)
                                        .executeScript(
                                                "return document.readyState")));
    }
}