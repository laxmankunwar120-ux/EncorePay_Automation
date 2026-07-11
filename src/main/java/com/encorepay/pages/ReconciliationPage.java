package com.encorepay.pages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.encorepay.actiondriver.ActionDriver;

public class ReconciliationPage extends BasePage {

    @FindBy(xpath = "//button[normalize-space()='Reconciliation']")
    private WebElement reconciliationMenu;

    @FindBy(xpath = "//button[normalize-space()='CASH Reconciliation']")
    private WebElement cashRecon;

    @FindBy(xpath =
        "//*[contains(normalize-space(),'Receipt Status')]"
            + " | //button[.//span[normalize-space()='Cleared In Bank']]"
            + " | //button[contains(normalize-space(),'Cleared In Bank')]"
            + " | //input[@name='depositSlipNum']"
            + " | //button[contains(normalize-space(),'Search')]")
    private WebElement cashReconciliationShell;

    private final By tableRows = By.xpath("//tbody/tr");

    @FindBy(xpath = "//button[.//span[normalize-space()='Cleared In Bank']]")
    private WebElement clearedBtn;

    @FindBy(xpath = "//textarea")
    private WebElement remarkBox;

    @FindBy(xpath = "//button[normalize-space()='Ok']")
    private WebElement okBtn;

    @FindBy(xpath = "//span[contains(text(),'Deposit Slip')]")
    private WebElement countLabel;

    private final By toast = By.xpath("//*[contains(@class,'toast') or @role='alert']");

    public ReconciliationPage(WebDriver driver) {
        super(driver);
    }

    public void openCashDeposits() {
        waitForUiStable();
        new Actions(driver).moveToElement(visible(reconciliationMenu)).pause(Duration.ofSeconds(1)).perform();

        try {
            ActionDriver.globalSafeClick(driver, clickable(cashRecon));
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cashRecon);
        }

        wait.until(ExpectedConditions.or(
            ExpectedConditions.visibilityOf(cashReconciliationShell),
            ExpectedConditions.presenceOfElementLocated(tableRows)
        ));
        waitForUiStable();
    }

    public void openCashReconciliation() {
        openCashDeposits();
    }

    public boolean hasSubmittedSlipsForClearing() {
        waitForUiStable();
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(tableRows));
        for (WebElement row : driver.findElements(tableRows)) {
            if (row.isDisplayed() && row.getText().contains("DEPOSIT_SLIP_SUBMITTED")) {
                return true;
            }
        }
        return false;
    }

    public String selectFirstSubmittedSlip() {
        waitForUiStable();
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(tableRows));

        for (WebElement row : driver.findElements(tableRows)) {
            List<WebElement> cols = row.findElements(By.tagName("td"));
            if (cols.size() < 7) {
                continue;
            }

            String slipNumber = cols.get(1).getText().trim();
            String slipStatus = cols.get(5).getText().trim();
            String receiptStatus = cols.get(6).getText().trim();

            if (slipStatus.equals("DEPOSIT_SLIP_SUBMITTED")
                && receiptStatus.equals("DEPOSIT_SLIP_SUBMITTED")) {
                WebElement cb = cols.get(0).findElement(By.xpath(".//input[@type='checkbox']"));
                ActionDriver.globalSafeClick(driver, wait.until(ExpectedConditions.elementToBeClickable(cb)));
                return slipNumber;
            }
        }

        throw new RuntimeException("No valid rows found for reconciliation");
    }

    public int getSelectedCount() {
        try {
            return Integer.parseInt(visible(countLabel).getText().split(" ")[0]);
        } catch (Exception e) {
            return 0;
        }
    }

    public void clickClearedInBank() {
        waitForUiStable();
        WebElement btn = clickable(clearedBtn);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btn);
        try {
            ActionDriver.globalSafeClick(driver, btn);
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
        visible(remarkBox);
        waitForUiStable();
    }

    public String confirmClearedInBank(String remark) {
        waitForUiStable();
        clearAndType(remarkBox, remark);

        try {
            ActionDriver.globalSafeClick(driver, clickable(okBtn));
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", okBtn);
        }

        WebElement toastElement = wait.until(ExpectedConditions.visibilityOfElementLocated(toast));
        String msg = toastElement.getText();
        wait.until(ExpectedConditions.invisibilityOf(toastElement));
        waitForUiStable();
        return msg;
    }

    public boolean isSlipShowingStatus(String slip, String status) {
        return wait.until(d -> {
            for (WebElement row : driver.findElements(tableRows)) {
                if (row.getText().contains(slip) && row.getText().contains(status)) {
                    return true;
                }
            }
            return false;
        });
    }
}
