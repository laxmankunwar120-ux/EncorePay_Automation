package com.encorepay.listeners;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.encorepay.utilities.CustomReportUtil;
import com.encorepay.utilities.ScreenshotUtil;
import com.encorepay.utilities.TestResultNameUtil;

public class TestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        String testName = TestResultNameUtil.getReportName(result);

        ScreenshotUtil.recordTestStart(testName);
        ScreenshotUtil.addStep(testName, "Scenario started: " + testName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = TestResultNameUtil.getReportName(result);

        ScreenshotUtil.addStep(testName, "Scenario completed successfully");
        ScreenshotUtil.recordTestEnd(testName, "PASS");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = TestResultNameUtil.getReportName(result);

        String error = result.getThrowable() != null
            ? result.getThrowable().toString()
            : "Unknown error";

        ScreenshotUtil.addStep(testName, "Failure: " + error);
        ScreenshotUtil.recordTestEnd(testName, "FAIL");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = TestResultNameUtil.getReportName(result);

        ScreenshotUtil.recordTestStart(testName);
        ScreenshotUtil.addStep(testName, "Scenario skipped because business preconditions were not available");
        ScreenshotUtil.recordTestEnd(testName, "SKIP");
    }

    @Override
    public void onFinish(ITestContext context) {
        CustomReportUtil.generateReport(context);
    }
}
