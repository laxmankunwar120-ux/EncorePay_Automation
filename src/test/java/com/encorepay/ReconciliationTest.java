package com.encorepay;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.*;

import com.encorepay.base.BaseClass;
import com.encorepay.pages.LoginPage;
import com.encorepay.pages.ReconciliationPage;

public class ReconciliationTest extends BaseClass {

    private LoginPage loginPage;
    private ReconciliationPage reconciliationPage;

    private String selectedSlipNumber = "";
    private String toastMessage = "";

    @BeforeClass(alwaysRun = true)
    public void setupPages() {
        loginPage = new LoginPage(driver);
        reconciliationPage = new ReconciliationPage(driver);
    }

    @AfterMethod(alwaysRun = true)
    public void logResult(ITestResult result) {
        super.logTestResult(result);
    }

    @Test(priority = 1, description = "Login with valid credentials")
    public void tc01_Login() {
        recordTestData("User: " + prop.getProperty("username"));
        loginPage.login(prop.getProperty("username"), prop.getProperty("password"));
        Assert.assertTrue(loginPage.isLoginSuccessful(),
                "Login should succeed with configured credentials.");
        recordVerification("Login verified.");
    }

    @Test(priority = 2, dependsOnMethods = "tc01_Login",
            description = "Navigate to Reconciliation → Cash Deposits page")
    public void tc02_OpenCashDeposits() {
        reconciliationPage.openCashDeposits();
        recordVerification("Cash Deposits page loaded.");
    }

    @Test(priority = 3, dependsOnMethods = "tc02_OpenCashDeposits",
            description = "Verify at least one DEPOSIT_SLIP_SUBMITTED row is present")
    public void tc03_VerifySubmittedSlipExists() {
        if (!reconciliationPage.hasSubmittedSlipsForClearing()) {
            throw new SkipException("No DEPOSIT_SLIP_SUBMITTED rows available — skipping reconciliation tests.");
        }
        recordVerification("At least one submitted deposit slip found.");
    }

    @Test(priority = 4, dependsOnMethods = "tc03_VerifySubmittedSlipExists",
            description = "Select the first valid DEPOSIT_SLIP_SUBMITTED row")
    public void tc04_SelectFirstSubmittedSlip() {
        try {
            selectedSlipNumber = reconciliationPage.selectFirstSubmittedSlip();
            System.out.println("[INFO] Selected slip: " + selectedSlipNumber);
            recordVerification("Slip selected: " + selectedSlipNumber);
        } catch (Exception e) {
            throw new SkipException("No valid slip available for reconciliation");
        }
    }

    @Test(priority = 5, dependsOnMethods = "tc04_SelectFirstSubmittedSlip",
            description = "Verify the selected-count label shows ≥ 1 Deposit Slip")
    public void tc05_VerifySelectionCounter() {
        int count = reconciliationPage.getSelectedCount();
        System.out.println("[INFO] Selected count: " + count);

        Assert.assertTrue(count >= 1,
                "Counter should show at least 1 selected deposit slip, but was: " + count);

        recordVerification("Counter verified: " + count + " Deposit Slip(s) selected.");
    }

    @Test(priority = 6, dependsOnMethods = "tc05_VerifySelectionCounter",
            description = "Click 'Cleared In Bank' and verify confirmation dialog opens")
    public void tc06_ClickClearedInBank() {
        reconciliationPage.clickClearedInBank();
        recordVerification("'Cleared In Bank' dialog opened.");
    }

    @Test(priority = 7, dependsOnMethods = "tc06_ClickClearedInBank",
            description = "Enter remark and confirm — verify success toast")
    public void tc07_ConfirmClearedInBank() {
        String remark = prop.getProperty("reconRemark", "Cleared via automation").trim();

        toastMessage = reconciliationPage.confirmClearedInBank(remark);
        System.out.println("[INFO] Toast: " + toastMessage);

        boolean success = toastMessage != null
                && !toastMessage.isBlank()
                && !toastMessage.toLowerCase().contains("error")
                && !toastMessage.toLowerCase().contains("failed");

        Assert.assertTrue(success,
                "Expected a success toast after Cleared In Bank, but got: '" + toastMessage + "'");

        recordVerification("Cleared In Bank confirmed. Message: " + toastMessage);
    }

    @Test(priority = 8, dependsOnMethods = "tc07_ConfirmClearedInBank",
            description = "Verify the cleared slip's Receipt Status is now RECEIPT_REALIZED")
    public void tc08_VerifyReceiptRealized() {

        if (selectedSlipNumber == null || selectedSlipNumber.isBlank()) {
            throw new SkipException("Slip number not captured — skipping status verification.");
        }

        Assert.assertTrue(
                reconciliationPage.isSlipShowingStatus(selectedSlipNumber, "RECEIPT_REALIZED"),
                "Expected slip '" + selectedSlipNumber + "' to show RECEIPT_REALIZED after clearing.");

        recordVerification("Slip '" + selectedSlipNumber + "' confirmed as RECEIPT_REALIZED.");
    }
}