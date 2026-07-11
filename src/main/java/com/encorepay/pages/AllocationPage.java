package com.encorepay.pages;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AllocationPage extends BasePage {

    public enum AllocationType {
        CUSTOMER,
        COLLECTION_ITEM
    }

    private final By rowCheckbox         = By.xpath("//button[.//span[contains(text(),'check_box')]]");
    private final By closePanel          = By.xpath("//button[contains(@class,'mat-mdc-icon-button')]");
    private final By successToast        = By.xpath("//*[contains(text(),'Allocated')]");
    private final By failureToast        = By.xpath("//*[contains(text(),'failed') or contains(text(),'already')]");
    private final By employeeRadioInput  = By.xpath("//input[@name='userType' and @value='EMPLOYEE']");
    private final By agentRadioInput     = By.xpath("//input[@name='userType' and @value='AGENT']");
    private final By allocateCustomerInput = By.xpath("//input[@name='allocateTaskToCustomer' and @value='true']");
    private final By allocateCollectionItemInput = By.xpath("//input[@name='allocateTaskToCustomer' and @value='false']");
    private final By addAllocationButton = By.xpath(
        "//button[normalize-space()='+ Add'"
            + " or contains(normalize-space(),'+ Add')"
            + " or normalize-space()='Add'"
            + " or contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'add')]");
    private final By submitAllocationButton = By.xpath(
        "//button[not(contains(normalize-space(),'Bulk'))]"
            + "[normalize-space()='Allocate' or .//span[normalize-space()='Allocate']]");
    private final By employeeRadioOption = By.xpath(
        "//*[self::label or self::mat-radio-button or contains(@class,'mat-mdc-radio-button') or contains(@class,'mat-radio-button')]"
            + "[.//input[@name='userType' and @value='EMPLOYEE'] or contains(normalize-space(.),'Employee')]");
    private final By agentRadioOption = By.xpath(
        "//*[self::label or self::mat-radio-button or contains(@class,'mat-mdc-radio-button') or contains(@class,'mat-radio-button')]"
            + "[.//input[@name='userType' and @value='AGENT'] or contains(normalize-space(.),'Agent')]");
    private final By allocateCustomerOption = By.xpath(
        "//*[self::label or self::mat-radio-button or contains(@class,'mat-mdc-radio-button') or contains(@class,'mat-radio-button')]"
            + "[.//input[@name='allocateTaskToCustomer' and @value='true'] or contains(normalize-space(.),'Customer')]");
    private final By allocateCollectionItemOption = By.xpath(
        "//*[self::label or self::mat-radio-button or contains(@class,'mat-mdc-radio-button') or contains(@class,'mat-radio-button')]"
            + "[.//input[@name='allocateTaskToCustomer' and @value='false'] or contains(normalize-space(.),'Collection Item')]");

    @FindBy(xpath = "(//button[contains(.,'Actions')])[1]")
    private WebElement actionsButton;

    @FindBy(xpath = "//span[normalize-space()='Allocate']")
    private WebElement allocateMenu;

    @FindBy(xpath = "//button[contains(.,'Bulk Allocate')]")
    private WebElement bulkAllocateButton;

    @FindBy(xpath = "//select[@name='userId']")
    private WebElement employeeDropdown;

    @FindBy(xpath = "//button[contains(.,'Allocate')]")
    private WebElement finalAllocateButton;

    private final By nativeDropdowns = By.xpath("//select[contains(@class,'input') or @name='userId']");

    public AllocationPage(WebDriver driver) {
        super(driver);
    }

    public void performSingleAllocation(String employeeName, AllocationType allocationType) {
        click(actionsButton);
        click(allocateMenu);
        completeAllocation(employeeName, allocationType);
    }

    public void performBulkAllocation(String employeeName, AllocationType allocationType, int rowCount) {
        selectRowCheckboxes(rowCount);
        click(bulkAllocateButton);
        completeAllocation(employeeName, allocationType);
    }

    public void waitForAllocationPage() {
        waitForOverlayToDisappear();
        By actionsBy = By.xpath("(//button[contains(.,'Actions')])[1]");
        WebDriverWait allocationWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        allocationWait.until(d -> {
            String url = d.getCurrentUrl().toLowerCase(java.util.Locale.ROOT);
            try {
                String body = d.findElement(By.tagName("body")).getText().toLowerCase(java.util.Locale.ROOT);
                return url.contains("allocation")
                        || body.contains("allocate")
                        || countVisibleElements(rowCheckbox) > 0
                        || isDisplayed(actionsBy);
            } catch (Exception e) {
                return false;
            }
        });
    }

    public boolean hasRows() {
        return countVisibleElements(rowCheckbox) > 0;
    }

    public boolean hasAtLeastRows(int expectedCount) {
        return countVisibleElements(rowCheckbox) >= expectedCount;
    }

    public int getDynamicBulkRowCount(int preferredMaximum) {
        List<WebElement> visibleCheckboxes = getVisibleElements(rowCheckbox);
        int dataRowCount = Math.max(0, visibleCheckboxes.size() - 1);
        if (dataRowCount == 0) {
            throw new IllegalStateException("No rows are available for bulk allocation.");
        }
        return Math.min(Math.max(1, preferredMaximum), dataRowCount);
    }

    public void selectEmployeeRadio() {
        clickRadioOption(employeeRadioInput, employeeRadioOption, "Employee");
        visible(employeeDropdown);
    }

    public void selectAgentRadio() {
        clickRadioOption(agentRadioInput, agentRadioOption, "Agent");
        visible(employeeDropdown);
    }

    public void selectAllocationType(AllocationType allocationType) {
        if (allocationType == AllocationType.CUSTOMER) {
            clickRadioOption(allocateCustomerInput, allocateCustomerOption, "Customer");
        } else {
            clickRadioOption(allocateCollectionItemInput, allocateCollectionItemOption, "Collection Item");
        }
    }

    public void selectRole() {
        List<WebElement> visibleDropdowns = getVisibleElements(nativeDropdowns);
        WebElement roleDropdown = null;
        for (WebElement dropdown : visibleDropdowns) {
            try {
                if (!"userId".equalsIgnoreCase(dropdown.getAttribute("name"))) {
                    roleDropdown = dropdown;
                    break;
                }
            } catch (Exception ignored) {
            }
        }
        if (roleDropdown == null && visibleDropdowns.size() >= 2) {
            roleDropdown = visibleDropdowns.get(0);
        }
        if (roleDropdown == null) {
            return;
        }
        Select select = new Select(clickable(roleDropdown));
        wait.until(d -> select.getOptions().size() > 1);

        for (WebElement option : select.getOptions()) {
            String text = normalize(option.getText());
            if (!text.isBlank()
                    && !text.toLowerCase().contains("choose")
                    && !text.toLowerCase().contains("select")
                    && !text.startsWith("--")) {
                select.selectByVisibleText(option.getText());
                triggerDropdownChange(roleDropdown);
                return;
            }
        }
    }

    public void selectEmployeeNew(String employeeName) {
        WebElement empDropdown = waitForEmployeeDropdown();
        Select select = new Select(clickable(empDropdown));
        wait.until(d -> select.getOptions().size() > 1);

        List<EmployeeChoice> candidates = collectEmployeeCandidates(select, employeeName);
        for (EmployeeChoice candidate : candidates) {
            if (trySelectEmployeeCandidate(candidate)) {
                System.out.println("[INFO] Allocation employee selected: " + candidate.text);
                return;
            }
        }

        throw new IllegalStateException(
                "No branch-compatible allocation user could be selected. Options: " + describeOptions(select));
    }

    private void completeAllocation(String employeeName, AllocationType allocationType) {
        waitForSidePanel();
        selectUserType(employeeName);
        selectAllocationType(allocationType);
        selectRole();
        selectEmployeeNew(employeeName);
        clickOptional(addAllocationButton);
        clickSubmitAllocationButton();
        handleAllocationResult();
    }

    private void selectUserType(String employeeName) {
        String requestedUser = normalize(employeeName).toLowerCase();
        if (requestedUser.contains("agent") && !driver.findElements(agentRadioInput).isEmpty()) {
            selectAgentRadio();
            return;
        }

        selectEmployeeRadio();
    }

    private void handleAllocationResult() {
        WebDriverWait resultWait = new WebDriverWait(driver, Duration.ofSeconds(8));
        resultWait.pollingEvery(Duration.ofMillis(100));

        try {
            WebElement toastElement = resultWait.until(d -> {
                List<WebElement> success = d.findElements(successToast);
                List<WebElement> failure = d.findElements(failureToast);
                try {
                    if (!success.isEmpty() && success.get(0).isDisplayed()) return success.get(0);
                } catch (Exception ignored) {
                }
                try {
                    if (!failure.isEmpty() && failure.get(0).isDisplayed()) return failure.get(0);
                } catch (Exception ignored) {
                }
                return null;
            });

            String msg = normalize(toastElement.getText());
            System.out.println("ALLOCATION RESULT: " + msg);

            if (msg.toLowerCase().contains("success") || msg.toLowerCase().contains("allocated")) {
                System.out.println("Allocation succeeded.");
            } else {
                System.out.println("Allocation failed. Message: " + msg);
            }

            resultWait.until(ExpectedConditions.invisibilityOf(toastElement));
            waitForAllocationCompletion();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Allocation result toast not captured within the timeout. Cause: " + e.getMessage(), e);
        }
    }

    private void waitForSidePanel() {
        try {
            visible(employeeDropdown);
        } catch (Exception ignored) {
            visible(finalAllocateButton);
        }
    }

    private void ensurePanelClosed() {
        try {
            wait.until(ExpectedConditions.invisibilityOf(employeeDropdown));
        } catch (Exception e) {
            if (isDisplayed(closePanel)) {
                click(closePanel);
                wait.until(ExpectedConditions.invisibilityOf(employeeDropdown));
            } else {
                System.out.println("[WARN] Allocation panel did not close automatically: " + e.getMessage());
            }
        }
    }

    private void waitForAllocationCompletion() {
        ensurePanelClosed();
        visible(actionsButton);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOf(bulkAllocateButton),
                ExpectedConditions.visibilityOfElementLocated(rowCheckbox)));
    }

    private void selectRowCheckboxes(int requestedCount) {
        waitForAllocationPage();
        List<WebElement> visibleCheckboxes = getVisibleElements(rowCheckbox);

        List<WebElement> rowLevelCheckboxes = visibleCheckboxes.size() > 1
                ? new ArrayList<>(visibleCheckboxes.subList(1, visibleCheckboxes.size()))
                : visibleCheckboxes;

        if (rowLevelCheckboxes.isEmpty()) {
            throw new IllegalStateException("No allocation rows are available to select.");
        }

        int selectedCount = 0;
        for (WebElement checkbox : rowLevelCheckboxes) {
            if (selectedCount >= requestedCount) break;
            click(checkbox);
            selectedCount++;
        }

        if (selectedCount < requestedCount) {
            throw new IllegalStateException(
                    "Requested " + requestedCount + " rows but only " + selectedCount + " were selectable.");
        }
    }

    private void triggerDropdownChange(WebElement dropdown) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('input',  {bubbles:true}));"
                + "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                dropdown);
    }

    private boolean isPlaceholderOption(String selectedText) {
        return selectedText.isBlank()
                || selectedText.toLowerCase().contains("choose")
                || selectedText.toLowerCase().contains("select")
                || selectedText.startsWith("--");
    }

    private void selectEmployeeWithKeyboard(WebElement dropdown, int optionIndex) {
        scrollIntoView(dropdown);
        dropdown.click();
        int steps = Math.max(1, optionIndex);
        for (int i = 0; i < steps; i++) {
            dropdown.sendKeys(Keys.ARROW_DOWN);
        }
        dropdown.sendKeys(Keys.ENTER);
        triggerDropdownChange(dropdown);
        waitForUiStable();
    }

    private void selectByValueWithEvents(WebElement dropdown, String value) {
        ((JavascriptExecutor) driver).executeScript(
                "const select = arguments[0];"
                    + "const value = arguments[1];"
                    + "select.value = value;"
                    + "Array.from(select.options).forEach(option => option.selected = option.value === value);"
                    + "select.dispatchEvent(new Event('input', {bubbles:true}));"
                    + "select.dispatchEvent(new Event('change', {bubbles:true}));"
                    + "select.dispatchEvent(new Event('blur', {bubbles:true}));",
                dropdown,
                value);
    }

    private List<EmployeeChoice> collectEmployeeCandidates(Select select, String preferredEmployeeName) {
        List<EmployeeChoice> preferred = new ArrayList<>();
        List<EmployeeChoice> fallback = new ArrayList<>();
        String preferredName = normalize(preferredEmployeeName).toLowerCase();

        int optionIndex = 0;
        for (WebElement option : select.getOptions()) {
            String text = normalize(option.getText());
            if (isPlaceholderOption(text)) {
                optionIndex++;
                continue;
            }

            EmployeeChoice choice = new EmployeeChoice(optionIndex, text, option.getAttribute("value"));
            if (!preferredName.isBlank()
                    && (text.equalsIgnoreCase(preferredName) || text.toLowerCase().contains(preferredName))) {
                preferred.add(choice);
            } else {
                fallback.add(choice);
            }
            optionIndex++;
        }

        preferred.addAll(fallback);
        return preferred;
    }

    private boolean trySelectEmployeeCandidate(EmployeeChoice candidate) {
        WebElement dropdown = waitForEmployeeDropdown();
        Select select = new Select(clickable(dropdown));
        try {
            if (candidate.value != null && !candidate.value.isBlank()) {
                select.selectByValue(candidate.value);
                triggerDropdownChange(dropdown);
                waitForUiStable();
                if (isSelectedEmployeeAccepted(dropdown, candidate)) {
                    return true;
                }

                selectByValueWithEvents(dropdown, candidate.value);
            } else {
                select.selectByIndex(candidate.index);
                triggerDropdownChange(dropdown);
            }

            waitForUiStable();
            if (isSelectedEmployeeAccepted(dropdown, candidate)) {
                return true;
            }

            selectEmployeeWithKeyboard(dropdown, candidate.index);
            return isSelectedEmployeeAccepted(dropdown, candidate);
        } catch (Exception e) {
            System.out.println("[WARN] Skipping allocation user '" + candidate.text + "': " + e.getMessage());
            return false;
        }
    }

    private boolean isSelectedEmployeeAccepted(WebElement dropdown, EmployeeChoice candidate) {
        Select select = new Select(dropdown);
        String selectedText = normalize(select.getFirstSelectedOption().getText());
        String selectedValue = dropdown.getAttribute("value");
        if (isPlaceholderOption(selectedText)) {
            return false;
        }

        if (candidate.value != null && !candidate.value.isBlank() && !candidate.value.equals(selectedValue)) {
            return false;
        }

        clickOptional(addAllocationButton);
        WebElement submitButton = visible(submitAllocationButton);
        return submitButton != null && submitButton.isEnabled();
    }

    private static class EmployeeChoice {
        private final int index;
        private final String text;
        private final String value;

        private EmployeeChoice(int index, String text, String value) {
            this.index = index;
            this.text = text;
            this.value = value;
        }
    }

    private String describeOptions(Select select) {
        StringBuilder options = new StringBuilder("[");
        for (WebElement option : select.getOptions()) {
            options.append("{text='")
                .append(normalize(option.getText()))
                .append("', value='")
                .append(option.getAttribute("value"))
                .append("'}, ");
        }
        options.append("]");
        return options.toString();
    }

    private void clickRadioOption(By inputLocator, By visibleOptionLocator, String label) {
        wait.until(d -> !d.findElements(inputLocator).isEmpty() || findVisibleElement(visibleOptionLocator) != null);

        WebElement input = driver.findElements(inputLocator).isEmpty()
                ? null
                : driver.findElements(inputLocator).get(0);
        if (input != null && input.isSelected()) {
            return;
        }

        WebElement visibleOption = findVisibleElement(visibleOptionLocator);
        if (visibleOption != null) {
            scrollIntoView(visibleOption);
            try {
                click(visibleOption);
            } catch (Exception e) {
                jsClick(visibleOption);
            }
            waitForUiStable();
            if (isInputSelected(inputLocator)) {
                return;
            }
        }

        if (input != null) {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].checked = true;"
                    + "arguments[0].dispatchEvent(new Event('input', {bubbles:true}));"
                    + "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));"
                    + "arguments[0].click();",
                    input);
            waitForUiStable();
            return;
        }

        throw new IllegalStateException("Unable to select allocation radio option: " + label);
    }

    private boolean isInputSelected(By inputLocator) {
        try {
            List<WebElement> inputs = driver.findElements(inputLocator);
            return !inputs.isEmpty() && inputs.get(0).isSelected();
        } catch (Exception ignored) {
            return false;
        }
    }

    private WebElement waitForEmployeeDropdown() {
        return wait.until(d -> {
            List<WebElement> visibleDropdowns = getVisibleElements(nativeDropdowns);

            for (WebElement dropdown : visibleDropdowns) {
                try {
                    if ("userId".equalsIgnoreCase(dropdown.getAttribute("name"))) {
                        return dropdown;
                    }
                } catch (Exception ignored) {
                }
            }

            if (visibleDropdowns.size() >= 2) {
                return visibleDropdowns.get(1);
            }

            if (visibleDropdowns.size() == 1) {
                return visibleDropdowns.get(0);
            }

            return null;
        });
    }

    private void clickOptional(By locator) {
        WebElement element = findVisibleElement(locator);
        if (element == null) {
            return;
        }

        scrollIntoView(element);
        try {
            click(element);
        } catch (Exception e) {
            jsClick(element);
        }
        waitForUiStable();
    }

    private void clickSubmitAllocationButton() {
        try {
            click(visible(submitAllocationButton));
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Allocate button is visible but not clickable. Current allocation form state: "
                    + describeAllocationFormState(),
                    e);
        }
    }

    private String describeAllocationFormState() {
        StringBuilder state = new StringBuilder();
        state.append("buttons=[");
        for (WebElement button : driver.findElements(By.tagName("button"))) {
            try {
                if (button.isDisplayed()) {
                    state.append("{text='")
                        .append(normalize(button.getText()))
                        .append("', enabled=")
                        .append(button.isEnabled())
                        .append("}, ");
                }
            } catch (Exception ignored) {
            }
        }
        state.append("]; selects=[");
        for (WebElement selectElement : driver.findElements(nativeDropdowns)) {
            try {
                if (selectElement.isDisplayed()) {
                    Select select = new Select(selectElement);
                    state.append("{name='")
                        .append(selectElement.getAttribute("name"))
                        .append("', value='")
                        .append(normalize(select.getFirstSelectedOption().getText()))
                        .append("'}, ");
                }
            } catch (Exception ignored) {
            }
        }
        state.append("]");
        return state.toString();
    }

}
