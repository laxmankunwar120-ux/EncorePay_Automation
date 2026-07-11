package com.encorepay;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.*;

import com.encorepay.base.BaseClass;
import com.encorepay.pages.CollectionsPage;
import com.encorepay.pages.LoginPage;
import com.encorepay.pages.BatchPage;
import com.encorepay.pages.LogoutPage;

public class BatchAcknowledgementTest extends BaseClass {

    private LoginPage loginPage;
    private CollectionsPage collectionsPage;
    private BatchPage batchPage;
    private LogoutPage logoutPage;

    @BeforeClass(alwaysRun = true)
    public void setupPages() {
        loginPage = new LoginPage(driver);
        collectionsPage = new CollectionsPage(driver);
        batchPage = new BatchPage(driver);
        logoutPage = new LogoutPage(driver);
    }

    @Test(priority = 1, description = "Login with valid credentials")
    public void tc01_Login() {
        recordTestData("User: " + prop.getProperty("username"));
        loginPage.login(prop.getProperty("username"), prop.getProperty("password"));
        Assert.assertTrue(loginPage.isLoginSuccessful(), "Login should succeed.");
        recordVerification("Login verified.");
    }

    @Test(priority = 2, dependsOnMethods = "tc01_Login",
          description = "Navigate directly to Acknowledge Batch page")
    public void tc02_NavigateToAcknowledgeBatch() {
        collectionsPage.openBatchAcknowledgement();
        batchPage.waitForPage();
        recordVerification("Acknowledge Batch page loaded.");
    }

    @Test(priority = 3, dependsOnMethods = "tc02_NavigateToAcknowledgeBatch",
          description = "Click Show Filter button")
    public void tc03_ClickShowFilter() {
        batchPage.clickShowFilter();
        recordVerification("'Show Filter' button clicked.");
    }

    @Test(priority = 4, dependsOnMethods = "tc03_ClickShowFilter",
          description = "Click Search button")
    public void tc04_ClickSearch() {
        batchPage.clickClear();
        batchPage.clickSearch();
        recordVerification("'Search' button clicked.");
    }

    @Test(priority = 5, dependsOnMethods = "tc04_ClickSearch",
          description = "Click first Acknowledge button in table")
    public void tc05_ClickFirstAcknowledge() {
        if (!batchPage.hasSubmittedBatchReadyForAcknowledgement()) {
            throw new SkipException("No batch ready for acknowledgement in this environment.");
        }
        batchPage.clickFirstAcknowledgeButton();
        recordVerification("First Acknowledge button clicked.");
    }

    @Test(priority = 6, dependsOnMethods = "tc05_ClickFirstAcknowledge",
          description = "Scroll to Cash Received checkbox in side panel")
    public void tc06_ScrollToCashReceived() {
        batchPage.scrollToCashReceivedCheckbox();
        recordVerification("Scrolled to Cash Received checkbox.");
    }

    @Test(priority = 7, dependsOnMethods = "tc06_ScrollToCashReceived",
          description = "Select Cash Received checkbox")
    public void tc07_TickCashReceived() {
        batchPage.tickCashReceivedCheckboxInDrawer();
        recordVerification("'Cash Received' checkbox selected.");
    }

    @Test(priority = 8, dependsOnMethods = "tc07_TickCashReceived",
          description = "Scroll to top of side panel")
    public void tc08_ScrollToTop() {
        batchPage.scrollToTopOfSidePanel();
        recordVerification("Scrolled to top of side panel.");
    }

    @Test(priority = 9, dependsOnMethods = "tc08_ScrollToTop",
          description = "Click Acknowledge Batch button in side panel")
    public void tc09_ClickAcknowledgeBatchInDrawer() {
        batchPage.clickProceedWithAcknowledge();
        recordVerification("'Acknowledge Batch' button clicked in side panel.");
    }

    @Test(priority = 10, dependsOnMethods = "tc09_ClickAcknowledgeBatchInDrawer",
          description = "Click OK in confirmation dialog")
    public void tc10_ClickOkInConfirmation() {
        batchPage.clickOkInConfirmation();
        recordVerification("'OK' button clicked in confirmation dialog.");
    }

    @Test(priority = 11, dependsOnMethods = "tc10_ClickOkInConfirmation",
          description = "Capture and validate success message")
    public void tc11_ValidateSuccessMessage() {
        String successMessage = batchPage.captureAndValidateSuccessMessage();
        Assert.assertEquals(successMessage, "Batch Acknowledged Successfully",
            "Success message should match expected text.");
        recordVerification("Success message validated: " + successMessage);
        batchPage.waitForToastAndDisappear();
    }

    @Test(priority = 12, dependsOnMethods = "tc11_ValidateSuccessMessage",
          description = "Logout from application")
    public void tc12_Logout() {
        logoutPage.logout();
        recordVerification("User logged out successfully.");
    }
}
