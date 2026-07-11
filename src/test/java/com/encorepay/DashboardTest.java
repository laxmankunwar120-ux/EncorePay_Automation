package com.encorepay;

import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.encorepay.base.BaseClass;
import com.encorepay.pages.DashboardPage;
import com.encorepay.pages.LoginPage;
import com.encorepay.pages.LogoutPage;
import com.encorepay.utilities.DashboardPdfGenerator;

public class DashboardTest extends BaseClass {

    private LoginPage loginPage;
    private DashboardPage dashboardPage;
    private List<String> selectedBranches;
    private Map<String, String> sanityResults;
    private Map<String, String> performanceData;
    private String dashboardPdfPath;

    @BeforeClass(alwaysRun = true)
    public void setupPages() {
        loginPage = new LoginPage(driver);
        dashboardPage = new DashboardPage(driver);
    }

    @Test(priority = 1, description = "Login to the application before dashboard checks")
    public void tc01_LoginToDashboard() {
        loginPage.login(prop.getProperty("username"), prop.getProperty("password"));
        Assert.assertTrue(loginPage.isLoginSuccessful(), "Login failed.");
    }

    @Test(
        priority = 2,
        dependsOnMethods = "tc01_LoginToDashboard",
        description = "Validate primary dashboard KPI cards and chart walkthrough"
    )
    public void tc02_ValidatePrimaryDashboardCards() {
        Map<String, String> cards = dashboardPage.performDashboardWalkthrough();

        Assert.assertFalse(cards.isEmpty(), "No cards were captured.");

        List<String> requiredCards = List.of("Total Overdue", "Pending", "Recovered");
        for (String cardName : requiredCards) {
            Assert.assertFalse(cards.getOrDefault(cardName, "").isBlank(),
                    cardName + " value is blank.");
        }

        String lmsValue = cards.getOrDefault("LMS", "");
        if (lmsValue.isBlank()) {
            System.out.println("[INFO] LMS card is blank for the current environment; continuing.");
        }
    }

    @Test(
        priority = 3,
        dependsOnMethods = "tc02_ValidatePrimaryDashboardCards",
        description = "Branch-wise validation skipped - removed as requested"
    )
    public void tc03_ApplyDashboardFilter() {
        throw new SkipException("Branch-wise validation removed as requested by user");
    }

    @Test(
        priority = 4,
        dependsOnMethods = "tc02_ValidatePrimaryDashboardCards",
        alwaysRun = true,
        description = "Clear dashboard branch filter skipped - removed as requested"
    )
    public void tc04_ClearDashboardFilter() {
        throw new SkipException("Clear filter test removed as requested by user");
    }

    @Test(
        priority = 5,
        dependsOnMethods = "tc01_LoginToDashboard",
        alwaysRun = true,
        description = "Perform dashboard sanity check and generate PDF report"
    )
    public void tc05_DashboardSanityCheckAndPdf() {
        sanityResults = dashboardPage.performSanityCheck();
        Assert.assertFalse(sanityResults.isEmpty(), "No sanity check results captured.");

        long missingCount = sanityResults.values().stream().filter(v -> v.equalsIgnoreCase("Missing")).count();
        Assert.assertEquals(missingCount, 0, "Dashboard has missing sections: " + missingCount);

        performanceData = dashboardPage.capturePerformanceTiming();
        dashboardPdfPath = DashboardPdfGenerator.generateDashboardReport(sanityResults, performanceData);

        recordVerification("Dashboard PDF report generated: " + dashboardPdfPath);
        System.out.println("[INFO] Dashboard PDF report: " + dashboardPdfPath);
    }

    @Test(
        priority = 6,
        dependsOnMethods = "tc01_LoginToDashboard",
        alwaysRun = true,
        description = "Logout from the dashboard"
    )
    public void tc06_LogoutFromDashboard() {
        LogoutPage logoutPage = new LogoutPage(driver);
        logoutPage.logout();

        Assert.assertTrue(logoutPage.isLogoutSuccessful(), "Logout from dashboard failed.");
    }
}
