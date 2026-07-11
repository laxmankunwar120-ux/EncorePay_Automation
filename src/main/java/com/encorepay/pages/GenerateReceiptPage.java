package com.encorepay.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenerateReceiptPage extends BasePage {

    private String latestReceiptNumber = "";

    public GenerateReceiptPage(WebDriver driver) {
        super(driver);
    }

    // ── Locators ─────────────────────────────────────────────────────────────

    private final By usernameField = By.xpath(
        "//input[@formcontrolname='username']"
        + " | //input[@name='username']"
        + " | //input[@placeholder='User Name']"
        + " | //input[@placeholder='Enter User Name']");

    private final By passwordField = By.xpath(
        "//input[@formcontrolname='password']"
        + " | //input[@name='password']"
        + " | //input[@placeholder='Password']"
        + " | //input[@placeholder='Enter Password']");

    private final By loginBtn = By.xpath(
        "//button[normalize-space()='Log In']"
        + " | //button[@type='submit' and contains(normalize-space(),'Log')]");

    private final By postLoginElement = By.xpath(
        "//button[contains(@class,'menu-btn')]"
        + " | //nav"
        + " | //app-header");

    private final By accountsMenu = By.xpath(
        "//button[contains(@class,'menu-btn') and normalize-space()='Accounts']"
        + " | //button[normalize-space()='Accounts']"
        + " | //a[normalize-space()='Accounts']");

    private final By collectionsMenu = By.xpath(
        "//button[contains(@class,'menu-btn') and normalize-space()='Collections']"
        + " | //button[normalize-space()='Collections']"
        + " | //a[normalize-space()='Collections']");

    private final By collectionItemsLink = By.xpath(
        "//a[normalize-space()='Collection Items']"
        + " | //button[normalize-space()='Collection Items']"
        + " | //span[normalize-space()='Collection Items']/ancestor::*[self::a or self::button][1]");

    private final By collectionPageShell = By.xpath(
        "//button[contains(normalize-space(),'Show Filter')]"
        + " | //button[contains(normalize-space(),'Bulk Allocate')]"
        + " | //button[contains(normalize-space(),'Bulk Upload')]");

    private final By collectionCountLabel = By.xpath(
        "//*[contains(normalize-space(),'Collection Item') and string-length(normalize-space()) <= 40]");

    private final By paginatorSummary = By.xpath(
        "//*[contains(@class,'mat-mdc-paginator-range-label')]"
        + " | //*[contains(@class,'paginator-range-label')]"
        + " | //*[contains(normalize-space(),' of ') and (contains(normalize-space(),'–') or contains(normalize-space(),'-'))]");

    private final By tableRows = By.xpath(
        "//tbody/tr[.//td]"
        + " | //tr[contains(@class,'mat-mdc-row')]"
        + " | //mat-row"
        + " | //div[contains(@class,'ag-row') and @row-id]");

    // Actions button: class="mat-mdc-menu-trigger link inline-flex items-center" (not menu-btn)
    private final By firstRowActionsBtn = By.xpath(
        "(//tbody/tr[.//td]//button[contains(@class,'mat-mdc-menu-trigger') and not(contains(@class,'menu-btn'))])[1]"
        + " | (//tbody/tr[.//td]//button[contains(normalize-space(),'Actions')])[1]"
        + " | (//tr[contains(@class,'mat-mdc-row')]//button[contains(@class,'mat-mdc-menu-trigger')])[1]"
        + " | (//mat-row//button[contains(@class,'mat-mdc-menu-trigger')])[1]");

    // Generate Receipt: button[role='menuitem'] > span.mat-mdc-menu-item-text "Generate Receipt"
    private final By generateReceiptOption = By.xpath(
        "//button[@role='menuitem' and .//span[normalize-space()='Generate Receipt']]"
        + " | //button[contains(@class,'mat-mdc-menu-item') and contains(normalize-space(),'Generate Receipt')]"
        + " | //span[contains(@class,'mat-mdc-menu-item-text') and normalize-space()='Generate Receipt']/ancestor::button[1]"
        + " | //*[@role='menuitem'][contains(normalize-space(),'Generate Receipt')]");

    // Pay button: class="btn btn-dark-blue ml-auto" text="Pay ₹ amount"
    private final By modalPayBtn = By.xpath(
        "//button[contains(@class,'btn-dark-blue') and contains(normalize-space(),'Pay')]"
        + " | //div[contains(@class,'modal-wrapper')]//button[contains(normalize-space(),'Pay')]"
        + " | //mat-dialog-content//button[contains(normalize-space(),'Pay')]");

    // Partial/Excess: input[type='radio'][name='repaymentType'][value='PARTIAL_OR_EXCESS_PAYMENT']
    private final By partialPaymentRadio = By.xpath(
        "//input[@type='radio' and @name='repaymentType' and @value='PARTIAL_OR_EXCESS_PAYMENT']"
        + " | //mat-radio-button[contains(.,'Partial') or contains(.,'Excess')]//input[@type='radio']"
        + " | //label[contains(.,'Partial or Excess')]");

    // Amount: NOT inside app-cash-payment denomination grid
    private final By amountInput = By.xpath(
        "//input[@formcontrolname='paymentAmount']"
        + " | //input[@name='paymentAmount']"
        + " | //input[@type='number'"
        + "   and not(ancestor::app-cash-payment)"
        + "   and not(ancestor::*[contains(@class,'listl')])"
        + "   and not(ancestor::*[contains(@class,'list-content')])]"
        + " | //label[contains(normalize-space(),'Amount')]/following::input[not(ancestor::app-cash-payment)][1]"
        + " | //input[@placeholder='Amount']");

    // Cash radio: input[type='radio'][name='instrument'][value='CASH']
    private final By cashRadio = By.xpath(
        "//input[@type='radio' and @value='CASH']");

    private final By chequeOrDdMode = By.xpath(
        "//input[@type='radio' and @value='CHEQUE_OR_DD']");

    private final By chequeInstrumentOption = By.xpath(
        "(//*[normalize-space()='SELECT' or normalize-space()='SELECT *']"
        + "/following::*[self::mat-radio-button or self::label]"
        + "[.//input[@type='radio' and @value='CHEQUE'] or .//*[normalize-space()='CHEQUE'] or normalize-space()='CHEQUE'])[1]");

    private final By ddInstrumentOption = By.xpath(
        "(//*[normalize-space()='SELECT' or normalize-space()='SELECT *']"
        + "/following::*[self::mat-radio-button or self::label]"
        + "[.//input[@type='radio' and @value='DD'] or .//*[normalize-space()='DD'] or normalize-space()='DD'])[1]");

    private final By directTransferMode = By.xpath(
        "//mat-radio-button[contains(.,'Direct Transfer')]"
        + " | //label[contains(normalize-space(),'Direct Transfer')]");

    private final By sendPaymentLinkMode = By.xpath(
        "//mat-radio-button[contains(.,'Payment Link')]"
        + " | //label[contains(normalize-space(),'Payment Link')]");

    private final By qrCodeMode = By.xpath(
        "//mat-radio-button[contains(.,'QR Code')]"
        + " | //label[contains(normalize-space(),'QR Code')]");

    // Denomination inputs scoped to app-cash-payment component
    private final By denominationInputs = By.xpath(
        "//app-cash-payment//input[@type='number' or @type='text']"
        + " | //div[contains(@class,'list-content')]//input[@type='number' or @type='text']"
        + " | //div[contains(@class,'listl')]//input[@type='number' or @type='text']");

    private final By paymentBankDropdown = By.xpath(
        "//label[contains(normalize-space(),'Bank')]/following::*[self::select or self::mat-select][1]"
        + " | //select[contains(@name,'bank')]"
        + " | //mat-select[contains(@placeholder,'Bank')]"
        + " | //select");

    private final By paymentReferenceInput = By.xpath(
        "//input[contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'reference')]"
        + " | //input[contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'utr')]"
        + " | //input[contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'transaction id')]");

    private final By paymentInstrumentInput = By.xpath(
        "//input[@name='chequeNum']");

    private final By chequeDdNumberInput = By.xpath(
        "//input[@name='chequeNum']");

    private final By chequeDdIfscInput = By.xpath(
        "//input[@name='bankIfscCode']");

    private final By chequeDdAccountHolderInput = By.xpath(
        "//input[@name='bankAccountHolderName']");

    private final By chequeDdBankNameInput = By.xpath(
        "//input[@name='bankName']");

    // Exclude receiptDate (auto-filled on Payment Collection page)
    private final By paymentDateInput = By.xpath(
        "//input[@type='date' and @name='chequeDate']"
        + " | //input[@type='date' and not(@name='receiptDate')]");

    private final By paymentPanInput = By.xpath(
        "//input[contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'pan')]"
        + " | //input[@placeholder='Enter Pan Number']");

    private final By paymentLinkPreview = By.xpath(
        "//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'payment link')]"
        + "[contains(normalize-space(.),'http') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'generated')]");

    private final By qrCodePreview = By.xpath(
        "//canvas | //img[contains(@src,'qr')] | //qrcode//*[self::img or self::canvas]");

    // PROCEED: class="btn btn-dark-blue"
    private final By proceedBtn = By.xpath(
        "//button[normalize-space()='PROCEED' or normalize-space()='Proceed']");

    private final By paymentCollectionTitle = By.xpath(
        "//*[contains(normalize-space(),'Payment Collection')]");

    // Relationship: select[@name='state']
    private final By relationshipSelect = By.xpath(
        "//select[@name='state']"
        + " | //select[contains(@name,'relation')]"
        + " | //label[contains(normalize-space(),'Select Relationship')]/following::select[1]"
        + " | //*[normalize-space()='Select Relationship']/following::select[1]"
        + " | //*[normalize-space()='Select Relationship']/following::*[self::mat-select or @role='combobox' or contains(@class,'ng-select') or contains(@class,'select')][1]"
        + " | //*[normalize-space()='Select Relationship']/following::*[contains(normalize-space(),'Choose Relationship')][1]"
        + " | //select[.//option[contains(normalize-space(),'Relationship')] or .//option[normalize-space()='Self']]"
        + " | //mat-select[contains(@placeholder,'Relation')]");

    private final By payerNameInput = By.xpath(
        "//input[@placeholder='Payer Name'] | //input[@name='payerName']");

    // Mobile: gracefully skipped if absent (not on Payment Collection page per images 9-11)
    private final By mobileNumberInput = By.xpath(
        "//input[@name='mobileNumber']"
        + " | //input[@placeholder='Mobile Number']"
        + " | //input[@type='tel']"
        + " | //label[contains(normalize-space(),'Mobile')]/following::input[1]");

    // CONFIRM AND ISSUE RECEIPT: class="btn btn-dark-blue ml-auto"
    private final By confirmBtn = By.xpath(
        "//button[contains(@class,'btn-dark-blue') and contains(normalize-space(),'CONFIRM AND ISSUE RECEIPT')]"
        + " | //button[contains(normalize-space(),'CONFIRM AND ISSUE RECEIPT')]"
        + " | //button[contains(normalize-space(),'Confirm and Issue Receipt')]");

    private final By successToast = By.xpath(
        "//*[contains(@class,'bg-green') and contains(@class,'p-3')]"
        + " | //*[contains(@class,'toast') and contains(@class,'success')]"
        + " | //*[contains(@class,'snack') and contains(@class,'success')]");

    private final By errorToast = By.xpath(
        "//*[contains(@class,'bg-red') or contains(@class,'toast') or contains(@class,'snack') or @role='alert']"
        + "[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'error')"
        + " or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'failed')"
        + " or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'invalid')]");

    private final By receiptTitle = By.xpath(
        "//*[contains(normalize-space(),'View Receipt')]"
        + " | //*[contains(normalize-space(),'Receipt No')]");

    private final By receiptCloseButton = By.xpath(
        "//button[.//span[contains(@class,'material-symbols-rounded') and normalize-space()='close']]"
        + " | //span[contains(@class,'material-symbols-rounded') and normalize-space()='close']/ancestor::button[1]"
        + " | //mat-dialog-container//button[contains(@aria-label,'Close')]"
        + " | //button[contains(@class,'close') and not(contains(normalize-space(.),'Cancel'))]");

    private final By paymentModeOptions = By.xpath(
        "//mat-radio-button[contains(.,'Cash') or contains(.,'Cheque') or contains(.,'DD')"
        + " or contains(.,'Direct Transfer') or contains(.,'Payment Link') or contains(.,'QR Code')]"
        + " | //label[contains(normalize-space(),'Cash') or contains(normalize-space(),'Cheque')"
        + " or contains(normalize-space(),'DD') or contains(normalize-space(),'Direct Transfer')"
        + " or contains(normalize-space(),'Payment Link') or contains(normalize-space(),'QR Code')]");

    private final By visiblePaymentFields = By.xpath(
        "//input[not(@type='hidden')] | //select | //textarea | //button | //mat-select | //label");

    // ── LOGOUT LOCATORS ───────────────────────────────────────────────────────
    // Image 1: button.mat-mdc-menu-trigger.menu-btn > span.material-symbols-rounded "account_circle"
    private final By accountCircleBtn = By.xpath(
        "//button[contains(@class,'menu-btn') and contains(@class,'mat-mdc-menu-trigger')"
        + " and .//span[contains(@class,'material-symbols-rounded') and normalize-space()='account_circle']]"
        + " | //button[contains(@class,'mat-mdc-menu-trigger')"
        + " and .//span[normalize-space()='account_circle']]");

    // Image 2: button.mat-mdc-menu-item[@role='menuitem'] > span.text-gray-800 "Logout"
    private final By logoutOption = By.xpath(
        "//button[@role='menuitem' and .//span[normalize-space()='Logout']]"
        + " | //button[contains(@class,'mat-mdc-menu-item') and .//span[normalize-space()='Logout']]"
        + " | //span[contains(@class,'text-gray-800') and normalize-space()='Logout']/ancestor::button[1]"
        + " | //*[@role='menuitem'][normalize-space()='Logout']");

    // Post-logout: login page appears
    private final By loginPageElement = By.xpath(
        "//input[@formcontrolname='username']"
        + " | //input[@placeholder='User Name']"
        + " | //input[@placeholder='Enter User Name']"
        + " | //button[normalize-space()='Log In']");

    // ── Public Methods ────────────────────────────────────────────────────────

    public void login() {
        String appUrl = config.getURL().trim();
        if (shouldOpenLoginPage(appUrl)) driver.get(appUrl);
        pause(1500);
        WebElement user = wait.until(ExpectedConditions.elementToBeClickable(usernameField));
        jsClick(user);
        user.clear();
        user.sendKeys(com.encorepay.config.ConfigLoader.getInstance().getUsername().trim());
        pause(150);
        WebElement pass = wait.until(ExpectedConditions.elementToBeClickable(passwordField));
        jsClick(pass);
        pass.clear();
        pass.sendKeys(com.encorepay.config.ConfigLoader.getInstance().getPassword().trim());
        pause(150);
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(loginBtn));
        jsClick(btn);
        wait.until(ExpectedConditions.visibilityOfElementLocated(postLoginElement));
        action.waitForTransientFeedbackToClear();
        pause(800);
        action.recordVerification("Receipt workflow login completed successfully.");
        action.captureStep("STEP 1 - Logged In");
    }

    public void navigateToCollectionItems() {
        new CollectionsPage(driver).openGenerateReceiptQueue();
        pause(300);
        System.out.println("[INFO] Collection Items URL: " + driver.getCurrentUrl());
        action.recordVerification("Collection Items page opened at " + driver.getCurrentUrl());
        action.captureStep("STEP 2 - Collection Items landing");

        wait.until(ExpectedConditions.or(
            ExpectedConditions.visibilityOfElementLocated(collectionPageShell),
            ExpectedConditions.visibilityOfElementLocated(collectionCountLabel),
            ExpectedConditions.visibilityOfElementLocated(firstRowActionsBtn),
            ExpectedConditions.visibilityOfElementLocated(tableRows)
        ));

        if (!hasCollectionItemsAvailable()) {
            String countSummary = getCollectionCountSummary();
            throw new IllegalStateException(
                "No collection items available"
                + (countSummary.isBlank() ? "." : ". Observed: " + countSummary));
        }

        String countSummary = getCollectionCountSummary();
        if (!countSummary.isBlank()) action.recordVerification("Receipt queue: " + countSummary);
        pause(500);
        action.captureStep("STEP 2 - Generate Receipt queue loaded from Collections module");
    }

    public void openGenerateReceiptModal() {
        openGenerateReceiptModalForRow(0);
    }

    private void openGenerateReceiptModalForRow(int itemIndex) {
        if (itemIndex <= 0) selectFirstRowIfNeeded();
        WebElement actionsBtn = waitForVisibleRowActionsButton(itemIndex);
        scrollIntoView(actionsBtn);
        pause(200);
        jsClick(actionsBtn);
        pause(400);

        WebElement option = waitForVisibleElement(generateReceiptOption);
        jsClick(option);
        pause(400);

        action.recordVerification("Generate Receipt triggered. URL: " + driver.getCurrentUrl());
        waitForVisibleElement(modalPayBtn);
        action.recordVerification("Generate Receipt modal opened.");
        action.captureStep("STEP 3 - Generate Receipt modal open");
    }

    public void clickPayButton() {
        clickVisiblePayButton();
        waitForPaymentFlowToLoad();
        action.recordVerification("Payment form opened.");
        action.captureStep("STEP 4 - Pay clicked");
    }

    public void selectPartialOrExcessPayment() {
        WebElement radio = waitForVisibleElement(partialPaymentRadio);
        scrollIntoView(radio);
        pause(150);
        jsClick(radio);
        pause(300);
        WebElement input = waitForVisibleElement(amountInput);
        clearAmountField(input);
        pause(200);
        action.recordVerification("Partial/Excess payment selected, amount field cleared.");
        action.captureStep("STEP 5 - Partial/Excess Payment selected", input);
    }

    public void enterAmount(String amount) {
        WebElement input = waitForVisibleElement(amountInput);
        scrollIntoView(input);
        pause(200);
        setAmountFieldValue(input, amount);
        pause(300);
        action.recordTestData("Receipt amount: " + amount);
        action.recordVerification("Amount entered: " + amount);
        action.captureStep("STEP 6 - Amount: " + amount, input);
    }

    public void selectCashPayment() {
        selectAngularRadio(cashRadio, "Cash");
        assertNoSystemError("Cash payment selection");
        action.recordVerification("Cash selected.");
        action.captureStep("STEP 7 - Cash selected");
    }

    public void selectChequeOrDdPayment() {
        selectAngularRadio(chequeOrDdMode, "Cheque Or DD");
        assertNoSystemError("Cheque Or DD payment selection");
        action.recordVerification("Cheque Or DD selected.");
        action.captureStep("Cheque Or DD - Payment mode selected");
    }

    public void enterDenomination() {
        try {
            pause(500);
            DenominationSelection selection = findBestDenominationSelection();
            if (selection != null) {
                WebElement input = selection.input();
                scrollIntoView(input);
                pause(300);
                input.click();
                input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
                input.sendKeys(Keys.DELETE);
                input.sendKeys(selection.quantity());
                input.sendKeys(Keys.TAB);
                pause(400);
                String actualQty = readInputValue(input);
                if (actualQty.isBlank()) actualQty = selection.quantity();
                action.recordTestData("Denomination: " + selection.label() + " x " + actualQty);
                action.recordVerification("Denomination entered: " + selection.label() + " x " + actualQty);
                action.captureStep("STEP 8 - Denomination " + selection.label() + " x " + actualQty, input);
            } else {
                System.out.println("[WARN] No denomination input found");
                action.captureStep("STEP 8 - Denomination input not found");
            }
        } catch (Exception e) {
            System.out.println("[WARN] Denomination error: " + e.getMessage());
        }
    }

    public void clickProceed() {
        clickFreshElement(proceedBtn, "PROCEED");
        wait.until(ExpectedConditions.visibilityOfElementLocated(paymentCollectionTitle));
        pause(500);
        action.recordVerification("Proceeded to Payment Collection.");
        action.captureStep("STEP 9 - PROCEED clicked");
    }

    public void selectRelationshipSelf() {
        WebElement selectEl = wait.until(ExpectedConditions.visibilityOfElementLocated(relationshipSelect));
        scrollIntoView(selectEl);
        pause(150);
        if (selectEl.getTagName().equalsIgnoreCase("select")) {
            new Select(selectEl).selectByVisibleText("Self");
        } else {
            clickWithJavascriptFallback(selectEl);
            pause(200);
            if (!clickVisibleRelationshipOption("Self"))
                throw new IllegalStateException("Cannot select relationship 'Self'.");
        }
        pause(300);
        action.recordTestData("Relationship: Self");
        try {
            WebElement payer = shortWait.until(ExpectedConditions.visibilityOfElementLocated(payerNameInput));
            String payerValue = payer.getAttribute("value");
            action.recordVerification("Self selected; payer: " + payerValue);
            action.captureStep("STEP 10 - Self selected, Payer: " + payerValue);
        } catch (Exception e) {
            action.captureStep("STEP 10 - Self selected");
        }
    }

    /**
     * Mobile field is absent on Payment Collection page (images 9-11) — gracefully skips.
     */
    public void enterMobileNumber(String mobile) {
        pause(200);
        boolean fieldVisible = driver.findElements(mobileNumberInput).stream().anyMatch(el -> {
            try { return el.isDisplayed(); } catch (Exception e) { return false; }
        });
        if (!fieldVisible) {
            System.out.println("[INFO] Mobile field not present - skipping.");
            action.recordVerification("Mobile field absent - skipped.");
            action.captureStep("STEP 11 - Mobile field not present, skipped");
            return;
        }
        WebElement input = waitForVisibleElement(mobileNumberInput);
        scrollIntoView(input);
        pause(250);
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
        input.clear();
        input.sendKeys(mobile);
        input.sendKeys(Keys.TAB);
        pause(300);
        action.recordTestData("Mobile: ..." + maskMobile(mobile));
        action.recordVerification("Mobile entered: ..." + maskMobile(mobile));
        action.captureStep("STEP 11 - Mobile: " + mobile);
    }

    public void clickConfirmAndIssueReceipt() {
        pause(300);
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(confirmBtn));
        scrollIntoView(btn);
        pause(200);
        safeClick(btn);
        action.recordVerification("Confirm and Issue Receipt submitted.");
        action.captureStep("STEP 12 - CONFIRM AND ISSUE RECEIPT clicked");
    }

    public boolean verifyReceiptSuccess() {
        try {
            String text = new WebDriverWait(driver, Duration.ofSeconds(20))
                .ignoring(StaleElementReferenceException.class)
                .until(driver -> {
                    String toastText = readVisibleText(successToast);
                    return toastText.isBlank() ? null : toastText;
                });
            latestReceiptNumber = extractReceiptNumber(text);
            System.out.println("[INFO] Toast: " + text);
            action.recordVerification("Receipt success: " + text);
            if (!latestReceiptNumber.isBlank())
                action.recordVerification("Receipt number: " + latestReceiptNumber);
            action.captureStep("STEP 13 - SUCCESS: " + text);
            action.waitForTransientFeedbackToClear();
            return true;
        } catch (Exception e) {
            System.out.println("[WARN] Success toast not found: " + e.getMessage());
            action.captureStep("STEP 13 - Toast not found");
            return false;
        }
    }

    public String getReceiptNumber() {
        if (!latestReceiptNumber.isBlank()) return latestReceiptNumber;
        try {
            WebElement el = shortWait.until(ExpectedConditions.visibilityOfElementLocated(receiptTitle));
            String receiptNumber = extractReceiptNumber(el.getText().trim());
            if (!receiptNumber.isBlank()) action.recordVerification("Receipt number: " + receiptNumber);
            return receiptNumber;
        } catch (Exception e) { return ""; }
    }

    public void closeReceipt() {
        if (driver.findElements(receiptCloseButton).isEmpty()) return;
        WebElement closeButton = waitForVisibleElement(receiptCloseButton);
        if (closeButton == null) return;
        scrollIntoView(closeButton);
        safeClick(closeButton);
        pause(400);
        action.recordVerification("Receipt closed.");
        action.captureStep("STEP 14 - Receipt closed");
    }

    /**
     * LOGOUT — clicks account_circle icon then Logout from the dropdown.
     *
     * Image 1: button.mat-mdc-menu-trigger.menu-btn containing span.material-symbols-rounded "account_circle"
     * Image 2: button.mat-mdc-menu-item[@role='menuitem'] containing span.text-gray-800 "Logout"
     */
    public void logout() {
        action.waitForTransientFeedbackToClear();

        if (isVisible(loginPageElement)
                || driver.getCurrentUrl().contains("signin")
                || driver.getCurrentUrl().contains("login")) {
            action.recordVerification("Already on login page.");
            action.captureStep("STEP 15 - Already Logged Out");
            return;
        }

        // 1. Click the account circle button (top-right nav)
        clickFreshElement(accountCircleBtn, "Account circle");
        pause(300);

        // 2. Click Logout from the dropdown menu
        pause(100);
        clickFreshElement(logoutOption, "Logout");

        // 3. Wait for the login page to confirm logout
        wait.until(ExpectedConditions.or(
            ExpectedConditions.visibilityOfElementLocated(loginPageElement),
            ExpectedConditions.urlContains("login"),
            ExpectedConditions.urlContains("#/home")
        ));
        pause(300);

        action.recordVerification("Logged out successfully via account circle menu.");
        action.captureStep("STEP 15 - Logged Out");
    }

    // ── Helper / Support Methods ──────────────────────────────────────────────

    public void navigateBackForNextIteration() {
        driver.navigate().to(buildCollectionItemsUrl());
        pause(300);
        action.captureStep("BACK - Collection Items for next iteration");
    }

    public void selectCashAndFillDenominations(int amount) {
        selectCashPayment();
        pause(200);
        enterDenomination();
    }

    public void fillPayerDetails(String email, String mobile) {
        selectRelationshipSelf();
        enterMobileNumber(mobile);
        action.recordVerification("Payer details filled. Mobile: ..." + maskMobile(mobile));
    }

    public void clickConfirm()          { clickConfirmAndIssueReceipt(); }
    public void waitForReceiptSuccess() { verifyReceiptSuccess(); }
    public void closeReceiptModal()     { closeReceipt(); }

    public boolean isReceiptVisible() {
        try {
            return driver.findElements(receiptTitle).stream().anyMatch(WebElement::isDisplayed);
        } catch (Exception e) { return false; }
    }

    public void configureChequeOrDdPayment(String bankName, String instrumentNumber,
                                            String reference, String paymentDate) {
        configureChequeOrDdPayment("CHEQUE", instrumentNumber, "", "", bankName);
        enterPaymentReference(reference);
        enterPaymentDate(paymentDate);
    }

    public void configureChequeOrDdPayment(String instrumentType, String chequeNumber,
                                            String ifscCode, String accountHolderName, String bankName) {
        selectChequeOrDdPayment();
        String normalizedInstrumentType = normalizeChequeDdInstrumentType(instrumentType);

        selectChequeDdInstrumentType(normalizedInstrumentType);
        fillChequeDdInput(chequeDdNumberInput, normalizedInstrumentType + " Number", chequeNumber);
        fillChequeDdInput(chequeDdIfscInput, "IFSC Code", ifscCode);
        fillChequeDdInput(chequeDdAccountHolderInput, "Account Holder Name", accountHolderName);
        fillChequeDdInput(chequeDdBankNameInput, "Bank Name", bankName);

        assertNoSystemError(normalizedInstrumentType + " details entry");
        action.recordTestData("Instrument Type: " + normalizedInstrumentType);
        action.recordVerification(normalizedInstrumentType + " details filled.");
        action.captureStep("Cheque Or DD - " + normalizedInstrumentType + " details entered");
    }

    public void configureDirectTransferPayment(String bankName, String reference, String paymentDate) {
        selectDirectTransferPayment();
        selectPaymentBank(bankName);
        enterPaymentReference(reference);
        enterPaymentDate(paymentDate);
        action.captureStep("Direct Transfer - Details entered");
    }

    public void prepareSendPaymentLinkMode() {
        selectSendPaymentLinkMode();
        action.captureStep("Send Payment Link - Ready");
    }

    public void prepareQrCodeMode() {
        selectQrCodePayment();
        action.captureStep("QR Code - Ready");
    }

    public void enterPanNumberIfVisible(String panNumber) {
        typeIfVisible(paymentPanInput, panNumber, "PAN Number");
    }

    public List<String> getAvailablePaymentModes() {
        List<String> modes = new ArrayList<>();
        for (WebElement element : driver.findElements(paymentModeOptions)) {
            try {
                if (!element.isDisplayed()) continue;
                String text = sanitizeUiText(element.getText());
                if (!text.isBlank() && !modes.contains(text)) modes.add(text);
            } catch (Exception ignored) {}
        }
        return modes;
    }

    public String inspectPaymentMode(String modeLabel) {
        WebElement option = waitForVisibleElement(paymentModeLocator(modeLabel));
        scrollIntoView(option);
        pause(200);
        safeClick(option);
        pause(600);
        assertNoSystemError("Payment mode inspection for " + modeLabel);
        String summary = captureVisiblePaymentFieldSummary();
        action.recordVerification("Payment mode '" + modeLabel + "' fields: " + summary);
        action.captureStep("Payment Mode - " + modeLabel);
        return summary;
    }

    public void processMultipleReceiptGenerations(int itemCount, String... paymentModes) {
        if (itemCount <= 0) return;
        for (int itemIdx = 0; itemIdx < itemCount; itemIdx++) {
            String paymentMode = paymentModes[itemIdx % paymentModes.length];
            if (itemIdx == 0) {
                navigateToCollectionItems();
            } else {
                if (isReceiptVisible()) closeReceiptModal();
                refreshLoginBeforeNextReceipt(itemIdx + 1);
                navigateBackAndRefresh(itemIdx + 1);
            }
            selectNextItemIfNeeded(itemIdx);
            pause(200);
            openGenerateReceiptModalForRow(itemIdx);
            clickPayButton();
            selectPartialOrExcessPayment();
            enterAmount("500");
            if ("Cash".equalsIgnoreCase(paymentMode)) {
                selectCashAndFillDenominations(500);
            } else if ("Cheque".equalsIgnoreCase(paymentMode)) {
                configureChequeOrDdPayment("Cheque",
                    configValue("receiptChequeNumber", "123456789"),
                    configValue("receiptChequeIfsc", "HDFC0001234"),
                    configValue("receiptChequeAccountHolder", "Test Payer"),
                    configValue("receiptChequeBankName", "HDFC Bank"));
            } else if ("DD".equalsIgnoreCase(paymentMode) || "Demand Draft".equalsIgnoreCase(paymentMode)) {
                configureChequeOrDdPayment("DD",
                    configValue("receiptDdNumber", "987654321"),
                    configValue("receiptDdIfsc", "ICIC0001234"),
                    configValue("receiptDdAccountHolder", "Test Payer"),
                    configValue("receiptDdBankName", "ICICI Bank"));
            }
            clickProceed();
            pause(200);
            fillPayerDetails("test" + itemIdx + "@example.com", "7348308" + (100 + itemIdx));
            clickConfirm();
            pause(300);
            waitForReceiptSuccess();
            if (isReceiptVisible())
                action.recordVerification("Receipt #" + (itemIdx + 1) + " (" + paymentMode + "): " + getReceiptNumber());
            action.captureStep("Receipt " + (itemIdx + 1) + " generated (" + paymentMode + ")");
        }
        if (isReceiptVisible()) closeReceiptModal();
        action.recordVerification("Multiple receipt generation done for " + itemCount + " items.");
    }

    private void refreshLoginBeforeNextReceipt(int receiptNumber) {
        action.recordVerification("Refreshing session before receipt #" + receiptNumber + ".");
        try {
            logout();
        } catch (Exception e) {
            System.out.println("[WARN] Session refresh logout skipped: " + e.getMessage());
        }
        login();
    }

    public void processChequeDdPaymentMultipleIterations(String... chequeDetails) {
        int iterations = chequeDetails.length / 4;
        for (int i = 0; i < iterations; i++) {
            int idx = i * 4;
            processChequeDdIteration(
                chequeDetails[idx], chequeDetails[idx + 1],
                chequeDetails[idx + 2], chequeDetails[idx + 3], i + 1);
        }
    }

    // ── Private Methods ───────────────────────────────────────────────────────

    /** Single place to tune all delays */
    private void pause(long ms) {
        action.humanPause(ms);
    }

    private void selectNextItemIfNeeded(int itemIndex) {
        if (itemIndex == 0) { selectFirstRowIfNeeded(); return; }
        List<WebElement> rows = getVisibleWorkItemRows();
        if (itemIndex < rows.size()) {
            WebElement nextRow = rows.get(itemIndex);
            try {
                if (!isRowSelected(nextRow)) {
                    WebElement target = findRowSelectionTarget(nextRow);
                    if (target != null) { scrollIntoView(target); pause(150); safeClick(target); pause(300); }
                }
            } catch (Exception e) {
                System.out.println("[WARN] Row select: " + e.getMessage());
            }
        }
    }

    private WebElement waitForVisibleRowActionsButton(int itemIndex) {
        return wait.until(driver -> {
            List<WebElement> rows = getVisibleWorkItemRows();
            if (!rows.isEmpty()) {
                int safeIndex = Math.max(0, Math.min(itemIndex, rows.size() - 1));
                WebElement rowAction = findRowActionsButton(rows.get(safeIndex));
                if (rowAction != null) return rowAction;
            }
            if (itemIndex <= 0) {
                for (WebElement actionButton : driver.findElements(firstRowActionsBtn)) {
                    try { if (actionButton.isDisplayed()) return actionButton; }
                    catch (Exception ignored) {}
                }
            }
            return null;
        });
    }

    private WebElement findRowActionsButton(WebElement row) {
        for (By locator : List.of(
            By.xpath(".//button[contains(@class,'mat-mdc-menu-trigger') and not(contains(@class,'menu-btn'))]"),
            By.xpath(".//button[contains(normalize-space(),'Actions')]"),
            By.xpath(".//button[.//span[contains(normalize-space(),'more_vert') or contains(normalize-space(),'more_horiz')]]"))) {
            for (WebElement button : row.findElements(locator)) {
                try { if (button.isDisplayed()) return button; }
                catch (Exception ignored) {}
            }
        }
        return null;
    }

    private List<WebElement> getVisibleWorkItemRows() {
        List<WebElement> rows = new ArrayList<>();
        for (WebElement row : driver.findElements(tableRows)) {
            try {
                if (row.isDisplayed() && !row.getText().replaceAll("\\s+", " ").trim().isBlank())
                    rows.add(row);
            } catch (Exception ignored) {}
        }
        return rows;
    }

    private void navigateBackAndRefresh(int itemNumber) {
        driver.navigate().to(buildCollectionItemsUrl());
        pause(300);
        try { wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(tableRows)); }
        catch (Exception e) { System.out.println("[WARN] Table load: " + e.getMessage()); }
        pause(200);
    }

    private void processChequeDdIteration(String chequeNum, String ifscCode,
                                           String holderName, String bankName, int iteration) {
        System.out.println("[INFO] Cheque/DD iteration " + iteration);
        try {
            configureChequeOrDdPayment("CHEQUE", chequeNum, ifscCode, holderName, bankName);
            action.captureStep("Cheque/DD Iteration " + iteration + " - Fields Filled");
            clickProceed();
            action.captureStep("Cheque/DD Iteration " + iteration + " - Proceeded");
        } catch (Exception e) {
            System.out.println("[ERROR] Cheque/DD iteration " + iteration + ": " + e.getMessage());
        }
    }

    private boolean clickVisibleRelationshipOption(String optionText) {
        List<By> optionLocators = List.of(
            By.xpath("//mat-option[normalize-space()=\"" + escapeXpathText(optionText) + "\"]"),
            By.xpath("//option[normalize-space()=\"" + escapeXpathText(optionText) + "\"]"),
            By.xpath("//*[@role='option' and normalize-space()=\"" + escapeXpathText(optionText) + "\"]"),
            By.xpath("//*[self::li or self::div or self::span][normalize-space()=\""
                + escapeXpathText(optionText) + "\"]")
        );

        for (By optionLocator : optionLocators) {
            for (WebElement option : driver.findElements(optionLocator)) {
                try {
                    if (!option.isDisplayed()) continue;
                    scrollIntoView(option);
                    clickWithJavascriptFallback(option);
                    return true;
                } catch (Exception ignored) {}
            }
        }
        return false;
    }

    private void fillInputFieldForIteration(String label, By locator, String value) {
        fillChequeDdInput(locator, label, value);
    }

    private void selectAngularRadio(By locator, String radioLabel) {
        WebElement radio = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        WebElement clickTarget = getRadioClickTarget(radio);
        scrollIntoView(clickTarget);
        pause(150);
        clickWithJavascriptFallback(clickTarget);
        wait.until(driver -> isRadioSelected(locator));
        pause(250);
        action.recordTestData("Selected radio: " + radioLabel);
    }

    private WebElement getRadioClickTarget(WebElement radioInput) {
        try {
            String tagName = radioInput.getTagName();
            String className = radioInput.getAttribute("class");
            if ("mat-radio-button".equalsIgnoreCase(tagName)
                    || "label".equalsIgnoreCase(tagName)
                    || (className != null && className.contains("mat-mdc-radio-button"))) {
                return radioInput;
            }
            WebElement container = radioInput.findElement(By.xpath(
                "./ancestor::*[self::mat-radio-button or self::label or contains(@class,'mat-mdc-radio-button')][1]"));
            if (container.isDisplayed()) return container;
        } catch (Exception ignored) {}
        return radioInput;
    }

    private void clickWithJavascriptFallback(WebElement element) {
        action.waitForTransientFeedbackToClear();
        try {
            safeClick(element);
        } catch (Exception e) {
            jsClick(element);
        }
    }

    private void clickFreshElement(By locator, String elementName) {
        RuntimeException lastError = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
                scrollIntoView(element);
                clickWithJavascriptFallback(element);
                return;
            } catch (StaleElementReferenceException | ElementClickInterceptedException e) {
                lastError = e;
                System.out.println("[WARN] " + elementName + " click retry " + attempt + ": " + e.getMessage());
                action.waitForTransientFeedbackToClear();
            }
        }
        throw lastError == null
            ? new IllegalStateException("Unable to click " + elementName)
            : new IllegalStateException("Unable to click " + elementName, lastError);
    }

    private boolean isRadioSelected(By locator) {
        for (WebElement radio : driver.findElements(locator)) {
            try {
                WebElement container = getRadioClickTarget(radio);
                String ariaChecked = container.getAttribute("aria-checked");
                String className = container.getAttribute("class");
                if ("true".equalsIgnoreCase(ariaChecked)
                        || (className != null && (className.contains("mat-radio-checked")
                        || className.contains("mat-mdc-radio-checked")))) {
                    return true;
                }
            } catch (Exception ignored) {
                try {
                    if (radio.isSelected()
                            || "true".equalsIgnoreCase(radio.getAttribute("checked"))
                            || "true".equalsIgnoreCase(radio.getAttribute("aria-checked"))) {
                        return true;
                    }
                } catch (Exception ignoredAgain) {}
            }
        }
        return false;
    }

    private String normalizeChequeDdInstrumentType(String instrumentType) {
        String type = instrumentType == null ? "" : instrumentType.trim().toUpperCase();
        if ("DD".equals(type) || "DEMAND_DRAFT".equals(type) || "DEMAND DRAFT".equals(type)) return "DD";
        if ("CHEQUE".equals(type) || "CHECK".equals(type) || type.isBlank()) return "CHEQUE";
        throw new IllegalArgumentException("Unsupported Cheque/DD instrument type: " + instrumentType);
    }

    private void selectChequeDdInstrumentType(String instrumentType) {
        wait.until(driver -> isVisible(chequeDdNumberInput)
            || isVisible("DD".equals(instrumentType) ? ddInstrumentOption : chequeInstrumentOption));

        boolean selected = isChequeDdInstrumentSelected(instrumentType)
            || clickChequeDdInstrumentByLocator(instrumentType)
            || clickChequeDdInstrumentByScript(instrumentType);

        if (!selected || !isChequeDdInstrumentSelected(instrumentType)) {
            action.captureStep("Cheque Or DD - " + instrumentType + " subtype not selected");
            throw new IllegalStateException("Cannot select Cheque/DD subtype '" + instrumentType
                + "'. Visible fields: " + captureVisiblePaymentFieldSummary());
        }

        pause(300);
        action.recordTestData("Selected radio: " + instrumentType);
    }

    private boolean clickChequeDdInstrumentByLocator(String instrumentType) {
        By configuredLocator = "DD".equals(instrumentType) ? ddInstrumentOption : chequeInstrumentOption;
        List<By> locators = List.of(
            configuredLocator,
            chequeDdInstrumentTextOption(instrumentType),
            By.xpath("(//input[@type='radio' and @value=\"" + escapeXpathText(instrumentType) + "\"])[1]"),
            By.xpath("(//mat-radio-button[.//*[normalize-space()=\"" + escapeXpathText(instrumentType)
                + "\"] or normalize-space()=\"" + escapeXpathText(instrumentType) + "\"])[1]"),
            By.xpath("(//label[.//*[normalize-space()=\"" + escapeXpathText(instrumentType)
                + "\"] or normalize-space()=\"" + escapeXpathText(instrumentType) + "\"])[1]")
        );

        for (By locator : locators) {
            for (WebElement option : driver.findElements(locator)) {
                try {
                    if (!option.isDisplayed()) continue;
                    WebElement clickTarget = getRadioClickTarget(option);
                    scrollIntoView(clickTarget);
                    clickWithJavascriptFallback(clickTarget);
                    pause(250);
                    if (isChequeDdInstrumentSelected(instrumentType)) return true;
                } catch (Exception ignored) {}
            }
        }
        return false;
    }

    private By chequeDdInstrumentTextOption(String instrumentType) {
        return By.xpath("(//*[normalize-space()='SELECT' or normalize-space()='SELECT *']"
            + "/following::*[normalize-space()=\"" + escapeXpathText(instrumentType) + "\"])[1]");
    }

    private boolean clickChequeDdInstrumentByScript(String instrumentType) {
        try {
            return Boolean.TRUE.equals(((JavascriptExecutor) driver).executeScript(
                "const expected = arguments[0];"
                    + "const normalize = value => (value || '').replace(/\\s+/g, ' ').trim().toUpperCase();"
                    + "const visible = el => {"
                    + "  if (!el) return false;"
                    + "  const r = el.getBoundingClientRect();"
                    + "  const s = window.getComputedStyle(el);"
                    + "  return r.width > 0 && r.height > 0 && s.display !== 'none' && s.visibility !== 'hidden';"
                    + "};"
                    + "const containerFor = el => el.closest('mat-radio-button,label') || el.parentElement || el;"
                    + "const checked = el => {"
                    + "  const input = el.matches && el.matches('input[type=radio]') ? el : el.querySelector && el.querySelector('input[type=radio]');"
                    + "  const cls = String(el.className || '');"
                    + "  return (input && input.checked) || el.getAttribute('aria-checked') === 'true'"
                    + "    || cls.includes('mat-radio-checked') || cls.includes('mat-mdc-radio-checked');"
                    + "};"
                    + "const click = el => {"
                    + "  if (!el) return;"
                    + "  el.scrollIntoView && el.scrollIntoView({block:'center', inline:'nearest'});"
                    + "  ['mouseover','pointerdown','mousedown','pointerup','mouseup','click'].forEach(type =>"
                    + "    el.dispatchEvent(new MouseEvent(type, {bubbles:true, cancelable:true, view:window}))"
                    + "  );"
                    + "  if (typeof el.click === 'function') el.click();"
                    + "};"
                    + "const byInput = Array.from(document.querySelectorAll('input[type=radio]'))"
                    + "  .find(input => normalize(input.value) === expected);"
                    + "const byText = Array.from(document.querySelectorAll('mat-radio-button,label,span,div'))"
                    + "  .filter(visible)"
                    + "  .find(el => normalize(el.innerText || el.textContent) === expected);"
                    + "const target = byInput ? containerFor(byInput) : (byText ? containerFor(byText) : null);"
                    + "if (!target) return false;"
                    + "const candidates = [target, target.querySelector && target.querySelector('label'),"
                    + "  target.querySelector && target.querySelector('.mdc-radio'),"
                    + "  target.querySelector && target.querySelector('input[type=radio]')].filter(Boolean);"
                    + "candidates.forEach(click);"
                    + "return checked(target);",
                instrumentType));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isChequeDdInstrumentSelected(String instrumentType) {
        try {
            return Boolean.TRUE.equals(((JavascriptExecutor) driver).executeScript(
                "const expected = arguments[0];"
                    + "const normalize = value => (value || '').replace(/\\s+/g, ' ').trim().toUpperCase();"
                    + "const visible = el => {"
                    + "  if (!el) return false;"
                    + "  const r = el.getBoundingClientRect();"
                    + "  const s = window.getComputedStyle(el);"
                    + "  return r.width > 0 && r.height > 0 && s.display !== 'none' && s.visibility !== 'hidden';"
                    + "};"
                    + "const checked = el => {"
                    + "  const input = el.matches && el.matches('input[type=radio]') ? el : el.querySelector && el.querySelector('input[type=radio]');"
                    + "  const cls = String(el.className || '');"
                    + "  return (input && input.checked) || el.getAttribute('aria-checked') === 'true'"
                    + "    || cls.includes('mat-radio-checked') || cls.includes('mat-mdc-radio-checked');"
                    + "};"
                    + "for (const input of Array.from(document.querySelectorAll('input[type=radio]'))) {"
                    + "  if (normalize(input.value) !== expected) continue;"
                    + "  const container = input.closest('mat-radio-button,label') || input;"
                    + "  if (checked(container) || input.checked) return true;"
                    + "}"
                    + "for (const el of Array.from(document.querySelectorAll('mat-radio-button,label')).filter(visible)) {"
                    + "  if (normalize(el.innerText || el.textContent) === expected && checked(el)) return true;"
                    + "}"
                    + "return false;",
                instrumentType));
        } catch (Exception e) {
            return false;
        }
    }

    private void fillChequeDdInput(By locator, String fieldLabel, String value) {
        if (value == null || value.isBlank()) {
            action.recordVerification(fieldLabel + " skipped because value is blank.");
            return;
        }

        WebElement input = waitForVisibleEnabledInput(locator);
        scrollIntoView(input);
        enterInputValue(input, value);
        boolean valueEntered = wait.until(driver -> {
            for (WebElement current : driver.findElements(locator)) {
                try {
                    if (current.isDisplayed() && current.isEnabled()
                            && inputValuesMatch(readInputValue(current), value)) return true;
                } catch (Exception ignored) {}
            }
            return false;
        });
        if (!valueEntered) {
            WebElement current = waitForVisibleEnabledInput(locator);
            throw new IllegalStateException("Cannot enter " + fieldLabel
                + ". Expected '" + value + "' but found '" + readInputValue(current) + "'.");
        }
        action.recordTestData(fieldLabel + ": " + value);
        action.recordVerification(fieldLabel + " entered.");
    }

    private WebElement waitForVisibleEnabledInput(By locator) {
        return wait.until(driver -> {
            for (WebElement element : driver.findElements(locator)) {
                try {
                    if (element.isDisplayed() && element.isEnabled()) return element;
                } catch (Exception ignored) {}
            }
            return null;
        });
    }

    private void enterInputValue(WebElement input, String value) {
        try {
            input.click();
            input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            input.sendKeys(Keys.DELETE);
            input.clear();
            input.sendKeys(value);
            input.sendKeys(Keys.TAB);
        } catch (Exception e) {
            setInputValueWithEvents(input, value);
        }

        if (!inputValuesMatch(readInputValue(input), value)) {
            setInputValueWithEvents(input, value);
        }
    }

    private boolean inputValuesMatch(String actual, String expected) {
        String a = actual == null ? "" : actual.trim();
        String e = expected == null ? "" : expected.trim();
        return a.equals(e) || a.equalsIgnoreCase(e);
    }

    private String configValue(String key, String defaultValue) {
        String value = config.getProperty(key, defaultValue);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private void selectDirectTransferPayment() { selectPaymentMode(directTransferMode, "Pay Online(Direct Transfer)"); }
    public  void selectSendPaymentLinkMode()   { selectPaymentMode(sendPaymentLinkMode, "Send Payment Link"); }
    public  void selectQrCodePayment()         { selectPaymentMode(qrCodeMode, "QR Code"); }

    @Override
    protected void jsClick(WebElement el) {
        action.waitForTransientFeedbackToClear();
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
    }

    @Override
    protected void scrollIntoView(WebElement el) {
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({block:'center', behavior:'instant'});", el);
        pause(100); // instant scroll + small settle
    }

    private void safeClick(WebElement el) {
        action.waitForTransientFeedbackToClear();
        try { new Actions(driver).moveToElement(el).click().perform(); }
        catch (Exception e) { jsClick(el); }
    }

    private void clickVisiblePayButton() {
        action.waitForTransientFeedbackToClear();
        Boolean clicked = wait.until(driver -> (Boolean) ((JavascriptExecutor) driver).executeScript(
            "const normalize = value => (value || '').replace(/\\s+/g, ' ').trim();"
                + "const isVisible = el => {"
                + "  const rect = el.getBoundingClientRect();"
                + "  const style = window.getComputedStyle(el);"
                + "  return rect.width > 0 && rect.height > 0 && style.display !== 'none' && style.visibility !== 'hidden';"
                + "};"
                + "const scrollables = Array.from(document.querySelectorAll('html, body, .cdk-overlay-pane, mat-dialog-container, .modal-wrapper, .modal-content, [class*=modal], [class*=dialog]'))"
                + "  .filter(el => el && el.scrollHeight > el.clientHeight);"
                + "scrollables.forEach(el => { el.scrollTop = el.scrollHeight; });"
                + "const buttons = Array.from(document.querySelectorAll('button')).filter(isVisible);"
                + "const target = buttons.find(button => {"
                + "  const text = normalize(button.innerText || button.textContent);"
                + "  return /^Pay(\\s|$)/i.test(text) && !button.disabled;"
                + "});"
                + "if (!target) return false;"
                + "target.scrollIntoView({ block: 'center', inline: 'nearest' });"
                + "['mouseover','pointerdown','mousedown','pointerup','mouseup','click'].forEach(type =>"
                + "  target.dispatchEvent(new MouseEvent(type, { bubbles: true, cancelable: true, view: window }))"
                + ");"
                + "return true;"
        ));

        if (!Boolean.TRUE.equals(clicked)) {
            WebElement pay = waitForVisibleElement(modalPayBtn);
            scrollIntoView(pay);
            jsClick(pay);
        }
        pause(500);
    }

    private boolean shouldOpenLoginPage(String appUrl) {
        String currentUrl = driver.getCurrentUrl();
        return currentUrl == null || currentUrl.isBlank() || !currentUrl.equalsIgnoreCase(appUrl.trim());
    }

    private void selectFirstRowIfNeeded() {
        WebElement row = wait.until(driver -> driver.findElements(tableRows).stream()
            .filter(el -> { try { return el.isDisplayed() && !el.getText().trim().isBlank(); } catch (Exception e) { return false; } })
            .findFirst().orElse(null));
        if (row == null || isRowSelected(row)) return;
        WebElement target = findRowSelectionTarget(row);
        if (target != null) { safeClick(target); pause(300); action.recordVerification("First row selected."); }
    }

    private WebElement waitForVisibleElement(By locator) {
        return wait.until(driver -> {
            for (WebElement c : driver.findElements(locator)) {
                try { if (c.isDisplayed()) return c; } catch (Exception ignored) {}
            }
            return null;
        });
    }

    private void waitForPaymentFlowToLoad() {
        wait.until(driver -> {
            if (!driver.findElements(By.xpath(
                "//mat-radio-group[@name='repaymentType'] | //input[@name='repaymentType']")).isEmpty())
                return true;
            if (isVisible(partialPaymentRadio) || isVisible(amountInput)
                    || isVisible(cashRadio) || isVisible(paymentCollectionTitle)
                    || isVisible(proceedBtn)) return true;
            String b = driver.findElement(By.tagName("body")).getText().replaceAll("\\s+", " ").trim();
            return b.contains("Partial") || b.contains("Full Payment") || b.contains("PAY BY")
                || b.contains("Payment Collection");
        });
    }

    private boolean openCollectionItemsFromMenu() {
        for (By menuLocator : List.of(accountsMenu, collectionsMenu)) {
            List<WebElement> menuMatches = driver.findElements(menuLocator);
            if (menuMatches.isEmpty()) continue;
            WebElement menu = menuMatches.get(0);
            action.waitForTransientFeedbackToClear();
            scrollIntoView(menu);
            try { new Actions(driver).moveToElement(menu).pause(Duration.ofMillis(300)).perform(); }
            catch (Exception e) { safeClick(menu); }
            pause(600);
            List<WebElement> links = driver.findElements(collectionItemsLink);
            if (!links.isEmpty()) { safeClick(links.get(0)); return true; }
            if ("Collections".equalsIgnoreCase(menu.getText().trim())) { safeClick(menu); return true; }
        }
        return false;
    }

    private String buildCollectionItemsUrl() {
        String baseUrl = config.getURL().trim();
        int routeIndex = baseUrl.indexOf("#/");
        if (routeIndex > -1)
            return baseUrl.substring(0, routeIndex)
                + "#/collectionsitem?page=0&size=10&sort=dpd,desc&openFlag=true&taskCategories=COLLECT";
        return baseUrl;
    }

    private boolean hasCollectionItemsAvailable() {
        if (getVisibleWorkItemRowCount() > 0) return true;
        int count = getCollectionCount();
        if (count >= 0) return count > 0;
        return !driver.findElements(firstRowActionsBtn).isEmpty() || !driver.findElements(tableRows).isEmpty();
    }

    private String getCollectionCountSummary() {
        String paginatorText = getVisiblePaginatorSummaryText();
        int rowCount = getVisibleWorkItemRowCount();
        String labelText = getVisibleCollectionCountLabel();
        if (!paginatorText.isBlank() && rowCount > 0) return rowCount + " visible; paginator: " + paginatorText;
        if (!paginatorText.isBlank()) return paginatorText;
        if (rowCount > 0)            return rowCount + " visible work item(s)";
        return labelText;
    }

    private int getCollectionCount() {
        int pc = getPaginatorItemCount();
        if (pc >= 0) return pc;
        int rc = getVisibleWorkItemRowCount();
        if (rc > 0)  return rc;
        String ct = getVisibleCollectionCountLabel();
        if (ct.isBlank()) return -1;
        String digits = ct.replaceAll("\\D+", "");
        if (digits.isBlank()) return -1;
        try { return Integer.parseInt(digits); } catch (NumberFormatException e) { return -1; }
    }

    private int getVisibleWorkItemRowCount() {
        return getVisibleWorkItemRows().size();
    }

    private int getPaginatorItemCount() {
        String pt = getVisiblePaginatorSummaryText();
        if (!pt.isBlank() && pt.matches(".*\\bof\\s+\\d+.*")) {
            try { return Integer.parseInt(pt.replaceFirst(".*\\bof\\s+(\\d+).*", "$1")); }
            catch (NumberFormatException ignored) {}
        }
        for (WebElement s : driver.findElements(paginatorSummary)) {
            try {
                if (!s.isDisplayed()) continue;
                String t = s.getText().replaceAll("\\s+", " ").trim();
                if (t.matches(".*\\bof\\s+\\d+.*"))
                    return Integer.parseInt(t.replaceFirst(".*\\bof\\s+(\\d+).*", "$1"));
            } catch (Exception ignored) {}
        }
        return -1;
    }

    private String getVisibleCollectionCountLabel() {
        for (WebElement label : driver.findElements(collectionCountLabel)) {
            try {
                if (!label.isDisplayed()) continue;
                String text = sanitizeUiText(label.getText());
                if (!text.isBlank()) return text;
            } catch (Exception ignored) {}
        }
        return "";
    }

    private By paymentModeLocator(String modeLabel) {
        String l = modeLabel == null ? "" : modeLabel.trim();
        return By.xpath("//*[self::label or self::span or self::div or self::mat-radio-button]"
            + "[contains(normalize-space(),\"" + escapeXpathText(l) + "\")]");
    }

    private void selectPaymentMode(By locator, String modeName) {
        WebElement option = waitForVisibleElement(locator);
        scrollIntoView(option);
        pause(200);
        safeClick(option);
        pause(500);
        assertNoSystemError(modeName + " selection");
        action.recordVerification(modeName + " selected.");
    }

    private void selectPaymentBank(String bankName) {
        if (bankName == null || bankName.isBlank()) return;
        if (driver.findElements(paymentBankDropdown).isEmpty()) { action.recordVerification("Bank field not visible."); return; }
        WebElement bankField = waitForVisibleElement(paymentBankDropdown);
        scrollIntoView(bankField);
        try {
            if ("select".equalsIgnoreCase(bankField.getTagName())) {
                new Select(bankField).selectByVisibleText(bankName);
            } else {
                safeClick(bankField);
                safeClick(waitForVisibleElement(By.xpath(
                    "//mat-option[normalize-space()='" + escapeXpathText(bankName) + "']"
                    + " | //option[normalize-space()='" + escapeXpathText(bankName) + "']")));
            }
            assertNoSystemError("Bank selection");
            action.recordVerification("Bank: " + bankName);
        } catch (Exception e) { throw new IllegalStateException("Cannot select bank '" + bankName + "'.", e); }
    }

    private void enterPaymentReference(String r) { typeIfVisible(paymentReferenceInput, r, "Payment Reference"); }
    private void enterInstrumentNumber(String n)  { typeIfVisible(paymentInstrumentInput, n, "Cheque/DD Number"); }
    private void enterPaymentDate(String d)        { typeIfVisible(paymentDateInput, d, "Payment Date"); }

    private void typeIfVisible(By locator, String value, String fieldLabel) {
        if (value == null || value.isBlank()) return;
        if (driver.findElements(locator).isEmpty()) { action.recordVerification(fieldLabel + " not visible."); return; }
        WebElement input = waitForVisibleElement(locator);
        scrollIntoView(input);
        try {
            input.click();
            input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            input.sendKeys(Keys.DELETE);
            input.sendKeys(value);
            input.sendKeys(Keys.TAB);
            assertNoSystemError(fieldLabel + " entry");
            action.recordTestData(fieldLabel + ": " + value);
        } catch (Exception e) { throw new IllegalStateException("Cannot enter " + fieldLabel + ".", e); }
    }

    private boolean isPaymentLinkPreviewVisible() { return isVisible(paymentLinkPreview); }
    private boolean isQrCodeVisible()             { return isVisible(qrCodePreview); }

    private String captureVisiblePaymentFieldSummary() {
        List<String> labels = new ArrayList<>();
        for (WebElement element : driver.findElements(visiblePaymentFields)) {
            try {
                if (!element.isDisplayed()) continue;
                String ph = element.getAttribute("placeholder");
                String text = sanitizeUiText(ph != null && !ph.isBlank() ? ph : element.getText());
                if (!text.isBlank() && labels.stream().noneMatch(e -> e.equalsIgnoreCase(text))) labels.add(text);
            } catch (Exception ignored) {}
        }
        return labels.isEmpty() ? "No visible fields" : String.join(" | ", labels);
    }

    private String getVisiblePaginatorSummaryText() {
        for (WebElement s : driver.findElements(paginatorSummary)) {
            try {
                if (!s.isDisplayed()) continue;
                String snippet = extractPaginatorSummary(sanitizeUiText(s.getText()));
                if (!snippet.isBlank()) return snippet;
            } catch (Exception ignored) {}
        }
        return "";
    }

    private DenominationSelection findBestDenominationSelection() {
        WebElement amountEl = waitForVisibleElement(amountInput);
        BigDecimal paymentAmount = parseDecimal(readInputValue(amountEl));
        DenominationSelection exactMatch = null, divisibleMatch = null;

        for (WebElement candidate : driver.findElements(denominationInputs)) {
            try {
                if (!candidate.isDisplayed()) continue;
                BigDecimal dv = resolveDenominationValue(candidate);
                if (dv == null || dv.compareTo(BigDecimal.ZERO) <= 0) continue;

                if (paymentAmount != null && paymentAmount.compareTo(dv) == 0)
                    return new DenominationSelection(candidate, formatWholeNumber(dv), "1");

                if (paymentAmount != null && paymentAmount.compareTo(dv) > 0
                        && paymentAmount.remainder(dv).compareTo(BigDecimal.ZERO) == 0) {
                    if (divisibleMatch == null || dv.compareTo(parseDecimal(divisibleMatch.label())) > 0)
                        divisibleMatch = new DenominationSelection(candidate,
                            formatWholeNumber(dv), formatWholeNumber(paymentAmount.divide(dv)));
                    continue;
                }
                if (exactMatch == null)
                    exactMatch = new DenominationSelection(candidate, formatWholeNumber(dv), "1");
            } catch (Exception ignored) {}
        }
        return divisibleMatch != null ? divisibleMatch : exactMatch;
    }

    private BigDecimal resolveDenominationValue(WebElement input) {
        WebElement current = input;
        for (int i = 0; i < 8; i++) {
            try {
                BigDecimal v = extractFirstWholeNumber(sanitizeUiText(current.getText()));
                if (v != null && v.compareTo(BigDecimal.ZERO) > 0) return v;
                current = current.findElement(By.xpath(".."));
            } catch (Exception e) { return null; }
        }
        return null;
    }

    private BigDecimal extractFirstWholeNumber(String value) {
        if (value == null || value.isBlank()) return null;
        Matcher m = Pattern.compile("\\b\\d+\\b").matcher(value);
        return m.find() ? new BigDecimal(m.group()) : null;
    }

    private BigDecimal parseDecimal(String value) {
        String n = normalizeNumericValue(value);
        if (n.isBlank()) return null;
        try { return new BigDecimal(n); } catch (NumberFormatException e) { return null; }
    }

    private String formatWholeNumber(BigDecimal v) {
        return v == null ? "" : v.stripTrailingZeros().toPlainString();
    }

    private String extractReceiptNumber(String text) {
        if (text == null || text.isBlank()) return "";
        Matcher m = Pattern.compile("\\b[A-Z0-9]{2,}(?:-[A-Z0-9]{2,}){2,}\\b").matcher(text);
        return m.find() ? m.group() : "";
    }

    private String extractPaginatorSummary(String text) {
        if (text == null || text.isBlank()) return "";
        Matcher rm = Pattern.compile("\\b\\d+\\s*[–-]\\s*\\d+\\s+of\\s+\\d+\\b").matcher(text);
        if (rm.find()) return rm.group().replaceAll("\\s+", " ").trim();
        Matcher tm = Pattern.compile("\\b\\d+\\s+of\\s+\\d+\\b").matcher(text);
        return tm.find() ? tm.group().replaceAll("\\s+", " ").trim() : "";
    }

    private String sanitizeUiText(String v)  { return v == null ? "" : v.replaceAll("\\s+", " ").trim(); }
    private String escapeXpathText(String v) { return v == null ? "" : v.replace("\"", "\\\""); }

    private void assertNoSystemError(String context) {
        String errorMessage = readVisibleText(errorToast);
        if (!errorMessage.isBlank())
            throw new IllegalStateException(context + " failed: " + errorMessage);
    }

    private String readVisibleText(By locator) {
        for (WebElement element : driver.findElements(locator)) {
            try {
                if (!element.isDisplayed()) continue;
                String text = sanitizeUiText(element.getText());
                if (!text.isBlank()) return text;
            } catch (Exception ignored) {}
        }
        return "";
    }

    private record DenominationSelection(WebElement input, String label, String quantity) {}

    private String maskMobile(String mobile) {
        if (mobile == null || mobile.isBlank()) return "";
        String digits = mobile.replaceAll("\\D+", "");
        return digits.length() <= 4 ? digits : digits.substring(digits.length() - 4);
    }

    private WebElement findRowSelectionTarget(WebElement row) {
        for (By target : List.of(
            By.xpath(".//*[@role='checkbox']"),
            By.xpath(".//input[@type='checkbox']/ancestor::*[self::label or self::span or self::div][1]"),
            By.xpath(".//input[@type='checkbox']"),
            By.xpath(".//td[1]"))) {
            for (WebElement c : row.findElements(target)) {
                try { if (c.isDisplayed()) return c; } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private boolean isRowSelected(WebElement row) {
        try {
            for (WebElement cb : row.findElements(By.xpath(".//*[@role='checkbox'] | .//input[@type='checkbox']"))) {
                if (!cb.isDisplayed()) continue;
                if ("true".equalsIgnoreCase(cb.getAttribute("aria-checked"))
                        || "true".equalsIgnoreCase(cb.getAttribute("checked"))
                        || "true".equalsIgnoreCase(cb.getAttribute("aria-selected"))) return true;
            }
            String markup = row.getAttribute("outerHTML");
            return markup != null && (markup.contains("mat-mdc-checkbox-checked") || markup.contains("aria-checked=\"true\""));
        } catch (Exception e) { return false; }
    }

    private boolean isVisible(By locator) {
        try {
            for (WebElement el : driver.findElements(locator)) if (el.isDisplayed()) return true;
        } catch (Exception ignored) {}
        return false;
    }

    private void clearAmountField(WebElement input) {
        scrollIntoView(input);
        pause(100);
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
        pause(100);
        if (!readInputValue(input).isBlank()) { input.sendKeys(Keys.chord(Keys.CONTROL, "a")); input.sendKeys(Keys.BACK_SPACE); pause(100); }
        if (!readInputValue(input).isBlank()) { setInputValueWithEvents(input, ""); pause(100); }
        wait.until(driver -> readInputValue(input).isBlank());
        input.sendKeys(Keys.TAB);
    }

    private void setAmountFieldValue(WebElement input, String amount) {
        clearAmountField(input);
        pause(100);
        try { input.click(); input.sendKeys(amount); input.sendKeys(Keys.TAB); }
        catch (InvalidElementStateException e) { setInputValueWithEvents(input, amount); input.sendKeys(Keys.TAB); }
        pause(150);
        String actual = readInputValue(input);
        if (!numericValuesMatch(actual, amount)) {
            setInputValueWithEvents(input, amount);
            input.sendKeys(Keys.TAB);
            pause(150);
            actual = readInputValue(input);
        }
        if (!numericValuesMatch(actual, amount))
            throw new IllegalStateException("Cannot set amount. Expected " + amount + " got '" + actual + "'.");
    }

    private void setInputValueWithEvents(WebElement input, String value) {
        ((JavascriptExecutor) driver).executeScript(
            "const input = arguments[0];"
            + "const value = arguments[1];"
            + "input.focus();"
            + "const setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;"
            + "setter.call(input, value);"
            + "input.dispatchEvent(new Event('input',{bubbles:true}));"
            + "input.dispatchEvent(new Event('change',{bubbles:true}));"
            + "input.dispatchEvent(new Event('blur',{bubbles:true}));",
            input, value);
    }

    private String readInputValue(WebElement input) {
        try { String v = input.getDomProperty("value"); if (v != null) return v.trim(); }
        catch (Exception ignored) {}
        try { String v = input.getAttribute("value"); return v == null ? "" : v.trim(); }
        catch (Exception ignored) { return ""; }
    }

    private boolean numericValuesMatch(String actual, String expected) {
        if (normalizeNumericValue(actual).equals(normalizeNumericValue(expected))) return true;
        return actual != null && actual.trim().equals(expected == null ? "" : expected.trim());
    }

    private String normalizeNumericValue(String value) {
        if (value == null) return "";
        String c = value.replaceAll("[^0-9.\\-]", "").trim();
        if (c.isBlank()) return "";
        try { return new BigDecimal(c).stripTrailingZeros().toPlainString(); }
        catch (NumberFormatException e) { return c; }
    }
}
