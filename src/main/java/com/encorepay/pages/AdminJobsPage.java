package com.encorepay.pages;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.encorepay.actiondriver.ActionDriver;
import com.encorepay.models.JobStatus;
import com.encorepay.pages.LogoutPage;

public class AdminJobsPage extends BasePage {

    public static final List<String> MONITORED_JOB_NAMES = List.of(
        "Post Receipts Job",
        "Encore Download Collection Items Job",
        "Encore Upcoming Demands Job");

    private static final String POST_RECEIPTS_JOB    = "Post Receipts Job";
    private static final String COLLECTION_ITEMS_JOB = "Encore Download Collection Items Job";
    private static final String UPCOMING_DEMANDS_JOB = "Encore Upcoming Demands Job";

    private static final Map<String, List<String>> JOB_ALIASES = new LinkedHashMap<>();

    static {
        JOB_ALIASES.put(POST_RECEIPTS_JOB, List.of(
            "Post Receipts Job", "Post Receipt Job", "POST RECEIPT", "POST_RECEIPT"));
        JOB_ALIASES.put(COLLECTION_ITEMS_JOB, List.of(
            "Encore Download Collection Items Job", "Download Collection Items Job", "COLLECTION_ITEMS"));
        JOB_ALIASES.put(UPCOMING_DEMANDS_JOB, List.of(
            "Encore Upcoming Demands Job", "Encore Up Coming Demands Job",
            "Upcoming Demands Job", "UP_COMING_DEMAND"));
    }

    @FindBy(xpath = "//button[contains(@class,'menu-btn') and normalize-space()='Admin']")
    private WebElement adminMenuBtn;

    private final By jobsMenu = By.xpath(
        "//*[self::button or self::a][normalize-space()='Job' or normalize-space()='Jobs']");

    private final By jobListTable = By.xpath(
        "//table[.//tbody//tr[td]] | //app-custom-table[.//*[td]]");

    private final By jobListRows = By.xpath("//table//tbody//tr");

    private final By receiptButton = By.xpath(
        ".//button[normalize-space()='Receipt'] | .//a[normalize-space()='Receipt']");

    private final By firstActionViewBtn =
        By.xpath("//tbody/tr[1]//button[normalize-space()='View']");

    private final By modalCloseBtn = By.xpath(
        "//button[normalize-space()='×']"
        + " | //button[@aria-label='Close' or @aria-label='close']"
        + " | //span[contains(text(),'close')]/ancestor::button[1]"
        + " | //*[local-name()='mat-icon' and normalize-space()='close']/ancestor::button[1]");

    private final By showFilterButton = By.xpath(
        "//button[contains(@class,'btn-light-blue') and contains(.,'Show Filter')]"
        + " | //button[contains(.,'Show Filter')]");

    private final By receiptDateInput = By.xpath(
        "//input[@name='receiptDate'] | //input[@placeholder='Receipt Date'] | //input[@type='date']");

    private final By lmsPostingStatus = By.xpath(
        "//select[@name='lmsPostingStatus']"
        + " | //select[contains(@name,'lms') or contains(@name,'status') or contains(@name,'posting')]"
        + " | //app-receipts//table//select | (//app-receipts//div//select)[1]");

    private final By searchButton = By.xpath(
        "//button[normalize-space()='Search' or contains(normalize-space(),'Search')]");

    private final By clearButton = By.xpath(
        "//button[normalize-space()='Clear' or contains(normalize-space(),'Clear')]");

    private final By errorIconInRow = By.xpath(
        ".//*[contains(@class,'error') and (contains(@class,'icon') or contains(@class,'outline'))]"
        + " | .//*[normalize-space()='error_outline']"
        + " | .//mat-icon[normalize-space()='error_outline']");

    private final By errorPopup = By.xpath(
        "//*[contains(@class,'modal-box') or contains(@class,'mat-mdc-dialog-container')"
        + " or @role='dialog' or contains(@class,'swal2-popup')]");

    private final By receiptTableRows = By.xpath(
        "//table[.//th[contains(normalize-space(),'Account') or contains(normalize-space(),'Receipt')]]"
        + "//tbody//tr[td]");

    private final By activeModal = By.xpath(
        "//*[contains(@class,'mat-mdc-dialog-container') and @aria-modal='true']"
        + " | //*[@role='dialog' and not(contains(@style,'display: none'))]"
        + " | //*[contains(@class,'modal') and contains(@class,'show')]"
        + " | //*[contains(@class,'swal2-popup') and not(contains(@style,'display: none'))]");

    // Real container for the execution details view (app-job-details-view), not a Material Dialog.
    private final By jobDetailsViewLocator = By.cssSelector("app-job-details-view");

    public AdminJobsPage(WebDriver driver) {
        super(driver);
    }

    public void navigateToAdminJobs() {
        if (!tryOpenFromMenu()) openJobsRouteDirectly();
        waitForJobsPage();
    }

    public boolean isJobsPageLoaded() {
        return isDisplayed(jobListTable);
    }

    public List<JobStatus> captureAdminJobMonitoringData() {
        return captureAdminJobMonitoringData(config.getClientName());
    }

    public List<JobStatus> captureAdminJobMonitoringData(String clientName) {
        List<JobStatus> statuses = new ArrayList<>();

        ensureOnJobsListPage();
        statuses.addAll(capturePostReceiptsJob(clientName));

        ensureOnJobsListPage();
        statuses.add(captureCollectionItemsJob(clientName));

        ensureOnJobsListPage();
        statuses.add(captureUpcomingDemandsJob(clientName));

        try {
            navigateBackToJobsList();
            waitForJobsPage();
            new LogoutPage(driver).logout();
        } catch (Exception ignored) {}

        return statuses;
    }

    private List<JobStatus> capturePostReceiptsJob(String clientName) {
        List<JobStatus> statuses = new ArrayList<>();

        WebElement postReceiptsRow = findJobRow(POST_RECEIPTS_JOB);
        WebElement receiptBtn = firstVisibleInRow(postReceiptsRow, receiptButton);
        if (receiptBtn == null)
            throw new IllegalStateException("Receipt button not found for Post Receipts Job.");

        scrollIntoView(receiptBtn);
        ActionDriver.globalSafeClick(driver, receiptBtn);
        waitForReceiptPage();

        List<JobStatus> failedRows  = captureReceiptRows("FAILED",  clientName);
        List<JobStatus> pendingRows = captureReceiptRows("PENDING", clientName);

        clearReceiptFilters();
        closeModal();
        System.out.println("Receipt page closed");

        ensureOnJobsListPage();

        JobStatus summary = new JobStatus();
        summary.setJobName(POST_RECEIPTS_JOB);
        summary.setClientName(clientName);
        try {
            WebElement jobRow = findJobRow(POST_RECEIPTS_JOB);
            summary.setStatus(readRowStatus(jobRow));
            summary.setCurrentStatus(summary.getStatus());

            System.out.println("Clicking Post Receipts View");
            clickViewInRow(jobRow);
            waitForJobDetailPage();

            clickFirstActionView();
            waitForViewModal();

            try {
                new WebDriverWait(driver, Duration.ofSeconds(12))
                    .until(d -> {
                        try {
                            List<WebElement> modals = d.findElements(jobDetailsViewLocator);
                            return !modals.isEmpty() && modals.get(0).isDisplayed();
                        } catch (Exception e) { return false; }
                    });
            } catch (Exception ignored) {}

            waitMillis(300);

            readModalDetails(summary);

            closeExecutionAndReturnToJobs();
            System.out.println("Returning to Jobs page successfully");

        } catch (Exception e) {
            e.printStackTrace();
            summary.setStatus(firstNonBlank(summary.getStatus(), "NOT_CAPTURED"));
            summary.setCurrentStatus(summary.getStatus());
            summary.setErrorMessage(shortError(e));
            safeRecoverToJobsList();
        }

        summary.setFailedCount(failedRows.size());
        summary.setPendingCount(pendingRows.size());
        summary.setPendingRecords(joinReceiptRefs(pendingRows));
        summary.setRemarks(firstNonBlank(summary.getRemarks(),
            "Failed: " + failedRows.size() + ", Pending: " + pendingRows.size()));
        summary.setExecutionResult(firstNonBlank(summary.getExecutionResult(), summary.getStatus()));

        statuses.add(summary);
        statuses.addAll(failedRows);
        return statuses;
    }

    private JobStatus captureCollectionItemsJob(String clientName) {
        return captureJobDetails(COLLECTION_ITEMS_JOB, clientName);
    }

    private JobStatus captureUpcomingDemandsJob(String clientName) {
        return captureJobDetails(UPCOMING_DEMANDS_JOB, clientName);
    }

    private JobStatus captureJobDetails(String jobName, String clientName) {
        JobStatus status = new JobStatus();
        status.setJobName(jobName);
        status.setClientName(clientName);

        try {
            WebElement jobRow = findJobRow(jobName);
            status.setStatus(readRowStatus(jobRow));
            status.setCurrentStatus(status.getStatus());

            clickViewInRow(jobRow);
            waitForJobDetailPage();

            clickFirstActionView();
            waitForViewModal();

            try {
                new WebDriverWait(driver, Duration.ofSeconds(12))
                    .until(d -> {
                        try {
                            List<WebElement> modals = d.findElements(jobDetailsViewLocator);
                            return !modals.isEmpty() && modals.get(0).isDisplayed();
                        } catch (Exception e) { return false; }
                    });
            } catch (Exception ignored) {}
            waitMillis(800);

            readModalDetails(status);

            closeExecutionAndReturnToJobs();

        } catch (Exception e) {
            e.printStackTrace();
            status.setStatus(firstNonBlank(status.getStatus(), "NOT_CAPTURED"));
            status.setCurrentStatus(status.getStatus());
            status.setErrorMessage(shortError(e));
            safeRecoverToJobsList();
        }

        status.setExecutionResult(firstNonBlank(status.getExecutionResult(), status.getStatus()));
        return status;
    }

    // Clicks the View button in a job list row.
    private void clickViewInRow(WebElement row) {
        System.out.println("Opening Job Details");

        List<WebElement> candidates = row.findElements(
            By.xpath(".//button[normalize-space()='View']"));

        for (WebElement btn : candidates) {
            try {
                if (!btn.isDisplayed() || !btn.isEnabled()) continue;
                scrollIntoView(btn);
                waitForUiStable();
                try {
                    btn.click();
                } catch (Exception clickEx) {
                    ((JavascriptExecutor) driver)
                        .executeScript("arguments[0].click();", btn);
                }
                return;
            } catch (Exception ignored) {}
        }
        throw new IllegalStateException(
            "View button not found in job row: " + row.getText().trim());
    }

    // Opens the latest execution record from the job history table.
    private void clickFirstActionView() {
        System.out.println("Opening Latest Execution");

        try {
            By jobInfoTab = By.xpath(
                "//button[normalize-space()='Job Info']"
                + " | //a[normalize-space()='Job Info']"
                + " | //*[@role='tab' and normalize-space()='Job Info']");

            List<WebElement> tabs = driver.findElements(jobInfoTab);
            for (WebElement tab : tabs) {
                if (!tab.isDisplayed()) continue;
                String ariaSelected = tab.getAttribute("aria-selected");
                String classes      = tab.getAttribute("class");
                boolean alreadyActive =
                    "true".equals(ariaSelected)
                    || (classes != null
                        && (classes.contains("active") || classes.contains("selected")));
                if (!alreadyActive) {
                    scrollIntoView(tab);
                    try {
                        tab.click();
                    } catch (Exception e) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", tab);
                    }
                    waitForUiStable();
                    System.out.println("Opening Job Info");
                }
                break;
            }
        } catch (Exception ignored) {}

        try {
            new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//tbody/tr[1]")));
        } catch (Exception ignored) {}

        WebElement actionView = null;
        try {
            actionView = new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.elementToBeClickable(firstActionViewBtn));
        } catch (Exception ignored) {}

        if (actionView == null)
            throw new IllegalStateException(
                "View button not found in execution-history table (first row).");

        scrollIntoView(actionView);
        waitForUiStable();
        try {
            actionView.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", actionView);
        }
    }

    // Closes the front-most visible overlay.
    private void closeModal() {
        try {
            List<WebElement> btns = driver.findElements(modalCloseBtn);

            WebElement target = null;
            for (int i = btns.size() - 1; i >= 0; i--) {
                if (btns.get(i).isDisplayed() && btns.get(i).isEnabled()) {
                    target = btns.get(i);
                    break;
                }
            }

            if (target != null) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", target);
                waitForUiStable();
                try {
                    new WebDriverWait(driver, Duration.ofSeconds(3))
                        .until(ExpectedConditions.invisibilityOfElementLocated(activeModal));
                } catch (Exception ignored) {}
                return;
            }
        } catch (Exception ignored) {}

        System.out.println("No modal close button found. Skipping close.");
    }

    // Closes the execution details view and returns to the Jobs page.
    private void closeExecutionAndReturnToJobs() {

        WebElement closeBtn1 = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(
                        By.xpath("(//span[normalize-space()='close']/ancestor::button)[last()]")));

        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", closeBtn1);

        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.invisibilityOfElementLocated(jobDetailsViewLocator));
        } catch (Exception ignored) {}
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(text(),'Job Info')]") ));
        } catch (Exception ignored) {}

        System.out.println("Closed execution detail");

        WebElement closeBtn2 = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(
                        By.xpath("(//span[normalize-space()='close']/ancestor::button)[last()]")));

        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", closeBtn2);

        waitForJobsPage();

        System.out.println("Returned to Jobs page");
    }

    // Recovers to the Jobs list page after an unexpected failure.
    private void safeRecoverToJobsList() {
        System.out.println("DETAILS CAPTURED");

        navigateBackToJobsList();
        navigateBackToJobsList();
    }

    // Captures receipt rows for a given LMS posting status.
    private List<JobStatus> captureReceiptRows(String postingStatus, String clientName) {
        clickShowFilter();
        selectPostingStatus(postingStatus);
        selectTodayReceiptDate();
        WebElement searchBtn = null;
        try {
            searchBtn = clickable(searchButton);
        } catch (Exception e) {
            System.out.println("[WARN] Search button not clickable: " + e.getMessage());
        }
        if (searchBtn != null) {
            try {
                ActionDriver.globalSafeClick(driver, searchBtn);
            } catch (Exception e) {
                System.out.println("[WARN] Search button click failed: " + e.getMessage());
            }
        }
        waitForUiStable();

        System.out.println("Capturing receipt records for LMS Posting Status = " + postingStatus);
        List<JobStatus> receipts = new ArrayList<>();
        for (WebElement row : driver.findElements(receiptTableRows)) {
            try {
                if (!row.isDisplayed()) continue;
                String rowText = row.getText().trim();
                if (rowText.isEmpty() || rowText.toLowerCase(Locale.ROOT).contains("no record")) continue;

                JobStatus status = readReceiptRow(row, postingStatus);
                status.setClientName(clientName);
                if ("FAILED".equalsIgnoreCase(postingStatus)) readReceiptErrorPopup(row, status);
                receipts.add(status);
            } catch (Exception ignored) {}
        }

        clearReceiptFilters();
        return receipts;
    }

    // Reads a single receipt row into a JobStatus.
    private JobStatus readReceiptRow(WebElement row, String postingStatus) {
        List<WebElement> cells = row.findElements(By.xpath("./td"));
        Map<String, Integer> hdrs = headerIndexes(row);

        JobStatus status = new JobStatus();
        status.setJobName(POST_RECEIPTS_JOB);
        status.setStatus(firstNonBlank(cell(cells, hdrs, "status", "lms posting status"), postingStatus));
        status.setCurrentStatus(status.getStatus());
        status.setExecutionResult(status.getStatus());
        status.setAccountNumber(firstNonBlank(
            cell(cells, hdrs, "account number", "account no", "account"), cellText(cells, 0)));
        status.setReceiptNumber(firstNonBlank(
            cell(cells, hdrs, "receipt number", "receipt no", "receipt"), cellText(cells, 1)));
        status.setAmount(firstNonBlank(
            cell(cells, hdrs, "amount", "receipt amount"), cellText(cells, 2)));
        status.setBranch(firstNonBlank(
            cell(cells, hdrs, "branch", "branch name"), cellText(cells, 3)));
        status.setRemarks(cell(cells, hdrs, "remarks", "remark"));
        return status;
    }

    // Reads the error popup for a failed receipt row.
    private void readReceiptErrorPopup(WebElement row, JobStatus status) {
        WebElement icon = firstVisibleInRow(row, errorIconInRow);
        if (icon == null) {
            status.setErrorMessage(firstNonBlank(status.getErrorMessage(), "-"));
            return;
        }

        ActionDriver.globalSafeClick(driver, icon);
        WebElement popupEl = null;
        try {
            popupEl = new WebDriverWait(driver, Duration.ofSeconds(8))
                .until(ExpectedConditions.visibilityOfElementLocated(errorPopup));
        } catch (Exception ignored) {}

        String details = popupEl != null ? popupEl.getText() : "";
        status.setPopupDetails(details);
        status.setErrorMessage(firstNonBlank(readDetail(details, "Error Message", "Error"), status.getErrorMessage()));
        status.setFailureReason(firstNonBlank(readDetail(details, "Failure Reason", "Reason"), status.getFailureReason()));
        status.setRemarks(firstNonBlank(readDetail(details, "Remarks", "Remark"), status.getRemarks()));
        status.setLmsResponse(readDetail(details, "LMS Response", "Response"));

        closeModal();
    }

    // Opens the receipt filter panel.
    private void clickShowFilter() {
        try {
            WebElement showFilter = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(showFilterButton));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", showFilter);
            waitForUiStable();
        } catch (Exception ignored) {}
    }

    // Selects the LMS posting status filter.
    private void selectPostingStatus(String postingStatus) {
        List<WebElement> selects = driver.findElements(lmsPostingStatus);
        if (selects.isEmpty()) return;

        WebElement selectEl = null;
        for (WebElement s : selects) {
            try { if (s.isDisplayed()) { selectEl = s; break; } }
            catch (Exception ignored) {}
        }
        if (selectEl == null) return;

        Select select = new Select(selectEl);
        String targetValue = null;
        String targetText  = null;
        for (WebElement option : select.getOptions()) {
            String optText  = option.getText().trim();
            String optValue = option.getAttribute("value");
            if (optText.equalsIgnoreCase(postingStatus)
                    || (optValue != null && optValue.equalsIgnoreCase(postingStatus))) {
                targetValue = optValue;
                targetText  = optText;
                break;
            }
        }
        if (targetText == null) return;

        try {
            ((JavascriptExecutor) driver).executeScript(
                "var sel=arguments[0], val=arguments[1];"
                + "if(val!==null && val!==''){ sel.value=val; }"
                + "sel.dispatchEvent(new Event('change',{bubbles:true}));"
                + "sel.dispatchEvent(new Event('input',{bubbles:true}));"
                + "sel.dispatchEvent(new Event('ngModelChange',{bubbles:true}));",
                selectEl, targetValue);
            waitForUiStable();

            boolean selected = false;
            try {
                WebElement chosen = select.getFirstSelectedOption();
                selected = chosen != null
                    && targetText.equalsIgnoreCase(clean(chosen.getText()));
            } catch (Exception ignored) {}
            if (!selected) select.selectByVisibleText(targetText);
        } catch (Exception fallback) {
            try { select.selectByVisibleText(targetText); } catch (Exception ignored) {}
        }
        waitForUiStable();
    }

    // Selects today's date in the receipt date filter.
    private void selectTodayReceiptDate() {
        List<WebElement> inputs = driver.findElements(receiptDateInput);
        if (inputs.isEmpty() || !inputs.get(0).isDisplayed()) return;

        WebElement dateInput = inputs.get(0);
        scrollIntoView(dateInput);
        ActionDriver.globalSafeClick(driver, dateInput);

        try {
            WebElement today = new WebDriverWait(driver, Duration.ofSeconds(4))
                .until(ExpectedConditions.elementToBeClickable(By.xpath(
                    "//*[contains(@class,'mat-calendar') or contains(@class,'datepicker')"
                    + " or contains(@class,'calendar')]//button[normalize-space()='Today']"
                    + " | //button[normalize-space()='Today']")));
            ActionDriver.globalSafeClick(driver, today);
            waitForUiStable();
            return;
        } catch (Exception ignored) {}

        String todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].value = arguments[1];"
            + "arguments[0].dispatchEvent(new Event('input',  {bubbles:true}));"
            + "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));"
            + "arguments[0].dispatchEvent(new Event('blur',   {bubbles:true}));",
            dateInput, todayStr);
        waitForUiStable();
    }

    // Clears the receipt filters.
    private void clearReceiptFilters() {
        try {
            List<WebElement> clears = driver.findElements(clearButton);
            if (!clears.isEmpty() && clears.get(0).isDisplayed()) {
                ActionDriver.globalSafeClick(driver, clears.get(0));
                waitForUiStable();
            }
        } catch (Exception ignored) {}
    }

    // Opens the Jobs page from the Admin menu.
    private boolean tryOpenFromMenu() {
        try {
            ActionDriver.globalSafeClick(driver, clickable(adminMenuBtn));
            WebElement jobMenu = new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.elementToBeClickable(jobsMenu));
            ActionDriver.globalSafeClick(driver, jobMenu);
            return true;
        } catch (Exception ignored) {
            try {
                new Actions(driver).moveToElement(visible(adminMenuBtn)).perform();
                WebElement jobMenu = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(jobsMenu));
                ActionDriver.globalSafeClick(driver, jobMenu);
                return true;
            } catch (Exception innerIgnored) {
                return false;
            }
        }
    }

    // Navigates to the Jobs route directly via URL.
    private void openJobsRouteDirectly() {
        String currentUrl = driver.getCurrentUrl();
        String baseUrl = currentUrl.contains("#")
            ? currentUrl.substring(0, currentUrl.indexOf('#'))
            : currentUrl;
        driver.get(baseUrl + "#/admin/job");
        waitForPageLoad();
    }

    // Waits for the Jobs list page to load.
    private void waitForJobsPage() {
        new WebDriverWait(driver, Duration.ofSeconds(20)).until(d -> {
            String url  = d.getCurrentUrl().toLowerCase(Locale.ROOT);
            String body = d.findElement(By.tagName("body")).getText().toLowerCase(Locale.ROOT);
            return (url.contains("/admin/job") && !url.contains("/details"))
                || body.contains("cron expression")
                || body.contains("job state")
                || !d.findElements(jobListTable).isEmpty();
        });
    }

    // Waits for the Receipt page to load.
    private void waitForReceiptPage() {
        new WebDriverWait(driver, Duration.ofSeconds(20)).until(d -> {
            String body = d.findElement(By.tagName("body")).getText().toLowerCase(Locale.ROOT);
            return !d.findElements(lmsPostingStatus).isEmpty()
                || !d.findElements(showFilterButton).isEmpty()
                || body.contains("receipt")
                || body.contains("lms posting");
        });
        waitForUiStable();
    }

    // Waits for the Job Detail page to load.
    private void waitForJobDetailPage() {
        new WebDriverWait(driver, Duration.ofSeconds(20)).until(d -> {
            String url  = d.getCurrentUrl().toLowerCase(Locale.ROOT);
            String body = d.findElement(By.tagName("body")).getText().toLowerCase(Locale.ROOT);
            return url.contains("/admin/job/details")
                || url.contains("/admin/job/detail")
                || body.contains("job history")
                || body.contains("collection_items")
                || body.contains("up_coming_demand")
                || body.contains("post_receipt")
                || (body.contains("start date") && body.contains("actions"));
        });

        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//tbody/tr[1]")));
        } catch (Exception ignored) {}

        waitForUiStable();
    }

    // Waits for the execution details view to render.
    private void waitForViewModal() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(12))
                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                    "//*[contains(normalize-space(),'View -')]"
                    + " | //*[normalize-space()='Basic']"
                    + " | //*[contains(normalize-space(),'End Date')]"
                    + " | //*[contains(normalize-space(),'End Time')]")));
            waitForUiStable();
        } catch (Exception ignored) {}
    }

    // Waits for approximately the given milliseconds without Thread.sleep.
    private void waitMillis(long millis) {
        try {
            long start = System.currentTimeMillis();
            new WebDriverWait(driver, Duration.ofMillis(Math.max(1, millis)))
                .until(d -> System.currentTimeMillis() - start >= millis);
        } catch (Exception ignored) {}
    }

    // Ensures the driver is back on the Jobs list page.
    private void ensureOnJobsListPage() {
        String url = driver.getCurrentUrl().toLowerCase(Locale.ROOT);
        if (url.contains("/admin/job") && !url.contains("/details") && !url.contains("/detail")) {
            try {
                waitForJobsPage();
                if (!driver.findElements(jobListTable).isEmpty()) {
                    if (!driver.findElements(activeModal).isEmpty()
                            || !driver.findElements(modalCloseBtn).isEmpty()) {
                        closeModal();
                    }
                    System.out.println("Returned to Jobs page");
                    return;
                }
            } catch (Exception ignored) {}
        }
        openJobsRouteDirectly();
        waitForJobsPage();
        System.out.println("Returned to Jobs page");
    }

    // Navigates back to the Jobs list page.
    private void navigateBackToJobsList() {
        openJobsRouteDirectly();
        waitForJobsPage();
    }

    // Finds the job row matching the given job name.
    private WebElement findJobRow(String jobName) {
        waitForJobsPage();

        try {
            new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.presenceOfElementLocated(jobListRows));
        } catch (Exception ignored) {}

        System.out.println("Found Job Row: " + jobName);

        for (String alias : JOB_ALIASES.getOrDefault(jobName, List.of(jobName))) {
            try {
                WebElement row = new WebDriverWait(driver, Duration.ofSeconds(8))
                    .until(d -> firstMatchingJobListRow(alias));
                if (row != null) return row;
            } catch (Exception ignored) {}
        }
        throw new IllegalStateException("Job row not found: " + jobName);
    }

    // Finds the first job list row matching the given job name.
    private WebElement firstMatchingJobListRow(String jobName) {
        String expected = normalizeForMatch(jobName);
        for (WebElement row : driver.findElements(jobListRows)) {
            try {
                if (row.isDisplayed() && normalizeForMatch(row.getText()).contains(expected))
                    return row;
            } catch (Exception ignored) {}
        }
        return null;
    }

    // Finds the first visible element matching the locator within a row.
    private WebElement firstVisibleInRow(WebElement row, By locator) {
        try {
            for (WebElement el : row.findElements(locator)) {
                if (el.isDisplayed()) return el;
            }
        } catch (Exception ignored) {}
        return null;
    }

    // Reads execution details from the job details view.
    private void readModalDetails(JobStatus status) {
        try {
            final WebElement modalEl = waitForExecutionModal();
            if (modalEl == null) return;

            WebElement scrollable = findScrollableContainer(modalEl);

            scrollContainerTo(scrollable, "bottom");
            waitForLabelsVisible(modalEl, "Status", "Execution Status", "Result",
                "Failure Reason", "Reason", "Error Message", "Error");
            String uiStatus = readField(modalEl, scrollable, "Status", "Execution Status", "Result");
            String uiReason = readField(modalEl, scrollable,
                "Failure Reason", "Reason", "Error Message", "Error");

            scrollContainerTo(scrollable, "top");
            waitForLabelsVisible(modalEl, "Start Date", "End Date");
            String uiStartDate = readField(modalEl, scrollable, "Start Date");
            String uiStartTime = readField(modalEl, scrollable, "Start Time");
            String uiEndDate   = readField(modalEl, scrollable, "End Date");
            String uiEndTime   = readField(modalEl, scrollable, "End Time");

            if (!uiStatus.isBlank()) {
                status.setStatus(normalizeStatus(uiStatus));
                status.setCurrentStatus(status.getStatus());
            }
            if (!uiStartDate.isBlank()) status.setStartDate(uiStartDate);
            if (!uiStartTime.isBlank()) status.setStartTime(uiStartTime);
            if (!uiEndDate.isBlank())   status.setEndDate(uiEndDate);
            if (!uiEndTime.isBlank())   status.setEndTime(uiEndTime);
            if (!uiReason.isBlank())    status.setFailureReason(uiReason);

            status.setExecutionResult(firstNonBlank(status.getExecutionResult(), status.getStatus()));
        } catch (Exception ignored) {}
    }

    // Waits for the execution details view to appear.
    private WebElement waitForExecutionModal() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(12))
                .until(ExpectedConditions.visibilityOfElementLocated(jobDetailsViewLocator));
            return findExecutionModal();
        } catch (Exception ignored) {
            return null;
        }
    }

    // Locates the execution details view.
    private WebElement findExecutionModal() {
        try {
            for (WebElement el : driver.findElements(jobDetailsViewLocator)) {
                if (el.isDisplayed()) return el;
            }
        } catch (Exception ignored) {}
        return null;
    }

    // Waits until at least one of the given field labels is visible.
    private void waitForLabelsVisible(WebElement modal, String... labels) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(6)).until(d -> {
                for (String label : labels) {
                    try {
                        List<WebElement> els = modal.findElements(
                            By.xpath(".//*[normalize-space()='" + label + "']"));
                        for (WebElement el : els) {
                            if (el.isDisplayed()) return true;
                        }
                    } catch (Exception ignored) {}
                }
                return false;
            });
        } catch (Exception ignored) {}
    }

    // Reads a labelled field's value from the modal.
    private String readField(WebElement modal, WebElement scrollable, String... labels) {
        for (String label : labels) {
            try {
                List<WebElement> labelEls = modal.findElements(
                        By.xpath(".//*[normalize-space(text())='" + label + "']"));
                WebElement chosen = null;
                for (WebElement el : labelEls) {
                    if (el.isDisplayed()) chosen = el;
                }
                if (chosen != null) {
                    scrollLabelIntoView(chosen);
                    final WebElement labelEl = chosen;
                    try {
                        new WebDriverWait(driver, Duration.ofSeconds(3))
                            .until(d -> !valueOfLabel(labelEl).isBlank());
                    } catch (Exception ignored) {}
                    String v = valueOfLabel(chosen);
                    if (!v.isBlank()) return v;
                }
            } catch (Exception ignored) {}
        }
        return "";
    }

    // Extracts a single value for the given label element.
    private String valueOfLabel(WebElement labelEl) {
        String labelText = clean(labelEl.getText());
        try {
            WebElement sib = labelEl.findElement(By.xpath("following-sibling::*[1]"));
            String v = clean(sib.getText());
            if (!v.isBlank() && !v.equalsIgnoreCase(labelText)) return v;
        } catch (Exception ignored) {}
        try {
            WebElement parent = labelEl.findElement(By.xpath(".."));
            for (WebElement k : parent.findElements(By.xpath("*"))) {
                String v = clean(k.getText());
                if (!v.isBlank() && !v.equalsIgnoreCase(labelText)) return v;
            }
        } catch (Exception ignored) {}
        try {
            for (WebElement n : labelEl.findElements(
                    By.xpath("following::node()[position()<5]"))) {
                String t = clean(n.getText());
                if (!t.isBlank() && !t.equalsIgnoreCase(labelText)) return t;
            }
        } catch (Exception ignored) {}
        return "";
    }

    // Extracts a labelled field's value using sibling/parent traversal.
    private String getModalFieldValue(WebElement labelEl) {
        String labelText = clean(labelEl.getText());
        try {
            WebElement sib = labelEl.findElement(By.xpath("following-sibling::*[1]"));
            String v = clean(sib.getText());
            if (!v.isBlank() && !v.equalsIgnoreCase(labelText)) return v;
        } catch (Exception ignored) {}
        try {
            WebElement parent = labelEl.findElement(By.xpath(".."));
            for (WebElement k : parent.findElements(By.xpath("*"))) {
                String v = clean(k.getText());
                if (!v.isBlank() && !v.equalsIgnoreCase(labelText)) return v;
            }
        } catch (Exception ignored) {}
        return "";
    }

    // Scrolls a label element into view.
    private void scrollLabelIntoView(WebElement labelEl) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center', inline:'nearest'});", labelEl);
        } catch (Exception ignored) {}
    }

    // Finds the scrollable execution details container, waiting for content to render.
    private WebElement findScrollableContainer(WebElement modal) {
        WebElement target = null;
        try {
            List<WebElement> containers = modal.findElements(
                By.xpath(".//div[contains(@class,'overflow-auto')]"));
            for (WebElement c : containers) {
                if (c.isDisplayed()) { target = c; break; }
            }
        } catch (Exception ignored) {}

        if (target == null) target = modal;

        final WebElement container = target;
        try {
            new WebDriverWait(driver, Duration.ofSeconds(8)).until(d -> isScrollable(container));
            return container;
        } catch (Exception ignored) {}

        WebElement deep = findDeepestScrollable(modal);
        return deep != null ? deep : container;
    }

    // Falls back to the descendant with the greatest scrollable overflow.
    private WebElement findDeepestScrollable(WebElement modal) {
        WebElement best = null;
        long bestOverflow = 0;
        try {
            for (WebElement c : modal.findElements(By.xpath(".//*"))) {
                try {
                    long overflow = (Long) ((JavascriptExecutor) driver).executeScript(
                        "var e=arguments[0]; return e.scrollHeight - e.clientHeight;", c);
                    if (overflow > bestOverflow) { bestOverflow = overflow; best = c; }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return (best != null && bestOverflow > 2) ? best : null;
    }

    // Checks whether an element is scrollable.
    private boolean isScrollable(WebElement el) {
        try {
            return (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var e=arguments[0]; return e.scrollHeight > e.clientHeight + 2;", el);
        } catch (Exception ignored) { return false; }
    }

    // Scrolls the given container to top or bottom.
    private void scrollContainerTo(WebElement container, String position) {
        try {
            if ("bottom".equals(position)) {
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollTop = arguments[0].scrollHeight;", container);
            } else {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollTop = 0;", container);
            }
            Long scrollTop = (Long) ((JavascriptExecutor) driver)
                .executeScript("return Math.round(arguments[0].scrollTop);", container);
            System.out.println("Scrolled execution details to " + position + ", scrollTop=" + scrollTop);
        } catch (Exception ignored) {}
    }

    // Normalizes a raw status string into a canonical status value.
    private String normalizeStatus(String raw) {
        if (raw == null) return "";
        String s = clean(raw).toUpperCase(Locale.ROOT);
        if (s.contains("SUCCESS") || s.contains("COMPLET")) return "SUCCESSFUL";
        if (s.contains("FAIL") || s.contains("ERROR"))    return "FAILED";
        if (s.contains("RUNN"))                           return "RUNNING";
        if (s.contains("PEND"))                           return "PENDING";
        if (s.contains("SCHED"))                          return "SCHEDULED";
        if (s.contains("ACTIV"))                          return "ACTIVE";
        if (s.contains("INACTIV"))                        return "INACTIVE";
        String[] parts = s.split("\\s+");
        return parts.length > 0 ? parts[0] : raw;
    }

    // Applies parsed detail text onto a JobStatus.
    private void applyDetails(String details, JobStatus status) {
        status.setJobName(firstNonBlank(readDetail(details, "Job Name"), status.getJobName()));
        status.setJobType(firstNonBlank(readDetail(details, "Job Type"), status.getJobType()));
        status.setStartDate(firstNonBlank(readDetail(details, "Start Date"), status.getStartDate()));
        status.setEndDate(firstNonBlank(readDetail(details, "End Date"), status.getEndDate()));
        status.setStartTime(firstNonBlank(readDetail(details, "Start Time"), status.getStartTime()));
        status.setEndTime(firstNonBlank(readDetail(details, "End Time"), status.getEndTime()));
        status.setDuration(firstNonBlank(readDetail(details, "Duration", "Time Duration"), status.getDuration()));
        status.setNextFireTime(firstNonBlank(readDetail(details, "Next Fire Time"), status.getNextFireTime()));
        String modalStatus = readDetail(details, "Status", "Execution Status", "Result");
        if (modalStatus == null) modalStatus = "";
        modalStatus = modalStatus.trim();
        status.setStatus(firstNonBlank(modalStatus, "NOT_CAPTURED"));
        status.setCurrentStatus(status.getStatus());
        status.setExecutionResult(firstNonBlank(status.getExecutionResult(), status.getStatus()));

        String failReason = readDetail(details, "Failure Reason", "Reason", "Error Message", "Error");
        if (!failReason.isBlank()) status.setFailureReason(failReason);
    }

    // Reads the status value from a job list row.
    private String readRowStatus(WebElement row) {
        List<WebElement> cells = row.findElements(By.xpath("./td"));
        return firstNonBlank(
            cell(cells, headerIndexes(row), "status", "job state", "state"),
            findStatus(cells));
    }

    // Finds a status-like value among a row's cells.
    private String findStatus(List<WebElement> cells) {
        for (WebElement cell : cells) {
            String n = normalizeForMatch(cell.getText());
            if (n.matches(
                ".*\\b(success|successful|completed|complete|failed|failure|pending|running|scheduled|active|inactive)\\b.*"))
                return cell.getText().trim();
        }
        return "";
    }

    // Extracts a labelled value from raw text using known label boundaries.
    private String readDetail(String source, String... labels) {
        String bodyText = clean(source);
        if (bodyText.isBlank()) return "";

        List<String> knownLabels = List.of(
            "Job Name", "Job Type", "Start Date", "End Date",
            "Start Time", "End Time", "Duration", "Time Duration",
            "Next Fire Time", "Status", "Execution Status", "Result",
            "Error Message", "Error", "Failure Reason", "Reason",
            "Remarks", "Remark", "LMS Response", "Response");

        for (String label : labels) {
            int start = bodyText.toLowerCase(Locale.ROOT).indexOf(label.toLowerCase(Locale.ROOT));
            if (start < 0) continue;

            int valueStart = start + label.length();
            int valueEnd   = bodyText.length();

            for (String nextLabel : knownLabels) {
                if (nextLabel.equalsIgnoreCase(label)) continue;
                int next = bodyText.toLowerCase(Locale.ROOT)
                    .indexOf(nextLabel.toLowerCase(Locale.ROOT), valueStart);
                if (next >= valueStart && next < valueEnd) valueEnd = next;
            }

            String value = clean(bodyText.substring(valueStart, valueEnd)
                .replaceFirst("^[\\-\\s]+", ""));
            if (!value.isBlank()) return value;
        }
        return "";
    }

    // Builds a map of table header names to column indexes.
    private Map<String, Integer> headerIndexes(WebElement row) {
        Map<String, Integer> indexes = new LinkedHashMap<>();
        try {
            List<WebElement> headers = row.findElements(
                By.xpath("./ancestor::table[1]//thead//th"));
            for (int i = 0; i < headers.size(); i++)
                indexes.put(normalizeHeader(headers.get(i).getText()), i);
        } catch (Exception ignored) {}
        return indexes;
    }

    // Reads a cell value by header name.
    private String cell(List<WebElement> cells, Map<String, Integer> headers, String... names) {
        for (String name : names) {
            String expected = normalizeHeader(name);
            for (Map.Entry<String, Integer> entry : headers.entrySet()) {
                if ((entry.getKey().equals(expected) || entry.getKey().contains(expected))
                        && entry.getValue() >= 0 && entry.getValue() < cells.size())
                    return text(cells.get(entry.getValue()));
            }
        }
        return "";
    }

    // Reads a cell value by index.
    private String cellText(List<WebElement> cells, int index) {
        return (index >= 0 && index < cells.size()) ? text(cells.get(index)) : "";
    }

    // Joins receipt reference numbers into a single string.
    private String joinReceiptRefs(List<JobStatus> receipts) {
        List<String> refs = new ArrayList<>();
        for (JobStatus r : receipts)
            refs.add(firstNonBlank(r.getReceiptNumber(), r.getAccountNumber()));
        return String.join(", ", refs);
    }

    // Returns the first non-blank value, cleaned of extra whitespace.
    private String firstNonBlank(String... values) {
        for (String v : values)
            if (v != null && !v.trim().isEmpty()) return clean(v);
        return "";
    }

    // Reads and cleans the text of an element.
    private String text(WebElement element) {
        try { return clean(element.getText()); }
        catch (Exception ignored) { return ""; }
    }

    // Collapses whitespace and trims a string.
    private String clean(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }

    // Normalizes a header name for comparison.
    private String normalizeHeader(String value) {
        return clean(value).toLowerCase(Locale.ROOT).replace(":", "");
    }

    // Normalizes text for loose matching.
    private String normalizeForMatch(String value) {
        return clean(value).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
    }

    // Builds a short error message from an exception.
    private String shortError(Exception e) {
        if (e == null) return "";
        String msg = clean(e.getMessage());
        if (msg.length() > 160) msg = msg.substring(0, 157) + "...";
        return firstNonBlank(msg, e.getClass().getSimpleName());
    }

    // Waits until the modal text stabilises or timeout is reached.
    private void waitForModalTextStability(WebElement modal, Duration timeout) {
        try {
            final int[] stableCount = {0};
            final int[] lastLen = {-1};
            new WebDriverWait(driver, timeout)
                .pollingEvery(Duration.ofMillis(300))
                .until(d -> {
                    String txt = "";
                    try { txt = modal.getText(); } catch (Exception ignored) {}
                    int len = txt == null ? 0 : txt.length();
                    if (len == lastLen[0]) stableCount[0]++; else { stableCount[0] = 0; lastLen[0] = len; }
                    return stableCount[0] >= 3;
                });

            long minMillis = 400;
            long start = System.currentTimeMillis();
            if (minMillis > 0) {
                new WebDriverWait(driver, Duration.ofMillis(minMillis))
                    .until(d -> System.currentTimeMillis() - start >= minMillis);
            }
        } catch (Exception ignored) {}
    }

    // Scrolls the modal to the first occurrence of any of the given labels.
    private void scrollToModalSection(WebElement modal, String... labels) {
        try {
            for (String label : labels) {
                try {
                    List<WebElement> els = modal.findElements(By.xpath(
                        ".//*[normalize-space()='" + label + "']"));
                    for (WebElement el : els) {
                        if (el.isDisplayed()) {
                            ((JavascriptExecutor) driver).executeScript(
                                "arguments[0].scrollIntoView({block:'center'});", el);
                            try { waitMillis(150); } catch (Exception ignored) {}
                            return;
                        }
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

    // Extracts a label/value pair from the modal using DOM traversal.
    private String getModalLabelValue(WebElement modal, String... labels) {
        try {
            for (String label : labels) {
                try {
                    List<WebElement> els = modal.findElements(By.xpath(
                        ".//*[normalize-space(text())='" + label + "']/following-sibling::*[1]"));
                    for (WebElement el : els) {
                        if (el.isDisplayed()) return text(el);
                    }

                    List<WebElement> parents = modal.findElements(By.xpath(
                        ".//*[normalize-space(text())='" + label + "']/parent::*"));
                    for (WebElement p : parents) {
                        try {
                            for (WebElement child : p.findElements(By.xpath("*"))) {
                                if (!child.getText().trim().equalsIgnoreCase(label) && child.isDisplayed())
                                    return text(child);
                            }
                        } catch (Exception ignored) {}
                    }

                    List<WebElement> near = modal.findElements(By.xpath(
                        ".//*[contains(normalize-space(text()), '" + label + "')]/following::node()[position()<4]"));
                    for (WebElement n : near) {
                        try { String t = text(n); if (!t.isBlank() && !t.equalsIgnoreCase(label)) return t; } catch (Exception ignored) {}
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return "";
    }
}