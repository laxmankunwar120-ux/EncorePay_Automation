package com.encorepay.utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public final class ExcelUtil {

    private static final String DARK_BG = "0F172A";
    private static final String HEADER_BG = "1E293B";
    private static final String ACCENT = "2563EB";
    private static final String ACCENT_DARK = "1D4ED8";
    private static final String SUCCESS = "16A34A";
    private static final String SUCCESS_LITE = "DCFCE7";
    private static final String DANGER = "DC2626";
    private static final String DANGER_LITE = "FEE2E2";
    private static final String WARN = "D97706";
    private static final String WARN_LITE = "FEF3C7";
    private static final String WHITE = "FFFFFF";
    private static final String GREY_100 = "F8FAFC";
    private static final String GREY_200 = "F1F5F9";
    private static final String GREY_300 = "E2E8F0";
    private static final String GREY_500 = "64748B";
    private static final String GREY_700 = "334155";

    private static final String PROJECT_ROOT = System.getProperty("user.dir");
    private static final String OUTPUT_DIR = PROJECT_ROOT + File.separator + "reports";

    private static XSSFWorkbook workbook;
    private static XSSFSheet executiveSummarySheet;
    private static XSSFSheet metadataSheet;
    private static XSSFSheet testResultsSheet;
    private static XSSFSheet analyticsSheet;
    private static XSSFSheet exceptionsSheet;
    private static XSSFSheet evidenceSheet;
    private static XSSFSheet traceabilitySheet;

    private static int testResultsRowIndex;
    private static int exceptionsRowIndex;
    private static int evidenceRowIndex;
    private static int traceabilityRowIndex;
    private static int totalTests;
    private static int passedTests;
    private static int failedTests;
    private static int skippedTests;
    private static long totalDuration;
    private static String suiteName;
    private static String environmentName;
    private static String browserName;
    private static String clientName;
    private static String applicationName;
    private static String baseUrl;
    private static String buildVersion;
    private static String releaseName;
    private static String executedBy;
    private static Date reportStartTime;

    private static final Map<String, int[]> moduleStats = new LinkedHashMap<>();

    private ExcelUtil() {
    }

    public static void createSheet(String suite, String environment, String browser, String client,
                                   String application, String url, String build, String release,
                                   String executionOwner) {
        workbook = new XSSFWorkbook();
        executiveSummarySheet = workbook.createSheet("Executive Summary");
        metadataSheet = workbook.createSheet("Run Metadata");
        testResultsSheet = workbook.createSheet("Test Results");
        traceabilitySheet = workbook.createSheet("Traceability Matrix");
        analyticsSheet = workbook.createSheet("Analytics");
        exceptionsSheet = workbook.createSheet("Exceptions & Blockers");
        evidenceSheet = workbook.createSheet("Evidence Register");

        testResultsRowIndex = 11;
        exceptionsRowIndex = 8;
        evidenceRowIndex = 8;
        traceabilityRowIndex = 8;
        totalTests = 0;
        passedTests = 0;
        failedTests = 0;
        skippedTests = 0;
        totalDuration = 0;
        suiteName = sanitizeText(suite, "Automation Suite");
        environmentName = sanitizeText(environment, "Configured Environment");
        browserName = sanitizeText(browser, "Configured Browser");
        clientName = sanitizeText(client, "EncorePay");
        applicationName = sanitizeText(application, "EncorePay");
        baseUrl = sanitizeText(url, "Configured URL");
        buildVersion = sanitizeText(build, "Current QA Build");
        releaseName = sanitizeText(release, "QA Regression Cycle");
        executedBy = sanitizeText(executionOwner, "Automation Engineer");
        reportStartTime = new Date();
        moduleStats.clear();

        executiveSummarySheet.setDisplayGridlines(false);
        metadataSheet.setDisplayGridlines(false);
        testResultsSheet.setDisplayGridlines(false);
        traceabilitySheet.setDisplayGridlines(false);
        analyticsSheet.setDisplayGridlines(false);
        exceptionsSheet.setDisplayGridlines(false);
        evidenceSheet.setDisplayGridlines(false);

        executiveSummarySheet.setTabColor(new XSSFColor(hexBytes(ACCENT), null));
        metadataSheet.setTabColor(new XSSFColor(hexBytes(ACCENT_DARK), null));
        testResultsSheet.setTabColor(new XSSFColor(hexBytes(SUCCESS), null));
        traceabilitySheet.setTabColor(new XSSFColor(hexBytes(ACCENT), null));
        analyticsSheet.setTabColor(new XSSFColor(hexBytes(ACCENT_DARK), null));
        exceptionsSheet.setTabColor(new XSSFColor(hexBytes(DANGER), null));
        evidenceSheet.setTabColor(new XSSFColor(hexBytes(ACCENT), null));

        buildMetadataShell();
        buildTestResultsShell();
        buildTraceabilityShell();
        buildExceptionsShell();
        buildEvidenceShell();
    }

    public static void addRow(String caseId, String requirementId, String moduleName, String scenario,
                              String priority, String testType, String automationScope, String precondition,
                              String testData, String expectedResult, String verificationDetails,
                              String actualResult, String status, String executionType,
                              long startMillis, long endMillis) {

        long duration = Math.max(0, endMillis - startMillis);
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(startMillis));

        totalTests++;
        totalDuration += duration;
        if ("PASS".equals(status)) {
            passedTests++;
        } else if ("FAIL".equals(status)) {
            failedTests++;
        } else {
            skippedTests++;
        }

        int[] moduleData = moduleStats.computeIfAbsent(moduleName, key -> new int[4]);
        moduleData[0]++;
        if ("PASS".equals(status)) {
            moduleData[1]++;
        } else if ("FAIL".equals(status)) {
            moduleData[2]++;
        } else {
            moduleData[3]++;
        }

        XSSFRow row = testResultsSheet.createRow(testResultsRowIndex - 1);

        boolean alternate = testResultsRowIndex % 2 == 0;
        String rowColor = alternate ? GREY_100 : WHITE;
        String statusBackground = "PASS".equals(status) ? SUCCESS_LITE : ("FAIL".equals(status) ? DANGER_LITE : WARN_LITE);
        String statusForeground = "PASS".equals(status) ? SUCCESS : ("FAIL".equals(status) ? DANGER : WARN);

        Object[] values = {
            testResultsRowIndex - 10,
            caseId,
            requirementId,
            moduleName,
            scenario,
            priority,
            testType,
            automationScope,
            precondition,
            testData,
            expectedResult,
            verificationDetails,
            actualResult,
            status,
            executionType,
            duration + " ms",
            timestamp
        };

        row.setHeightInPoints(calculateRowHeightInPoints(values));

        for (int i = 0; i < values.length; i++) {
            XSSFCell cell = row.createCell(i + 1);
            if (i == 0) {
                cell.setCellValue((int) values[i]);
            } else {
                cell.setCellValue(values[i] == null ? "" : values[i].toString());
            }

            if (i == 13) {
                cell.setCellStyle(buildStatusStyle(statusBackground, statusForeground));
            } else {
                boolean wrap = i == 4 || (i >= 8 && i <= 12);
                boolean center = i == 0 || i == 1 || i == 2 || i == 5 || i == 6 || i == 7 || i == 13 || i == 14 || i == 15 || i == 16;
                cell.setCellStyle(buildDataStyle(rowColor, false, wrap, center, GREY_700));
            }
        }

        addTraceabilityRow(caseId, requirementId, moduleName, scenario, priority, testType, automationScope, status);

        testResultsRowIndex++;

        if ("FAIL".equals(status) || "SKIP".equals(status)) {
            XSSFRow exceptionRow = exceptionsSheet.createRow(exceptionsRowIndex - 1);

            String issueRowColor = "FAIL".equals(status) ? DANGER_LITE : WARN_LITE;
            Object[] exceptionValues = {
                exceptionsRowIndex - 7,
                caseId,
                requirementId,
                moduleName,
                scenario,
                priority,
                testType,
                testData,
                verificationDetails,
                status,
                actualResult,
                duration + " ms"
            };

            exceptionRow.setHeightInPoints(calculateRowHeightInPoints(exceptionValues));

            for (int i = 0; i < exceptionValues.length; i++) {
                XSSFCell cell = exceptionRow.createCell(i + 1);
                if (i == 0) {
                    cell.setCellValue((int) exceptionValues[i]);
                } else {
                    cell.setCellValue(exceptionValues[i] == null ? "" : exceptionValues[i].toString());
                }

                if (i == 9) {
                    cell.setCellStyle(buildStatusStyle(issueRowColor, "FAIL".equals(status) ? DANGER : WARN));
                } else {
                    boolean wrap = i == 4 || i == 7 || i == 8 || i == 10;
                    boolean center = i == 0 || i == 1 || i == 2 || i == 5 || i == 6 || i == 9 || i == 11;
                    cell.setCellStyle(buildDataStyle(issueRowColor, false, wrap, center, GREY_700));
                }
            }

            exceptionsRowIndex++;
        }
    }

    public static void save() {
        try {
            buildExecutiveSummary();
            buildAnalytics();

            File outputDirectory = new File(OUTPUT_DIR);
            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String suiteLabel = sanitizeFileSegment(suiteName);
            String filePath = OUTPUT_DIR + File.separator
                + suiteLabel + "_Client_Execution_Report_" + timestamp + ".xlsx";

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }

            System.out.println("Excel Report Saved: " + filePath);
        } catch (Exception e) {
            System.err.println("Report save failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void buildExecutiveSummary() {
        double passRate = totalTests > 0 ? passedTests * 100.0 / totalTests : 0;

        int[] widths = {512, 6800, 4600, 4600, 4600, 4600, 4600, 6800};
        for (int i = 0; i < widths.length; i++) {
            executiveSummarySheet.setColumnWidth(i, widths[i]);
        }

        executiveSummarySheet.createRow(0).setHeightInPoints(12);
        XSSFRow titleRow = executiveSummarySheet.createRow(1);
        titleRow.setHeightInPoints(52);
        executiveSummarySheet.createRow(2).setHeightInPoints(12);
        for (int col = 0; col <= 7; col++) {
            for (int row = 0; row < 3; row++) {
                XSSFRow currentRow = executiveSummarySheet.getRow(row);
                if (currentRow == null) {
                    currentRow = executiveSummarySheet.createRow(row);
                }
                currentRow.createCell(col).setCellStyle(buildFill(DARK_BG));
            }
        }

        executiveSummarySheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 6));
        XSSFCell titleCell = titleRow.createCell(1);
        titleCell.setCellValue("ENCOREPAY CLIENT EXECUTION REPORT");
        titleCell.setCellStyle(buildBanner(DARK_BG, WHITE, 20, true));

        XSSFRow metaSpacer = executiveSummarySheet.createRow(3);
        metaSpacer.setHeightInPoints(6);
        XSSFRow metaRow = executiveSummarySheet.createRow(4);
        metaRow.setHeightInPoints(20);
        executiveSummarySheet.addMergedRegion(new CellRangeAddress(4, 4, 1, 6));
        XSSFCell metaCell = metaRow.createCell(1);
        metaCell.setCellValue("Client: " + clientName + " | Application: " + applicationName + " | Suite: " + suiteName + " | Generated: "
            + new SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(new Date())
            + " | Environment: " + environmentName + " | Execution: Automated | Browser: " + browserName);
        XSSFCellStyle metaStyle = buildBanner(WHITE, GREY_500, 9, false);
        metaStyle.setAlignment(HorizontalAlignment.LEFT);
        metaCell.setCellStyle(metaStyle);

        XSSFRow sectionSpacer = executiveSummarySheet.createRow(5);
        sectionSpacer.setHeightInPoints(8);
        XSSFRow sectionLabelRow = executiveSummarySheet.createRow(6);
        sectionLabelRow.setHeightInPoints(18);
        executiveSummarySheet.addMergedRegion(new CellRangeAddress(6, 6, 1, 6));
        XSSFCell sectionLabelCell = sectionLabelRow.createCell(1);
        sectionLabelCell.setCellValue("EXECUTION SUMMARY");
        sectionLabelCell.setCellStyle(buildSmallLabel());

        XSSFRow tableHeaderRow = executiveSummarySheet.createRow(8);
        tableHeaderRow.setHeightInPoints(26);
        String[] headers = {"Metric", "Value"};
        for (int i = 0; i < headers.length; i++) {
            XSSFCell cell = tableHeaderRow.createCell(i + 1);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(buildColumnHeader());
        }

        String[][] rows = {
            {"Client", clientName},
            {"Application", applicationName},
            {"Build / Release", buildVersion + " / " + releaseName},
            {"Total Test Cases", String.valueOf(totalTests)},
            {"Passed", String.valueOf(passedTests)},
            {"Failed", String.valueOf(failedTests)},
            {"Skipped", String.valueOf(skippedTests)},
            {"Pass Rate", String.format("%.1f%%", passRate)},
            {"Total Duration", String.format("%.1f s", totalDuration / 1000.0)},
            {"Average Duration", totalTests > 0 ? String.format("%.1f s", totalDuration / 1000.0 / totalTests) : "0.0 s"},
            {"Modules Covered", String.valueOf(moduleStats.size())}
        };

        for (int i = 0; i < rows.length; i++) {
            XSSFRow row = executiveSummarySheet.createRow(9 + i);
            row.setHeightInPoints(22);
            String rowColor = i % 2 == 0 ? GREY_100 : WHITE;

            XSSFCell metricCell = row.createCell(1);
            metricCell.setCellValue(rows[i][0]);
            metricCell.setCellStyle(buildDataStyle(rowColor, false, false, false, GREY_700));

            XSSFCell valueCell = row.createCell(2);
            valueCell.setCellValue(rows[i][1]);
            valueCell.setCellStyle(buildDataStyle(rowColor, true, false, true, GREY_700));
        }

        XSSFRow footerRow = executiveSummarySheet.createRow(22);
        footerRow.setHeightInPoints(20);
        executiveSummarySheet.addMergedRegion(new CellRangeAddress(22, 22, 1, 6));
        XSSFCell footerCell = footerRow.createCell(1);
        footerCell.setCellValue("Run started: "
            + new SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(reportStartTime)
            + " | Prepared for QA/client review from automated execution evidence.");
        footerCell.setCellStyle(buildDataStyle(GREY_200, false, false, true, GREY_500));
    }

    private static void buildTestResultsShell() {
        int[] widths = {512, 2400, 2400, 4200, 12000, 3200, 3200, 3600, 11000, 11000, 12000, 15000, 17000, 3200, 4200, 4200, 6200, 512};
        for (int i = 0; i < widths.length; i++) {
            testResultsSheet.setColumnWidth(i, widths[i]);
        }

        testResultsSheet.createFreezePane(0, 10);

        testResultsSheet.createRow(0).setHeightInPoints(12);
        XSSFRow titleRow = testResultsSheet.createRow(1);
        titleRow.setHeightInPoints(44);
        testResultsSheet.createRow(2).setHeightInPoints(12);
        for (int col = 0; col <= 17; col++) {
            for (int row = 0; row < 3; row++) {
                XSSFRow currentRow = testResultsSheet.getRow(row);
                if (currentRow == null) {
                    currentRow = testResultsSheet.createRow(row);
                }
                currentRow.createCell(col).setCellStyle(buildFill(DARK_BG));
            }
        }

        testResultsSheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 17));
        XSSFCell titleCell = titleRow.createCell(1);
        titleCell.setCellValue("Client Test Case Execution Matrix");
        titleCell.setCellStyle(buildBanner(DARK_BG, WHITE, 18, true));

        testResultsSheet.createRow(3).setHeightInPoints(6);
        XSSFRow metaRow = testResultsSheet.createRow(4);
        metaRow.setHeightInPoints(18);
        testResultsSheet.addMergedRegion(new CellRangeAddress(4, 4, 1, 17));
        XSSFCell metaCell = metaRow.createCell(1);
        metaCell.setCellValue("Execution View: QA/client-ready execution log generated from automation | Suite: "
            + suiteName + " | Environment: " + environmentName + " | Browser: " + browserName + " | Build: " + buildVersion);
        XSSFCellStyle metaStyle = buildBanner(WHITE, GREY_500, 9, false);
        metaStyle.setAlignment(HorizontalAlignment.LEFT);
        metaCell.setCellStyle(metaStyle);

        for (int row = 5; row <= 8; row++) {
            testResultsSheet.createRow(row).setHeightInPoints(8);
        }

        XSSFRow headerRow = testResultsSheet.createRow(9);
        headerRow.setHeightInPoints(28);
        String[] headers = {
            "#", "Test Case ID", "Requirement ID", "Module", "Test Scenario", "Priority", "Test Type",
            "Automation Scope", "Precondition", "Test Data", "Expected Result", "Verification Details",
            "Actual Result", "Status", "Execution Type", "Duration", "Executed At"
        };

        for (int i = 0; i < headers.length; i++) {
            XSSFCell cell = headerRow.createCell(i + 1);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(buildColumnHeader());
        }

        testResultsSheet.setAutoFilter(new CellRangeAddress(9, 9, 1, 17));
    }

    private static void buildExceptionsShell() {
        int[] widths = {512, 2400, 2400, 4200, 11000, 3200, 3200, 12000, 14000, 3200, 17000, 4200, 512};
        for (int i = 0; i < widths.length; i++) {
            exceptionsSheet.setColumnWidth(i, widths[i]);
        }

        exceptionsSheet.createRow(0).setHeightInPoints(12);
        XSSFRow titleRow = exceptionsSheet.createRow(1);
        titleRow.setHeightInPoints(44);
        exceptionsSheet.createRow(2).setHeightInPoints(12);
        for (int col = 0; col <= 12; col++) {
            for (int row = 0; row < 3; row++) {
                XSSFRow currentRow = exceptionsSheet.getRow(row);
                if (currentRow == null) {
                    currentRow = exceptionsSheet.createRow(row);
                }
                currentRow.createCell(col).setCellStyle(buildFill(DARK_BG));
            }
        }

        exceptionsSheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 12));
        XSSFCell titleCell = titleRow.createCell(1);
        titleCell.setCellValue("Exceptions & Blockers");
        titleCell.setCellStyle(buildBanner(DARK_BG, WHITE, 18, true));

        exceptionsSheet.createRow(3).setHeightInPoints(6);
        XSSFRow metaRow = exceptionsSheet.createRow(4);
        metaRow.setHeightInPoints(18);
        exceptionsSheet.addMergedRegion(new CellRangeAddress(4, 4, 1, 12));
        XSSFCell metaCell = metaRow.createCell(1);
        metaCell.setCellValue("Only failed and blocked scenarios are listed in this sheet for " + suiteName + ".");
        XSSFCellStyle metaStyle = buildBanner(WHITE, GREY_500, 9, false);
        metaStyle.setAlignment(HorizontalAlignment.LEFT);
        metaCell.setCellStyle(metaStyle);

        exceptionsSheet.createRow(5).setHeightInPoints(8);
        XSSFRow headerRow = exceptionsSheet.createRow(6);
        headerRow.setHeightInPoints(28);
        String[] headers = {"#", "Test Case ID", "Requirement ID", "Module", "Scenario", "Priority", "Test Type",
            "Test Data / Context", "Verification Details", "Status", "Reason / Blocker", "Duration"};

        for (int i = 0; i < headers.length; i++) {
            XSSFCell cell = headerRow.createCell(i + 1);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(buildColumnHeader());
        }

        exceptionsSheet.createFreezePane(0, 7);
        exceptionsSheet.setAutoFilter(new CellRangeAddress(6, 6, 1, 12));
    }

    public static void addEvidenceRows(String caseId, String requirementId, String moduleName, String scenario,
                                       String testData, String verificationDetails, List<Map<String, String>> steps) {
        addEvidenceEntries(caseId, requirementId, moduleName, scenario, "Test Data", splitMultilineEntries(testData), "");
        addEvidenceEntries(caseId, requirementId, moduleName, scenario, "Verification", splitMultilineEntries(verificationDetails), "");

        if (steps == null || steps.isEmpty()) {
            return;
        }

        for (Map<String, String> step : steps) {
            if (step == null) {
                continue;
            }

            String label = sanitizeText(step.getOrDefault("label", ""), "");
            String path = sanitizeText(step.getOrDefault("path", ""), "");
            if (label.isBlank() && path.isBlank()) {
                continue;
            }

            addEvidenceEntry(caseId, requirementId, moduleName, scenario, "Screenshot Step", label, path);
        }
    }

    private static void buildMetadataShell() {
        int[] widths = {512, 5200, 12500, 512};
        for (int i = 0; i < widths.length; i++) {
            metadataSheet.setColumnWidth(i, widths[i]);
        }

        metadataSheet.createRow(0).setHeightInPoints(12);
        XSSFRow titleRow = metadataSheet.createRow(1);
        titleRow.setHeightInPoints(44);
        metadataSheet.createRow(2).setHeightInPoints(12);
        for (int col = 0; col <= 3; col++) {
            for (int row = 0; row < 3; row++) {
                XSSFRow currentRow = metadataSheet.getRow(row);
                if (currentRow == null) {
                    currentRow = metadataSheet.createRow(row);
                }
                currentRow.createCell(col).setCellStyle(buildFill(DARK_BG));
            }
        }

        metadataSheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 2));
        XSSFCell titleCell = titleRow.createCell(1);
        titleCell.setCellValue("Run Metadata");
        titleCell.setCellStyle(buildBanner(DARK_BG, WHITE, 18, true));

        String[][] rows = {
            {"Client", clientName},
            {"Application", applicationName},
            {"Suite", suiteName},
            {"Environment", environmentName},
            {"Base URL", baseUrl},
            {"Browser", browserName},
            {"Build Version", buildVersion},
            {"Release Cycle", releaseName},
            {"Executed By", executedBy},
            {"Execution Type", "Automated UI End-to-End"},
            {"Host Machine", sanitizeText(System.getenv("COMPUTERNAME"), "Local Machine")},
            {"Operating System", sanitizeText(System.getProperty("os.name") + " " + System.getProperty("os.version"), "Unknown OS")},
            {"Java Runtime", sanitizeText(System.getProperty("java.version"), "Unknown Java")},
            {"Run Started", new SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(reportStartTime)}
        };

        XSSFRow headerRow = metadataSheet.createRow(5);
        headerRow.setHeightInPoints(28);
        String[] headers = {"Field", "Value"};
        for (int i = 0; i < headers.length; i++) {
            XSSFCell cell = headerRow.createCell(i + 1);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(buildColumnHeader());
        }

        for (int i = 0; i < rows.length; i++) {
            XSSFRow row = metadataSheet.createRow(6 + i);
            row.setHeightInPoints(22);
            String rowColor = i % 2 == 0 ? GREY_100 : WHITE;

            XSSFCell fieldCell = row.createCell(1);
            fieldCell.setCellValue(rows[i][0]);
            fieldCell.setCellStyle(buildDataStyle(rowColor, true, false, false, GREY_700));

            XSSFCell valueCell = row.createCell(2);
            valueCell.setCellValue(rows[i][1]);
            valueCell.setCellStyle(buildDataStyle(rowColor, false, true, false, GREY_700));
        }
    }

    private static void buildTraceabilityShell() {
        int[] widths = {512, 2400, 2400, 4200, 12000, 3200, 3200, 3600, 3200, 512};
        for (int i = 0; i < widths.length; i++) {
            traceabilitySheet.setColumnWidth(i, widths[i]);
        }

        traceabilitySheet.createRow(0).setHeightInPoints(12);
        XSSFRow titleRow = traceabilitySheet.createRow(1);
        titleRow.setHeightInPoints(44);
        traceabilitySheet.createRow(2).setHeightInPoints(12);
        for (int col = 0; col <= 9; col++) {
            for (int row = 0; row < 3; row++) {
                XSSFRow currentRow = traceabilitySheet.getRow(row);
                if (currentRow == null) {
                    currentRow = traceabilitySheet.createRow(row);
                }
                currentRow.createCell(col).setCellStyle(buildFill(DARK_BG));
            }
        }

        traceabilitySheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 9));
        XSSFCell titleCell = titleRow.createCell(1);
        titleCell.setCellValue("Traceability Matrix");
        titleCell.setCellStyle(buildBanner(DARK_BG, WHITE, 18, true));

        traceabilitySheet.createRow(3).setHeightInPoints(6);
        XSSFRow metaRow = traceabilitySheet.createRow(4);
        metaRow.setHeightInPoints(18);
        traceabilitySheet.addMergedRegion(new CellRangeAddress(4, 4, 1, 9));
        XSSFCell metaCell = metaRow.createCell(1);
        metaCell.setCellValue("Requirement-style control view for automated scenarios executed in this run.");
        XSSFCellStyle metaStyle = buildBanner(WHITE, GREY_500, 9, false);
        metaStyle.setAlignment(HorizontalAlignment.LEFT);
        metaCell.setCellStyle(metaStyle);

        traceabilitySheet.createRow(5).setHeightInPoints(8);
        XSSFRow headerRow = traceabilitySheet.createRow(6);
        headerRow.setHeightInPoints(28);
        String[] headers = {"#", "Test Case ID", "Requirement ID", "Module", "Scenario", "Priority", "Test Type", "Automation Scope", "Status"};
        for (int i = 0; i < headers.length; i++) {
            XSSFCell cell = headerRow.createCell(i + 1);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(buildColumnHeader());
        }

        traceabilitySheet.createFreezePane(0, 7);
        traceabilitySheet.setAutoFilter(new CellRangeAddress(6, 6, 1, 9));
    }

    private static void buildEvidenceShell() {
        int[] widths = {512, 2400, 2400, 4200, 12000, 4200, 17000, 16000, 512};
        for (int i = 0; i < widths.length; i++) {
            evidenceSheet.setColumnWidth(i, widths[i]);
        }

        evidenceSheet.createRow(0).setHeightInPoints(12);
        XSSFRow titleRow = evidenceSheet.createRow(1);
        titleRow.setHeightInPoints(44);
        evidenceSheet.createRow(2).setHeightInPoints(12);
        for (int col = 0; col <= 8; col++) {
            for (int row = 0; row < 3; row++) {
                XSSFRow currentRow = evidenceSheet.getRow(row);
                if (currentRow == null) {
                    currentRow = evidenceSheet.createRow(row);
                }
                currentRow.createCell(col).setCellStyle(buildFill(DARK_BG));
            }
        }

        evidenceSheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 8));
        XSSFCell titleCell = titleRow.createCell(1);
        titleCell.setCellValue("Evidence Register");
        titleCell.setCellStyle(buildBanner(DARK_BG, WHITE, 18, true));

        evidenceSheet.createRow(3).setHeightInPoints(6);
        XSSFRow metaRow = evidenceSheet.createRow(4);
        metaRow.setHeightInPoints(18);
        evidenceSheet.addMergedRegion(new CellRangeAddress(4, 4, 1, 8));
        XSSFCell metaCell = metaRow.createCell(1);
        metaCell.setCellValue("Execution evidence captured automatically from the application under test.");
        XSSFCellStyle metaStyle = buildBanner(WHITE, GREY_500, 9, false);
        metaStyle.setAlignment(HorizontalAlignment.LEFT);
        metaCell.setCellStyle(metaStyle);

        evidenceSheet.createRow(5).setHeightInPoints(8);
        XSSFRow headerRow = evidenceSheet.createRow(6);
        headerRow.setHeightInPoints(28);
        String[] headers = {"#", "Test Case ID", "Requirement ID", "Module", "Scenario", "Evidence Type", "Evidence Detail", "Artifact / Value"};

        for (int i = 0; i < headers.length; i++) {
            XSSFCell cell = headerRow.createCell(i + 1);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(buildColumnHeader());
        }

        evidenceSheet.createFreezePane(0, 7);
        evidenceSheet.setAutoFilter(new CellRangeAddress(6, 6, 1, 8));
    }

    private static void buildAnalytics() {
        double passRate = totalTests > 0 ? passedTests * 100.0 / totalTests : 0;

        int[] widths = {512, 5200, 3200, 3200, 3200, 3200, 3200, 512};
        for (int i = 0; i < widths.length; i++) {
            analyticsSheet.setColumnWidth(i, widths[i]);
        }

        analyticsSheet.createRow(0).setHeightInPoints(12);
        XSSFRow titleRow = analyticsSheet.createRow(1);
        titleRow.setHeightInPoints(44);
        analyticsSheet.createRow(2).setHeightInPoints(12);
        for (int col = 0; col <= 7; col++) {
            for (int row = 0; row < 3; row++) {
                XSSFRow currentRow = analyticsSheet.getRow(row);
                if (currentRow == null) {
                    currentRow = analyticsSheet.createRow(row);
                }
                currentRow.createCell(col).setCellStyle(buildFill(DARK_BG));
            }
        }

        analyticsSheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 6));
        XSSFCell titleCell = titleRow.createCell(1);
        titleCell.setCellValue("Analytics - Result by Module");
        titleCell.setCellStyle(buildBanner(DARK_BG, WHITE, 18, true));

        analyticsSheet.createRow(3).setHeightInPoints(8);
        XSSFRow summaryRow = analyticsSheet.createRow(4);
        summaryRow.setHeightInPoints(18);
        analyticsSheet.addMergedRegion(new CellRangeAddress(4, 4, 1, 6));
        XSSFCell summaryCell = summaryRow.createCell(1);
        summaryCell.setCellValue(String.format("Overall pass rate: %.1f%% | Passed: %d | Failed: %d | Skipped: %d",
            passRate, passedTests, failedTests, skippedTests));
        XSSFCellStyle summaryStyle = buildBanner(WHITE, GREY_500, 9, false);
        summaryStyle.setAlignment(HorizontalAlignment.LEFT);
        summaryCell.setCellStyle(summaryStyle);

        XSSFRow headerRow = analyticsSheet.createRow(6);
        headerRow.setHeightInPoints(28);
        String[] headers = {"Module", "Total", "Passed", "Failed", "Skipped", "Pass Rate"};
        for (int i = 0; i < headers.length; i++) {
            XSSFCell cell = headerRow.createCell(i + 1);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(buildColumnHeader());
        }

        int rowIndex = 7;
        for (Map.Entry<String, int[]> entry : moduleStats.entrySet()) {
            int[] values = entry.getValue();
            double modulePassRate = values[0] > 0 ? values[1] * 100.0 / values[0] : 0;
            String rowColor = rowIndex % 2 == 0 ? GREY_100 : WHITE;
            XSSFRow row = analyticsSheet.createRow(rowIndex++);
            row.setHeightInPoints(22);

            Object[] cells = {
                entry.getKey(),
                values[0],
                values[1],
                values[2],
                values[3],
                String.format("%.1f%%", modulePassRate)
            };

            String[] colors = {GREY_700, GREY_700, SUCCESS, DANGER, WARN, modulePassRate >= 80 ? SUCCESS : ACCENT};

            for (int i = 0; i < cells.length; i++) {
                XSSFCell cell = row.createCell(i + 1);
                cell.setCellValue(cells[i].toString());
                cell.setCellStyle(buildDataStyle(rowColor, i == 0 || i == 5, false, i > 0, colors[i]));
            }
        }
    }

    private static XSSFCellStyle buildBanner(String backgroundColor, String foregroundColor, int fontSize, boolean bold) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(hex(backgroundColor));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(buildFont(fontSize, bold, foregroundColor));
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private static XSSFCellStyle buildFill(String backgroundColor) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(hex(backgroundColor));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private static XSSFCellStyle buildSmallLabel() {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(hex(WHITE));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(buildFont(8, true, GREY_500));
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private static XSSFCellStyle buildColumnHeader() {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(hex(HEADER_BG));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(buildFont(9, true, WHITE));
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        applyBorder(style, GREY_300);
        return style;
    }

    private static XSSFCellStyle buildDataStyle(String backgroundColor, boolean bold, boolean wrap,
                                                boolean center, String foregroundColor) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(hex(backgroundColor));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(buildFont(9, bold, foregroundColor));
        style.setWrapText(wrap);
        style.setAlignment(center ? HorizontalAlignment.CENTER : HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        applyBorder(style, GREY_300);
        return style;
    }

    private static XSSFCellStyle buildStatusStyle(String backgroundColor, String foregroundColor) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(hex(backgroundColor));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(buildFont(9, true, foregroundColor));
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        applyBorder(style, GREY_300);
        return style;
    }

    private static void applyBorder(XSSFCellStyle style, String borderColor) {
        XSSFColor color = hex(borderColor);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(color);
        style.setBottomBorderColor(color);
        style.setLeftBorderColor(color);
        style.setRightBorderColor(color);
    }

    private static XSSFFont buildFont(int fontSize, boolean bold, String color) {
        XSSFFont font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) fontSize);
        font.setBold(bold);
        font.setColor(hex(color));
        return font;
    }

    private static XSSFColor hex(String value) {
        return new XSSFColor(hexBytes(value), null);
    }

    private static byte[] hexBytes(String value) {
        String color = value.replace("#", "");
        return new byte[] {
            (byte) Integer.parseInt(color.substring(0, 2), 16),
            (byte) Integer.parseInt(color.substring(2, 4), 16),
            (byte) Integer.parseInt(color.substring(4, 6), 16)
        };
    }

    private static String sanitizeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static String sanitizeFileSegment(String value) {
        String sanitized = sanitizeText(value, "AutomationSuite")
            .replaceAll("[\\\\/:*?\"<>|]+", "_")
            .replaceAll("\\s+", "_");
        return sanitized.isBlank() ? "AutomationSuite" : sanitized;
    }

    private static void addTraceabilityRow(String caseId, String requirementId, String moduleName, String scenario,
                                           String priority, String testType, String automationScope, String status) {
        XSSFRow row = traceabilitySheet.createRow(traceabilityRowIndex - 1);
        Object[] values = {
            traceabilityRowIndex - 7,
            caseId,
            requirementId,
            moduleName,
            scenario,
            priority,
            testType,
            automationScope,
            status
        };

        row.setHeightInPoints(calculateRowHeightInPoints(values));
        String rowColor = traceabilityRowIndex % 2 == 0 ? GREY_100 : WHITE;
        String statusBackground = "PASS".equals(status) ? SUCCESS_LITE : ("FAIL".equals(status) ? DANGER_LITE : WARN_LITE);
        String statusForeground = "PASS".equals(status) ? SUCCESS : ("FAIL".equals(status) ? DANGER : WARN);

        for (int i = 0; i < values.length; i++) {
            XSSFCell cell = row.createCell(i + 1);
            if (i == 0) {
                cell.setCellValue((int) values[i]);
            } else {
                cell.setCellValue(values[i] == null ? "" : values[i].toString());
            }

            if (i == 8) {
                cell.setCellStyle(buildStatusStyle(statusBackground, statusForeground));
            } else {
                boolean wrap = i == 4;
                boolean center = i != 4;
                cell.setCellStyle(buildDataStyle(rowColor, false, wrap, center, GREY_700));
            }
        }

        traceabilityRowIndex++;
    }

    private static void addEvidenceEntries(String caseId, String requirementId, String moduleName, String scenario, String evidenceType,
                                           List<String> details, String artifactValue) {
        if (details == null || details.isEmpty()) {
            return;
        }

        for (String detail : details) {
            addEvidenceEntry(caseId, requirementId, moduleName, scenario, evidenceType, detail, artifactValue);
        }
    }

    private static void addEvidenceEntry(String caseId, String requirementId, String moduleName, String scenario, String evidenceType,
                                         String detail, String artifactValue) {
        if (sanitizeText(detail, "").isBlank() && sanitizeText(artifactValue, "").isBlank()) {
            return;
        }

        XSSFRow row = evidenceSheet.createRow(evidenceRowIndex - 1);
        Object[] values = {
            evidenceRowIndex - 7,
            caseId,
            requirementId,
            moduleName,
            scenario,
            evidenceType,
            sanitizeText(detail, ""),
            sanitizeText(artifactValue, "")
        };

        row.setHeightInPoints(calculateRowHeightInPoints(values));

        String rowColor = evidenceRowIndex % 2 == 0 ? GREY_100 : WHITE;
        for (int i = 0; i < values.length; i++) {
            XSSFCell cell = row.createCell(i + 1);
            if (i == 0) {
                cell.setCellValue((int) values[i]);
            } else {
                cell.setCellValue(values[i] == null ? "" : values[i].toString());
            }

            boolean wrap = i >= 4;
            boolean center = i == 0 || i == 1 || i == 2;
            cell.setCellStyle(buildDataStyle(rowColor, false, wrap, center, GREY_700));
        }

        evidenceRowIndex++;
    }

    private static List<String> splitMultilineEntries(String value) {
        String sanitized = sanitizeText(value, "");
        if (sanitized.isBlank()) {
            return List.of();
        }

        String[] entries = sanitized.split("\\R");
        List<String> lines = new java.util.ArrayList<>();
        for (String entry : entries) {
            String cleaned = sanitizeText(entry.replaceFirst("^-\\s*", ""), "");
            if (!cleaned.isBlank()) {
                lines.add(cleaned);
            }
        }
        return lines;
    }

    private static float calculateRowHeightInPoints(Object[] values) {
        int maxLines = 1;

        for (Object value : values) {
            if (value == null) {
                continue;
            }

            String text = value.toString();
            int lineBreaks = text.isBlank() ? 1 : text.split("\\R", -1).length;
            int wrappedLines = Math.max(1, (int) Math.ceil(text.length() / 70.0));
            maxLines = Math.max(maxLines, Math.max(lineBreaks, wrappedLines));
        }

        return Math.min(160, Math.max(24, maxLines * 16));
    }
}
