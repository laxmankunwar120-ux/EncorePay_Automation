package com.encorepay.pages;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.encorepay.actiondriver.ActionDriver;
import com.encorepay.config.ConfigLoader;
import com.encorepay.utilities.helpers.ElementHelper;
import com.encorepay.utilities.helpers.JavaScriptHelper;
import com.encorepay.utilities.helpers.TextHelper;
import com.encorepay.utilities.helpers.WaitHelper;

public class BasePage {

    protected static final By DEFAULT_TOAST = By.xpath(
        "//*[contains(@class,'toast') or contains(@class,'snack') or contains(@class,'notification')"
            + " or contains(@class,'bg-green') or contains(@class,'bg-red') or @role='alert']");
    protected static final By DEFAULT_OVERLAY = By.cssSelector(
        ".cdk-overlay-backdrop, .cdk-overlay-pane, .loader, .spinner, .ngx-spinner, .loading");

    protected final WebDriver driver;
    protected final WebDriverWait wait;
    protected final WebDriverWait shortWait;
    protected final ActionDriver action;
    protected final ConfigLoader config;

    protected BasePage(WebDriver driver) {
        if (driver == null) {
            throw new IllegalArgumentException("WebDriver must not be null when creating a page object.");
        }
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        this.shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
        this.action = new ActionDriver(driver);
        this.config = ConfigLoader.getInstance();
        PageFactory.initElements(driver, this);
    }

    protected void waitForPageLoad() {
        WaitHelper.waitForDocumentReady(driver, config.getExplicitWait());
    }

    protected void waitForOverlayToDisappear() {
        action.waitForOverlayToClear();
    }

    protected void waitForTransientFeedbackToClear() {
        action.waitForTransientFeedbackToClear();
    }

    protected void waitForUiStable() {
        action.waitForUiStable();
    }

    protected WebElement visible(By locator) {
        waitForUiStable();
        WebElement element = WaitHelper.waitForVisible(driver, locator, config.getExplicitWait());
        if (element == null) {
            throw new org.openqa.selenium.TimeoutException("Element not visible: " + locator);
        }
        return element;
    }

    protected WebElement visible(WebElement element) {
        waitForUiStable();
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    protected WebElement clickable(By locator) {
        waitForUiStable();
        WebElement element = WaitHelper.waitForClickable(driver, locator, config.getExplicitWait());
        if (element == null) {
            // Fallback: try to find a present element and attempt to make it clickable
            try {
                java.util.List<WebElement> candidates = driver.findElements(locator);
                for (WebElement cand : candidates) {
                    try {
                        if (cand.isDisplayed()) {
                            action.scrollToElement(cand);
                            JavaScriptHelper.click(driver, cand);
                            return cand;
                        }
                    } catch (Exception ignored) {}
                }
                if (!candidates.isEmpty()) {
                    WebElement cand = candidates.get(0);
                    try {
                        action.scrollToElement(cand);
                        JavaScriptHelper.click(driver, cand);
                        return cand;
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}
            throw new org.openqa.selenium.TimeoutException("Element not clickable: " + locator);
        }
        return element;
    }

    protected WebElement clickable(WebElement element) {
        waitForUiStable();
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    protected void click(By locator) {
        action.click(locator);
    }

    protected void click(WebElement element) {
        action.click(clickable(element));
    }

    protected void clickInput(By locator) {
        WebElement element = clickable(locator);
        action.scrollToElement(element);
        try {
            if (!element.isSelected()) {
                ActionDriver.globalSafeClick(driver, element);
            }
        } catch (Exception ex) {
            action.scrollToElement(element);
            jsClick(element);
        }
        waitForUiStable();
    }

    protected void clickInput(WebElement element) {
        WebElement clickableElement = clickable(element);
        action.scrollToElement(clickableElement);
        try {
            if (!clickableElement.isSelected()) {
                ActionDriver.globalSafeClick(driver, clickableElement);
            }
        } catch (Exception ex) {
            action.scrollToElement(clickableElement);
            jsClick(clickableElement);
        }
        waitForUiStable();
    }

    protected void type(By locator, String value) {
        action.type(locator, value);
    }

    protected void type(WebElement element, String value) {
        action.clearAndType(visible(element), value);
    }

    protected void selectByVisibleText(By locator, String value) {
        selectByVisibleText(clickable(locator), value);
    }

    protected void selectByVisibleText(WebElement element, String value) {
        Select select = new Select(clickable(element));
        select.selectByVisibleText(value);
        waitForUiStable();
    }

    protected void selectByPartialVisibleText(By locator, String partialText) {
        selectByPartialVisibleText(clickable(locator), partialText);
    }

    protected void selectByPartialVisibleText(WebElement element, String partialText) {
        Select select = new Select(clickable(element));
        List<WebElement> options = select.getOptions();
        for (WebElement option : options) {
            String text = normalize(option.getText());
            if (text.equalsIgnoreCase(normalize(partialText))
                || text.toLowerCase().contains(normalize(partialText).toLowerCase())) {
                select.selectByVisibleText(option.getText());
                waitForUiStable();
                return;
            }
        }
        if (options.size() > 1) {
            select.selectByIndex(1);
            waitForUiStable();
            return;
        }
        throw new IllegalStateException("No selectable option found for value: " + partialText);
    }

    protected void waitUntil(ExpectedCondition<?> condition) {
        wait.until(condition);
    }

    protected boolean isDisplayed(By locator) {
        return ElementHelper.isAnyDisplayed(driver, locator);
    }

    protected boolean isDisplayed(WebElement element) {
        return ElementHelper.isDisplayed(element);
    }

    protected int countVisibleElements(By locator) {
        return ElementHelper.countVisible(driver, locator);
    }

    protected List<WebElement> getVisibleElements(By locator) {
        return ElementHelper.getVisibleElements(driver, locator);
    }

    protected WebElement findVisibleElement(By locator) {
        return ElementHelper.findFirstVisible(driver, locator);
    }

    protected List<WebElement> findAll(By locator) {
        return driver.findElements(locator);
    }

    protected String waitForToastAndDisappear(By toastLocator) {
        try {
            WebElement toast = wait.until(ExpectedConditions.visibilityOfElementLocated(toastLocator));
            String message = normalize(toast.getText());
            wait.until(ExpectedConditions.invisibilityOfElementLocated(toastLocator));
            waitForUiStable();
            return message;
        } catch (Exception ignored) {
            return "";
        }
    }

    public String waitForToastAndDisappear() {
        return waitForToastAndDisappear(DEFAULT_TOAST);
    }

    protected void waitForVisibleThenDisappear(By locator) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    protected void waitForInvisible(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    protected void jsClick(WebElement element) {
        JavaScriptHelper.click(driver, element);
    }

    protected void scrollIntoView(WebElement element) {
        JavaScriptHelper.scrollIntoView(driver, element);
    }

    protected void clearAndType(WebElement element, String value) {
        action.clearAndType(element, value);
    }

    protected String currentDateForInput() {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    protected String uniqueCode(String prefix) {
        return prefix + System.currentTimeMillis();
    }

    protected String normalize(String value) {
        return TextHelper.normalize(value);
    }
}
