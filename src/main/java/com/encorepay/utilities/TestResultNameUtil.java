package com.encorepay.utilities;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.ITestResult;

public final class TestResultNameUtil {

    private static final Pattern TEST_CASE_PATTERN = Pattern.compile("(?i)tc(\\d+)");

    private TestResultNameUtil() {
    }

    public static String getFeatureName(ITestResult result) {
        if (result == null || result.getTestClass() == null) {
            return "Automation";
        }

        String className = result.getTestClass().getRealClass().getSimpleName();
        className = className.replaceAll("Test$", "");
        return prettifyIdentifier(className);
    }

    public static String getFeatureCode(ITestResult result) {
        String featureName = getFeatureName(result).trim();
        if (featureName.isBlank()) {
            return "AUTO";
        }

        String[] words = featureName.split("\\s+");
        StringBuilder acronym = new StringBuilder();
        for (String word : words) {
            String cleanedWord = word.replaceAll("[^A-Za-z]", "");
            if (!cleanedWord.isBlank()) {
                acronym.append(Character.toUpperCase(cleanedWord.charAt(0)));
            }
        }

        if (acronym.length() >= 2) {
            return acronym.toString();
        }

        String alpha = featureName.replaceAll("[^A-Za-z]", "").toUpperCase(Locale.ENGLISH);
        if (alpha.isBlank()) {
            return "AUTO";
        }
        return alpha.length() >= 4 ? alpha.substring(0, 4) : alpha;
    }

    public static String getCaseId(ITestResult result) {
        return getFeatureCode(result) + "-TC-" + getSequence(result);
    }

    public static String getRequirementId(ITestResult result) {
        return getFeatureCode(result) + "-REQ-" + getSequence(result);
    }

    public static String getDisplayName(ITestResult result) {
        if (result == null || result.getMethod() == null) {
            return "Unnamed Scenario";
        }

        String description = result.getMethod().getDescription();
        if (description != null && !description.isBlank()) {
            return description.trim();
        }

        return prettifyIdentifier(result.getMethod().getMethodName());
    }

    public static String getReportName(ITestResult result) {
        String featureName = getFeatureName(result);
        String displayName = getDisplayName(result);
        return featureName.isBlank() ? displayName : featureName + " | " + displayName;
    }

    public static String getExpectedResult(ITestResult result) {
        String scenario = getDisplayName(result)
            .replaceFirst("^(?i)(confirm|verify|validate)\\s+", "")
            .trim();

        if (scenario.isBlank()) {
            return "Expected business behavior should be achieved.";
        }

        scenario = Character.toUpperCase(scenario.charAt(0)) + scenario.substring(1);
        return scenario.endsWith(".") ? scenario : scenario + ".";
    }

    public static String getPrecondition(ITestResult result) {
        String scenario = getDisplayName(result).toLowerCase(Locale.ENGLISH);
        String feature = getFeatureName(result).toLowerCase(Locale.ENGLISH);

        if (scenario.contains("sign out") || scenario.contains("logout")) {
            return "Configured QA user is already authenticated and the profile menu is available.";
        }
        if (feature.contains("login") || scenario.contains("sign in") || scenario.contains("login")) {
            return "EncorePay sign-in page is reachable in the configured QA environment.";
        }
        if (feature.contains("dashboard") || scenario.contains("dashboard")) {
            return "Configured QA user is authenticated and dashboard navigation is available.";
        }
        if (feature.contains("allocation") || scenario.contains("allocation")) {
            return "Configured QA user is authenticated and the allocation queue is reachable.";
        }
        if (feature.contains("receipt") || scenario.contains("receipt")) {
            return "Configured QA user is authenticated and the collection items queue is reachable.";
        }
        if (feature.contains("deposit slips") || scenario.contains("deposit slip")) {
            return "Configured QA user is authenticated and the Collections batch and deposit slip screens are reachable.";
        }
        if (feature.contains("collections") || scenario.contains("batch")) {
            return "Configured QA user is authenticated and Collections navigation is available.";
        }

        return "Configured QA environment is reachable and scenario prerequisites are available.";
    }

    public static String getTestData(ITestResult result) {
        String reportName = getReportName(result);
        String recordedTestData = ScreenshotUtil.getTestDataMultiline(reportName);
        if (!recordedTestData.isBlank()) {
            return recordedTestData;
        }

        String scenario = getDisplayName(result).toLowerCase(Locale.ENGLISH);
        if (scenario.contains("invalid")) {
            return "Configured username with invalid negative password data.";
        }
        if (scenario.contains("blank credentials")) {
            return "Blank username and blank password.";
        }
        if (scenario.contains("branch filter")) {
            return "First available branch option from the dashboard filter.";
        }
        if (scenario.contains("allocation")) {
            return "Configured QA user and first eligible allocation item from the queue.";
        }
        if (scenario.contains("receipt")) {
            return "Configured QA user and first eligible receipt item from the queue.";
        }
        if (scenario.contains("deposit slip")) {
            return "Configured QA user, a visible cash receipt, configured bank, generated reference transaction, and upload document.";
        }
        if (scenario.contains("batch")) {
            return "Configured QA user and current Collections batch state.";
        }
        if (scenario.contains("dashboard")) {
            return "Configured QA user and live dashboard business data.";
        }
        if (scenario.contains("sign in") || scenario.contains("login")) {
            return "Configured valid QA user credentials.";
        }

        return "Execution data captured automatically from the configured QA environment.";
    }

    public static String getPriority(ITestResult result) {
        String scenario = getDisplayName(result).toLowerCase(Locale.ENGLISH);
        String feature = getFeatureName(result).toLowerCase(Locale.ENGLISH);

        if (feature.contains("login") || scenario.contains("logout") || scenario.contains("receipt")
            || scenario.contains("deposit slip")
            || scenario.contains("allocation completes")) {
            return "High";
        }
        if (feature.contains("dashboard") || scenario.contains("filter") || scenario.contains("queue")) {
            return "Medium";
        }
        return "Medium";
    }

    public static String getTestType(ITestResult result) {
        String scenario = getDisplayName(result).toLowerCase(Locale.ENGLISH);

        if (scenario.contains("invalid") || scenario.contains("blank")) {
            return "Negative";
        }
        if (scenario.contains("access") || scenario.contains("loads") || scenario.contains("reachable")
            || scenario.contains("login") || scenario.contains("logout")) {
            return "Smoke";
        }
        return "Functional";
    }

    public static String getAutomationScope(ITestResult result) {
        return "UI End-to-End";
    }

    public static String getActualResult(ITestResult result, String status, String fallbackMessage) {
        return getActualResult(result, status, fallbackMessage, "");
    }

    public static String getActualResult(ITestResult result, String status, String fallbackMessage, String verificationSummary) {
        String normalizedStatus = status == null ? "" : status.trim().toUpperCase(Locale.ENGLISH);
        if ("PASS".equals(normalizedStatus)) {
            String evidence = sanitize(verificationSummary);
            if (!evidence.isBlank()) {
                return "Expected result achieved. Verified with: " + evidence;
            }
            return "Expected result achieved during automated execution.";
        }
        if ("SKIP".equals(normalizedStatus)) {
            String skipMessage = normalizeSkipMessage(result, fallbackMessage);
            String evidence = sanitize(verificationSummary);
            return evidence.isBlank() ? skipMessage : skipMessage + " Observed before skip: " + evidence;
        }

        String evidence = sanitize(verificationSummary);
        String message = sanitize(fallbackMessage);
        if (!evidence.isBlank() && !message.isBlank()) {
            return "Observed: " + evidence + " Failure: " + message;
        }
        if (!evidence.isBlank()) {
            return "Observed: " + evidence + " Actual behavior did not match the expected result.";
        }
        return message.isBlank() ? "Actual behavior did not match the expected result." : message;
    }

    public static String getVerificationDetails(ITestResult result) {
        return ScreenshotUtil.getVerificationMultiline(getReportName(result));
    }

    private static String normalizeSkipMessage(ITestResult result, String fallbackMessage) {
        String message = sanitize(fallbackMessage);
        String lower = message.toLowerCase(Locale.ENGLISH);

        if (lower.contains("collection items are available")
            || lower.contains("collection item's")
            || lower.contains("allocatable collection items")
            || lower.contains("eligible collection items")) {
            return "Not executed because eligible collection items were not available in the QA environment.";
        }
        if (lower.contains("deposit slip") && lower.contains("available")) {
            return "Not executed because eligible deposit slip data was not available in the QA environment.";
        }
        if (lower.contains("no selectable receipt")) {
            return "Not executed because no selectable cash receipt was available for deposit slip creation.";
        }

        if (lower.contains("depends on not successfully finished methods")) {
            return "Not executed because a prerequisite automated scenario did not complete.";
        }

        if (!message.isBlank()) {
            return message;
        }

        if (result != null && result.getThrowable() != null) {
            String throwableMessage = sanitize(result.getThrowable().getMessage());
            if (!throwableMessage.isBlank()) {
                return throwableMessage;
            }
        }

        return "Not executed because business preconditions were not available.";
    }

    private static String getSequence(ITestResult result) {
        if (result == null || result.getMethod() == null) {
            return "00";
        }

        Matcher matcher = TEST_CASE_PATTERN.matcher(result.getMethod().getMethodName());
        if (matcher.find()) {
            return String.format("%02d", Integer.parseInt(matcher.group(1)));
        }

        return "00";
    }

    private static String sanitize(String value) {
        if (value == null) {
            return "";
        }

        return value.replaceAll("\\s+", " ").trim();
    }

    private static String prettifyIdentifier(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String cleaned = value
            .replaceFirst("^(?i)tc\\d+[_-]*", "")
            .replace('_', ' ')
            .replace('-', ' ')
            .replaceAll("([a-z])([A-Z])", "$1 $2")
            .replaceAll("\\s+", " ")
            .trim();

        if (cleaned.isBlank()) {
            return "";
        }

        StringBuilder builder = new StringBuilder(cleaned.length());
        boolean capitalizeNext = true;

        for (char ch : cleaned.toCharArray()) {
            if (Character.isWhitespace(ch)) {
                builder.append(ch);
                capitalizeNext = true;
                continue;
            }

            builder.append(capitalizeNext ? Character.toUpperCase(ch) : ch);
            capitalizeNext = false;
        }

        return builder.toString();
    }
}
