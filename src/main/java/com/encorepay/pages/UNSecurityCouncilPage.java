package com.encorepay.pages;

import java.io.IOException;
import java.nio.file.Path;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.encorepay.utilities.DownloadFileUtil;
import com.encorepay.utilities.ScreenshotUtil;

public class UNSecurityCouncilPage extends BasePage {

    private static final By XML_LINK =
            By.cssSelector("a.documentlinks[href*='consolidated.xml']");
    private static final By PAGE_LOAD_LOCATOR =
            By.cssSelector("body");

    private final DownloadFileUtil downloadFileUtil;

    public UNSecurityCouncilPage(WebDriver driver) {
        super(driver);
        this.downloadFileUtil = new DownloadFileUtil();
    }

    public String openPage() {
        try {
            System.out.println("[INFO] Opening UN website...");

            driver.get(config.getProperty("un.security.council.url"));

            waitForPageLoad();

            wait.until(ExpectedConditions.presenceOfElementLocated(PAGE_LOAD_LOCATOR));
            wait.until(ExpectedConditions.visibilityOfElementLocated(XML_LINK));

            System.out.println("[INFO] Waiting for page...");
            System.out.println("[INFO] Page loaded successfully.");

            return driver.getCurrentUrl();

        } catch (NoSuchElementException e) {
            System.out.println("[ERROR] Page could not be opened: " + e.getMessage());
            ScreenshotUtil.captureCurrentTestStep(driver, "openPage_FAILED");
            throw e;

        } catch (TimeoutException e) {
            System.out.println("[ERROR] Page load timed out: " + e.getMessage());
            ScreenshotUtil.captureCurrentTestStep(driver, "openPage_TIMEOUT");
            throw e;
        }
    }

    public String downloadXml() {
        try {
            System.out.println("[INFO] Finding XML link...");

            WebElement xmlLink =
                    wait.until(ExpectedConditions.visibilityOfElementLocated(XML_LINK));

            String sourceUrl = extractXmlUrl(xmlLink);

            System.out.println("[INFO] XML link found: " + sourceUrl);

            scrollIntoView(xmlLink);

            wait.until(ExpectedConditions.elementToBeClickable(xmlLink));

            System.out.println("[INFO] Clicking XML");

            try {
                xmlLink.click();
            } catch (Exception clickException) {
                System.out.println("[WARN] Standard click failed, using JavaScript click.");
                jsClick(xmlLink);
            }

            System.out.println("[INFO] Downloading XML directly via HTTP");

            Path finalFile = downloadFileUtil.downloadFileDirectly(
                    sourceUrl,
                    Path.of(createTodayFolder()).resolve("consolidated.xml"));

            downloadFileUtil.cleanupDownloadArtifacts();

            System.out.println("[INFO] XML stored successfully at: "
                    + finalFile.toAbsolutePath());

            return finalFile.toAbsolutePath().toString();

        } catch (NoSuchElementException e) {
            System.out.println("[ERROR] XML download link was not found: " + e.getMessage());
            ScreenshotUtil.captureCurrentTestStep(driver, "downloadXml_LINK_NOT_FOUND");
            throw e;

        } catch (TimeoutException e) {
            System.out.println("[ERROR] XML download action timed out: " + e.getMessage());
            ScreenshotUtil.captureCurrentTestStep(driver, "downloadXml_TIMEOUT");
            throw e;

        } catch (Exception e) {
            System.out.println("[ERROR] XML download failed: " + e.getMessage());
            ScreenshotUtil.captureCurrentTestStep(driver, "downloadXml_FAILED");
            throw new RuntimeException(e);
        }
    }

    public String createTodayFolder() throws IOException {
        return downloadFileUtil.createTodayFolder();
    }

    private String extractXmlUrl(WebElement xmlLink) {

        String href = xmlLink.getAttribute("href");

        if (href == null || href.isBlank()) {
            throw new IllegalArgumentException("XML link href attribute is empty or null");
        }

        if (href.startsWith("http://") || href.startsWith("https://")) {
            return href;
        }

        String baseUrl = driver.getCurrentUrl();

        String domain = baseUrl.split("/", 4)[0] + "//"
                + baseUrl.split("/", 4)[2];

        if (href.startsWith("/")) {
            return domain + href;
        }

        String basePath =
                baseUrl.substring(0, baseUrl.lastIndexOf("/") + 1);

        return basePath + href;
    }
}