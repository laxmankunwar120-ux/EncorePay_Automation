package com.encorepay.listeners;

import java.util.List;
import java.util.Map;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.encorepay.config.ConfigLoader;
import com.encorepay.utilities.ExcelUtil;
import com.encorepay.utilities.ScreenshotUtil;
import com.encorepay.utilities.TestResultNameUtil;

public class ExcelReportListener implements ITestListener {

    private ConfigLoader config = ConfigLoader.getInstance();

    @Override
    public void onStart(ITestContext context) {
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        addResultRow(result, "PASS", "");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Throwable error = result.getThrowable();
        String message = error != null ? error.getMessage() : "Unknown error";
        addResultRow(result, "FAIL", message);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        addResultRow(result, "SKIP", result.getThrowable() != null ? result.getThrowable().getMessage() : "");
    }

    @Override
    public void onFinish(ITestContext context) {
        ExcelUtil.save();
    }

    private void addResultRow(ITestResult result, String status, String fallbackMessage) {
        String reportName = TestResultNameUtil.getReportName(result);
        String caseId = TestResultNameUtil.getCaseId(result);
        String moduleName = TestResultNameUtil.getFeatureName(result);
        String scenario = TestResultNameUtil.getDisplayName(result);
        String verificationSummary = ScreenshotUtil.getVerificationSummary(reportName);
        String testData = TestResultNameUtil.getTestData(result);
        String verificationDetails = TestResultNameUtil.getVerificationDetails(result);
        List<Map<String, String>> steps = ScreenshotUtil.getAllSteps().getOrDefault(reportName, List.of());

        ExcelUtil.addRow(
            caseId,
            TestResultNameUtil.getRequirementId(result),
            moduleName,
            scenario,
            TestResultNameUtil.getPriority(result),
            TestResultNameUtil.getTestType(result),
            TestResultNameUtil.getAutomationScope(result),
            TestResultNameUtil.getPrecondition(result),
            testData,
            TestResultNameUtil.getExpectedResult(result),
            verificationDetails,
            TestResultNameUtil.getActualResult(result, status, fallbackMessage, verificationSummary),
            status,
            "Automated",
            result.getStartMillis(),
            result.getEndMillis()
        );

        ExcelUtil.addEvidenceRows(
            caseId,
            TestResultNameUtil.getRequirementId(result),
            moduleName,
            scenario,
            testData,
            verificationDetails,
            steps
        );
    }
}
