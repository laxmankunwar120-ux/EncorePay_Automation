package com.encorepay.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.encorepay.actiondriver.ActionDriver;

public class SubmitDepositSlipPage extends BasePage {

    private final By tableRows = By.xpath("//tbody/tr");
    private final By uploadFileAction = By.xpath(".//button[contains(text(),'Upload File')]");
    private final By toast = By.xpath("//*[contains(@class,'toast') or @role='alert']");
    private final By submitDepositSlipButtonLocator = By.xpath(
        "//button[contains(normalize-space(),'Submit') and not(contains(normalize-space(),'Submitted'))]"
            + " | //span[contains(normalize-space(),'Submit')]/ancestor::button[1]"
            + " | //a[contains(normalize-space(),'Submit')]");
    private final By rowSubmitAction = By.xpath(
        ".//button[contains(normalize-space(),'Submit') and not(contains(normalize-space(),'Submitted'))]"
            + " | .//span[contains(normalize-space(),'Submit')]/ancestor::button[1]"
            + " | .//a[contains(normalize-space(),'Submit')]");

    @FindBy(xpath = "//*[normalize-space()='Deposit Slip']")
    private WebElement pageTitle;

    @FindBy(xpath = "//*[normalize-space()='File Upload']")
    private WebElement uploadPageTitle;

    @FindBy(xpath = "//button[contains(text(),'Submit')]")
    private WebElement submitDepositSlipButton;

    @FindBy(xpath = "//input[@type='file']")
    private WebElement fileInput;

    @FindBy(xpath = "//button[normalize-space()='Upload']")
    private WebElement uploadButton;

    @FindBy(xpath = "//button[normalize-space()='Cancel']")
    private WebElement cancelButton;

    @FindBy(xpath = "//button[@title='Back']")
    private WebElement backButton;

    @FindBy(xpath = "//button[normalize-space()='Confirm' or normalize-space()='Yes' or normalize-space()='Ok']")
    private WebElement confirmationButton;

    public SubmitDepositSlipPage(WebDriver driver) {
        super(driver);
    }

    public void waitForPage() {
        wait.until(ExpectedConditions.or(
            ExpectedConditions.visibilityOf(pageTitle),
            ExpectedConditions.presenceOfElementLocated(tableRows)
        ));
        waitForUiStable();
    }

    public boolean hasCreatedDepositSlipReadyForSubmission() {
        return findRow("") != null;
    }

    public String uploadFileForFirstCreatedDepositSlip(String path) {
        return uploadFile("", path);
    }

    public String uploadFile(String slipHint, String path) {
        waitForPage();
        WebElement row = wait.until(d -> findRow(slipHint));
        if (row == null) {
            throw new RuntimeException("No deposit slip found");
        }

        safeClick(row.findElement(uploadFileAction));
        visible(fileInput);
        fileInput.sendKeys(path);
        safeClick(uploadButton);
        waitForToast();
        returnToList();
        return extractSlip(row);
    }

    public String submitCreatedDepositSlip(String slipHint) {
        waitForPage();
        WebElement row = wait.until(d -> findRow(slipHint));
        if (row == null) {
            throw new RuntimeException("No deposit slip found");
        }

        String slipNumber = extractSlip(row);
        clickRequired(row, "created deposit slip row");
        WebElement submitButton = findSubmitButton(row);
        clickRequired(submitButton, "Submit Deposit Slip button");
        handleConfirm();
        String message = waitForToast();
        waitForSubmissionToReflect(slipNumber);
        return message.isBlank() ? "Deposit slip submitted: " + slipNumber : message;
    }

    private WebElement findRow(String hint) {
        for (WebElement row : driver.findElements(tableRows)) {
            if (!row.isDisplayed()) {
                continue;
            }
            String text = row.getText();
            if (text.contains("DEPOSIT_SLIP_CREATED") && (hint.isBlank() || text.contains(hint))) {
                return row;
            }
        }
        return null;
    }

    private String extractSlip(WebElement row) {
        for (WebElement cell : row.findElements(By.xpath(".//td"))) {
            String text = sanitize(cell.getText());
            if (text.isBlank() || text.toLowerCase().contains("check_box")) {
                continue;
            }
            return text.split("\\s+")[0];
        }
        return sanitize(row.getText()).split("\\s+")[0];
    }

    private void returnToList() {
        try {
            if (isDisplayed(cancelButton)) {
                safeClick(cancelButton);
            } else if (isDisplayed(backButton)) {
                safeClick(backButton);
            }
        } catch (Exception ignored) {
        }
        waitForPage();
    }

    private void handleConfirm() {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(confirmationButton));
            clickRequired(confirmationButton, "submit confirmation button");
        } catch (Exception ignored) {
        }
    }

    private String waitForToast() {
        return waitForToastAndDisappear(toast);
    }

    private void safeClick(WebElement element) {
        waitForUiStable();
        try {
            ActionDriver.globalSafeClick(driver, element);
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
        waitForUiStable();
    }

    private WebElement findSubmitButton(WebElement selectedRow) {
        WebElement rowAction = findVisibleChild(selectedRow, rowSubmitAction);
        if (rowAction != null) {
            return rowAction;
        }

        try {
            return wait.until(driver -> findVisibleSubmitElement(submitDepositSlipButtonLocator));
        } catch (Exception e) {
            throw new IllegalStateException("Submit button was not visible after selecting a created deposit slip.");
        }
    }

    private WebElement findVisibleSubmitElement(By locator) {
        for (WebElement candidate : driver.findElements(locator)) {
            try {
                if (candidate.isDisplayed()) {
                    return candidate;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private WebElement findVisibleChild(WebElement parent, By locator) {
        for (WebElement candidate : parent.findElements(locator)) {
            try {
                if (candidate.isDisplayed()) {
                    return candidate;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private void clickRequired(WebElement element, String elementName) {
        if (element == null) {
            throw new IllegalStateException(elementName + " was not found.");
        }

        waitForUiStable();
        scrollIntoView(element);
        try {
            element.click();
        } catch (Exception firstFailure) {
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            } catch (Exception secondFailure) {
                throw new IllegalStateException("Unable to click " + elementName + ".", secondFailure);
            }
        }
        waitForUiStable();
    }

    private void waitForSubmissionToReflect(String slipNumber) {
        try {
            wait.until(driver -> {
                WebElement row = findRow(slipNumber);
                if (row == null) {
                    return true;
                }
                String text = row.getText().toUpperCase();
                return !text.contains("DEPOSIT_SLIP_CREATED")
                    || text.contains("DEPOSIT_SLIP_SUBMITTED")
                    || text.contains("SUBMITTED");
            });
        } catch (Exception ignored) {
        }
    }

    private String sanitize(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }
}
