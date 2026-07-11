package com.encorepay;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.*;

import com.encorepay.base.BaseClass;
import com.encorepay.pages.*;

public class DepositSlipsTest extends BaseClass {

    private LoginPage        loginPage;
    private CollectionsPage  collectionsPage;
    private DepositSlipsPage depositSlipsPage;
    private SubmitDepositSlipPage submitDepositSlipPage;
    private BatchPage batchPage;

    private String toastMessage = "";
    private String successMessage = "";

    @BeforeClass(alwaysRun = true)
    public void setupPages() {
        loginPage = new LoginPage(driver);
        collectionsPage = new CollectionsPage(driver);
        depositSlipsPage = new DepositSlipsPage(driver);
        submitDepositSlipPage = new SubmitDepositSlipPage(driver);
        batchPage = new BatchPage(driver);
    }

    @AfterMethod(alwaysRun = true)
    public void logResult(ITestResult result) {
        super.logTestResult(result);
    }

    @Test(priority = 1, description = "Login with valid credentials")
    public void tc01_Login() {
        recordTestData("User: " + prop.getProperty("username"));
        loginPage.login(prop.getProperty("username"), prop.getProperty("password"));
        Assert.assertTrue(loginPage.isLoginSuccessful(), "Login should succeed with configured credentials.");
        recordVerification("Login verified.");
    }

    @Test(priority = 2, dependsOnMethods = "tc01_Login",
          description = "Navigate to Collections → Create Deposit Slip")
    public void tc02_NavigateToCreateDepositSlip() {
        collectionsPage.openDepositSlipCreation();
        depositSlipsPage.waitForPage();
        recordVerification("Create Deposit Slip page loaded.");
    }

    @Test(priority = 3, dependsOnMethods = "tc02_NavigateToCreateDepositSlip",
          description = "Select CASH instrument radio button")
    public void tc03_SelectCashRadio() {
        depositSlipsPage.selectCash();
        Assert.assertTrue(depositSlipsPage.hasSelectableReceipts(),
            "After selecting CASH, at least one receipt row should appear.");
        recordVerification("CASH radio selected; receipt table populated.");
    }

    @Test(priority = 4, dependsOnMethods = "tc03_SelectCashRadio",
          description = "Tick checkbox on the first READY_TO_DEPOSIT receipt")
    public void tc04_SelectFirstReceipt() {
        if (!depositSlipsPage.hasSelectableReceipts())
            throw new SkipException("No CASH receipt rows available in this environment.");
        depositSlipsPage.selectFirstReceipt();
        recordVerification("First receipt row selected.");
    }

    @Test(priority = 5, dependsOnMethods = "tc04_SelectFirstReceipt",
          description = "Click Add To Dep Slip and confirm side panel opens")
    public void tc05_ClickAddToDepositSlip() {
        depositSlipsPage.clickAddToDepositSlip();
        recordVerification("Side panel opened with bank account selector.");
    }

    @Test(priority = 6, dependsOnMethods = "tc05_ClickAddToDepositSlip",
          description = "Select bank account using depositSlipBank from config")
    public void tc06_SelectBankAccount() {
        String bankName = prop.getProperty("depositSlipBank", "").trim();
        depositSlipsPage.selectBank(bankName);
        recordVerification("Bank selected: " + (bankName.isBlank() ? "(first available)" : bankName));
    }

    @Test(priority = 7, dependsOnMethods = "tc06_SelectBankAccount",
          description = "Enter deposit slip name using depositSlipNumberPrefix from config")
    public void tc07_EnterDepositSlipName() {
        String slipName = prop.getProperty("depositSlipNumberPrefix", "DEP").trim();
        depositSlipsPage.enterDepositSlipName(slipName);
        recordVerification("Deposit Slip Name entered: " + slipName);
    }

    @Test(priority = 8, dependsOnMethods = "tc07_EnterDepositSlipName",
          description = "Enter Ref Transaction ID using depositSlipRefPrefix from config")
    public void tc08_EnterRefTransactionId() {
        String refId = prop.getProperty("depositSlipRefPrefix", "").trim();
        depositSlipsPage.enterRefTransactionId(refId);
        recordVerification("Ref Transaction ID entered: " + (refId.isBlank() ? "(skipped)" : refId));
    }

    @Test(priority = 9, dependsOnMethods = "tc08_EnterRefTransactionId",
          description = "Click Update and verify deposit slip is created successfully")
    public void tc09_ClickUpdateAndVerify() {
        depositSlipsPage.clickUpdate();

        toastMessage = depositSlipsPage.getToastMessage();
        System.out.println("[INFO] Toast after Update: " + toastMessage);

        boolean success = !toastMessage.isBlank()
            && !toastMessage.toLowerCase().contains("error")
            && !toastMessage.toLowerCase().contains("failed");

        Assert.assertTrue(success,
            "Expected success confirmation after Update, but got: '" + toastMessage + "'");
        recordVerification("Deposit slip created. Message: " + toastMessage);
    }

    @Test(priority = 10, dependsOnMethods = "tc09_ClickUpdateAndVerify",
          description = "Navigate to Submit Deposit Slip page")
    public void tc10_NavigateToSubmitDepositSlip() {
        collectionsPage.openDepositSlipSubmission();
        submitDepositSlipPage.waitForPage();
        recordVerification("Submit Deposit Slip page loaded.");
    }

    @Test(priority = 11, dependsOnMethods = "tc10_NavigateToSubmitDepositSlip",
          description = "Submit the created deposit slip")
    public void tc11_SubmitDepositSlip() {
        if (!submitDepositSlipPage.hasCreatedDepositSlipReadyForSubmission()) {
            throw new SkipException("No deposit slip ready for submission in this environment.");
        }
        submitDepositSlipPage.submitCreatedDepositSlip("");
        recordVerification("Deposit slip submitted successfully.");
    }

    @Test(priority = 12, dependsOnMethods = "tc11_SubmitDepositSlip",
          description = "Navigate to Batch Acknowledgement page")
    public void tc12_NavigateToBatchAcknowledgement() {
        collectionsPage.openBatchAcknowledgement();
        batchPage.waitForPage();
        recordVerification("Batch Acknowledgement page loaded.");
    }

    @Test(priority = 13, dependsOnMethods = "tc12_NavigateToBatchAcknowledgement",
          description = "Click Show Filter button")
    public void tc13_ClickShowFilter() {
        batchPage.clickShowFilter();
        recordVerification("'Show Filter' button clicked.");
    }

    @Test(priority = 14, dependsOnMethods = "tc13_ClickShowFilter",
          description = "Click Search button")
    public void tc14_ClickSearch() {
        batchPage.clickSearch();
        recordVerification("'Search' button clicked.");
    }

    @Test(priority = 15, dependsOnMethods = "tc14_ClickSearch",
          description = "Click first Acknowledge button in table")
    public void tc15_ClickFirstAcknowledge() {
        if (!batchPage.hasSubmittedBatchReadyForAcknowledgement()) {
            throw new SkipException("No batch ready for acknowledgement in this environment.");
        }
        batchPage.clickFirstAcknowledgeButton();
        recordVerification("First Acknowledge button clicked.");
    }

    @Test(priority = 16, dependsOnMethods = "tc15_ClickFirstAcknowledge",
          description = "Scroll to Cash Received checkbox in side panel")
    public void tc16_ScrollToCashReceived() {
        batchPage.scrollToCashReceivedCheckbox();
        recordVerification("Scrolled to Cash Received checkbox.");
    }

    @Test(priority = 17, dependsOnMethods = "tc16_ScrollToCashReceived",
          description = "Select Cash Received checkbox")
    public void tc17_TickCashReceived() {
        batchPage.tickCashReceivedCheckboxInDrawer();
        recordVerification("'Cash Received' checkbox selected.");
    }

    @Test(priority = 18, dependsOnMethods = "tc17_TickCashReceived",
          description = "Scroll to top of side panel")
    public void tc18_ScrollToTop() {
        batchPage.scrollToTopOfSidePanel();
        recordVerification("Scrolled to top of side panel.");
    }

    @Test(priority = 19, dependsOnMethods = "tc18_ScrollToTop",
          description = "Click Proceed with Acknowledge button")
    public void tc19_ClickProceed() {
        batchPage.clickProceedWithAcknowledge();
        recordVerification("'Proceed with Acknowledge' button clicked.");
    }

    @Test(priority = 20, dependsOnMethods = "tc19_ClickProceed",
          description = "Click OK in confirmation dialog")
    public void tc20_ClickOk() {
        batchPage.clickOkInConfirmation();
        recordVerification("'OK' button clicked in confirmation dialog.");
    }

    @Test(priority = 21, dependsOnMethods = "tc20_ClickOk",
          description = "Capture and validate success message")
    public void tc21_ValidateSuccessMessage() {
        successMessage = batchPage.captureAndValidateSuccessMessage();
        Assert.assertEquals(successMessage, "Batch Acknowledged Successfully",
            "Success message should match expected text.");
        recordVerification("Success message validated: " + successMessage);
    }
}
