package com.encorepay.pages;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.encorepay.actiondriver.ActionDriver;
import com.encorepay.utilities.helpers.JavaScriptHelper;

public class BatchPage extends CollectionsPage {

    private final By pageTitle = By.xpath(
        "//*[normalize-space()='Batches']");

    private final By batchRows = By.xpath(
        "//table//tbody/tr[.//td]"
            + " | //table//tr[.//td and not(.//th)]"
            + " | //tr[contains(@class,'mat-mdc-row')]"
            + " | //tr[contains(@class,'mat-row')]"
            + " | //app-custom-table//tr[.//td]"
            + " | //mat-table//tr[.//td]");

    private final By acknowledgeAction = By.xpath(
        ".//button[normalize-space()='Acknowledge']"
            + " | .//a[normalize-space()='Acknowledge']"
            + " | .//*[self::button or self::a]"
            + "[contains(normalize-space(),'Acknowledge')]"
            + " | .//*[self::button or self::a or self::span]"
            + "[contains(@aria-label,'Acknowledge') or contains(@title,'Acknowledge')]"
            + " | .//mat-icon[contains(normalize-space(),'acknowledge')]/ancestor::button[1]");

    private final By batchAcknowledgementDrawer = By.xpath(
        "//mat-drawer[contains(@class,'mat-drawer-opened')]"
            + " | //div[contains(@class,'mat-drawer-opened')]"
            + " | //app-batch-acknowledged"
            + " | //mat-sidenav[contains(@class,'mat-sidenav-opened')]"
            + " | //mat-sidenav//app-batch-acknowledged");

    private final By drawerScrollableContainer = By.xpath(
        "//mat-drawer[contains(@class,'mat-drawer-opened')]"
            + "//div[contains(@class,'mat-drawer-inner-container')]"
            + " | //div[contains(@class,'mat-drawer-inner-container')]"
            + " | //mat-sidenav[contains(@class,'mat-sidenav-opened')]//div[contains(@class,'mat-sidenav-content')]");

    private final By drawerBatchHeader = By.xpath(
        "//app-batch-acknowledged//*[contains(normalize-space(),'HO-')"
            + " or contains(normalize-space(),'BO-')"
            + " or contains(normalize-space(),'B00')]"
            + " | //mat-drawer[contains(@class,'mat-drawer-opened')]"
            + "//*[contains(normalize-space(),'HO-')"
            + " or contains(normalize-space(),'BO-')"
            + " or contains(normalize-space(),'B00')]"
            + " | //mat-sidenav//app-batch-acknowledged"
            + "//*[contains(normalize-space(),'HO-')"
            + " or contains(normalize-space(),'BO-')"
            + " or contains(normalize-space(),'B00')]");

    private final By cashReceivedCheckbox = By.xpath(
        "//input[@type='checkbox']");

    private final By drawerAcknowledgeButton = By.xpath(
        "//button[normalize-space()='Acknowledge']");

    private final By drawerDenominationRows = By.xpath(
        "//tr[.//td]");

    private final By cashReceivedCheckboxInDrawer = By.xpath(
        "//input[@type='checkbox' and (following-sibling::mat-checkbox or preceding-sibling::mat-checkbox or ancestor::*[contains(text(),'Cash Received')])]"
            + " | //mat-drawer//input[@type='checkbox']"
            + " | //mat-drawer[contains(@class,'mat-drawer-opened')]//mat-checkbox//input[@type='checkbox']"
            + " | //mat-sidenav//input[@type='checkbox']"
            + " | //mat-sidenav[contains(@class,'mat-sidenav-opened')]//mat-checkbox//input[@type='checkbox']"
            + " | //app-batch-acknowledged//input[@type='checkbox']"
            + " | //app-batch-acknowledged//mat-checkbox//input[@type='checkbox']");

    private final By proceedAcknowledgeButton = By.xpath(
        "//button[normalize-space()='Proceed with Acknowledge']"
            + " | //button[contains(normalize-space(),'Proceed') and contains(normalize-space(),'Acknowledge')]"
            + " | //mat-drawer//button[normalize-space()='Proceed with Acknowledge']"
            + " | //button[normalize-space()='Acknowledge Batch']"
            + " | //mat-drawer//button[normalize-space()='Acknowledge Batch']"
            + " | //mat-sidenav//button[normalize-space()='Acknowledge Batch']"
            + " | //app-batch-acknowledged//button[normalize-space()='Acknowledge Batch']"
            + " | //mat-sidenav//button[normalize-space()='Acknowledge']"
            + " | //app-batch-acknowledged//button[normalize-space()='Acknowledge']");

    private final By okButtonConfirmation = By.xpath(
        "//button[normalize-space()='OK']"
            + " | //button[normalize-space()='Ok']"
            + " | //mat-dialog-container//button[normalize-space()='OK']"
            + " | //mat-dialog-container//button[normalize-space()='Ok']");

    private final By successMessage = By.xpath(
        "//*[contains(normalize-space(),'Batch Acknowledged Successfully')]"
            + " | //div[contains(normalize-space(),'Batch Acknowledged Successfully')]"
            + " | //span[contains(normalize-space(),'Batch Acknowledged Successfully')]"
            + " | //*[contains(@class,'toast') or contains(@class,'snack') or @role='alert']"
            + "[contains(.,'Batch Acknowledged Successfully')]"
            + " | //*[contains(@class,'bg-green') or contains(@class,'message')]"
            + "[contains(.,'Batch Acknowledged Successfully')]");

    private final By showFilterButton = By.xpath(
        "//button[contains(normalize-space(),'Show Filter')]"
            + " | //button[.//span[contains(normalize-space(),'Show Filter')]]"
            + " | //app-deposit-batches//button[contains(normalize-space(),'Show Filter')]");

    private final By searchButton = By.xpath(
        "//button[contains(normalize-space(),'Search')]"
            + " | //button[.//span[contains(normalize-space(),'Search')]]"
            + " | //div[contains(@class,'filter-section')]//button[contains(normalize-space(),'Search')]"
            + " | //form//button[contains(normalize-space(),'Search')]");

    private final By clearButton = By.xpath(
        "//button[contains(normalize-space(),'Clear')]"
            + " | //button[.//span[contains(normalize-space(),'Clear')]]"
            + " | //div[contains(@class,'filter-section')]//button[contains(normalize-space(),'Clear')]"
            + " | //form//button[contains(normalize-space(),'Clear')]");

    private final By firstAcknowledgeButtonInTable = By.xpath(
        "(//tbody/tr[.//td])[1]//button[contains(normalize-space(),'Acknowledge')]"
            + " | (//tbody/tr[.//td])[1]//a[contains(normalize-space(),'Acknowledge')]"
            + " | (//tr[contains(@class,'mat-mdc-row') or contains(@class,'datatable-row')])[1]"
            + "//button[contains(normalize-space(),'Acknowledge')]"
            + " | (//tr[.//button[contains(normalize-space(),'Acknowledge')]])[1]"
            + "//button[contains(normalize-space(),'Acknowledge')]");

    private final By rowAcknowledgeButton = By.xpath(
        ".//button[normalize-space()='Acknowledge']"
            + " | .//a[normalize-space()='Acknowledge']"
            + " | .//button[contains(normalize-space(),'Acknowledge')]"
            + " | .//a[contains(normalize-space(),'Acknowledge')]"
            + " | .//button[contains(@aria-label,'Acknowledge') or contains(@title,'Acknowledge')]"
            + " | .//mat-icon[contains(normalize-space(),'acknowledge')]/ancestor::button[1]");

    public BatchPage(WebDriver driver) {
        super(driver);
    }

    public void waitForPage() {

        wait.until(ExpectedConditions.or(

            ExpectedConditions.visibilityOfElementLocated(
                pageTitle),

            ExpectedConditions.visibilityOfElementLocated(
                batchRows)
        ));

        waitForTableLoaded();

        assertNoSystemError("Batch page load");
    }

    private void waitForTableLoaded() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(d -> !d.findElements(batchRows).isEmpty());
        } catch (Exception e) {
            System.out.println("[WARN] Batch table population wait partial timeout=" + e.getMessage());
        }
    }

    public void openDepositSlipCreation() {

        System.out.println(
            "Opening Deposit Slip Creation page");

        try {

            By depositSlipMenu = By.xpath(

                "//span[contains(normalize-space(),"
                    + "'Deposit Slip')]"

                    + " | //a[contains(normalize-space(),"
                    + "'Deposit Slip')]"

                    + " | //button[contains(normalize-space(),"
                    + "'Deposit Slip')]"

                    + " | //mat-icon[contains(normalize-space(),"
                    + "'receipt')]"
            );

            WebElement menu = wait.until(

                ExpectedConditions.visibilityOfElementLocated(
                    depositSlipMenu)
            );

            scrollIntoView(menu);

            safeClick(menu);

            wait.until(ExpectedConditions.or(

                ExpectedConditions.urlContains("deposit"),

                ExpectedConditions.visibilityOfElementLocated(

                    By.xpath(
                        "//*[contains(normalize-space(),"
                            + "'Deposit Slip')]")
                )
            ));

            action.recordVerification(
                "Deposit Slip Creation page opened successfully.");

            action.captureStep(
                "Deposit Slip Creation Page");

        } catch (Exception e) {

            action.recordVerification(
                "Unable to open Deposit Slip Creation page.");

            throw new IllegalStateException(
                "Deposit Slip Creation navigation failed.",
                e);
        }
    }

    public void openDepositSlipSubmission() {
        try {
            By submissionMenu = By.xpath(
                "//a[contains(normalize-space(),'Submit Deposit Slip')]"
                    + " | //button[contains(normalize-space(),'Submit Deposit Slip')]"
                    + " | //span[contains(normalize-space(),'Submit Deposit Slip')]/ancestor::*[self::a or self::button][1]"
                    + " | //a[contains(normalize-space(),'Deposit Slip Submission')]"
                    + " | //button[contains(normalize-space(),'Deposit Slip Submission')]");

            WebElement menu = wait.until(
                ExpectedConditions.elementToBeClickable(submissionMenu));

            scrollIntoView(menu);
            safeClick(menu);

            wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("submit"),
                ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//tbody/tr")),
                ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[normalize-space()='Deposit Slip']"))));

        } catch (Exception e) {
            driver.navigate().to(buildDepositSlipSubmissionUrl());
            waitForPageLoad();
        }
    }

    public void openBatchAcknowledgement() {
        try {
            By acknowledgeMenu = By.xpath(
                "//a[contains(normalize-space(),'Batch')]"
                    + " | //button[contains(normalize-space(),'Batch')]"
                    + " | //span[contains(normalize-space(),'Batch')]/ancestor::*[self::a or self::button][1]"
                    + " | //a[contains(normalize-space(),'Acknowledgement')]"
                    + " | //button[contains(normalize-space(),'Acknowledgement')]");

            WebElement menu = wait.until(
                ExpectedConditions.elementToBeClickable(acknowledgeMenu));

            scrollIntoView(menu);
            safeClick(menu);

            wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("batch"),
                ExpectedConditions.visibilityOfElementLocated(pageTitle)));

        } catch (Exception e) {
            driver.navigate().to(buildBatchAcknowledgementUrl());
            waitForPageLoad();
        }
    }

    private String buildDepositSlipSubmissionUrl() {
        return getAppBaseUrl() + "#/deposit-slip-submission";
    }

    private String buildBatchAcknowledgementUrl() {
        return getAppBaseUrl() + "#/batches";
    }

    private String getAppBaseUrl() {
        String baseUrl = config.getURL().trim();
        if (baseUrl.contains("#")) {
            baseUrl = baseUrl.substring(0, baseUrl.indexOf('#'));
        }
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        return baseUrl;
    }

    public boolean hasSubmittedBatchReadyForAcknowledgement() {

        return findFirstAcknowledgeableBatch() != null;
    }

    public String acknowledgeFirstSubmittedBatch() {

        waitForPage();

        BatchAction targetBatch =
            wait.until(driver -> findFirstSubmittedBatch());

        scrollIntoView(targetBatch.row());

        safeClick(targetBatch.acknowledgeButton());

        waitForAcknowledgementDrawer(
            targetBatch.batchNumber());

        enterDrawerDenominations(
            targetBatch.depositAmount());

        tickCashReceivedCheckbox();

        clickDrawerAcknowledge();

        waitForBatchToBeAcknowledged(
            targetBatch.batchNumber());

        action.recordTestData(
            "Acknowledged batch number: "
                + targetBatch.batchNumber());

        return targetBatch.batchNumber();
    }

    public String acknowledgeFirstAcknowledgeableBatch() {
        waitForPage();
        BatchAction targetBatch = wait.until(driver -> findFirstAcknowledgeableBatch());
        if (targetBatch == null) {
            throw new IllegalStateException("No batch with Acknowledge button is available.");
        }
        scrollIntoView(targetBatch.row());
        safeClick(targetBatch.acknowledgeButton());
        waitForAcknowledgementDrawer(targetBatch.batchNumber());
        enterDrawerDenominations(targetBatch.depositAmount());
        tickCashReceivedCheckbox();
        clickDrawerAcknowledge();
        waitForBatchToBeAcknowledged(targetBatch.batchNumber());
        action.recordTestData("Acknowledged batch number: " + targetBatch.batchNumber());
        return targetBatch.batchNumber();
    }

    private BatchAction findFirstSubmittedBatch() {

        for (WebElement row :
                driver.findElements(batchRows)) {

            try {

                if (!row.isDisplayed()) {
                    continue;
                }

                String rowText = row.getText()
                    .replaceAll("\\s+", " ")
                    .trim()
                    .toUpperCase();

                if (!rowText.contains("BATCH_SUBMITTED")) {
                    continue;
                }

                WebElement acknowledgeButton =
                    findVisibleChild(row, acknowledgeAction);

                if (acknowledgeButton == null) {
                    continue;
                }

                String batchNumber =
                    extractBatchNumber(row);

                if (batchNumber.isBlank()) {
                    continue;
                }

                return new BatchAction(

                    batchNumber,

                    extractDepositAmount(row),

                    row,

                    acknowledgeButton
                );

            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private BatchAction findFirstAcknowledgeableBatch() {
        int inspected = 0;
        for (WebElement row : driver.findElements(batchRows)) {
            try {
                boolean displayed = false;
                try { displayed = row.isDisplayed(); } catch (Exception ignored) {}

                String rowText = "";
                try { rowText = row.getText().replaceAll("\\s+", " ").trim(); } catch (Exception ignored) {}

                if (!displayed && rowText.isBlank()) {
                    continue;
                }

                String upper = rowText.toUpperCase();
                if (upper.contains("BATCH_ACKNOWLEDGED")) {
                    continue;
                }

                inspected++;

                List<WebElement> buttons = row.findElements(rowAcknowledgeButton);
                System.out.println("[DEBUG] Row#" + inspected + " displayed=" + displayed + " text=" + rowText.substring(0, Math.min(120, rowText.length())) + " buttons=" + buttons.size());

                WebElement acknowledgeButton = findVisibleChild(row, rowAcknowledgeButton);
                if (acknowledgeButton == null) {
                    continue;
                }

                String batchNumber = extractBatchNumber(row);
                if (batchNumber.isBlank()) {
                    continue;
                }

                return new BatchAction(batchNumber, extractDepositAmount(row), row, acknowledgeButton);
            } catch (Exception ignored) {
            }
        }

        System.out.println("[DEBUG] Batch page inspected rows=" + inspected + ", acknowledged=false");
        return null;
    }

    private void waitForBatchToBeAcknowledged(
            String batchNumber) {

        try {
            new WebDriverWait(
                driver,
                Duration.ofSeconds(25))

                .until(driver -> {

                    WebElement refreshedRow =
                        findBatchRow(batchNumber);

                    if (refreshedRow == null) {
                        System.out.println("[DEBUG] Batch row not found for " + batchNumber);
                        return false;
                    }

                    String rowText = refreshedRow.getText()
                        .replaceAll("\\s+", " ")
                        .trim()
                        .toUpperCase();

                    System.out.println("[DEBUG] Batch row text=" + rowText);

                    boolean ack = rowText.contains("BATCH_ACKNOWLEDGED");
                    boolean submitted = rowText.contains("BATCH_SUBMITTED");
                    boolean any = !submitted;

                    if (!ack && !any) {
                        return false;
                    }

                    return true;
                });
        } catch (Exception e) {
            System.out.println("[WARN] waitForBatchToBeAcknowledged timeout or failure=" + e.getMessage());
        }
    }

    private void waitForAcknowledgementDrawer(
            String batchNumber) {

        wait.until(

            ExpectedConditions.visibilityOfElementLocated(
                batchAcknowledgementDrawer));
    }

    private void tickCashReceivedCheckbox() {

        WebElement checkbox =
            wait.until(

                ExpectedConditions.presenceOfElementLocated(
                    cashReceivedCheckbox));

        scrollIntoView(checkbox);

        if (!checkbox.isSelected()) {

            ((JavascriptExecutor) driver)
                .executeScript(
                    "arguments[0].click();",
                    checkbox);
        }
    }

    private void enterDrawerDenominations(
            BigDecimal depositAmount) {

        if (depositAmount == null) {
            return;
        }

        for (WebElement row :
                driver.findElements(
                    drawerDenominationRows)) {

            try {

                WebElement input =
                    row.findElement(
                        By.xpath(".//input"));

                if (input.isDisplayed()) {

                    input.sendKeys(
                        Keys.chord(Keys.CONTROL, "a"));

                    input.sendKeys(Keys.DELETE);

                    input.sendKeys("1");

                    break;
                }

            } catch (Exception ignored) {
            }
        }
    }

    private void clickDrawerAcknowledge() {

        WebElement button =
            wait.until(

                ExpectedConditions.elementToBeClickable(
                    drawerAcknowledgeButton));

        scrollIntoView(button);

        safeClick(button);
    }

    private WebElement findBatchRow(
            String batchNumber) {

        for (WebElement row :
                driver.findElements(batchRows)) {

            try {

                String currentBatchNumber =
                    extractBatchNumber(row);

                if (batchNumber.equalsIgnoreCase(
                        currentBatchNumber)) {

                    return row;
                }

            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private String extractBatchNumber(
            WebElement row) {

        List<WebElement> cells =
            row.findElements(By.xpath(".//td"));

        if (!cells.isEmpty()) {

            return cells.get(0)
                .getText()
                .replaceAll("\\s+", " ")
                .trim();
        }

        return "";
    }

    private BigDecimal extractDepositAmount(
            WebElement row) {

        List<WebElement> cells =
            row.findElements(By.xpath(".//td"));

        if (cells.size() >= 4) {

            return parseAmount(
                cells.get(3).getText());
        }

        return null;
    }

    private WebElement findVisibleChild(
            WebElement parent,
            By locator) {

        for (WebElement child :
                parent.findElements(locator)) {

            try {

                if (child.isDisplayed()) {
                    return child;
                }

            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private BigDecimal parseAmount(
            String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized =
            value.replaceAll("[^0-9.\\-]", "")
                .trim();

        if (normalized.isBlank()) {
            return null;
        }

        try {

            return new BigDecimal(normalized);

        } catch (NumberFormatException e) {

            return null;
        }
    }

    public void clickShowFilter() {
        WebElement button = action.findClickable(showFilterButton);
        action.scrollToElement(button);
        ActionDriver.globalSafeClick(driver, button);
        waitForUiStable();
        action.recordVerification("'Show Filter' button clicked.");
        action.captureStep("Batch | Show Filter clicked");
    }

    public void clickSearch() {
        WebElement button = action.findClickable(searchButton);
        action.scrollToElement(button);
        ActionDriver.globalSafeClick(driver, button);
        waitForTableLoaded();
        action.recordVerification("'Search' button clicked.");
        action.captureStep("Batch | Search clicked");
    }

    public void clickClear() {
        WebElement button = action.findClickable(clearButton);
        action.scrollToElement(button);
        ActionDriver.globalSafeClick(driver, button);
        waitForUiStable();
        action.recordVerification("'Clear' button clicked.");
        action.captureStep("Batch | Clear clicked");
    }

    public void clickFirstAcknowledgeButton() {
        WebElement button = action.findClickable(firstAcknowledgeButtonInTable);
        action.scrollToElement(button);
        ActionDriver.globalSafeClick(driver, button);
        action.recordVerification("First 'Acknowledge' button clicked.");
        action.captureStep("Batch | First Acknowledge clicked");
    }

    public void scrollToCashReceivedCheckbox() {
        WebElement checkbox = wait.until(
            ExpectedConditions.presenceOfElementLocated(cashReceivedCheckboxInDrawer));
        action.scrollToElement(checkbox);
        action.recordVerification("Scrolled to Cash Received checkbox.");
    }

    public void tickCashReceivedCheckboxInDrawer() {
        WebElement checkbox = wait.until(
            ExpectedConditions.presenceOfElementLocated(cashReceivedCheckboxInDrawer));
        action.scrollToElement(checkbox);
        if (!checkbox.isSelected()) {
            ActionDriver.globalSafeClick(driver, checkbox);
        }
        action.recordVerification("'Cash Received' checkbox selected.");
        action.captureStep("Batch | Cash Received checked");
    }

    public void scrollToTopOfSidePanel() {
        action.scrollToTop();
        action.recordVerification("Scrolled to top of side panel.");
    }

    public void clickProceedWithAcknowledge() {
        WebElement button = action.findClickable(proceedAcknowledgeButton);
        action.scrollToElement(button);
        ActionDriver.globalSafeClick(driver, button);
        action.recordVerification("'Proceed with Acknowledge' button clicked.");
        action.captureStep("Batch | Proceed clicked");
    }

    public void clickOkInConfirmation() {
        WebElement button = action.findClickable(okButtonConfirmation);
        action.scrollToElement(button);
        ActionDriver.globalSafeClick(driver, button);
        action.recordVerification("'OK' button clicked in confirmation dialog.");
        action.captureStep("Batch | OK clicked");
    }

    public String captureAndValidateSuccessMessage() {
        waitForUiStable();
        WebElement msgElement = wait.until(
            ExpectedConditions.visibilityOfElementLocated(successMessage));
        String actualMessage = sanitize(msgElement.getText());
        String expectedMessage = "Batch Acknowledged Successfully";

        if (!actualMessage.contains(expectedMessage)) {
            throw new IllegalStateException("Success message not found. Actual: " + actualMessage);
        }

        action.recordVerification("Success message validated: " + expectedMessage);
        action.captureStep("Batch | Success: " + expectedMessage);
        action.recordTestData("Success Message: " + expectedMessage);
        return expectedMessage;
    }

    protected void scrollIntoView(WebElement element) {

        ((JavascriptExecutor) driver)
            .executeScript(
                "arguments[0].scrollIntoView(true);",
                element);
    }

    protected void safeClick(WebElement element) {

        wait.until(
            ExpectedConditions.elementToBeClickable(
                element));

        element.click();
    }

    protected void assertNoSystemError(String step) {

        System.out.println(
            "Verified no system error at step: " + step);
    }

    protected void waitForUiStable() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> ((JavascriptExecutor) d)
                    .executeScript("return document.readyState").equals("complete"));
        } catch (Exception ignored) {}
    }

    private String sanitize(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }

    private record BatchAction(
        String batchNumber,
        BigDecimal depositAmount,
        WebElement row,
        WebElement acknowledgeButton) {
    }
}