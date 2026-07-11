package com.encorepay;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.PageLoadStrategy;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.encorepay.config.ConfigLoader;
import com.encorepay.pages.UNSecurityCouncilPage;
import com.encorepay.utilities.ScreenshotUtil;

import io.github.bonigarcia.wdm.WebDriverManager;

public class UNSecurityCouncilDownloadTest {

    private WebDriver driver;
    private ConfigLoader config;

     @BeforeTest(alwaysRun = true)
    public void setUpUnTest() {
        config = ConfigLoader.getInstance();
        driver = createChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        driver.manage().window().maximize();
        System.out.println("[INFO] Chrome browser initialized for UN XML download test.");
    }

    @AfterTest(alwaysRun = true)
    public void tearDownUnTest() {
        if (driver != null) {
            try {
                System.out.println("[INFO] Closing browser.");
                driver.quit();
            } catch (Exception e) {
                System.out.println("[WARN] Error closing browser: " + e.getMessage());
            } finally {
                driver = null;
            }
        }
    }

    @Test(description = "Download and store the latest UN Security Council XML file")
    public void downloadLatestXmlFile() {
        try {
            UNSecurityCouncilPage page = new UNSecurityCouncilPage(driver);

            String pageUrl = page.openPage();
            Assert.assertTrue(pageUrl.contains("securitycouncil"),
                    "The UN Security Council page did not load.");

            String downloadedPath = page.downloadXml();
            Assert.assertNotNull(downloadedPath, "XML download path is null.");
            Assert.assertTrue(downloadedPath.contains(".xml") || downloadedPath.contains("consolidated"),
                    "The XML download path was not resolved correctly.");

            String finalPath = downloadedPath;
            Assert.assertTrue(finalPath.contains("downloads")
                    && finalPath.endsWith("consolidated.xml"),
                    "The XML file was not moved to the expected project folder.");

            Path xmlFile = Paths.get(finalPath);
            Assert.assertTrue(Files.exists(xmlFile), "The final XML file does not exist.");
            Assert.assertTrue(Files.size(xmlFile) > 0, "The final XML file is empty.");

            System.out.println("[INFO] Final file path: " + finalPath);
        } catch (Exception e) {
            System.out.println("[ERROR] Test failed: " + e.getMessage());
            ScreenshotUtil.captureCurrentTestStep(driver, "UN_XML_TEST_FAILED");
            throw new AssertionError("UN XML download test failed: " + e.getMessage(), e);
        }
    }

    private WebDriver createChromeDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setPageLoadStrategy(PageLoadStrategy.EAGER);

        chromeOptions.addArguments(
                "--disable-notifications",
                "--disable-geolocation",
                "--disable-popup-blocking",
                "--remote-allow-origins=*",
                "--safebrowsing-disable-download-protection",
                "--safebrowsing-disable-extension-blacklist",
                "--disable-features=DownloadBubble,DownloadBubbleV2,DownloadShelf",
                "--allow-running-insecure-content",
                "--disable-blink-features=AutomationControlled");

        java.util.Map<String, Object> prefs = new java.util.HashMap<>();
        prefs.put("profile.default_content_setting_values.automatic_downloads", 1);
        prefs.put("download.prompt_for_download", false);
        prefs.put("download.directory_upgrade", true);
        prefs.put("download.extensions_to_open", "");
        prefs.put("safebrowsing.enabled", false);
        prefs.put("safebrowsing.disable_download_protection", true);
        prefs.put("safebrowsing.disable_auto_download_warning", true);

        String downloadPath = config.getProperty("download.path", "downloads");
        Path downloadDirectory = resolvePath(downloadPath);
        prefs.put("download.default_directory", downloadDirectory.toString());

        chromeOptions.setExperimentalOption("prefs", prefs);

        return new ChromeDriver(chromeOptions);
    }

    private Path resolvePath(String configuredPath) {
        if (configuredPath == null || configuredPath.isBlank()) {
            return Paths.get("downloads").toAbsolutePath().normalize();
        }
        Path path = Paths.get(configuredPath);
        return path.isAbsolute()
                ? path.normalize()
                : Paths.get(System.getProperty("user.dir"), configuredPath).normalize();
    }
}
