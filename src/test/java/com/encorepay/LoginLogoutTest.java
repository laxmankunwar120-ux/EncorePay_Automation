package com.encorepay;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.encorepay.base.BaseClass;
import com.encorepay.pages.LoginPage;
import com.encorepay.pages.LogoutPage;

public class LoginLogoutTest extends BaseClass {

    private static final String INVALID_PASSWORD = "Invalid@123";

    private LoginPage loginPage;
    private LogoutPage logoutPage;

    @BeforeClass(alwaysRun = true)
    public void setupPages() {
        loginPage = new LoginPage(driver);
        logoutPage = new LogoutPage(driver);
    }

    @Test(priority = 1, description = "Confirm configured user can sign in successfully")
    public void tc01_LoginWithValidCredentials() {
        recordTestData("Credential set: configured valid QA user");
        loginPage.login(prop.getProperty("username"), prop.getProperty("password"));
        Assert.assertTrue(loginPage.isLoginSuccessful(), "Login should succeed with configured credentials.");
        recordVerification("Configured user login verified successfully.");
    }

    @Test(
        priority = 2,
        dependsOnMethods = "tc01_LoginWithValidCredentials",
        description = "Confirm the user can sign out from the profile menu and return to sign-in"
    )
    public void tc02_LogoutEndsUserSession() {
        logoutPage.logout();
        Assert.assertTrue(logoutPage.isLogoutSuccessful(), "Logout should return the user to the sign-in page.");
        recordVerification("Logout verified by the visible sign-in page.");
    }

    @Test(
        priority = 3,
        dependsOnMethods = "tc02_LogoutEndsUserSession",
        description = "Confirm invalid credentials are rejected without creating a session"
    )
    public void tc03_InvalidLoginIsRejected() {
        recordTestData("Credential set: configured username with invalid password");
        loginPage.attemptInvalidLogin(prop.getProperty("username"), INVALID_PASSWORD);
        Assert.assertTrue(loginPage.isLoginFailed(), "Invalid credentials should not create an authenticated session.");
        Assert.assertTrue(loginPage.isLoginPageVisible(), "User should remain on the sign-in page after invalid login.");

        String errorMessage = loginPage.getLoginErrorMessage();
        if (!errorMessage.isBlank()) {
            System.out.println("[INFO] Invalid login message: " + errorMessage);
            recordVerification("Negative login validation text: " + errorMessage);
        }
        recordVerification("Invalid credentials did not create an authenticated session.");
    }

    @Test(
        priority = 4,
        dependsOnMethods = "tc03_InvalidLoginIsRejected",
        description = "Confirm blank credentials keep sign-in unavailable"
    )
    public void tc04_BlankCredentialsKeepLoginDisabled() {
        recordTestData("Credential set: blank username and blank password");
        loginPage.prepareBlankCredentialsState();
        Assert.assertFalse(loginPage.isLoginButtonEnabled(),
            "Log In button should remain disabled when mandatory credentials are blank.");
        Assert.assertTrue(loginPage.isLoginPageVisible(),
            "Blank credentials validation should leave the user on the sign-in page.");
        recordVerification("Blank credentials kept the Log In button disabled.");
    }
}
