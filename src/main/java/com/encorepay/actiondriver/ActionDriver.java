package com.encorepay.actiondriver;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.encorepay.utilities.ConfigReader;
import com.encorepay.utilities.ScreenshotUtil;
import com.encorepay.utilities.helpers.ElementHelper;
import com.encorepay.utilities.helpers.JavaScriptHelper;
import com.encorepay.utilities.helpers.TextHelper;
import com.encorepay.utilities.helpers.WaitHelper;

import io.qameta.allure.Step;

public class ActionDriver {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final WebDriverWait shortWait;
    private final ConfigReader config;

    private static final By TRANSIENT_FEEDBACK = By.xpath(
        "//*[contains(@class,'toast') or contains(@class,'snack')"
            + " or contains(@class,'notification')"
            + " or contains(@class,'toastr')"
            + " or contains(@class,'mat-mdc-snack')"
            + " or contains(@class,'mdc-snackbar')"
            + " or contains(@class,'ngx-toastr')"
            + " or @role='alert']");

    private static final By OVERLAYS = By.cssSelector(".cdk-overlay-backdrop, .cdk-overlay-pane, .loader, .spinner, .ngx-spinner, .loading");

    public ActionDriver(WebDriver driver) {
        this.driver = driver;
        this.config = new ConfigReader();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(config.getExplicitWait()));
        this.shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    public void waitForUiStable() {
        WaitHelper.waitForDocumentReady(driver, config.getExplicitWait());
        waitForOverlayToClear();
        waitForTransientFeedbackToClear();
    }

    public void waitForOverlayToClear() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(config.getOverlayTimeout()))
                .until(ExpectedConditions.invisibilityOfElementLocated(OVERLAYS));
        } catch (Exception ignored) {}
    }

    public WebElement findVisible(By locator) {
        waitForOverlayToClear();
        return WaitHelper.waitForVisible(driver, locator, config.getExplicitWait());
    }

    public WebElement findClickable(By locator) {
        waitForOverlayToClear();
        return WaitHelper.waitForClickable(driver, locator, config.getExplicitWait());
    }

    @Step("Entering value: '{1}' into locator: '{0}'")
    public void type(By locator, String value) {
        waitForUiStable();
        WebElement element = findVisible(locator);
        scrollToElement(element);
        clearAndType(element, value);
    }

    public void clearAndType(WebElement element, String value) {
        try {
            element.click();
            element.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            element.sendKeys(Keys.DELETE);
            if (value != null && !value.isEmpty()) {
                element.sendKeys(value);
            }
        } catch (Exception ex) {
            JavaScriptHelper.setValue(driver, element, value);
        }
    }

    @Step("Clicking on locator: '{0}'")
    public void click(By locator) {
        waitForUiStable();
        WebElement element = findClickable(locator);
        safeClick(element);
    }

    public void click(WebElement element) {
        waitForUiStable();
        wait.until(ExpectedConditions.elementToBeClickable(element));
        safeClick(element);
    }

    @Step("Checking visibility of locator: '{0}'")
    public boolean isVisible(By locator) {
        return ElementHelper.isAnyDisplayed(driver, locator);
    }

    public boolean isVisibleNow(By locator) {
        return ElementHelper.findFirstVisible(driver, locator) != null;
    }

    public boolean isPresent(By locator) {
        return ElementHelper.isPresent(driver, locator);
    }

    public boolean waitForVisibility(By locator) {
        try {
            findVisible(locator);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

   public boolean waitForAnyVisible(List<By> locators) {
        return WaitHelper.waitForAnyVisible(driver, locators, config.getExplicitWait());
    }

    public boolean waitForUrlContains(String fragment) {
        return WaitHelper.waitForUrlContains(driver, fragment, config.getExplicitWait());
    }

    public void waitForInvisibility(By locator) {
        WaitHelper.waitForInvisible(driver, locator, config.getExplicitWait());
    }

    public void waitForTransientFeedbackToClear() {
        waitForTransientFeedbackToClear(driver, this::recordVerification);
    }

 
    public static void waitForTransientFeedbackToClear(WebDriver driver) {
        waitForTransientFeedbackToClear(driver, null);
    }

    private static void waitForTransientFeedbackToClear(WebDriver driver, java.util.function.Consumer<String> verificationSink) {
        try {
            WebDriverWait invisibilityWait = new WebDriverWait(driver, Duration.ofSeconds(12));

            WebElement visibleToast = null;
            List<WebElement> toastElements = driver.findElements(TRANSIENT_FEEDBACK);
            for (WebElement el : toastElements) {
                try {
                    if (el.isDisplayed()) {
                        visibleToast = el;
                        break;
                    }
                } catch (Exception ignored) {}
            }

            if (visibleToast != null) {
                try {
                    if (verificationSink != null) {
                        String text = sanitizeText(visibleToast.getText());
                        if (!text.isBlank()) verificationSink.accept("Application feedback displayed: " + text);
                    }
                } catch (Exception ignored) {}

                try {
                    WebElement close = visibleToast.findElement(By.xpath(
                        ".//button[contains(@class,'close')]"
                            + " | .//button[@aria-label='Close' or @aria-label='close']"
                            + " | .//span[normalize-space()='close']/ancestor::button[1]"));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", close);
                } catch (Exception ignored) {}
            }

            invisibilityWait.until(ExpectedConditions.invisibilityOfElementLocated(TRANSIENT_FEEDBACK));
        } catch (Exception ignored) {}
    }

    public void smoothScrollToTop() {
        try {
            JavaScriptHelper.execute(driver, "window.scrollTo({ top: 0, behavior: 'smooth' });");
        } catch (Exception e) {
            scrollToTop();
        }
    }

    public void scrollToTop() {
        JavaScriptHelper.scrollToTop(driver);
    }

    public void scrollToElement(By locator) {
        WebElement element = ElementHelper.findFirstVisible(driver, locator);
        if (element == null) {
            element = ElementHelper.findFirstPresent(driver, locator);
        }
        if (element != null) {
            scrollToElement(element);
        }
    }

    public void scrollToElement(WebElement element) {
        JavaScriptHelper.scrollIntoView(driver, element);
    }

    public String getText(By locator) {
        try {
            return findVisible(locator).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public void humanPause(long millis) {
        System.out.println("[WARN] Deprecated humanPause (Thread.sleep) called. Ignoring hard sleep, ensuring UI stability instead.");
        waitForUiStable();
    }

    public boolean validate(boolean condition, String passMsg, String failMsg) {
        if (condition) {
            System.out.println("[PASS] " + passMsg);
        } else {
            System.out.println("[FAIL] " + failMsg);
        }
        return condition;
    }

    public boolean validateVisible(By locator, String elementName) {
        boolean visible = waitForVisibility(locator);
        if (visible) {
            System.out.println("[PASS] " + elementName + " is visible");
        } else {
            System.out.println("[FAIL] " + elementName + " is NOT visible");
        }
        return visible;
    }

    public void captureStep(String label) {
        try {
            waitForUiStable();
            ScreenshotUtil.captureCurrentTestStep(driver, label);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void captureStep(String label, By focusLocator) {
        WebElement focusElement = findFirstVisibleElement(focusLocator);
        if (focusElement == null) {
            focusElement = findFirstPresentElement(focusLocator);
        }
        try {
            if (focusElement != null) {
                scrollToElement(focusElement);
            }
        } catch (Exception ignored) {
        }
        captureStep(label);
    }

    public void captureStep(String label, WebElement focusElement) {
        try {
            scrollToElement(focusElement);
        } catch (Exception ignored) {
        }
        captureStep(label);
    }

    public void recordVerification(String detail) {
        ScreenshotUtil.addCurrentTestVerification(detail);
        System.out.println("[VERIFY] " + detail);
    }

    public void recordTestData(String detail) {
        ScreenshotUtil.addCurrentTestData(detail);
        System.out.println("[DATA] " + detail);
    }

    private void safeClick(WebElement element) {
        try {
            waitForTransientFeedbackToClear();
            closeFloatingMenus(driver);
            scrollToElement(element);
            waitForOverlayToClear();
            new Actions(driver).moveToElement(element).click().perform();
        } catch (Exception e) {
            JavaScriptHelper.click(driver, element);
        }
    }

    public static void closeFloatingMenus(WebDriver driver) {
        try {
            List<WebElement> activeOverlays = driver.findElements(By.cssSelector(".mat-mdc-menu-panel, .cdk-overlay-backdrop, .cdk-overlay-transparent-backdrop"));
            for (WebElement o : activeOverlays) {
                if (o.isDisplayed()) {
                    ((JavascriptExecutor) driver).executeScript("document.body.click();");
                    break;
                }
            }
        } catch (Exception ignored) {}
    }

    public static void globalSafeClick(WebDriver driver, WebElement element) {
        try {
            waitForTransientFeedbackToClear(driver);
            boolean isProtectedElement = false;
            try {
                String role = element.getAttribute("role");
                String clazz = element.getAttribute("class");
                if (role != null && (role.contains("menuitem") || role.contains("option"))) {
                    isProtectedElement = true;
                }
                if (clazz != null && (clazz.contains("mat-mdc-menu-item") || clazz.contains("mat-mdc-option"))) {
                    isProtectedElement = true;
                }
            } catch (Exception ignored) {}

            if (!isProtectedElement) {
                closeFloatingMenus(driver);
            }
            
            try {
                // Ensure no standard overlay is actively blocking globally
                new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".cdk-overlay-backdrop:not(.cdk-overlay-transparent-backdrop), .ngx-spinner, .loader")));
            } catch (Exception ignored) {}
            
            JavaScriptHelper.scrollIntoView(driver, element);
            
            try {
                new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.elementToBeClickable(element));
            } catch (Exception ignored) {}
            
            element.click();
        } catch (Exception e) {
            try {
                JavaScriptHelper.click(driver, element);
            } catch (Exception fatal) {
                System.out.println("[WARN] Global Safe Click fallback also failed: " + fatal.getMessage());
            }
        }
    }

    private WebElement findFirstVisibleElement(By locator) {
        return ElementHelper.findFirstVisible(driver, locator);
    }

    private WebElement findFirstPresentElement(By locator) {
        return ElementHelper.findFirstPresent(driver, locator);
    }


    private static String sanitizeText(String value) {
        return TextHelper.sanitize(value);
    }
}
