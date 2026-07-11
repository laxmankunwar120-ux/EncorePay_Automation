package com.encorepay.utilities.helpers;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Centralized utility for JavaScript execution operations.
 * Provides safe JavaScript execution with proper error handling.
 */
public final class JavaScriptHelper {

    private JavaScriptHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Executes JavaScript on the given driver.
     * 
     * @param driver the WebDriver instance
     * @param script the JavaScript to execute
     * @param args arguments to pass to the script
     * @return result of script execution, or null if execution fails
     */
    public static Object execute(WebDriver driver, String script, Object... args) {
        try {
            return ((JavascriptExecutor) driver).executeScript(script, args);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Scrolls the element into view using JavaScript.
     * 
     * @param driver the WebDriver instance
     * @param element the element to scroll to
     */
    public static void scrollIntoView(WebDriver driver, WebElement element) {
        execute(driver, "arguments[0].scrollIntoView({block:'center'});", element);
    }

    /**
     * Scrolls to the top of the page.
     * 
     * @param driver the WebDriver instance
     */
    public static void scrollToTop(WebDriver driver) {
        execute(driver, "window.scrollTo(0, 0);");
    }

    /**
     * Smoothly scrolls to a specific Y position.
     * 
     * @param driver the WebDriver instance
     * @param targetY the target Y position
     */
    public static void scrollToPosition(WebDriver driver, long targetY) {
        execute(driver, "window.scrollTo(0, arguments[0]);", targetY);
    }

    /**
     * Clicks an element using JavaScript.
     * 
     * @param driver the WebDriver instance
     * @param element the element to click
     */
    public static void click(WebDriver driver, WebElement element) {
        execute(driver, "arguments[0].click();", element);
    }

    /**
     * Sets the value of an element using JavaScript.
     * Triggers appropriate events for input fields.
     * 
     * @param driver the WebDriver instance
     * @param element the element to set value on
     * @param value the value to set
     */
    public static void setValue(WebDriver driver, WebElement element, String value) {
        execute(driver,
            "arguments[0].value = arguments[1];"
                + "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));"
                + "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));",
            element,
            value == null ? "" : value
        );
    }

    /**
     * Gets the scroll offset (Y position) of the page.
     * 
     * @param driver the WebDriver instance
     * @return current scroll offset, or 0 if execution fails
     */
    public static long getScrollOffset(WebDriver driver) {
        Object offset = execute(driver, "return Math.round(window.pageYOffset);");
        return offset instanceof Number ? ((Number) offset).longValue() : 0L;
    }

    /**
     * Gets the top position of an element relative to the viewport.
     * 
     * @param driver the WebDriver instance
     * @param element the element to get position for
     * @return top position offset, or 0 if execution fails
     */
    public static long getElementTop(WebDriver driver, WebElement element) {
        Object top = execute(driver,
            "const rect = arguments[0].getBoundingClientRect();"
                + "return Math.round(rect.top + window.pageYOffset - 120);",
            element
        );
        return top instanceof Number ? ((Number) top).longValue() : 0L;
    }

    /**
     * Checks if the document is fully loaded.
     * 
     * @param driver the WebDriver instance
     * @return true if document is complete, false otherwise
     */
    public static boolean isDocumentReady(WebDriver driver) {
        Object readyState = execute(driver, "return document.readyState");
        return "complete".equals(readyState);
    }

    /**
     * Gets the current URL of the page.
     * 
     * @param driver the WebDriver instance
     * @return current URL, or empty string if execution fails
     */
    public static String getCurrentUrl(WebDriver driver) {
        Object url = execute(driver, "return window.location.href");
        return url != null ? url.toString() : "";
    }

    /**
     * Clicks on the document body to close menus/modals.
     * 
     * @param driver the WebDriver instance
     */
    public static void clickBody(WebDriver driver) {
        execute(driver, "document.body.click();");
    }
}
