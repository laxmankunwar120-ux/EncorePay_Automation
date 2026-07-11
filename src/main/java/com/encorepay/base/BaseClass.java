package com.encorepay.base;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import org.testng.ITestResult;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import com.encorepay.actiondriver.ActionDriver;
import com.encorepay.config.ConfigLoader;
import com.encorepay.utilities.ServerAvailabilityChecker;
import com.encorepay.utilities.ScreenshotUtil;
import com.encorepay.utilities.helpers.WaitHelper;

import io.github.bonigarcia.wdm.WebDriverManager;

public class BaseClass {

    private static final String CONFIG_PATH =
            "src/main/resources/config.properties";

    protected WebDriver driver;
    protected Properties prop;
    protected ConfigLoader config;
    protected ActionDriver action;

    @BeforeSuite(alwaysRun = true)
    public void setUpBase() {

        config = ConfigLoader.getInstance();
        prop = loadProperties();

        // Check server availability before starting automation
        var serverStatus = ServerAvailabilityChecker.check(config.getURL(), 10);
        if (!serverStatus.isReachable()) {
            System.out.println("Server DOWN: " + serverStatus.getMessage());
        } else {
            System.out.println("Server UP: " + serverStatus.getMessage());
        }

        driver = createDriver(config.getProperty("browser", "chrome"));

        driver.manage().timeouts().implicitlyWait(
                Duration.ofSeconds(config.getImplicitWait()));

        driver.manage().timeouts().pageLoadTimeout(
                Duration.ofSeconds(getIntProperty("pageLoadTimeout", 45)));

        try {
            driver.manage().window().setSize(new Dimension(1920, 1080));
        } catch (Exception e) {
            System.out.println("[WARN] Window resize failed during setup: " + e.getMessage());
        }

        action = new ActionDriver(driver);

        try {
            driver.get(config.getURL());
            waitForPageReady();
        } catch (Exception e) {
            System.out.println("[WARN] Default URL load failed during setup: " + e.getMessage());
        }
    }

    @AfterSuite(alwaysRun = true)
    public void tearDownBase() {
        if (driver != null) {
            try {
                System.out.println("[INFO] Closing browser.");
                driver.quit();
            } catch (Exception ignored) {
                // swallow any quit errors to avoid masking suite results
            } finally {
                driver = null;
            }
        }
    }

    public WebDriver getDriver() {
        return driver;
    }

    protected void recordTestData(String detail) {

        ScreenshotUtil.addCurrentTestData(detail);

        System.out.println("[DATA] " + detail);
    }

    protected void recordVerification(String detail) {

        ScreenshotUtil.addCurrentTestVerification(detail);

        System.out.println("[VERIFY] " + detail);
    }

    protected void captureStep(String label) {

        ScreenshotUtil.captureCurrentTestStep(driver, label);
    }

    protected void waitForPageReady() {
        WaitHelper.waitForDocumentReady(driver, config.getExplicitWait());
    }

    /**
     * Compatibility alias: some tests call waitForPageLoad()
     */
    protected void waitForPageLoad() {
        waitForPageReady();
    }

    protected void logTestResult(ITestResult result) {

        String testName = result.getName();

        switch (result.getStatus()) {

            case ITestResult.SUCCESS:

                System.out.println("PASSED: " + testName);
                break;

            case ITestResult.FAILURE:

                System.out.println("FAILED: " + testName);

                if (result.getThrowable() != null) {
                    result.getThrowable().printStackTrace();
                }

                break;

            case ITestResult.SKIP:

                System.out.println("SKIPPED: " + testName);
                break;

            default:

                System.out.println("UNKNOWN STATUS: " + testName);
        }
    }

    private WebDriver createDriver(String browserName) {

        String browser = browserName == null
                ? "chrome"
                : browserName.trim().toLowerCase(Locale.ROOT);

        boolean headless =
                Boolean.parseBoolean(getProperty("headless", "false"));

        switch (browser) {

            case "firefox":

                WebDriverManager.firefoxdriver().setup();

                FirefoxOptions firefoxOptions = new FirefoxOptions();

                if (headless) {
                    firefoxOptions.addArguments("-headless");
                }

                return new FirefoxDriver(firefoxOptions);

            case "edge":
            case "msedge":

                WebDriverManager.edgedriver().setup();

                EdgeOptions edgeOptions = new EdgeOptions();

                edgeOptions.addArguments(
                        "--disable-notifications",
                        "--remote-allow-origins=*");

                if (headless) {
                    edgeOptions.addArguments(
                            "--headless=new",
                            "--window-size=1920,1080");
                }

                return new EdgeDriver(edgeOptions);

            case "chrome":
            default:

                WebDriverManager.chromedriver().setup();

                ChromeOptions chromeOptions = new ChromeOptions();

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

                Map<String, Object> prefs = new HashMap<>();
                prefs.put("profile.default_content_setting_values.geolocation", 2); // 2 = Block
                prefs.put("profile.default_content_setting_values.notifications", 2); // 2 = Block
                prefs.put("profile.default_content_setting_values.automatic_downloads", 1);
                prefs.put("download.default_directory", resolvePath(config.getProperty("download.path", "downloads")).toString());
                prefs.put("download.prompt_for_download", false);
                prefs.put("download.directory_upgrade", true);
                prefs.put("download.extensions_to_open", "");
                prefs.put("safebrowsing.enabled", false);
                prefs.put("safebrowsing.disable_download_protection", true);
                prefs.put("safebrowsing.disable_auto_download_warning", true);

                String configuredDownloadPath = config.getProperty("download.path", "downloads");
                Path downloadDirectory = resolvePath(configuredDownloadPath);
                try {
                    Files.createDirectories(downloadDirectory);
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to create download directory: " + downloadDirectory, e);
                }
                prefs.put("download.default_directory", downloadDirectory.toString());

                chromeOptions.setExperimentalOption("prefs", prefs);

                if (headless) {
                    chromeOptions.addArguments(
                            "--headless=new",
                            "--window-size=1920,1080");
                }

                return new ChromeDriver(chromeOptions);
        }
    }

    private Properties loadProperties() {

        Properties properties = new Properties();

        try (FileInputStream fis =
                     new FileInputStream(CONFIG_PATH)) {

            properties.load(fis);

            return properties;

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Unable to load config from " + CONFIG_PATH,
                    e);
        }
    }

    private String getProperty(
            String key,
            String defaultValue) {

        return prop == null
                ? defaultValue
                : prop.getProperty(key, defaultValue).trim();
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

    private int getIntProperty(
            String key,
            int defaultValue) {

        try {

            return Integer.parseInt(
                    getProperty(key, String.valueOf(defaultValue)));

        } catch (NumberFormatException e) {

            return defaultValue;
        }
    }
}