package com.encorepay.pages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.*;
 
public class DepositSlipsPage extends BasePage {

    public DepositSlipsPage(WebDriver driver) {
        super(driver);
    }

    // ── Page shell ────────────────────────────────────────────────────────────
    @FindBy(xpath =
        "//*[contains(normalize-space(),'Deposit Slip')]"
            + " | //input[@type='radio']"
            + " | //tbody/tr")
    private WebElement pageShell;

    // ── Payment type radios ───────────────────────────────────────────────────
    private final By allRadioInputs = By.xpath("//input[@type='radio']");

    private final By cashRadioInput = By.xpath(
        "//input[@type='radio'][@value='CASH']"
            + " | //input[@type='radio'][@value='cash']"
            + " | //mat-radio-button[contains(translate(.,'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'CASH')]//input[@type='radio']"
            + " | //label[contains(translate(normalize-space(),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'CASH')]//input[@type='radio']");

    // ── Receipt rows — ANY visible tbody row with content ─────────────────────
    private final By anyReceiptRow = By.xpath(
        "//tbody/tr[not(contains(@class,'cancelled'))"
            + "    and not(contains(@class,'header'))"
            + "    and not(contains(@class,'no-data'))]");

    // ── Checkbox inside a row ─────────────────────────────────────────────────
    private final By rowCheckboxPatterns = By.xpath(
        ".//input[@type='checkbox']"
            + " | .//*[@role='checkbox']"
            + " | .//mat-checkbox"
            + " | .//button[.//span[contains(@class,'material-symbols-rounded')"
            + "                  or contains(@class,'material-symbols')"
            + "                  or contains(@class,'material-icons')"
            + "                  or contains(@class,'check')]]"
            + " | .//span[contains(@class,'mdc-checkbox')]");

    // ── Add To Dep Slip button ────────────────────────────────────────────────
    private final By addToDepSlipBtn = By.xpath(
        "//button[contains(normalize-space(),'Add To Dep Slip')]"
            + " | //button[contains(normalize-space(),'Add to Dep')]"
            + " | //button[contains(normalize-space(),'Add to Deposit')]"
            + " | //button[contains(normalize-space(),'Add To Deposit')]"
            + " | //span[contains(normalize-space(),'Add To Dep Slip')]/ancestor::button[1]");

    // ── Bank / Account select ─────────────────────────────────────────────────
    private final By bankAccountSelect = By.xpath(
        "//select | //mat-select");

    // ── Deposit slip name field ───────────────────────────────────────────────
    private final By depositSlipNameField = By.xpath(
        "//input[contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'slip name')]"
            + " | //input[contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'deposit name')]"
            + " | //input[contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'slip')]"
            + " | //input[contains(translate(@formcontrolname,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'slipname')]"
            + " | //input[contains(translate(@formcontrolname,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'name')]"
            + " | //label[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'slip name')]/following::input[1]"
            + " | //label[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'name')]/following::input[1]");

    // ── Ref / transaction ID field ────────────────────────────────────────────
    private final By refTransactionField = By.xpath(
        "//input[contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'ref')]"
            + " | //input[contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'transaction')]"
            + " | //input[contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'reference')]"
            + " | //input[contains(translate(@formcontrolname,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'ref')]"
            + " | //input[contains(translate(@formcontrolname,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'transaction')]"
            + " | //label[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'ref')]/following::input[1]"
            + " | //label[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'transaction')]/following::input[1]");

    // ── Save / Update / Create button ─────────────────────────────────────────
    private final By updateBtn = By.xpath(
        "//button[normalize-space()='Update']"
            + " | //button[normalize-space()='Save']"
            + " | //button[normalize-space()='Create']"
            + " | //button[normalize-space()='Submit']"
            + " | //button[contains(@class,'btn-dark-blue')"
            + "        and (normalize-space()='Update'"
            + "          or normalize-space()='Save'"
            + "          or normalize-space()='Create')]");

    // ── Toast ─────────────────────────────────────────────────────────────────
    private final By toastContainer = By.xpath(
        "//*[contains(@class,'bg-green') or contains(@class,'bg-red')"
            + " or contains(@class,'toast') or contains(@class,'snack')"
            + " or @role='alert']");

    // =========================================================================
    // Public API
    // =========================================================================

    public void waitForPage() {
        visible(pageShell);
        waitForUiStable();
        action.recordVerification("Deposit Slips page loaded successfully.");
        action.captureStep("Deposit Slips Page Loaded");
    }

    /**
     * Selects CASH radio if present.
     * Falls back to first available radio.
     * Continues silently if no radios found (pre-selected).
     */
    public void selectCash() {
        waitForUiStable();

        // Try CASH radio first
        for (WebElement radio : driver.findElements(cashRadioInput)) {
            try {
                if (radio.isDisplayed()) {
                    if (!radio.isSelected()) {
                        jsClick(radio);
                        waitForUiStable();
                    }
                    action.recordVerification("Cash payment type selected.");
                    action.captureStep("Deposit Slips | Cash Selected");
                    return;
                }
            } catch (Exception ignored) {}
        }

        // Fallback: first visible radio
        for (WebElement radio : driver.findElements(allRadioInputs)) {
            try {
                if (radio.isDisplayed()) {
                    String value = radio.getAttribute("value");
                    if (!radio.isSelected()) {
                        jsClick(radio);
                        waitForUiStable();
                    }
                    action.recordVerification("Payment type selected (first available): " + value);
                    action.captureStep("Deposit Slips | Payment Type: " + value);
                    return;
                }
            } catch (Exception ignored) {}
        }

        action.recordVerification("No payment type radio found — assuming pre-selected.");
    }

    /**
     * Returns true if any visible receipt row exists.
     * No status text filtering — fully dynamic.
     */
    public boolean hasSelectableReceipts() {
        waitForUiStable();
        try { shortWait.until(ExpectedConditions.presenceOfElementLocated(anyReceiptRow)); }
        catch (Exception ignored) {}

        for (WebElement row : driver.findElements(anyReceiptRow)) {
            try {
                if (row.isDisplayed() && !row.getText().trim().isBlank()) return true;
            } catch (Exception ignored) {}
        }
        return false;
    }

    /**
     * Clicks the checkbox of the first unselected receipt row.
     * Falls back to any visible row.
     * No hardcoded status text.
     */
    public void selectFirstReceipt() {
        waitForUiStable();

        WebElement targetRow = findFirstUnselectedRow();
        if (targetRow == null) targetRow = findFirstSelectableRow();
        if (targetRow == null)
            throw new IllegalStateException(
                "No selectable receipt row found on the Deposit Slips page.");

        String rowText = "";
        try { rowText = targetRow.getText().replaceAll("\\s+", " ").trim(); }
        catch (Exception ignored) {}
        action.recordVerification("Receipt row selected: " + rowText);

        WebElement checkbox = findCheckboxInRow(targetRow);
        if (checkbox != null) {
            scrollIntoView(checkbox);
            jsClick(checkbox);
        } else {
            // No checkbox — click first cell
            List<WebElement> cells = targetRow.findElements(By.xpath(".//td"));
            if (!cells.isEmpty()) {
                scrollIntoView(cells.get(0));
                jsClick(cells.get(0));
            } else {
                scrollIntoView(targetRow);
                jsClick(targetRow);
            }
        }

        waitForUiStable();
        action.recordVerification("First receipt row selected on Deposit Slips page.");
        action.captureStep("Deposit Slips | First Receipt Selected");
    }

    /**
     * Clicks Add To Dep Slip and waits for the form to appear.
     */
    public void clickAddToDepositSlip() {
        waitForUiStable();
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(addToDepSlipBtn));
        safeClick(btn);
        wait.until(driver ->
            !driver.findElements(bankAccountSelect).isEmpty()
                || !driver.findElements(depositSlipNameField).isEmpty()
                || !driver.findElements(refTransactionField).isEmpty());
        waitForUiStable();
        action.recordVerification("'Add To Dep Slip' clicked — form visible.");
        action.captureStep("Deposit Slips | Add To Dep Slip clicked");
    }

    /**
     * Selects bank dynamically:
     * 1. Exact match on bankName
     * 2. Partial match on bankName
     * 3. Auto-select first non-placeholder option
     */
    public void selectBank(String bankName) {
        waitForUiStable();
        WebElement selectEl = waitForVisibleElement(bankAccountSelect);
        if (selectEl == null) {
            action.recordVerification("Bank select not visible — skipping.");
            return;
        }

        if ("select".equalsIgnoreCase(selectEl.getTagName())) {
            String chosen = chooseDynamicOption(new Select(selectEl), bankName, "Bank");
            action.recordVerification("Bank selected: " + chosen);
        } else {
            jsClick(selectEl);
            List<WebElement> options = driver.findElements(
                By.xpath("//mat-option[normalize-space()]"));
            if (options.isEmpty()) {
                action.recordVerification("No bank options found.");
                return;
            }
            // Try preferred
            if (bankName != null && !bankName.isBlank()) {
                for (WebElement opt : options) {
                    try {
                        String text = opt.getText().trim();
                        if (text.toLowerCase().contains(bankName.toLowerCase())
                                && opt.isDisplayed()) {
                            com.encorepay.actiondriver.ActionDriver.globalSafeClick(driver, opt);
                            action.recordVerification("Bank selected: " + text);
                            return;
                        }
                    } catch (Exception ignored) {}
                }
            }
            // Auto-select first
            for (WebElement opt : options) {
                try {
                    if (opt.isDisplayed() && !opt.getText().trim().isBlank()) {
                        String text = opt.getText().trim();
                        com.encorepay.actiondriver.ActionDriver.globalSafeClick(driver, opt);
                        action.recordVerification("Bank auto-selected: " + text);
                        return;
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    /** Enters deposit slip name — skips if field not found */
    public void enterDepositSlipName(String value) {
        if (value == null || value.isBlank()) return;
        WebElement field = waitForVisibleElement(depositSlipNameField);
        if (field == null) {
            action.recordVerification("Deposit Slip Name field not found — skipping.");
            return;
        }
        clearAndType(field, value);
        action.recordTestData("Deposit Slip Name: " + value);
    }

    /** Enters ref transaction ID — skips if field not found */
    public void enterRefTransactionId(String value) {
        if (value == null || value.isBlank()) return;
        WebElement field = waitForVisibleElement(refTransactionField);
        if (field == null) {
            action.recordVerification("Ref Transaction ID field not found — skipping.");
            return;
        }
        clearAndType(field, value);
        action.recordTestData("Ref Transaction ID: " + value);
    }

    /** Clicks Save/Update/Create */
    public void clickUpdate() {
        waitForUiStable();
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(updateBtn));
        scrollIntoView(btn);
        safeClick(btn);
        waitForUiStable();
        action.recordVerification("Save/Update button clicked.");
        action.captureStep("Deposit Slips | Save clicked");
    }

    /** Full flow — all params can be blank, auto-selected if missing */
    public String createCashDepositSlip(String bankName, String depositSlipName, String refTransactionId) {
        waitForPage();
        selectCash();

        if (!hasSelectableReceipts())
            throw new IllegalStateException(
                "No selectable receipt rows found on the Create Deposit Slip page.");

        selectFirstReceipt();
        clickAddToDepositSlip();
        selectBank(bankName);
        enterDepositSlipName(depositSlipName);
        enterRefTransactionId(refTransactionId);
        clickUpdate();

        String msg = getToastMessage();
        action.recordVerification("Deposit slip result: " + (msg.isBlank() ? "No toast" : msg));
        action.captureStep("Deposit Slips | Created - " + msg);
        return msg;
    }

    public String getToastMessage() {
        try {
            WebElement toast = wait.until(
                ExpectedConditions.visibilityOfElementLocated(toastContainer));
            String msg = sanitize(toast.getText());
            new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.invisibilityOf(toast));
            return msg;
        } catch (Exception e) {
            return "";
        }
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private WebElement findFirstUnselectedRow() {
        for (WebElement row : driver.findElements(anyReceiptRow)) {
            try {
                if (!row.isDisplayed() || row.getText().trim().isBlank()) continue;
                if (!isRowSelected(row)) return row;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private WebElement findFirstSelectableRow() {
        for (WebElement row : driver.findElements(anyReceiptRow)) {
            try {
                if (row.isDisplayed() && !row.getText().trim().isBlank()) return row;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private WebElement findCheckboxInRow(WebElement row) {
        for (WebElement candidate : row.findElements(rowCheckboxPatterns)) {
            try { if (candidate.isDisplayed()) return candidate; }
            catch (Exception ignored) {}
        }
        return null;
    }

    private boolean isRowSelected(WebElement row) {
        try {
            for (WebElement cb : row.findElements(
                    By.xpath(".//input[@type='checkbox'] | .//*[@role='checkbox']"))) {
                if (!cb.isDisplayed()) continue;
                String ariaChecked = cb.getAttribute("aria-checked");
                String checked     = cb.getDomProperty("checked");
                if ("true".equalsIgnoreCase(ariaChecked)
                        || "true".equalsIgnoreCase(checked)
                        || cb.isSelected()) return true;
            }
            String cls = row.getAttribute("class");
            return cls != null && (cls.contains("selected") || cls.contains("checked"));
        } catch (Exception e) {
            return false;
        }
    }

    private String chooseDynamicOption(Select select, String preferred, String fieldLabel) {
        List<WebElement> options = select.getOptions();

        if (preferred != null && !preferred.isBlank()) {
            for (WebElement opt : options) {
                String text = opt.getText().trim();
                if (text.equalsIgnoreCase(preferred)) {
                    select.selectByVisibleText(text);
                    System.out.println("[INFO] " + fieldLabel + " (exact): " + text);
                    return text;
                }
            }
            for (WebElement opt : options) {
                String text = opt.getText().trim();
                if (!text.isBlank()
                        && text.toLowerCase().contains(preferred.toLowerCase())) {
                    select.selectByVisibleText(text);
                    System.out.println("[INFO] " + fieldLabel + " (partial): " + text);
                    return text;
                }
            }
        }

        for (WebElement opt : options) {
            String text = opt.getText().trim();
            if (!text.isBlank()
                    && !text.toLowerCase().contains("select")
                    && !text.toLowerCase().contains("choose")
                    && !text.startsWith("-")) {
                select.selectByVisibleText(text);
                System.out.println("[INFO] " + fieldLabel + " (auto): " + text);
                return text;
            }
        }
        System.out.println("[WARN] No option found for: " + fieldLabel);
        return "";
    }

    private WebElement waitForVisibleElement(By locator) {
        return wait.until(driver -> {
            for (WebElement el : driver.findElements(locator)) {
                try { if (el.isDisplayed()) return el; } catch (Exception ignored) {}
            }
            return null;
        });
    }

    @Override
    protected void clearAndType(WebElement field, String value) {
        try {
            scrollIntoView(field);
            com.encorepay.actiondriver.ActionDriver.globalSafeClick(driver, field);
            field.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            field.sendKeys(Keys.DELETE);
            field.sendKeys(value);
            field.sendKeys(Keys.TAB);
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1];"
                    + "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));"
                    + "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));",
                field, value);
        }
    }

    private void safeClick(WebElement el) {
        try { com.encorepay.actiondriver.ActionDriver.globalSafeClick(driver, el); } catch (Exception e) { jsClick(el); }
    }

    @Override
    protected void jsClick(WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
    }

    @Override
    protected void scrollIntoView(WebElement el) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", el);
        } catch (Exception ignored) {}
    }

    @Override
    protected void waitForUiStable() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15)).until(d ->
                ((JavascriptExecutor) d)
                    .executeScript("return document.readyState").equals("complete"));
        } catch (Exception ignored) {}
    }

    private String sanitize(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }
}
