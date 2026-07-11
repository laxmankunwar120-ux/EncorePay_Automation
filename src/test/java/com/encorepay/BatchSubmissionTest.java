package com.encorepay;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.encorepay.base.BaseClass;
import com.encorepay.pages.BatchPage;
import com.encorepay.pages.DepositSlipsPage;
import com.encorepay.pages.LoginPage;
import com.encorepay.pages.LogoutPage;
import com.encorepay.pages.SubmitDepositSlipPage;

public class BatchSubmissionTest extends BaseClass {

    private LoginPage loginPage;
    private LogoutPage logoutPage;
    private BatchPage batchPage;
    private DepositSlipsPage depositSlipsPage;
    private SubmitDepositSlipPage submitDepositSlipPage;
    private String submittedDepositSlipMessage = "";
    private String acknowledgedBatchNumber = "";

    @BeforeClass(alwaysRun = true)
    public void setupPages() {
        loginPage = new LoginPage(driver);
        logoutPage = new LogoutPage(driver);
        batchPage = new BatchPage(driver);
        depositSlipsPage = new DepositSlipsPage(driver);
        submitDepositSlipPage = new SubmitDepositSlipPage(driver);
    }

    @AfterMethod(alwaysRun = true)
    public void logResult(ITestResult result) {
        super.logTestResult(result);
    }

    @Test(priority = 1, description = "Login before batch submission workflow")
    public void tc01_Login() {
        recordTestData("Credential set: configured valid QA user");
        loginPage.login(prop.getProperty("username"), prop.getProperty("password"));
        Assert.assertTrue(loginPage.isLoginSuccessful(), "Login should succeed before batch submission.");
        recordVerification("Batch submission user login verified.");
    }

    @Test(priority = 2, dependsOnMethods = "tc01_Login",
        description = "Ensure a created deposit slip is available for submission")
    public void tc02_EnsureDepositSlipReadyForSubmission() {
        batchPage.openDepositSlipSubmission();
        submitDepositSlipPage.waitForPage();

        if (submitDepositSlipPage.hasCreatedDepositSlipReadyForSubmission()) {
            recordVerification("Created deposit slip is already available for submission.");
            return;
        }

        batchPage.openDepositSlipCreation();
        depositSlipsPage.waitForPage();

        if (!depositSlipsPage.hasSelectableReceipts()) {
            throw new SkipException("No receipts available to create a deposit slip for batch submission.");
        }

        String slipName = prop.getProperty("depositSlipNumberPrefix", "DEP").trim();
        String refId = prop.getProperty("depositSlipRefPrefix", "DEPREF").trim();
        String bankName = prop.getProperty("depositSlipBank", "").trim();
        String createMessage = depositSlipsPage.createCashDepositSlip(bankName, slipName, refId);
        recordVerification("Deposit slip created for batch submission. Message: " + createMessage);

        batchPage.openDepositSlipSubmission();
        submitDepositSlipPage.waitForPage();
        Assert.assertTrue(submitDepositSlipPage.hasCreatedDepositSlipReadyForSubmission(),
            "A created deposit slip should be available after creating one.");
    }

    @Test(priority = 3, dependsOnMethods = "tc02_EnsureDepositSlipReadyForSubmission",
        description = "Submit the first created deposit slip")
    public void tc03_SubmitDepositSlip() {
        submittedDepositSlipMessage = submitDepositSlipPage.submitCreatedDepositSlip("");
        recordVerification("Deposit slip submitted successfully. Message: " + submittedDepositSlipMessage);
    }

    @Test(priority = 4, dependsOnMethods = "tc03_SubmitDepositSlip",
        description = "Acknowledge the submitted batch")
    public void tc04_AcknowledgeSubmittedBatch() {
        batchPage.openBatchAcknowledgement();
        batchPage.waitForPage();

        if (!batchPage.hasSubmittedBatchReadyForAcknowledgement()) {
            throw new SkipException("No submitted batch is available for acknowledgement.");
        }

        acknowledgedBatchNumber = batchPage.acknowledgeFirstSubmittedBatch();
        recordVerification("Submitted batch acknowledged successfully: " + acknowledgedBatchNumber);
    }

    @Test(priority = 5, dependsOnMethods = "tc01_Login", alwaysRun = true,
        description = "Logout after batch submission workflow")
    public void tc05_Logout() {
        logoutPage.logout();
        Assert.assertTrue(logoutPage.isLogoutSuccessful(), "Logout should return to sign-in page.");
        recordVerification("Logout completed after batch submission workflow.");
    }
}
