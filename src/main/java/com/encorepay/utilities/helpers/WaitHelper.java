package com.encorepay.utilities.helpers;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

/**
 * Centralized utility for wait operations.
 * Provides consistent wait patterns across the framework.
 */
public final class WaitHelper {

    private WaitHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a WebDriverWait with the specified timeout.
     * 
     * @param driver the WebDriver instance
     * @param timeoutSeconds timeout in seconds
     * @return WebDriverWait instance
     */
    public static WebDriverWait createWait(WebDriver driver, int timeoutSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }

    /**
     * Creates a WebDriverWait with default 15 second timeout.
     * 
     * @param driver the WebDriver instance
     * @return WebDriverWait instance with 15 second timeout
     */
    public static WebDriverWait createWait(WebDriver driver) {
        return createWait(driver, 15);
    }

    /**
     * Waits for the document to be fully loaded.
     * 
     * @param driver the WebDriver instance
     * @param timeoutSeconds timeout in seconds
     * @return true if document is ready, false if timeout occurs
     */
    public static boolean waitForDocumentReady(WebDriver driver, int timeoutSeconds) {
        try {
            return createWait(driver, timeoutSeconds).until(
                JavaScriptHelper::isDocumentReady
            );
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Waits for the document to be fully loaded with default timeout.
     * 
     * @param driver the WebDriver instance
     * @return true if document is ready, false if timeout occurs
     */
    public static boolean waitForDocumentReady(WebDriver driver) {
        return waitForDocumentReady(driver, 15);
    }

    /**
     * Waits for an element to be visible.
     * 
     * @param driver the WebDriver instance
     * @param locator the By locator
     * @param timeoutSeconds timeout in seconds
     * @return the visible element, or null if timeout occurs
     */
    public static WebElement waitForVisible(WebDriver driver, By locator, int timeoutSeconds) {
        try {
            return createWait(driver, timeoutSeconds).until(
                ExpectedConditions.visibilityOfElementLocated(locator)
            );
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Waits for an element to be visible with default timeout.
     * 
     * @param driver the WebDriver instance
     * @param locator the By locator
     * @return the visible element, or null if timeout occurs
     */
    public static WebElement waitForVisible(WebDriver driver, By locator) {
        return waitForVisible(driver, locator, 15);
    }

    /**
     * Waits for an element to be clickable.
     * 
     * @param driver the WebDriver instance
     * @param locator the By locator
     * @param timeoutSeconds timeout in seconds
     * @return the clickable element, or null if timeout occurs
     */
    public static WebElement waitForClickable(WebDriver driver, By locator, int timeoutSeconds) {
        try {
            return createWait(driver, timeoutSeconds).until(
                ExpectedConditions.elementToBeClickable(locator)
            );
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Waits for an element to be clickable with default timeout.
     * 
     * @param driver the WebDriver instance
     * @param locator the By locator
     * @return the clickable element, or null if timeout occurs
     */
    public static WebElement waitForClickable(WebDriver driver, By locator) {
        return waitForClickable(driver, locator, 15);
    }

    /**
     * Waits for an element to be invisible.
     * 
     * @param driver the WebDriver instance
     * @param locator the By locator
     * @param timeoutSeconds timeout in seconds
     * @return true if element becomes invisible, false if timeout occurs
     */
    public static boolean waitForInvisible(WebDriver driver, By locator, int timeoutSeconds) {
        try {
            createWait(driver, timeoutSeconds).until(
                ExpectedConditions.invisibilityOfElementLocated(locator)
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Waits for an element to be invisible with default timeout.
     * 
     * @param driver the WebDriver instance
     * @param locator the By locator
     * @return true if element becomes invisible, false if timeout occurs
     */
    public static boolean waitForInvisible(WebDriver driver, By locator) {
        return waitForInvisible(driver, locator, 15);
    }

    /**
     * Waits for URL to contain a specific fragment.
     * 
     * @param driver the WebDriver instance
     * @param fragment the URL fragment to wait for
     * @param timeoutSeconds timeout in seconds
     * @return true if URL contains fragment, false if timeout occurs
     */
    public static boolean waitForUrlContains(WebDriver driver, String fragment, int timeoutSeconds) {
        try {
            createWait(driver, timeoutSeconds).until(
                ExpectedConditions.urlContains(fragment)
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Waits for URL to contain a specific fragment with default timeout.
     * 
     * @param driver the WebDriver instance
     * @param fragment the URL fragment to wait for
     * @return true if URL contains fragment, false if timeout occurs
     */
    public static boolean waitForUrlContains(WebDriver driver, String fragment) {
        return waitForUrlContains(driver, fragment, 15);
    }

    /**
     * Waits for any of the provided locators to become visible.
     * 
     * @param driver the WebDriver instance
     * @param locators list of By locators to check
     * @param timeoutSeconds timeout in seconds
     * @return true if any locator becomes visible, false if timeout occurs
     */
    public static boolean waitForAnyVisible(WebDriver driver, List<By> locators, int timeoutSeconds) {
        try {
            createWait(driver, timeoutSeconds).until(d -> {
                for (By locator : locators) {
                    if (ElementHelper.isAnyDisplayed(d, locator)) {
                        return true;
                    }
                }
                return false;
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Waits for any of the provided locators to become visible with default timeout.
     * 
     * @param driver the WebDriver instance
     * @param locators list of By locators to check
     * @return true if any locator becomes visible, false if timeout occurs
     */
    public static boolean waitForAnyVisible(WebDriver driver, List<By> locators) {
        return waitForAnyVisible(driver, locators, 15);
    }

    /**
     * Waits for a custom condition to be met.
     * 
     * @param driver the WebDriver instance
     * @param condition the ExpectedCondition to wait for
     * @param timeoutSeconds timeout in seconds
     * @return true if condition is met, false if timeout occurs
     */
    public static boolean waitForCondition(WebDriver driver, ExpectedCondition<?> condition, int timeoutSeconds) {
        try {
            createWait(driver, timeoutSeconds).until(condition);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Waits for a custom condition to be met with default timeout.
     * 
     * @param driver the WebDriver instance
     * @param condition the ExpectedCondition to wait for
     * @return true if condition is met, false if timeout occurs
     */
    public static boolean waitForCondition(WebDriver driver, ExpectedCondition<?> condition) {
        return waitForCondition(driver, condition, 15);
    }

    /**
     * Waits for a custom function to return true.
     * 
     * @param driver the WebDriver instance
     * @param function the function to evaluate
     * @param timeoutSeconds timeout in seconds
     * @return true if function returns true, false if timeout occurs
     */
    public static boolean waitFor(WebDriver driver, Function<WebDriver, Boolean> function, int timeoutSeconds) {
        try {
            createWait(driver, timeoutSeconds).until(function);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Waits for a custom function to return true with default timeout.
     * 
     * @param driver the WebDriver instance
     * @param function the function to evaluate
     * @return true if function returns true, false if timeout occurs
     */
    public static boolean waitFor(WebDriver driver, Function<WebDriver, Boolean> function) {
        return waitFor(driver, function, 15);
    }

    /**
     * Pauses execution for the specified milliseconds (use sparingly).
     * 
     * @param millis milliseconds to pause
     * @deprecated Use explicit waits instead
     */
    @Deprecated
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
