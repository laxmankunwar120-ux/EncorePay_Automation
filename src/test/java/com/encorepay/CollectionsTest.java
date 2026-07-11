package com.encorepay;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.encorepay.base.BaseClass;
import com.encorepay.pages.LoginPage;

public class CollectionsTest extends BaseClass {

    private static final By COLLECTIONS_MENU = By.xpath(
        "//button[contains(normalize-space(),'Collections')]"
            + " | //a[contains(normalize-space(),'Collections')]"
            + " | //span[normalize-space()='Collections']/ancestor::*[self::a or self::button][1]");

    private LoginPage loginPage;
    private Path outputPath;

    @BeforeClass(alwaysRun = true)
    public void setupCollectionsTest() throws IOException {
        loginPage = new LoginPage(driver);
        outputPath = Paths.get(System.getProperty("user.dir"), "target", "collections-test.txt");
        Files.createDirectories(outputPath.getParent());
    }

    @Test(description = "Verify Collections menu opens and shows related workflow options")
    public void verifyCollectionsMenu() throws IOException {
        loginPage.login(prop.getProperty("username"), prop.getProperty("password"));
        Assert.assertTrue(loginPage.isLoginSuccessful(), "Login must succeed before menu inspection.");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(12));
        WebElement collectionsMenu = wait.until(driver -> {
            for (WebElement candidate : driver.findElements(COLLECTIONS_MENU)) {
                try {
                    if (candidate.isDisplayed()) {
                        return candidate;
                    }
                } catch (Exception ignored) {
                }
            }
            return null;
        });

        StringBuilder report = new StringBuilder();
        report.append("Current URL before interaction: ").append(driver.getCurrentUrl()).append(System.lineSeparator());
        report.append(System.lineSeparator()).append("Visible navigation elements before interaction").append(System.lineSeparator());
        appendLines(report, collectVisibleNavigationElements());
        report.append(System.lineSeparator()).append("Collections element").append(System.lineSeparator());
        report.append(describeElement(collectionsMenu)).append(System.lineSeparator());

        new Actions(driver).moveToElement(collectionsMenu).pause(Duration.ofMillis(300)).perform();
        sleep(1200);
        report.append(System.lineSeparator()).append("Visible Batch/Deposit/Acknowledge elements after hover").append(System.lineSeparator());
        appendLines(report, collectVisibleKeywordElements());
        report.append(System.lineSeparator()).append("Visible Allocation/Collection elements after hover").append(System.lineSeparator());
        appendLines(report, collectAllocationKeywordElements());
        report.append(System.lineSeparator()).append("Hidden Allocation/Collection elements (router-link) after hover").append(System.lineSeparator());
        appendLines(report, collectHiddenAllocationElements());

        clickWithJs(collectionsMenu);
        sleep(1500);
        report.append(System.lineSeparator()).append("Current URL after Collections click: ").append(driver.getCurrentUrl()).append(System.lineSeparator());
        report.append(System.lineSeparator()).append("Visible navigation elements after Collections click").append(System.lineSeparator());
        appendLines(report, collectVisibleNavigationElements());
        report.append(System.lineSeparator()).append("Visible Batch/Deposit/Acknowledge elements after Collections click").append(System.lineSeparator());
        appendLines(report, collectVisibleKeywordElements());
        report.append(System.lineSeparator()).append("Visible Allocation/Collection elements after Collections click").append(System.lineSeparator());
        appendLines(report, collectAllocationKeywordElements());
        report.append(System.lineSeparator()).append("Hidden Allocation/Collection elements (router-link) after Collections click").append(System.lineSeparator());
        appendLines(report, collectHiddenAllocationElements());
        report.append(System.lineSeparator()).append("Page text snippet after Collections click").append(System.lineSeparator());
        report.append(readBodySnippet());

        Files.writeString(outputPath, report.toString());
        System.out.println(report);
        System.out.println("Collections test report written to " + outputPath.toAbsolutePath());
    }

    @SuppressWarnings("unchecked")
    private List<String> collectVisibleNavigationElements() {
        List<Map<String, Object>> elements = (List<Map<String, Object>>) ((JavascriptExecutor) driver).executeScript(
            "return Array.from(document.querySelectorAll('button,a,span'))"
                + ".filter(el => {"
                + "  const text = ((el.innerText || el.textContent || '') + '').replace(/\\s+/g, ' ').trim();"
                + "  if (!text || text.length > 40) { return false; }"
                + "  const rect = el.getBoundingClientRect();"
                + "  const style = window.getComputedStyle(el);"
                + "  return rect.width > 0 && rect.height > 0 && style.visibility !== 'hidden' && style.display !== 'none';"
                + "})"
                + ".slice(0, 40)"
                + ".map(el => ({"
                + "  tag: el.tagName,"
                + "  text: ((el.innerText || el.textContent || '') + '').replace(/\\s+/g, ' ').trim(),"
                + "  href: el.getAttribute('href') || '',"
                + "  role: el.getAttribute('role') || '',"
                + "  cls: (el.className || '').toString()"
                + "}));");
        return formatElementMaps(elements);
    }

    @SuppressWarnings("unchecked")
    private List<String> collectVisibleKeywordElements() {
        List<Map<String, Object>> elements = (List<Map<String, Object>>) ((JavascriptExecutor) driver).executeScript(
            "const pattern = /(batch|deposit|acknowledge)/i;"
                + "return Array.from(document.querySelectorAll('button,a,span,div,li,mat-option,mat-menu-item'))"
                + ".filter(el => {"
                + "  const text = ((el.innerText || el.textContent || '') + '').replace(/\\s+/g, ' ').trim();"
                + "  if (!text || text.length > 120 || !pattern.test(text)) { return false; }"
                + "  const rect = el.getBoundingClientRect();"
                + "  const style = window.getComputedStyle(el);"
                + "  return rect.width > 0 && rect.height > 0 && style.visibility !== 'hidden' && style.display !== 'none';"
                + "})"
                + ".slice(0, 60)"
                + ".map(el => ({"
                + "  tag: el.tagName,"
                + "  text: ((el.innerText || el.textContent || '') + '').replace(/\\s+/g, ' ').trim(),"
                + "  href: el.getAttribute('href') || '',"
                + "  router: el.getAttribute('ng-reflect-router-link') || '',"
                + "  role: el.getAttribute('role') || '',"
                + "  cls: (el.className || '').toString(),"
                + "  outer: (el.outerHTML || '').replace(/\\s+/g, ' ').trim().slice(0, 280)"
                + "}));");
        return formatElementMaps(elements);
    }

    private List<String> formatElementMaps(List<Map<String, Object>> elements) {
        List<String> lines = new ArrayList<>();
        if (elements == null || elements.isEmpty()) {
            lines.add("No visible matching elements were found.");
            return lines;
        }

        for (Map<String, Object> element : elements) {
            lines.add(
                "tag=" + valueOf(element.get("tag"))
                    + " | text=" + valueOf(element.get("text"))
                    + " | role=" + valueOf(element.get("role"))
                    + " | href=" + valueOf(element.get("href"))
                    + " | router=" + valueOf(element.get("router"))
                    + " | class=" + valueOf(element.get("cls"))
                    + (valueOf(element.get("outer")).isBlank() ? "" : " | outer=" + valueOf(element.get("outer")))
            );
        }
        return lines;
    }

    private String describeElement(WebElement element) {
        return "tag=" + element.getTagName()
            + " | text=" + sanitize(element.getText())
            + " | class=" + sanitize(element.getAttribute("class"))
            + " | href=" + sanitize(element.getAttribute("href"))
            + " | outer=" + sanitize((String) ((JavascriptExecutor) driver)
                .executeScript("return (arguments[0].outerHTML || '').replace(/\\s+/g, ' ').trim().slice(0, 320);", element));
    }

    private void appendLines(StringBuilder report, List<String> lines) {
        for (String line : lines) {
            report.append(line).append(System.lineSeparator());
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> collectAllocationKeywordElements() {
        List<Map<String, Object>> elements = (List<Map<String, Object>>) ((JavascriptExecutor) driver).executeScript(
            "const pattern = /(allocation|collection item|collectionitems|allocate)/i;"
                + "return Array.from(document.querySelectorAll('button,a,span,div,li,mat-option,mat-menu-item'))"
                + ".filter(el => {"
                + "  const text = ((el.innerText || el.textContent || '') + '').replace(/\\s+/g, ' ').trim();"
                + "  if (!text || text.length > 120 || !pattern.test(text)) { return false; }"
                + "  const rect = el.getBoundingClientRect();"
                + "  const style = window.getComputedStyle(el);"
                + "  return rect.width > 0 && rect.height > 0 && style.visibility !== 'hidden' && style.display !== 'none';"
                + "})"
                + ".slice(0, 60)"
                + ".map(el => ({"
                + "  tag: el.tagName,"
                + "  text: ((el.innerText || el.textContent || '') + '').replace(/\\s+/g, ' ').trim(),"
                + "  href: el.getAttribute('href') || '',"
                + "  router: el.getAttribute('ng-reflect-router-link') || '',"
                + "  role: el.getAttribute('role') || '',"
                + "  cls: (el.className || '').toString(),"
                + "  outer: (el.outerHTML || '').replace(/\\s+/g, ' ').trim().slice(0, 280)"
                + "}));");
        return formatElementMaps(elements);
    }

    @SuppressWarnings("unchecked")
    private List<String> collectHiddenAllocationElements() {
        List<Map<String, Object>> elements = (List<Map<String, Object>>) ((JavascriptExecutor) driver).executeScript(
            "const pattern = /(allocation|collection item|collectionitems|allocate)/i;"
                + "return Array.from(document.querySelectorAll('button,a,span,div,li,mat-option,mat-menu-item,[routerlink],[ng-reflect-router-link]'))"
                + ".filter(el => {"
                + "  const text = ((el.innerText || el.textContent || '') + '').replace(/\\s+/g, ' ').trim();"
                + "  const router = (el.getAttribute('routerlink') || el.getAttribute('ng-reflect-router-link') || '') + '';"
                + "  if (!text && !router) { return false; }"
                + "  if (text.length > 200) { return false; }"
                + "  return pattern.test(text) || pattern.test(router);"
                + "})"
                + ".slice(0, 30)"
                + ".map(el => ({"
                + "  tag: el.tagName,"
                + "  text: ((el.innerText || el.textContent || '') + '').replace(/\\s+/g, ' ').trim().slice(0, 120),"
                + "  href: el.getAttribute('href') || '',"
                + "  router: el.getAttribute('ng-reflect-router-link') || '',"
                + "  routerlink: el.getAttribute('routerlink') || '',"
                + "  cls: (el.className || '').toString(),"
                + "  outer: (el.outerHTML || '').replace(/\\s+/g, ' ').trim().slice(0, 400)"
                + "}));");
        return formatElementMaps(elements);
    }

    private String readBodySnippet() {
        String text = driver.findElement(By.tagName("body")).getText().replaceAll("\\s+", " ").trim();
        if (text.length() > 600) {
            return text.substring(0, 600) + "...";
        }
        return text;
    }

    private void clickWithJs(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String sanitize(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }

    private String valueOf(Object value) {
        return value == null ? "" : sanitize(String.valueOf(value));
    }
}
