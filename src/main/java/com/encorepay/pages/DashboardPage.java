package com.encorepay.pages;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.*;
 
public class DashboardPage extends BasePage {

    @FindBy(xpath = "//button[normalize-space()='Dashboard']")
    private WebElement dashboardMenu;

    private final By cardTotalOverdue = By.xpath("//*[contains(text(),'Total overdue Amount')]/following::div[contains(@class,'text-xl')][1]");
    private final By cardPending = By.xpath("//*[contains(text(),'Pending to be recovery')]/following::div[contains(@class,'text-xl')][1]");
    private final By cardRecovered = By.xpath("//*[contains(text(),'Total overdue recovered')]/following::div[contains(@class,'text-xl')][1]");
    private final By cardLMS = By.xpath("//*[contains(text(),'LMS Posted Amount')]/following::div[contains(@class,'text-xl')][1]");

    private final By sliderNext = By.xpath("/html/body/app-root/div/div/app-dashboard/div[2]/div/div/div[2]/button[4]/span");

    private final By sliderPrevious = By.xpath(
        "//app-dashboard//button["
            + "(contains(@class,'absolute') and contains(@class,'left'))"
            + " or contains(@aria-label,'prev')"
            + " or contains(@class,'prev')"
            + " or .//span[contains(text(),'‹')]"
            + " or .//span[contains(text(),'<')]"
            + "]");
    private final By chartsSection = By.xpath(
        "//*[contains(text(),'Collection Movement Report')"
            + " or contains(text(),'Collection Comparison Report')"
            + " or contains(text(),'Allocation Cases')]"
    );

    private final By sectionCollectionMovement = By.xpath("//*[contains(text(),'Collection Movement')]");
    private final By sectionCollectionComparison = By.xpath("//*[contains(text(),'Collection Comparison')]");
    private final By sectionAllocationCases = By.xpath("//*[contains(text(),'Allocation Cases')]");
    private final By sectionDebtRecoveryStatus = By.xpath("//*[contains(text(),'Debt Recovery Status')]");
    private final By sectionDelinquencyCases = By.xpath("//*[contains(text(),'Delinquency Cases')]");
    private final By sectionUnallocatedCases = By.xpath("//*[contains(text(),'Unallocated Cases')]");
    private final By sectionRegionWiseRecovery = By.xpath("//*[contains(text(),'Region-wise Recovery') or contains(text(),'Region wise Recovery')]");
    private final By sectionCollectionByPaymentMode = By.xpath("//*[contains(text(),'Collection by Payment Mode') or contains(text(),'Collection By Payment Mode')]");
    private final By sectionAccountAttemptStatus = By.xpath("//*[contains(text(),'Account Attempt Status')]");
    private final By sectionBranchWiseVerification = By.xpath("//*[contains(text(),'Branch-wise Verification') or contains(text(),'Branch wise Verification')]");

    private final By cardCollectionMonthTillDate = By.xpath("//*[contains(text(),'Collection Month Till Date')]/following::div[contains(@class,'text-xl')][1]");
    private final By cardTotalOsBalance = By.xpath("//*[contains(text(),'Total O/S Balance') or contains(text(),'Total OS Balance')]/following::div[contains(@class,'text-xl')][1]");
    private final By cardFollowUpAccounts = By.xpath("//*[contains(text(),'Follow Up Accounts')]/following::div[contains(@class,'text-xl')][1]");

    private final By showFilterBtn = By.xpath("/html/body/app-root/div/div/app-dashboard/div[1]/button");
    private final By branchMenuTrigger = By.xpath("/html/body/app-root/div/div/app-dashboard/div[2]/div/div[1]/div/div[1]/app-multi-select/button");
    private final By branchMenuPanel = By.xpath("//div[@role='menu' and contains(@class,'mat-mdc-menu-panel')]");
    private final By branchMenuSearchInput = By.xpath(
        "//div[@role='menu']//input[@type='search' or contains(@placeholder,'Search')]"
    );
    private final By branchMenuBackdrop = By.cssSelector(".cdk-overlay-backdrop-showing");
    private final By branchDropdown = By.xpath(
        "//label[normalize-space()='Branch Code']/following::*[@role='combobox' or self::mat-select or self::button][1]"
            + " | //label[normalize-space()='Branch Code']/following::div[contains(@class,'select')][1]"
            + " | //span[normalize-space()='Select Branch']/ancestor::*[@role='combobox' or self::mat-select or self::button or contains(@class,'select')][1]"
    );
    private final By branchTextTrigger = By.xpath("//label[normalize-space()='Branch Code']/following::span[normalize-space()='Select Branch'][1]");
    private final By branchArrowTrigger = By.xpath(
        "//label[normalize-space()='Branch Code']/following::*[contains(@class,'mat-select-arrow')"
            + " or contains(@class,'mdc-select__dropdown-icon')"
            + " or contains(@class,'dropdown-toggle')"
            + " or contains(@class,'select-arrow')][1]"
    );
    private final By branchOptions = By.xpath(
        "//div[contains(@class,'cdk-overlay-pane')]//mat-option[not(@disabled)]//*[normalize-space()]"
            + " | //div[contains(@class,'cdk-overlay-pane')]//*[@role='option'][not(@aria-disabled='true')]//*[normalize-space()]"
            + " | //ul[contains(@class,'dropdown-menu')]//li[not(contains(@class,'disabled'))]//*[normalize-space()]"
    );

    @FindBy(xpath = "//button[contains(text(),'Search')]")
    private WebElement searchBtn;

    private final By clearBtnLocator = By.xpath("//button[contains(text(),'Clear') or contains(text(),'clear') or @aria-label='Clear' or @aria-label='clear']");
    @FindBy(xpath = "//button[contains(text(),'Clear')]")
    private WebElement clearBtn;

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    public void openDashboard() {
        action.click(dashboardMenu);
        waitForDashboardReady();
        logDashboardLoadTime();
        logDashboardApiTiming();
        action.captureStep("Dashboard opened");
    }

    public void logDashboardLoadTime() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object loadEventEnd = js.executeScript("return window.performance.timing.loadEventEnd;");
            Object navigationStart = js.executeScript("return window.performance.timing.navigationStart;");

            if (loadEventEnd instanceof Long && navigationStart instanceof Long) {
                long loadTime = ((Long) loadEventEnd) - ((Long) navigationStart);
                System.out.println("[PERF] Dashboard Page Load Time: " + loadTime + " ms");
                action.recordVerification("Dashboard page load time: " + loadTime + " ms");
                action.captureStep("Dashboard Page Load Time: " + loadTime + " ms");
                return;
            }
        } catch (Exception e) {
            System.out.println("[WARN] Unable to measure dashboard page load time: " + e.getMessage());
        }
        
        action.recordVerification("Dashboard page load timing unavailable from browser.");
        action.captureStep("Dashboard Page Load Time unavailable");
    }

    public void logDashboardApiTiming() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            long startTime = ((Number) js.executeScript("return window.performance.now();")).longValue();

            Object timingObj = js.executeScript(
                "var entries = window.performance.getEntriesByType('navigation');"
                + "if (entries && entries.length > 0) {"
                + "  return {"
                + "    domContentLoaded: entries[0].domContentLoadedEventEnd - entries[0].domContentLoadedEventStart,"
                + "    responseTime: entries[0].responseEnd - entries[0].requestStart,"
                + "    requestTime: entries[0].responseStart - entries[0].requestStart,"
                + "    frontendTime: entries[0].loadEventEnd - entries[0].domContentLoadedEventEnd,"
                + "    totalLoadTime: entries[0].loadEventEnd - entries[0].startTime"
                + "  };"
                + "}"
                + "return null;"
            );

            if (timingObj instanceof java.util.Map) {
                java.util.Map<String, Object> timing = (java.util.Map<String, Object>) timingObj;
                
                long responseTime = ((Number) timing.getOrDefault("responseTime", 0L)).longValue();
                long requestTime = ((Number) timing.getOrDefault("requestTime", 0L)).longValue();
                long totalLoadTime = ((Number) timing.getOrDefault("totalLoadTime", 0L)).longValue();
                long domContentLoaded = ((Number) timing.getOrDefault("domContentLoaded", 0L)).longValue();
                long frontendTime = ((Number) timing.getOrDefault("frontendTime", 0L)).longValue();

                System.out.println("[PERF] Dashboard API Response Time: " + responseTime + " ms");
                System.out.println("[PERF] Dashboard Request Time: " + requestTime + " ms");
                System.out.println("[PERF] Dashboard DOM Content Loaded: " + domContentLoaded + " ms");
                System.out.println("[PERF] Dashboard Frontend Processing: " + frontendTime + " ms");
                System.out.println("[PERF] Dashboard Total Load Time: " + totalLoadTime + " ms");

                action.recordTestData("Dashboard API Response Time: " + responseTime + " ms");
                action.recordTestData("Dashboard Request Time: " + requestTime + " ms");
                action.recordTestData("Dashboard DOM Content Loaded: " + domContentLoaded + " ms");
                action.recordTestData("Dashboard Frontend Processing: " + frontendTime + " ms");
                action.recordTestData("Dashboard Total Load Time: " + totalLoadTime + " ms");

                action.recordVerification("Dashboard API Performance - Response: " + responseTime + " ms, Total: " + totalLoadTime + " ms");
                action.captureStep("Dashboard API Timing - Response: " + responseTime + " ms");
                return;
            }
        } catch (Exception e) {
            System.out.println("[WARN] Unable to measure dashboard API timing: " + e.getMessage());
        }
        
        action.recordVerification("Dashboard API timing unavailable from browser.");
        action.captureStep("Dashboard API Timing unavailable");
    }

    public Map<String, String> capturePerformanceTiming() {
        waitForUiStable();
        Map<String, String> timing = new java.util.LinkedHashMap<>();
        
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object loadEventEnd = js.executeScript("return window.performance.timing.loadEventEnd;");
            Object navigationStart = js.executeScript("return window.performance.timing.navigationStart;");

            if (loadEventEnd instanceof Long && navigationStart instanceof Long) {
                long loadTime = ((Long) loadEventEnd) - ((Long) navigationStart);
                timing.put("Dashboard Load Time", loadTime + " ms");
                System.out.println("[PERF] Dashboard Load Time: " + loadTime + " ms");
            }
        } catch (Exception e) {
            timing.put("Dashboard Load Time", "N/A");
        }

        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object timingObj = js.executeScript(
                "var entries = window.performance.getEntriesByType('navigation');"
                + "if (entries && entries.length > 0) {"
                + "  return {"
                + "    responseTime: entries[0].responseEnd - entries[0].requestStart,"
                + "    requestTime: entries[0].responseStart - entries[0].requestStart,"
                + "    totalLoadTime: entries[0].loadEventEnd - entries[0].startTime"
                + "  };"
                + "}"
                + "return null;"
            );

            if (timingObj instanceof java.util.Map) {
                java.util.Map<String, Object> timingMap = (java.util.Map<String, Object>) timingObj;
                timing.put("API Response Time", ((Number) timingMap.getOrDefault("responseTime", 0L)).longValue() + " ms");
                timing.put("Request Time", ((Number) timingMap.getOrDefault("requestTime", 0L)).longValue() + " ms");
                timing.put("Total Load Time", ((Number) timingMap.getOrDefault("totalLoadTime", 0L)).longValue() + " ms");
            }
        } catch (Exception e) {
            timing.put("API Response Time", "N/A");
        }

        return timing;
    }

    public Map<String, String> captureFullDashboardData() {
        waitForDashboardReady();
        Map<String, String> data = new LinkedHashMap<>();

        data.put("Current Total Overdue Amount", getText(cardTotalOverdue));
        data.put("Collection Month Till Date", getText(cardCollectionMonthTillDate));
        data.put("Total O/S Balance", getText(cardTotalOsBalance));
        data.put("Follow Up Accounts", getText(cardFollowUpAccounts));
        data.put("LMS Posted Amount", getText(cardLMS));

        clickSliderAndCapture();

        scrollDownAndCapture();
        scrollUp();

        for (Map.Entry<String, String> entry : data.entrySet()) {
            System.out.println("[DATA] " + entry.getKey() + " : " + entry.getValue());
        }

        action.captureStep("Full Dashboard Data Captured");
        return data;
    }

    private String getFirstVisibleText(By locator) {
        String value = getText(locator);
        return value.isBlank() ? "Present" : value;
    }

    public Map<String, String> captureCardDetails() {
        waitForDashboardReady();

        Map<String, String> data = new LinkedHashMap<>();

        data.put("Total Overdue", getText(cardTotalOverdue));
        data.put("Pending", getText(cardPending));
        data.put("Recovered", getText(cardRecovered));
        data.put("LMS", getText(cardLMS));

        for (String key : data.keySet()) {
            System.out.println(key + " : " + data.get(key));
        }

        action.captureStep("Cards captured");
        return data;
    }

    private String getText(By locator) {
        for (WebElement element : driver.findElements(locator)) {
            try {
                String text = element.getText().trim();
                if (element.isDisplayed() && !text.isBlank()) {
                    return text;
                }
            } catch (Exception ignored) {
            }
        }
        return "";
    }

    public boolean clickSliderAndCapture() {
        WebElement button = null;
        
        System.out.println("[INFO] Attempting to find slider next button...");
        
        List<By> locators = List.of(
            sliderNext,
            By.cssSelector("app-dashboard button[class*='next'], app-dashboard button[class*='right'], app-dashboard button[aria-label*='next']"),
            By.xpath("//button[contains(@class,'mat-mdc-icon-button') and (contains(@class,'next') or contains(@class,'right'))]"),
            By.xpath("//button[.//mat-icon[contains(text(),'chevron_right') or contains(text(),'arrow_forward')]]"),
            By.xpath("//button[@title='Next' or @title='next']"),
            By.xpath("//button[.//span[contains(text(),'›') or contains(text(),'>') or contains(text(),'→')]]"),
            By.xpath("//button[contains(@class,'icon') and (contains(@class,'next') or contains(@class,'forward') or contains(@class,'right'))]"),
            By.xpath("//button[.//svg[contains(@class,'chevron-right') or contains(@class,'arrow-right')]]"),
            By.xpath("//div[contains(@class,'pagination') or contains(@class,'slider')]//button[last()]"),
            By.xpath("//button[not(contains(@class,'prev')) and not(contains(@class,'left')) and not(contains(@class,'back'))]")
        );
        
        for (int i = 0; i < locators.size(); i++) {
            By locator = locators.get(i);
            try {
                List<WebElement> elements = driver.findElements(locator);
                System.out.println("[INFO] Locator " + (i+1) + " found " + elements.size() + " elements");
                
                for (WebElement elem : elements) {
                    try {
                        if (elem.isDisplayed()) {
                            button = elem;
                            System.out.println("[INFO] Found visible slider button using locator: " + locator);
                            break;
                        }
                    } catch (Exception ignored) {
                    }
                }
                if (button != null) break;
            } catch (Exception e) {
                System.out.println("[INFO] Locator " + (i+1) + " failed: " + e.getMessage());
                continue;
            }
        }
        
        if (button == null) {
            action.recordVerification("Dashboard slider was not rendered for the current view; continuing without slider interaction.");
            System.out.println("[WARN] No slider button found after trying all locators");
            return false;
        }

        action.scrollToElement(button);
        action.captureStep("Slider located");

        try {
            wait.until(ExpectedConditions.elementToBeClickable(button));
            action.click(button);
        } catch (Exception ex) {
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
            } catch (Exception ex2) {
                try {
                    new Actions(driver).moveToElement(button).click().perform();
                } catch (Exception ex3) {
                    try {
                        ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, view: window }));",
                            button
                        );
                    } catch (Exception ex4) {
                        action.recordVerification("Slider click failed after all retry attempts.");
                        System.out.println("[ERROR] Slider click failed: " + ex4.getMessage());
                        return false;
                    }
                }
            }
        }

        waitForSliderTransition();
        action.captureStep("Slider clicked");
        return true;
    }

    public void scrollDownAndCapture() {

    JavascriptExecutor js = (JavascriptExecutor) driver;

    long lastHeight = 0;

    while (true) {

        js.executeScript("window.scrollBy(0,600);");

        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
        }

        long newHeight = ((Number) js.executeScript(
                "return window.pageYOffset;")).longValue();

        if (newHeight == lastHeight) {
            break;
        }

        lastHeight = newHeight;
    }

    action.captureStep("Scrolled to bottom");
}

    public void scrollUp() {
        System.out.println("[INFO] Starting smooth scroll up...");
        try {
            ((JavascriptExecutor) driver).executeScript(
                "window.scrollTo({ top: 0, behavior: 'smooth' });"
            );
            Thread.sleep(800);
            action.captureStep("Scrolled up");
            System.out.println("[INFO] Smooth scroll up completed");
        } catch (Exception e) {
            System.out.println("[WARN] Smooth scroll up failed: " + e.getMessage());
            smoothScrollToPosition(250);
            smoothScrollToPosition(0);
        }
    }

    public void openFilter() {
        if (action.isVisibleNow(branchMenuTrigger) || action.isVisibleNow(branchDropdown)) {
            action.captureStep("Filter already open");
            return;
        }

        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(showFilterBtn));
        action.click(btn);
        wait.until(driver -> action.isVisibleNow(branchMenuTrigger) || action.isVisibleNow(branchDropdown));
        action.captureStep("Filter opened");
    }

    public String selectBranch() {
        return selectBranch("", 0);
    }

    public String selectBranch(String preferredBranch) {
        return selectBranch(preferredBranch, 0);
    }

    public String selectBranchByIndex(int branchIndex) {
        return selectBranch("", branchIndex);
    }

    public String selectBranch(String preferredBranch, int branchIndex) {
        String selectedText = preferredBranch.isBlank()
            ? selectBranchByAvailableIndex(branchIndex)
            : selectBranchFromMenu(preferredBranch);

        if (!selectedText.isBlank()) {
            action.captureStep("Branch selected: " + selectedText);
        }

        return selectedText;
    }

    public void clickSearch() {

    WebElement search =
            wait.until(ExpectedConditions.elementToBeClickable(searchBtn));

    ((JavascriptExecutor) driver)
            .executeScript("arguments[0].scrollIntoView({block:'center'});", search);

    action.click(search);

    waitForDashboardReady();

    action.captureStep("Search clicked");
}

    public void clickClear() {
        System.out.println("[INFO] Attempting to click Clear button...");
        
        try {
            click(clearBtn);
            System.out.println("[INFO] Clear button clicked using @FindBy");
        } catch (Exception e) {
            System.out.println("[WARN] Primary Clear button locator failed: " + e.getMessage());
            try {
                WebElement clearButton = wait.until(ExpectedConditions.elementToBeClickable(clearBtnLocator));
                action.click(clearButton);
                System.out.println("[INFO] Clear button clicked using fallback locator");
            } catch (Exception e2) {
                System.out.println("[WARN] Fallback Clear button locator failed: " + e2.getMessage());
                
                try {
                    if (action.isVisibleNow(showFilterBtn)) {
                        action.click(showFilterBtn);
                        System.out.println("[INFO] Clicked Show Filter button to close filter panel");
                        return;
                    }
                } catch (Exception e3) {
                    System.out.println("[WARN] Could not close filter panel: " + e3.getMessage());
                }
                
                try {
                    ((JavascriptExecutor) driver).executeScript(
                        "const buttons = Array.from(document.querySelectorAll('button'));"
                            + "const clearBtn = buttons.find(b => b.textContent && b.textContent.toLowerCase().includes('clear'));"
                            + "if (clearBtn) clearBtn.click();"
                    );
                    System.out.println("[INFO] Attempted JavaScript click on Clear button");
                    return;
                } catch (Exception e4) {
                    System.out.println("[ERROR] All Clear button click attempts failed");
                    return;
                }
            }
        }
        waitForDashboardDataRefresh();
        action.captureStep("Clear clicked");
    }

    public Map<String, String> performDashboardWalkthrough() {
        openDashboard();
        Map<String, String> cards = captureCardDetails();
        clickSliderAndCapture();
        scrollDownAndCapture();
        scrollUp();
        return cards;
    }

    public String performBranchwiseWalkthrough(String preferredBranch) {
        openFilter();
        String selectedBranch = selectBranch(preferredBranch, 0);
        if (selectedBranch.isBlank()) {
            return "";
        }

        clickSearch();
        reviewCurrentDashboardData();
        return selectedBranch;
    }

    public List<String> performBranchwiseWalkthroughs(int maxBranches) {
        openFilter();

        List<String> availableBranches = getAvailableBranchNames(maxBranches);
        List<String> selectedBranches = new ArrayList<>();
        for (String branchName : availableBranches) {
            if (!selectedBranches.isEmpty()) {
                clickClear();
                openFilter();
            }

            String selectedBranch = selectBranchFromMenu(branchName);
            if (selectedBranch.isBlank()) {
                continue;
            }
            clickSearch();
            reviewCurrentDashboardData();
            selectedBranches.add(selectedBranch);
        }

        return selectedBranches;
    }

    private void waitForLoad() {
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(d -> ((JavascriptExecutor) d)
            .executeScript("return document.readyState").equals("complete"));
    }

    private void waitForDashboardReady() {
        waitForLoad();
        
        long startTime = System.nanoTime();
        
        try {
            wait.withTimeout(Duration.ofSeconds(45)).until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(cardTotalOverdue),
                ExpectedConditions.visibilityOfElementLocated(cardPending),
                ExpectedConditions.visibilityOfElementLocated(cardRecovered)
            ));
        } catch (Exception e) {
            System.out.println("[WARN] Dashboard ready primary wait failed, retrying with visibility checks...");
            wait.withTimeout(Duration.ofSeconds(30)).until(d -> 
                hasVisibleText(cardTotalOverdue) || 
                hasVisibleText(cardPending) || 
                hasVisibleText(cardRecovered)
            );
        }
        
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;
        System.out.println("[PERF] Dashboard Data Ready Time: " + durationMs + " ms");
        action.recordVerification("Dashboard data ready time: " + durationMs + " ms");
        action.recordTestData("Dashboard Data Ready Time: " + durationMs + " ms");
    }

    private boolean hasVisibleText(By locator) {
        return !getText(locator).isBlank();
    }

    private void waitForSliderTransition() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(4))
                .until(driver -> action.isVisibleNow(sliderPrevious) || hasVisibleText(cardLMS));
        } catch (TimeoutException ignored) {
            waitForLoad();
        }
    }

    private void waitForDashboardDataRefresh() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(driver -> hasVisibleText(cardTotalOverdue) || hasVisibleText(cardPending) || hasVisibleText(cardRecovered));
        } catch (TimeoutException e) {
            System.out.println("[WARN] Dashboard data refresh timeout, continuing anyway");
        }
        
        waitForUiStable();
    }

    private boolean openBranchDropdown() {
        if (hasVisibleBranchOptions()) {
            return true;
        }

        List<By> triggers = List.of(branchDropdown, branchTextTrigger, branchArrowTrigger);
        for (By trigger : triggers) {
            WebElement candidate = findFirstDisplayed(trigger);
            if (candidate == null) {
                continue;
            }

            if (tryOpenBranchDropdown(candidate)) {
                return true;
            }
        }

        if (openBranchDropdownViaJavaScriptFallback()) {
            return true;
        }

        return false;
    }

    private boolean tryOpenBranchDropdown(WebElement candidate) {
        try {
            action.scrollToElement(candidate);
        } catch (Exception ignored) {
        }

        try {
            action.click(candidate);
            if (waitForBranchOptions()) {
                return true;
            }
        } catch (Exception ignored) {
        }

        try {
            new Actions(driver).moveToElement(candidate).click().perform();
            if (waitForBranchOptions()) {
                return true;
            }
        } catch (Exception ignored) {
        }

        try {
            candidate.sendKeys(Keys.SPACE);
            if (waitForBranchOptions()) {
                return true;
            }
        } catch (Exception ignored) {
        }

        try {
            candidate.sendKeys(Keys.ENTER);
            if (waitForBranchOptions()) {
                return true;
            }
        } catch (Exception ignored) {
        }

        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", candidate);
            return waitForBranchOptions();
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean waitForBranchOptions() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(4)).until(driver -> hasVisibleBranchOptions());
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    private boolean hasVisibleBranchOptions() {
        return !getVisibleBranchOptions().isEmpty();
    }

    private List<WebElement> getVisibleBranchOptions() {
        List<WebElement> visibleOptions = new ArrayList<>();
        for (WebElement option : driver.findElements(branchOptions)) {
            try {
                if (option.isDisplayed() && !option.getText().trim().isBlank()) {
                    visibleOptions.add(option);
                }
            } catch (Exception ignored) {
            }
        }
        return visibleOptions;
    }

    private String selectBranchFromVisibleOptions(String preferredBranch, int branchIndex) {
        List<WebElement> options = wait.until(driver -> {
            List<WebElement> visibleOptions = getVisibleBranchOptions();
            return visibleOptions.isEmpty() ? null : visibleOptions;
        });
        WebElement selectedOption = null;
        List<WebElement> validOptions = new ArrayList<>();

        for (WebElement option : options) {
            String text = option.getText().trim();
            if (text.isBlank() || text.contains("Select")) {
                continue;
            }
            validOptions.add(option);
            if (!preferredBranch.isBlank() && text.equalsIgnoreCase(preferredBranch.trim())) {
                selectedOption = option;
                break;
            }
        }

        if (selectedOption == null && !validOptions.isEmpty()) {
            int optionIndex = Math.min(branchIndex, validOptions.size() - 1);
            selectedOption = validOptions.get(optionIndex);
        }

        if (selectedOption == null) {
            return "";
        }

        String selectedText = selectedOption.getText().trim();
        action.click(selectedOption);
        closeBranchDropdownIfStillOpen();
        return selectedText;
    }

    private String selectBranchUsingKeyboard(int branchIndex) {
        WebElement trigger = findFirstDisplayed(branchDropdown);
        if (trigger == null) {
            trigger = findFirstDisplayed(branchTextTrigger);
        }
        if (trigger == null) {
            return "";
        }

        String previousText = readSelectedBranchLabel();

        try {
            action.scrollToElement(trigger);
            action.click(trigger);
        } catch (Exception ignored) {
        }

        try {
            trigger.sendKeys(Keys.ENTER);
        } catch (Exception ignored) {
        }

        try {
            trigger.sendKeys(Keys.HOME);
        } catch (Exception ignored) {
        }

        for (int i = 0; i <= Math.max(0, branchIndex); i++) {
            try {
                trigger.sendKeys(Keys.ARROW_DOWN);
            } catch (Exception ignored) {
            }
        }

        try {
            trigger.sendKeys(Keys.ENTER);
        } catch (Exception ignored) {
        }

        try {
            return new WebDriverWait(driver, Duration.ofSeconds(5)).until(driver -> {
                String selectedText = readSelectedBranchLabel();
                if (selectedText.isBlank() || selectedText.equalsIgnoreCase("Select Branch")) {
                    return null;
                }
                if (!previousText.isBlank() && selectedText.equalsIgnoreCase(previousText) && branchIndex > 0) {
                    return null;
                }
                return selectedText;
            });
        } catch (TimeoutException e) {
            return "";
        }
    }

    private String selectBranchByAvailableIndex(int branchIndex) {
        List<String> availableBranches = getAvailableBranchNames(branchIndex + 1);
        if (availableBranches.isEmpty()) {
            return "";
        }

        int safeIndex = Math.min(branchIndex, availableBranches.size() - 1);
        return selectBranchFromMenu(availableBranches.get(safeIndex));
    }

   private String selectBranchFromMenu(String branchName) {

    if (branchName == null || branchName.isBlank()) {
        return "";
    }

    String previousSummary = readSelectedBranchSummary();

    openBranchMenu();

    clearBranchSelections();

    WebElement searchInput =
            wait.until(ExpectedConditions.visibilityOfElementLocated(branchMenuSearchInput));

    action.clearAndType(searchInput, branchName);

    wait.until(driver ->
            getVisibleBranchNamesFromOpenMenu(50)
                    .stream()
                    .anyMatch(name -> name.equalsIgnoreCase(branchName.trim())));

    if (!clickBranchMenuEntry(branchName.trim())) {
        closeBranchMenu();
        return "";
    }

    // Wait selection
    wait.until(driver ->
            isBranchSelectionApplied(previousSummary, branchName.trim()));

    // Close dropdown
    clickOutside();

    return branchName.trim();
}

private void clickOutside() {

    try {

        WebElement dashboard = driver.findElement(
            By.xpath("//h5[contains(text(),'DashBoard')]"));

        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", dashboard);

    } catch (Exception e) {

        try {

            new Actions(driver)
                    .moveByOffset(20, 20)
                    .click()
                    .perform();

        } catch (Exception ignored) {
        }
    }

    wait.until(ExpectedConditions.invisibilityOfElementLocated(branchMenuPanel));
}

private void openBranchMenu() {

    if (isBranchMenuOpen()) {
        return;
    }

    WebElement trigger = wait.until(ExpectedConditions.elementToBeClickable(branchMenuTrigger));
    action.click(trigger);
    wait.until(ExpectedConditions.visibilityOfElementLocated(branchMenuPanel));
    wait.until(ExpectedConditions.visibilityOfElementLocated(branchMenuSearchInput));
}

    @SuppressWarnings("unchecked")
    private List<String> getAvailableBranchNames(int maxBranches) {
        openBranchMenu();
        List<String> branches = getVisibleBranchNamesFromOpenMenu(maxBranches);
        closeBranchMenu();
        return branches;
    }

    private void closeBranchMenu() {
        if (!isBranchMenuOpen()) {
            return;
        }

        if (attemptBranchMenuCloseWithEscape()) {
            return;
        }

        WebElement trigger = findFirstDisplayed(branchMenuTrigger);
        if (trigger != null && attemptBranchMenuClose(trigger)) {
            return;
        }

        WebElement backdrop = findFirstDisplayed(branchMenuBackdrop);
        if (backdrop != null && attemptBranchMenuClose(backdrop)) {
            return;
        }

        ((JavascriptExecutor) driver).executeScript("document.body.click();");
        if (!waitForBranchMenuToClose()) {
            forceCloseBranchMenu();
        }
    }

    private boolean isBranchMenuOpen() {
        return action.isVisibleNow(branchMenuPanel);
    }

    private void clearBranchSelections() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                "const normalize = value => (value || '').replace(/\\s+/g, ' ').trim();"
                    + "const menu = document.querySelector('div[role=\"menu\"].mat-mdc-menu-panel');"
                    + "if (!menu) return;"
                    + "const candidates = Array.from(menu.querySelectorAll('button,[role=\"menuitem\"],label,div,span'))"
                    + "  .filter(el => normalize(el.innerText || el.textContent).toLowerCase() === 'deselect all');"
                    + "if (!candidates.length) return;"
                    + "const target = candidates[0].closest('button,[role=\"menuitem\"],label,div') || candidates[0];"
                    + "['mouseover','pointerdown','mousedown','pointerup','mouseup','click'].forEach(type =>"
                    + "  target.dispatchEvent(new MouseEvent(type, { bubbles: true, cancelable: true, view: window }))"
                    + ");"
            );
            waitForUiStable();
        } catch (Exception ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> getVisibleBranchNamesFromOpenMenu(int maxBranches) {
        List<Object> rawBranches = (List<Object>) ((JavascriptExecutor) driver).executeScript(
            "const menu = document.querySelector('div[role=\"menu\"].mat-mdc-menu-panel');"
                + "if (!menu) return [];"
                + "const normalize = value => (value || '').replace(/\\s+/g, ' ').trim();"
                + "const isVisible = el => {"
                + "  const rect = el.getBoundingClientRect();"
                + "  const style = window.getComputedStyle(el);"
                + "  return rect.width > 0 && rect.height > 0 && style.display !== 'none' && style.visibility !== 'hidden';"
                + "};"
                + "const results = [];"
                + "Array.from(menu.querySelectorAll('*')).forEach(el => {"
                + "  if (!isVisible(el)) return;"
                + "  const text = normalize(el.innerText || el.textContent);"
                + "  if (!/^[A-Za-z0-9]+\\s*-\\s*.+$/.test(text)) return;"
                + "  if (!results.includes(text)) results.push(text);"
                + "});"
                + "return results.slice(0, arguments[0]);",
            Math.max(1, maxBranches)
        );

        List<String> branches = new ArrayList<>();
        if (rawBranches == null) {
            return branches;
        }

        for (Object value : rawBranches) {
            String text = value == null ? "" : String.valueOf(value).trim();
            if (!text.isBlank() && !branches.contains(text)) {
                branches.add(text);
            }
        }
        return branches;
    }

    private boolean clickBranchMenuEntry(String branchName) {
        try {
            Object clicked = ((JavascriptExecutor) driver).executeScript(
                "const targetName = arguments[0];"
                    + "const menu = document.querySelector('div[role=\"menu\"].mat-mdc-menu-panel');"
                    + "if (!menu) return false;"
                    + "const normalize = value => (value || '').replace(/\\s+/g, ' ').trim();"
                    + "const isVisible = el => {"
                    + "  const rect = el.getBoundingClientRect();"
                    + "  const style = window.getComputedStyle(el);"
                    + "  return rect.width > 0 && rect.height > 0 && style.display !== 'none' && style.visibility !== 'hidden';"
                    + "};"
                    + "const candidates = Array.from(menu.querySelectorAll('*')).filter(el =>"
                    + "  isVisible(el) && normalize(el.innerText || el.textContent) === targetName"
                    + ");"
                    + "if (!candidates.length) return false;"
                    + "const target = candidates[0];"
                    + "const clickable = target.closest('button,[role=\"menuitem\"],[role=\"menuitemcheckbox\"],label,div') || target;"
                    + "['mouseover','mousedown','mouseup','click'].forEach(type =>"
                    + "  clickable.dispatchEvent(new MouseEvent(type, { bubbles: true, cancelable: true, view: window }))"
                    + ");"
                    + "return true;",
                branchName
            );
            return Boolean.TRUE.equals(clicked);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isBranchSelectionApplied(String previousSummary, String branchName) {
        String currentSummary = readSelectedBranchSummary();
        if (currentSummary.isBlank()) {
            return false;
        }

        String normalizedCurrent = currentSummary.toLowerCase();
        String normalizedPrevious = previousSummary == null ? "" : previousSummary.trim().toLowerCase();
        String normalizedBranch = branchName.toLowerCase();

        if (normalizedCurrent.contains(normalizedBranch)) {
            return true;
        }

        if (normalizedCurrent.contains("item selected") || normalizedCurrent.contains("items selected")) {
            return true;
        }

        return !normalizedCurrent.equals(normalizedPrevious) && !normalizedCurrent.contains("select branch");
    }

    private String readSelectedBranchSummary() {
        WebElement trigger = findFirstDisplayed(branchMenuTrigger);
        if (trigger == null) {
            return "";
        }

        try {
            return trigger.getText().replaceAll("\\s+", " ").trim();
        } catch (Exception e) {
            return "";
        }
    }

    private boolean attemptBranchMenuCloseWithEscape() {
        try {
            driver.switchTo().activeElement().sendKeys(Keys.ESCAPE);
        } catch (Exception ignored) {
        }
        if (waitForBranchMenuToClose()) {
            return true;
        }

        WebElement trigger = findFirstDisplayed(branchMenuTrigger);
        if (trigger != null) {
            try {
                trigger.sendKeys(Keys.ESCAPE);
            } catch (Exception ignored) {
            }
        }
        return waitForBranchMenuToClose();
    }

    private boolean attemptBranchMenuClose(WebElement element) {
        try {
            action.click(element);
        } catch (Exception firstFailure) {
            try {
                new Actions(driver).moveToElement(element).click().perform();
            } catch (Exception secondFailure) {
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
                } catch (Exception ignored) {
                }
            }
        }
        return waitForBranchMenuToClose();
    }

    private boolean waitForBranchMenuToClose() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(3))
                .until(ExpectedConditions.invisibilityOfElementLocated(branchMenuPanel));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    private void forceCloseBranchMenu() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                "const sendEscape = target => {"
                    + "  ['keydown','keyup'].forEach(type => target.dispatchEvent(new KeyboardEvent(type, {"
                    + "    key: 'Escape', code: 'Escape', keyCode: 27, which: 27, bubbles: true, cancelable: true"
                    + "  })));"
                    + "};"
                    + "sendEscape(document.activeElement || document.body);"
                    + "sendEscape(document);"
                    + "sendEscape(window);"
                    + "const backdrop = document.querySelector('.cdk-overlay-backdrop-showing, .cdk-overlay-backdrop');"
                    + "if (backdrop) {"
                    + "  ['pointerdown','mousedown','pointerup','mouseup','click'].forEach(type =>"
                    + "    backdrop.dispatchEvent(new MouseEvent(type, { bubbles: true, cancelable: true, view: window }))"
                    + "  );"
                    + "}"
            );
        } catch (Exception ignored) {
        }

        if (waitForBranchMenuToClose()) {
            return;
        }

        try {
            ((JavascriptExecutor) driver).executeScript(
                "document.querySelectorAll('.cdk-overlay-backdrop, .cdk-overlay-pane').forEach(el => el.remove());"
            );
            wait.until(driver -> !isBranchMenuOpen());
        } catch (Exception ignored) {
        }
    }

    private String readSelectedBranchLabel() {
        try {
            Object selectedText = ((JavascriptExecutor) driver).executeScript(
                "const label = Array.from(document.querySelectorAll('label,span,div'))"
                    + ".find(el => (el.textContent || '').replace(/\\s+/g, ' ').trim() === 'Branch Code');"
                    + "if (!label) { return ''; }"
                    + "let container = label.parentElement;"
                    + "while (container && !container.querySelector('[role=\"combobox\"], mat-select, .mat-select-trigger, .mat-mdc-select-trigger')) {"
                    + "  container = container.parentElement;"
                    + "}"
                    + "const scope = container || document;"
                    + "const trigger = scope.querySelector('[role=\"combobox\"], mat-select, .mat-select-trigger, .mat-mdc-select-trigger');"
                    + "if (!trigger) { return ''; }"
                    + "return ((trigger.innerText || trigger.textContent || '') + '').replace(/\\s+/g, ' ').trim();");
            return selectedText == null ? "" : String.valueOf(selectedText).trim();
        } catch (Exception e) {
            return "";
        }
    }

    private boolean openBranchDropdownViaJavaScriptFallback() {
        try {
            Object opened = ((JavascriptExecutor) driver).executeScript(
                "const label = Array.from(document.querySelectorAll('label,span,div'))"
                    + ".find(el => (el.textContent || '').replace(/\\s+/g, ' ').trim() === 'Branch Code');"
                    + "if (!label) { return false; }"
                    + "let container = label.parentElement;"
                    + "while (container && !container.querySelector('[role=\"combobox\"], mat-select, .mat-select-trigger, .mat-mdc-select-trigger')) {"
                    + "  container = container.parentElement;"
                    + "}"
                    + "const scope = container || document;"
                    + "const trigger = scope.querySelector('[role=\"combobox\"], mat-select, .mat-select-trigger, .mat-mdc-select-trigger');"
                    + "if (!trigger) { return false; }"
                    + "trigger.focus();"
                    + "['mouseover','mousedown','mouseup','click'].forEach(type =>"
                    + "  trigger.dispatchEvent(new MouseEvent(type, { bubbles: true, cancelable: true, view: window }))"
                    + ");"
                    + "return true;");
            return Boolean.TRUE.equals(opened) && waitForBranchOptions();
        } catch (Exception ignored) {
            return false;
        }
    }

    private void reviewCurrentDashboardData() {
        captureCardDetails();
        clickSliderAndCapture();
        scrollDownAndCapture();
        scrollUp();
    }

    private void smoothScrollToElement(By locator) {
        WebElement target = findFirstDisplayed(locator);
        if (target == null) {
            List<WebElement> elements = driver.findElements(locator);
            target = elements.isEmpty() ? null : elements.get(0);
        }

        if (target == null) {
            smoothScrollToPosition(900);
            return;
        }

        action.scrollToElement(target);
        long targetTop = getElementTop(target);
        smoothScrollToPosition(targetTop);
    }

    private void smoothScrollToPosition(long targetY) {
        long initialOffset = getScrollOffset();
        if (Math.abs(initialOffset - targetY) < 120) {
            return;
        }

        ((JavascriptExecutor) driver).executeScript(
        		"window.scrollTo(0, arguments[0]);",
            targetY
        );

        try {
            new WebDriverWait(driver, Duration.ofSeconds(5)).until(driver -> {
                long currentOffset = getScrollOffset();
                return Math.abs(currentOffset - targetY) < 180 || Math.abs(currentOffset - initialOffset) > 80;
            });
        } catch (TimeoutException e) {
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, arguments[0]);", targetY);
        }
    }

    private long getElementTop(WebElement element) {
        Object top = ((JavascriptExecutor) driver).executeScript(
            "const rect = arguments[0].getBoundingClientRect();"
                + "return Math.round(rect.top + window.pageYOffset - 120);",
            element
        );
        return top instanceof Number ? ((Number) top).longValue() : 0L;
    }

    private long getScrollOffset() {
        Object offset = ((JavascriptExecutor) driver).executeScript("return Math.round(window.pageYOffset);");
        return offset instanceof Number ? ((Number) offset).longValue() : 0L;
    }

    private void closeBranchDropdownIfStillOpen() {
        if (!hasVisibleBranchOptions()) {
            return;
        }

        ((JavascriptExecutor) driver).executeScript("document.body.click();");
        wait.until(driver -> !hasVisibleBranchOptions());
    }

    private WebElement findFirstDisplayed(By locator) {
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

    private String toXpathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }

        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }

        StringBuilder builder = new StringBuilder("concat(");
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i > 0) {
                builder.append(',');
            }

            if (chars[i] == '\'') {
                builder.append("\"'\"");
            } else {
                builder.append('\'').append(chars[i]).append('\'');
            }
        }
        builder.append(')');
        return builder.toString();
    }

    public void fullDashboardFlow() {
        performDashboardWalkthrough();
        performBranchwiseWalkthrough("");
        clickClear();
    }

    public Map<String, String> performSanityCheck() {
        waitForDashboardReady();
        Map<String, String> results = new LinkedHashMap<>();

        results.put("Current Total Overdue Amount", getText(cardTotalOverdue));
        results.put("Collection Month Till Date", getText(cardCollectionMonthTillDate));
        results.put("Total O/S Balance", getText(cardTotalOsBalance));
        results.put("Follow Up Accounts", getText(cardFollowUpAccounts));
        results.put("LMS Posted Amount", getText(cardLMS));
        results.put("Collection Movement", hasVisibleText(sectionCollectionMovement) ? "Present" : "Missing");
        results.put("Collection Comparison (Last 3 Months)", hasVisibleText(sectionCollectionComparison) ? "Present" : "Missing");
        results.put("Allocation Cases", hasVisibleText(sectionAllocationCases) ? "Present" : "Missing");
        results.put("Debt Recovery Status", hasVisibleText(sectionDebtRecoveryStatus) ? "Present" : "Missing");
        results.put("Delinquency Cases", hasVisibleText(sectionDelinquencyCases) ? "Present" : "Missing");
        results.put("Unallocated Cases", hasVisibleText(sectionUnallocatedCases) ? "Present" : "Missing");
        results.put("Region-wise Recovery", hasVisibleText(sectionRegionWiseRecovery) ? "Present" : "Missing");
        results.put("Collection by Payment Mode", hasVisibleText(sectionCollectionByPaymentMode) ? "Present" : "Missing");
        results.put("Account Attempt Status", hasVisibleText(sectionAccountAttemptStatus) ? "Present" : "Missing");
        results.put("Branch-wise Verification", hasVisibleText(sectionBranchWiseVerification) ? "Present" : "Missing");

        for (Map.Entry<String, String> entry : results.entrySet()) {
            System.out.println("[SANITY] " + entry.getKey() + " - " + entry.getValue());
        }

        action.captureStep("Dashboard Sanity Check completed");
        return results;
    }
}
