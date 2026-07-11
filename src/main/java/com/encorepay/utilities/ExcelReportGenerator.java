package com.encorepay.utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.encorepay.models.JobStatus;

public final class ExcelReportGenerator {

    private static final String[] HEADERS = {
        "Date", "Client", "Job Name", "Job Status", "Receipt Status", "Report Status",
        "Failed Count", "Pending Count", "Account Number", "Receipt Number", "Error Message",
        "Failure Reason", "Remarks", "Start Date", "End Date", "Start Time", "End Time",
        "Duration", "Next Fire Time"
    };

    private ExcelReportGenerator() {
    }

    public static String generateExcelReport(List<JobStatus> statuses) {
        File outputDir = new File(System.getProperty("user.dir"), "reports");
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IllegalStateException("Could not create report directory: " + outputDir.getAbsolutePath());
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        File reportFile = new File(outputDir, "Admin_Job_Monitoring_Report_" + timestamp + ".xlsx");

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             FileOutputStream outputStream = new FileOutputStream(reportFile)) {
            XSSFSheet sheet = workbook.createSheet("Admin Job Monitoring");
            sheet.createFreezePane(0, 1);

            XSSFCellStyle headerStyle = headerStyle(workbook);
            XSSFCellStyle dataStyle = dataStyle(workbook);

            XSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                XSSFCell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, Math.max(4200, HEADERS[i].length() * 320));
            }

            int rowIndex = 1;
            if (statuses != null) {
                for (JobStatus status : statuses) {
                    XSSFRow row = sheet.createRow(rowIndex++);
                    String[] values = {
                        LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                        value(status.getClientName()),
                        value(status.getJobName()),
                        value(firstNonBlank(status.getJobStatus(), status.getStatus())),
                        value(status.getReceiptStatus()),
                        value(firstNonBlank(status.getReportStatus(), status.getStatus())),
                        String.valueOf(status.getFailedCount()),
                        String.valueOf(status.getPendingCount()),
                        value(status.getAccountNumber()),
                        value(status.getReceiptNumber()),
                        value(status.getErrorMessage()),
                        value(status.getFailureReason()),
                        value(status.getRemarks()),
                        value(status.getStartDate()),
                        value(status.getEndDate()),
                        value(status.getStartTime()),
                        value(status.getEndTime()),
                        value(status.getDuration()),
                        value(status.getNextFireTime())
                    };
                    for (int i = 0; i < values.length; i++) {
                        XSSFCell cell = row.createCell(i);
                        cell.setCellValue(values[i]);
                        cell.setCellStyle(dataStyle);
                    }
                }
            }

            sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, Math.max(0, rowIndex - 1), 0, HEADERS.length - 1));
            workbook.write(outputStream);
            return reportFile.getAbsolutePath();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to generate Admin Job Monitoring Excel report.", e);
        }
    }

    private static XSSFCellStyle headerStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(new org.apache.poi.xssf.usermodel.XSSFColor(new byte[] {31, 78, 121}, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setColor(new org.apache.poi.xssf.usermodel.XSSFColor(new byte[] {(byte) 255, (byte) 255, (byte) 255}, null));
        style.setFont(font);
        return style;
    }

    private static XSSFCellStyle dataStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static String value(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }
}
