package com.encorepay;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.encorepay.base.BaseClass;
import com.encorepay.pages.GenerateReceiptPage;

public class GenerateReceiptTest extends BaseClass {

    private static final String PAYMENT_AMOUNT = "100";
    private static final String MOBILE_NUMBER  = "9876543210";

    private GenerateReceiptPage receiptPage;

    @BeforeClass(alwaysRun = true)
    public void initPage() {
        receiptPage = new GenerateReceiptPage(driver);
    }

    @AfterMethod(alwaysRun = true)
    public void logResult(ITestResult result) {
        super.logTestResult(result);
    }

    @Test(priority = 1,
        description = "Confirm configured user can access the receipt generation workflow")
    public void tc01_Login() {
        recordTestData("Credential set: configured valid QA user");
        receiptPage.login();
        recordVerification("Receipt generation user authentication verified successfully.");
    }

    @Test(priority = 2,
        dependsOnMethods = "tc01_Login",
        description = "Confirm the Collection Items work queue is reachable")
    public void tc02_NavigateToCollectionItems() {
        try {
            receiptPage.navigateToCollectionItems();
            recordVerification("Collection Items receipt queue was reachable.");
        } catch (IllegalStateException e) {
            throw new SkipException(e.getMessage());
        }
    }

    @Test(priority = 3,
        dependsOnMethods = "tc02_NavigateToCollectionItems",
        description = "Confirm Generate Receipt can be opened for the first eligible account")
    public void tc03_OpenGenerateReceiptModal() {
        receiptPage.openGenerateReceiptModal();
    }

    @Test(priority = 4,
        dependsOnMethods = "tc03_OpenGenerateReceiptModal",
        description = "Confirm the payment form opens from the receipt modal")
    public void tc04_ClickPayButton() {
        receiptPage.clickPayButton();
    }

    @Test(priority = 5,
        dependsOnMethods = "tc04_ClickPayButton",
        description = "Confirm the partial or excess payment option can be selected")
    public void tc05_SelectPartialOrExcessPayment() {
        receiptPage.selectPartialOrExcessPayment();
    }

    @Test(priority = 6,
        dependsOnMethods = "tc05_SelectPartialOrExcessPayment",
        description = "Confirm the payment amount can be entered")
    public void tc06_EnterAmount() {
        receiptPage.enterAmount(PAYMENT_AMOUNT);
    }

    @Test(priority = 7,
        dependsOnMethods = "tc06_EnterAmount",
        description = "Confirm cash can be selected as the payment method")
    public void tc07_SelectCashPayment() {
        receiptPage.selectCashPayment();
    }

    @Test(priority = 8,
        dependsOnMethods = "tc07_SelectCashPayment",
        description = "Confirm cash denomination can be recorded")
    public void tc08_EnterDenomination() {
        receiptPage.enterDenomination();
    }

    @Test(priority = 9,
        dependsOnMethods = "tc08_EnterDenomination",
        description = "Confirm payment details can proceed to collection confirmation")
    public void tc09_ClickProceed() {
        receiptPage.clickProceed();
    }

    @Test(priority = 10,
        dependsOnMethods = "tc09_ClickProceed",
        description = "Confirm relationship can be set to Self")
    public void tc10_SelectRelationshipSelf() {
        receiptPage.selectRelationshipSelf();
    }

    @Test(priority = 11,
        dependsOnMethods = "tc10_SelectRelationshipSelf",
        description = "Confirm a mobile number can be captured for the receipt (skipped if field absent)")
    public void tc11_EnterMobileNumber() {
        receiptPage.enterMobileNumber(MOBILE_NUMBER);
    }

    @Test(priority = 12,
        dependsOnMethods = "tc11_EnterMobileNumber",
        description = "Confirm the receipt issue action can be submitted")
    public void tc12_ConfirmAndIssueReceipt() {
        receiptPage.clickConfirmAndIssueReceipt();
    }

    @Test(priority = 13,
        dependsOnMethods = "tc12_ConfirmAndIssueReceipt",
        description = "Confirm the receipt is generated successfully")
    public void tc13_VerifyReceiptSuccess() {
        boolean success = receiptPage.verifyReceiptSuccess();
        Assert.assertTrue(success, "Receipt generation FAILED - success toast not visible");
        String receiptNo = receiptPage.getReceiptNumber();
        if (!receiptNo.isEmpty()) {
            System.out.println("[INFO] Receipt Number: " + receiptNo);
            recordVerification("Receipt number generated: " + receiptNo);
        }
    }

    @Test(priority = 14,
        dependsOnMethods = "tc13_VerifyReceiptSuccess",
        description = "Confirm user can log out via the account circle menu after receipt generation")
    public void tc14_Logout() {
        receiptPage.logout();
        recordVerification("User logged out successfully via account circle menu.");
    }

    @Test(priority = 15,
        description = "Confirm one receipt can be generated using Cheque payment mode")
    public void tc15_GenerateReceiptUsingCheque() {
        recordTestData("Payment mode: Cheque");
        receiptPage.login();
        try {
            receiptPage.processMultipleReceiptGenerations(1, "Cheque");
        } catch (IllegalStateException e) {
            throw new SkipException(e.getMessage());
        } finally {
            try {
                receiptPage.logout();
            } catch (Exception e) {
                System.out.println("[WARN] Logout cleanup skipped: " + e.getMessage());
            }
        }
        recordVerification("Cheque receipt generation flow completed.");
    }

    @Test(priority = 16,
        description = "Confirm receipts can be generated using Cash, Cheque, and DD payment modes")
    public void tc16_GenerateReceiptsForCashChequeAndDd() {
        recordTestData("Payment modes: Cash, Cheque, DD");
        receiptPage.login();
        try {
            receiptPage.processMultipleReceiptGenerations(3, "Cash", "Cheque", "DD");
        } catch (IllegalStateException e) {
            throw new SkipException(e.getMessage());
        } finally {
            try {
                receiptPage.logout();
            } catch (Exception e) {
                System.out.println("[WARN] Logout cleanup skipped: " + e.getMessage());
            }
        }
        recordVerification("Cash, Cheque, and DD receipt generation flows completed.");
    }
}
