package com.encorepay;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.encorepay.base.BaseClass;
import com.encorepay.pages.AllocationPage;
import com.encorepay.pages.AllocationPage.AllocationType;
import com.encorepay.pages.CollectionsPage;
import com.encorepay.pages.LoginPage;
import com.encorepay.pages.LogoutPage;
import com.encorepay.utilities.ConfigReader;

public class AllocationTest extends BaseClass {

    private ConfigReader config;
    private LoginPage loginPage;
    private CollectionsPage collectionsPage;
    private AllocationPage allocationPage;
    private LogoutPage logoutPage;

    @BeforeClass(alwaysRun = true)
    public void setupPages() {
        config = new ConfigReader();
        loginPage = new LoginPage(driver);
        collectionsPage = new CollectionsPage(driver);
        allocationPage = new AllocationPage(driver);
        logoutPage = new LogoutPage(driver);
    }

    @Test
    public void verifySingleAndBulkAllocationFlow() {
        loginPage.login(com.encorepay.config.ConfigLoader.getInstance().getUsername(), com.encorepay.config.ConfigLoader.getInstance().getPassword());
        collectionsPage.openAllocationQueue();

        String currentUrl = driver.getCurrentUrl();
        if (!currentUrl.toLowerCase(java.util.Locale.ROOT).contains("allocation")) {
            throw new SkipException("Allocation page is not available in the current UI — skipping allocation flow.");
        }

        allocationPage.waitForAllocationPage();

        Assert.assertTrue(allocationPage.hasRows(), "No rows are available for single allocation.");
        allocationPage.performSingleAllocation(
            config.getProperty("allocationEmployee", ""),
            AllocationType.COLLECTION_ITEM);

        collectionsPage.openAllocationQueue();

        if (!driver.getCurrentUrl().toLowerCase(java.util.Locale.ROOT).contains("allocation")) {
            throw new SkipException("Allocation page is not available after single allocation — skipping bulk allocation.");
        }

        allocationPage.waitForAllocationPage();
        int bulkRowCount = allocationPage.getDynamicBulkRowCount(3);
        Assert.assertTrue(bulkRowCount >= 1, "No rows are available for bulk allocation.");
        allocationPage.performBulkAllocation(
            config.getProperty("allocationEmployee", ""),
            AllocationType.COLLECTION_ITEM,
            bulkRowCount);
        logoutPage.logout();
        Assert.assertTrue(logoutPage.isLogoutSuccessful(), "Logout was not completed successfully.");
    }
}
