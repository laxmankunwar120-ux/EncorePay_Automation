package com.encorepay.pages;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.encorepay.actiondriver.ActionDriver;

public class LogoutPage extends BasePage {

    private static final By FLOATING_OVERLAYS = By.cssSelector(".cdk-overlay-backdrop, .cdk-overlay-pane");
    private static final By PROFILE_TRIGGER = By.xpath(
        "//button[.//*[normalize-space()='account_circle']]"
            + " | //button[contains(@aria-label,'Account') or contains(@aria-label,'Profile') or contains(@class,'profile')]"
            + " | //*[contains(@class,'material-symbols') and normalize-space()='account_circle']/ancestor::*[self::button or self::a or @role='button'][1]"
            + " | //span[normalize-space()='account_circle']/ancestor::*[self::button or self::a or @role='button'][1]"
            + " | //*[contains(@class,'material-symbols') and normalize-space()='account_circle']");

    @FindBy(xpath =
        "//span[contains(@class,'material-symbols-rounded') and normalize-space()='account_circle']"
            + " | //span[normalize-space()='account_circle']"
            + " | //button[contains(@aria-label,'Account')]"
            + " | //button[.//*[normalize-space()='account_circle']]"
            + " | //*[contains(@class,'material-symbols') and normalize-space()='account_circle']")
    private WebElement profileIcon;

    @FindBy(xpath =
        "//span[contains(@class,'text-gray-800') and normalize-space()='Logout']"
            + " | //span[normalize-space()='Logout']"
            + " | //span[normalize-space()='Log Out']"
            + " | //span[normalize-space()='Sign Out']"
            + " | //button[normalize-space()='Logout']"
            + " | //button[normalize-space()='Log Out']"
            + " | //button[normalize-space()='Sign Out']"
            + " | //a[normalize-space()='Logout']"
            + " | //a[normalize-space()='Log Out']"
            + " | //a[normalize-space()='Sign Out']"
            + " | //*[@role='menuitem' and (normalize-space()='Logout' or normalize-space()='Log Out' or normalize-space()='Sign Out')]")
    private WebElement logoutBtn;

    @FindBy(xpath =
        "//input[@placeholder='Enter User Name']"
            + " | //input[@name='username']"
            + " | //input[@formcontrolname='username']")
    private WebElement loginInput;

    public LogoutPage(WebDriver driver) {
        super(driver);
    }

    public void logout() {
        action.captureStep("Before Account Circle Click", profileIcon);
        openProfileMenu();
        action.captureStep("Account Circle Clicked - Profile Menu Open", logoutBtn);
        click(logoutBtn);
        visible(loginInput);
        action.recordVerification("Logout returned the user to the sign-in page.");
        action.captureStep("Logout Success", loginInput);
    }

    public boolean isLogoutSuccessful() {
        try {
            visible(loginInput);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void openProfileMenu() {
        closeFloatingOverlays();
        clickProfileTrigger();
        if (!isDisplayed(logoutBtn)) {
            waitForTransientFeedbackToClear();
            clickProfileTrigger();
            wait.until(d -> isDisplayed(logoutBtn));
        }
        wait.until(ExpectedConditions.visibilityOf(logoutBtn));
        action.recordVerification("Account circle opened the profile menu successfully.");
    }

    private void clickProfileTrigger() {
        WebElement trigger = findProfileTrigger();
        scrollIntoView(trigger);
        try {
            ActionDriver.globalSafeClick(driver, trigger);
        } catch (Exception ignored) {
            dispatchClick(trigger);
        }
    }

    private WebElement findProfileTrigger() {
        for (WebElement candidate : driver.findElements(PROFILE_TRIGGER)) {
            try {
                if (candidate.isDisplayed()) {
                    return resolveClickableProfileTarget(candidate);
                }
            } catch (Exception ignored) {
            }
        }
        return visible(profileIcon);
    }

    private WebElement resolveClickableProfileTarget(WebElement element) {
        try {
            Object target = ((JavascriptExecutor) driver).executeScript(
                "return arguments[0].closest('button,a,[role=\"button\"],.profile,.account') || arguments[0];",
                element
            );
            if (target instanceof WebElement webElement) {
                return webElement;
            }
        } catch (Exception ignored) {
        }
        return element;
    }

    private void closeFloatingOverlays() {
        try {
            if (driver.findElements(FLOATING_OVERLAYS).isEmpty()) {
                return;
            }
            ((JavascriptExecutor) driver).executeScript(
                "document.querySelectorAll('.cdk-overlay-backdrop, .cdk-overlay-pane').forEach(el => el.remove());"
            );
        } catch (Exception ignored) {
        }
    }

    private void dispatchClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({block:'center', inline:'nearest'});"
                + "['mouseover','pointerdown','mousedown','pointerup','mouseup','click'].forEach(type =>"
                + "  arguments[0].dispatchEvent(new MouseEvent(type, { bubbles: true, cancelable: true, view: window }))"
                + ");",
            element
        );
    }
}
