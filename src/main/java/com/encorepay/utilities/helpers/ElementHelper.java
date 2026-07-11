package com.encorepay.utilities.helpers;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralized utility for element finding and visibility operations.
 * Provides consistent element interaction patterns across the framework.
 */
public final class ElementHelper {

    private ElementHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Finds the first visible element matching the locator.
     * 
     * @param driver the WebDriver instance
     * @param locator the By locator to search for
     * @return first visible element, or null if none found
     */
    public static WebElement findFirstVisible(WebDriver driver, By locator) {
        for (WebElement element : driver.findElements(locator)) {
            try {
                if (element.isDisplayed()) {
                    return element;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * Finds the first present element matching the locator (regardless of visibility).
     * 
     * @param driver the WebDriver instance
     * @param locator the By locator to search for
     * @return first present element, or null if none found
     */
    public static WebElement findFirstPresent(WebDriver driver, By locator) {
        List<WebElement> elements = driver.findElements(locator);
        return elements.isEmpty() ? null : elements.get(0);
    }

    /**
     * Gets all visible elements matching the locator.
     * 
     * @param driver the WebDriver instance
     * @param locator the By locator to search for
     * @return list of visible elements
     */
    public static List<WebElement> getVisibleElements(WebDriver driver, By locator) {
        List<WebElement> visibleElements = new ArrayList<>();
        for (WebElement element : driver.findElements(locator)) {
            try {
                if (element.isDisplayed()) {
                    visibleElements.add(element);
                }
            } catch (Exception ignored) {
            }
        }
        return visibleElements;
    }

    /**
     * Counts visible elements matching the locator.
     * 
     * @param driver the WebDriver instance
     * @param locator the By locator to search for
     * @return count of visible elements
     */
    public static int countVisible(WebDriver driver, By locator) {
        return getVisibleElements(driver, locator).size();
    }

    /**
     * Checks if an element is displayed.
     * 
     * @param element the element to check
     * @return true if displayed, false otherwise
     */
    public static boolean isDisplayed(WebElement element) {
        try {
            return element != null && element.isDisplayed();
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Checks if any element matching the locator is displayed.
     * 
     * @param driver the WebDriver instance
     * @param locator the By locator to search for
     * @return true if any matching element is displayed, false otherwise
     */
    public static boolean isAnyDisplayed(WebDriver driver, By locator) {
        return findFirstVisible(driver, locator) != null;
    }

    /**
     * Checks if any element matching the locator is present in the DOM.
     * 
     * @param driver the WebDriver instance
     * @param locator the By locator to search for
     * @return true if any matching element is present, false otherwise
     */
    public static boolean isPresent(WebDriver driver, By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    /**
     * Gets text from an element with null safety.
     * 
     * @param element the element to get text from
     * @return trimmed text, or empty string if element is null or exception occurs
     */
    public static String getText(WebElement element) {
        try {
            return element.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Gets text from the first visible element matching the locator.
     * 
     * @param driver the WebDriver instance
     * @param locator the By locator to search for
     * @return trimmed text from first visible element, or empty string if none found
     */
    public static String getText(WebDriver driver, By locator) {
        WebElement element = findFirstVisible(driver, locator);
        return element != null ? getText(element) : "";
    }

    /**
     * Checks if an element is enabled.
     * 
     * @param element the element to check
     * @return true if enabled, false otherwise
     */
    public static boolean isEnabled(WebElement element) {
        try {
            return element != null && element.isEnabled();
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Checks if an element is selected (for checkboxes/radio buttons).
     * 
     * @param element the element to check
     * @return true if selected, false otherwise
     */
    public static boolean isSelected(WebElement element) {
        try {
            return element != null && element.isSelected();
        } catch (Exception ignored) {
            return false;
        }
    }
}
