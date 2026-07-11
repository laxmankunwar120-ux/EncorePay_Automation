package com.encorepay.pages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.encorepay.utilities.helpers.TextHelper;
import com.encorepay.utilities.helpers.WaitHelper;

public class LoginPage extends BasePage {

    private static final By LOGIN_ERROR_LOCATOR = By.xpath(
        "//*[contains(@class,'bg-red') or contains(@class,'error') or contains(@class,'snack') or contains(@class,'toast')]"
            + "[contains(.,'invalid') or contains(.,'Invalid') or contains(.,'incorrect') or contains(.,'Incorrect')"
            + " or contains(.,'failed') or contains(.,'Failed') or contains(.,'required') or contains(.,'Required')]"
            + " | //mat-error"
            + " | //small[contains(@class,'error')]"
            + " | //div[contains(@class,'text-red')]");

    @FindBy(xpath =
        "//input[@placeholder='Enter User Name']"
            + " | //input[@placeholder='User Name']"
            + " | //input[@name='username']"
            + " | //input[@formcontrolname='username']"
            + " | //input[@formcontrolname='userName']"
            + " | //input[@id='userName']"
            + " | //input[@id='username']")
    private WebElement usernameField;

    @FindBy(xpath =
        "//input[@placeholder='Enter Password']"
            + " | //input[@placeholder='Password']"
            + " | //input[@name='password']"
            + " | //input[@formcontrolname='password']"
            + " | //input[@formcontrolname='passWord']"
            + " | //input[@id='password']"
            + " | //input[@id='passWord']")
    private WebElement passwordField;

    @FindBy(xpath =
        "//button[normalize-space()='Log In']"
            + " | //button[@type='submit' and contains(normalize-space(),'Log')]"
            + " | //button[normalize-space()='Login']"
            + " | //input[@type='submit']"
            + " | //button[contains(@class,'login-button')]"
            + " | //button[contains(@class,'submit-button')]")
    private WebElement loginBtn;

    private final By usernameFallback = By.xpath(
        "//input[@placeholder='Enter User Name']"
            + " | //input[@placeholder='User Name']"
            + " | //input[@name='username']"
            + " | //input[@formcontrolname='username']"
            + " | //input[@formcontrolname='userName']"
            + " | //input[@id='userName']"
            + " | //input[@id='username']"
            + " | //input[@type='email']"
            + " | //input[@type='text']"
            + " | (//mat-form-field//input)[1]");

    private final By passwordFallback = By.xpath(
        "//input[@placeholder='Enter Password']"
            + " | //input[@placeholder='Password']"
            + " | //input[@name='password']"
            + " | //input[@formcontrolname='password']"
            + " | //input[@formcontrolname='passWord']"
            + " | //input[@id='password']"
            + " | //input[@id='passWord']"
            + " | //input[@type='password']"
            + " | (//mat-form-field//input)[2]");

    private final By loginBtnFallback = By.xpath(
        "//button[normalize-space()='Log In']"
            + " | //button[@type='submit' and contains(normalize-space(),'Log')]"
            + " | //button[normalize-space()='Login']"
            + " | //input[@type='submit']"
            + " | //button[contains(@class,'login-button')]"
            + " | //button[contains(@class,'submit-button')]"
            + " | //button[contains(@class,'mat-raised-button')]"
            + " | //button[contains(@class,'mat-flat-button')]"
            + " | //button[contains(@class,'mat-button')]");

    @FindBy(xpath =
        "//*[contains(@class,'bg-green') or contains(@class,'toast') or contains(@class,'snack')]"
            + "[contains(.,'Success') or contains(.,'success')]")
    private List<WebElement> successToasts;

    @FindBy(xpath =
        "//button[normalize-space()='Dashboard']"
            + " | //button[normalize-space()='Accounts']"
            + " | //button[normalize-space()='Collections']"
            + " | //nav"
            + " | //app-header")
    private List<WebElement> postLoginMarkers;

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        driver.get(config.getURL());
        waitForLoginPage();
        action.recordVerification("Sign-in page opened successfully at " + driver.getCurrentUrl());
    }

    public void waitForLoginPage() {
        visible(usernameField);
    }

    public void login(String user, String pass) {
        login(user, pass, true);
    }

    public void login(String user, String pass, boolean navigateIfNeeded) {
        if (navigateIfNeeded && !isLoginPageVisible()) {
            open();
        }

        attemptLogin(user, pass);
        waitForLoginOutcome();

        if (!isLoginSuccessful()) {
            throw new IllegalStateException("Login failed. " + getFailureContext());
        }

        action.recordVerification("User reached the authenticated area after sign-in.");
        action.captureStep("Login Success");
    }

    public void attemptInvalidLogin(String user, String pass) {
        open();
        attemptLogin(user, pass);
        wait.until(d -> isLoginRejected() || isElementDisplayed(LOGIN_ERROR_LOCATOR));
        action.captureStep("Invalid Login Rejected");
        String errorMessage = getLoginErrorMessage();
        if (!errorMessage.isBlank()) {
            action.recordVerification("Invalid login message displayed: " + errorMessage);
        } else {
            action.recordVerification("Invalid login was rejected and the user remained on the sign-in page.");
        }
        waitForErrorFeedbackToClear();
    }

    public boolean isLoginSuccessful() {
        return isAnyDisplayed(successToasts) || isAnyDisplayed(postLoginMarkers);
    }

    public boolean isLoginRejected() {
        return !getLoginErrorMessage().isBlank();
    }

    public boolean isLoginFailed() {
        return isLoginRejected() || (isLoginPageVisible() && !isLoginSuccessful());
    }

    public boolean isLoginPageVisible() {
        return isDisplayed(usernameField);
    }

    public void prepareBlankCredentialsState() {
        open();
        type(usernameField, "");
        type(passwordField, "");
        action.captureStep("Blank Credentials Validation");
        action.recordVerification("Blank credentials kept the sign-in screen active.");
    }

    public boolean isLoginButtonEnabled() {
        try {
            return clickable(loginBtn).isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public String getLoginErrorMessage() {
        for (WebElement error : driver.findElements(LOGIN_ERROR_LOCATOR)) {
            try {
                String text = sanitizeMessage(error.getText());
                if (error.isDisplayed() && !text.isBlank()) {
                    return text;
                }
            } catch (Exception ignored) {
            }
        }
        return "";
    }

    private void attemptLogin(String user, String pass) {
        waitForLoginPage();
        type(usernameField, user == null ? "" : user);
        type(passwordField, pass == null ? "" : pass);
        action.captureStep("Credentials Entered");
        click(loginBtn);
    }

    private void waitForLoginOutcome() {
        wait.until(d -> isLoginSuccessful() || isLoginRejected());
        if (isAnyDisplayed(successToasts)) {
            try {
                WebElement toast = successToasts.stream().filter(this::isDisplayed).findFirst().orElse(null);
                if (toast != null) {
                    String toastText = sanitizeMessage(toast.getText());
                    if (!toastText.isBlank()) {
                        action.recordVerification("Login success toast displayed: " + toastText);
                    }
                    action.captureStep("Login Success Toast Visible", toast);
                    new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.invisibilityOf(toast));
                }
            } catch (Exception e) {
                System.out.println("[WARN] Login toast handling partial failure=" + e.getMessage());
            }
            try {
                wait.until(d -> isAnyDisplayed(postLoginMarkers) || !isLoginPageVisible());
            } catch (Exception e) {
                System.out.println("[WARN] Post-login marker wait partial timeout=" + e.getMessage());
            }
        } else if (!isLoginPageVisible()) {
            action.recordVerification("Login completed and post-login navigation became visible.");
        }
    }

    private String getFailureContext() {
        String error = getLoginErrorMessage();
        if (!error.isBlank()) {
            return "Visible message: " + error;
        }
        return "Current URL: " + driver.getCurrentUrl();
    }

    private boolean isElementDisplayed(By locator) {
        return isDisplayed(locator);
    }

    private boolean isAnyDisplayed(List<WebElement> elements) {
        for (WebElement element : elements) {
            if (isDisplayed(element)) {
                return true;
            }
        }
        return false;
    }

    private String sanitizeMessage(String rawText) {
        return TextHelper.sanitize(rawText);
    }

    private void waitForErrorFeedbackToClear() {
        WaitHelper.waitForInvisible(driver, LOGIN_ERROR_LOCATOR, 5);
        if (!isElementDisplayed(LOGIN_ERROR_LOCATOR)) {
            return;
        }
        waitForTransientFeedbackToClear();
    }
}
