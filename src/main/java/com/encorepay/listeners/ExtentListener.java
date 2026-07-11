package com.encorepay.listeners;

import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.encorepay.utilities.CustomReportUtil;
import com.encorepay.utilities.ExtentManager;
import com.encorepay.utilities.ScreenshotUtil;
import com.encorepay.utilities.TestResultNameUtil;

public class ExtentListener implements ITestListener {

    private final ExtentReports extent = ExtentManager.getInstance();
    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    public static ExtentTest getTest() {
        return extentTest.get();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = TestResultNameUtil.getReportName(result);
        ExtentTest test = extent.createTest(testName);
        extentTest.set(test);
        ScreenshotUtil.recordTestStart(testName);
        test.info("Test started: " + testName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = TestResultNameUtil.getReportName(result);
        ExtentTest test = extentTest.get();
        if (test != null) {
            test.pass("Test passed successfully");
        }
        ScreenshotUtil.recordTestEnd(testName, "PASS");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = TestResultNameUtil.getReportName(result);
        ExtentTest test = extentTest.get();
        
        if (test != null) {
            test.fail("Test failed");
            test.fail(result.getThrowable());
        }
        
        captureFailureScreenshot(result);
        ScreenshotUtil.recordTestEnd(testName, "FAIL");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = TestResultNameUtil.getReportName(result);
        ExtentTest test = extentTest.get();
        
        if (test == null) {
            test = extent.createTest(testName);
            extentTest.set(test);
            ScreenshotUtil.recordTestStart(testName);
        }
        
        test.skip("Test skipped: " + (result.getThrowable() != null ? result.getThrowable().getMessage() : ""));
        ScreenshotUtil.recordTestEnd(testName, "SKIP");
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
        CustomReportUtil.generateReport(context);
        extentTest.remove();
    }

    private void captureFailureScreenshot(ITestResult result) {
        try {
            Object testInstance = result.getInstance();
            if (testInstance != null && testInstance instanceof com.encorepay.base.BaseClass) {
                com.encorepay.base.BaseClass baseClass = (com.encorepay.base.BaseClass) testInstance;
                WebDriver driver = baseClass.getDriver();
                if (driver != null) {
                    String testName = TestResultNameUtil.getReportName(result);
                    String methodName = result.getMethod().getMethodName();
                    ScreenshotUtil.captureScreenshot(driver, testName, "Failure - " + methodName);
                }
            }
        } catch (Exception ignored) {
        }
    }
}
