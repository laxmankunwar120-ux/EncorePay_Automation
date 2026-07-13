package com.encorepay.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.encorepay.actiondriver.ActionDriver;

public class CollectionsPage extends BasePage {

    private final By collectionsMenu = By.xpath(
        "//button[contains(@class,'menu-btn') and normalize-space()='Collections']"
            + " | //button[normalize-space()='Collections']"
            + " | //a[normalize-space()='Collections']");

    private final By collectionItemsLink = By.xpath(
        "//a[normalize-space()='Collection Items']"
            + " | //button[normalize-space()='Collection Items']"
            + " | //span[normalize-space()='Collection Items']/ancestor::*[self::a or self::button][1]");

    public CollectionsPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Navigates to the Collection Items listing screen using the existing
     * menu navigation with a URL fallback. Reuses the internal navigation
     * helpers so behaviour stays consistent with the other collection flows.
     */
    public void openCollectionItems() {
        if (!openCollectionItemsFromMenu()) {
            driver.navigate().to(buildCollectionItemsUrl());
            waitForPageLoad();
        }
        waitForUiStable();
    }

    public void openGenerateReceiptQueue() {
        if (!openCollectionItemsFromMenu()) {
            driver.navigate().to(buildCollectionItemsUrl());
            waitForPageLoad();
        }
        waitForUiStable();
    }

    public void openAllocationQueue() {
        if (!openCollectionLink("Allocation")
                && !openCollectionLink("Allocation Queue")) {
            String targetUrl = buildAllocationQueueUrl();
            try {
                driver.navigate().to(targetUrl);
            } catch (Exception ignored) {}
            if (!driver.getCurrentUrl().toLowerCase(java.util.Locale.ROOT).contains("allocation")) {
                try {
                    ((JavascriptExecutor) driver).executeScript("window.location.href = arguments[0];", targetUrl);
                } catch (Exception ignored) {}
            }
            waitForPageLoad();
        }
        waitForUiStable();
    }

    public void openDepositSlipCreation() {
        if (!openCollectionLink("Deposit Slip")
                && !openCollectionLink("Create Deposit Slip")
                && !openCollectionLink("Deposit Slips")) {
            driver.navigate().to(buildDepositSlipCreationUrl());
            waitForPageLoad();
        }
        waitForUiStable();
    }

    private boolean openCollectionLink(String label) {
        try {
            WebElement menu = wait.until(ExpectedConditions.elementToBeClickable(collectionsMenu));
            safeClick(menu);
            By option = By.xpath(
                "//a[contains(normalize-space(),'" + label + "')]"
                    + " | //button[contains(normalize-space(),'" + label + "')]"
                    + " | //span[contains(normalize-space(),'" + label + "')]/ancestor::*[self::a or self::button][1]");
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(option));
            safeClick(link);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String buildAllocationQueueUrl() {
        return getAppBaseUrl() + "#/allocation?page=0&size=10&sort=dpd,desc&openFlag=true&taskCategories=COLLECT";
    }

    private String buildDepositSlipCreationUrl() {
        return getAppBaseUrl() + "#/deposit-slip?page=0&size=10&sort=dpd,desc&openFlag=true&taskCategories=DEPOSIT";
    }

    private String buildDepositSlipSubmissionUrl() {
        return getAppBaseUrl() + "#/deposit-slip-submission";
    }

    private String buildBatchAcknowledgementUrl() {
        return getAppBaseUrl() + "#/batches";
    }

    public void openDepositSlipSubmission() {
        if (!openCollectionLink("Submit Deposit Slip")
                && !openCollectionLink("Deposit Slip Submission")) {
            driver.navigate().to(buildDepositSlipSubmissionUrl());
            waitForPageLoad();
        }
        waitForUiStable();
    }

    public void openBatchAcknowledgement() {
        if (!openCollectionLink("Acknowledge Batch")
                && !openCollectionLink("Batch Acknowledgement")) {
            driver.navigate().to(buildBatchAcknowledgementUrl());
            waitForPageLoad();
        }
        waitForUiStable();
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

    private boolean openCollectionItemsFromMenu() {
        try {
            WebElement menu = wait.until(ExpectedConditions.elementToBeClickable(collectionsMenu));
            safeClick(menu);
            List<WebElement> links = driver.findElements(collectionItemsLink);
            for (WebElement link : links) {
                if (link.isDisplayed()) {
                    safeClick(link);
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private String buildCollectionItemsUrl() {
        String baseUrl = config.getURL().trim();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + "#/collectionsitem?page=0&size=10&sort=dpd,desc&openFlag=true&taskCategories=COLLECT";
    }

    protected void safeClick(WebElement element) {
        try {
            ActionDriver.globalSafeClick(driver, element);
        } catch (Exception ignored) {
            jsClick(element);
        }
    }

    protected void scrollIntoView(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", element);
        } catch (Exception ignored) {
        }
    }

    protected WebElement waitForVisibleElement(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected String readVisibleText(By locator) {
        for (WebElement element : driver.findElements(locator)) {
            try {
                if (!element.isDisplayed()) {
                    continue;
                }
                String text = normalize(element.getText());
                if (!text.isBlank()) {
                    return text;
                }
            } catch (Exception ignored) {
            }
        }
        return "";
    }

    protected void assertNoSystemError(String context) {
        String errorMessage = readVisibleText(DEFAULT_TOAST);
        if (!errorMessage.isBlank()) {
            throw new IllegalStateException(context + " failed: " + errorMessage);
        }
    }
}
