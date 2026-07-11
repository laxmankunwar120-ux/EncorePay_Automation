package com.encorepay.utilities;

import com.encorepay.models.JobStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public final class JobMonitoringSummaryFormatter {

    private static final DateTimeFormatter DATE_DISPLAY = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private JobMonitoringSummaryFormatter() {
    }

    public static String formatAdminJobMonitoringReport(List<JobStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return "No job monitoring data available.";
        }

        JobStatus summary = statuses.stream()
                .filter(s -> s.getJobName() != null && s.getJobName().toLowerCase().contains("post"))
                .filter(s -> s.getReceiptNumber() == null || s.getReceiptNumber().isEmpty())
                .findFirst()
                .orElse(null);

        if (summary == null) {
            summary = statuses.stream()
                    .filter(s -> s.getJobName() != null && s.getJobName().toLowerCase().contains("post"))
                    .findFirst()
                    .orElse(new JobStatus());
        }

        List<JobStatus> pendingReceipts = statuses.stream()
                .filter(s -> s.getJobName() != null && s.getJobName().toLowerCase().contains("post"))
                .filter(s -> "PENDING".equalsIgnoreCase(s.getStatus()))
                .filter(s -> s.getReceiptNumber() != null && !s.getReceiptNumber().isEmpty())
                .collect(Collectors.toList());

        List<JobStatus> failedReceipts = statuses.stream()
                .filter(s -> s.getJobName() != null && s.getJobName().toLowerCase().contains("post"))
                .filter(s -> "FAILED".equalsIgnoreCase(s.getStatus()))
                .filter(s -> s.getReceiptNumber() != null && !s.getReceiptNumber().isEmpty())
                .collect(Collectors.toList());

        StringBuilder report = new StringBuilder();

        report.append("====================================================\n");
        report.append("              ADMIN JOB REPORT\n");
        report.append("Date   : ").append(LocalDate.now().format(DATE_DISPLAY)).append("\n");
        report.append("Client : ").append(value(summary.getClientName())).append("\n");
        report.append("Job    : Post Receipt Job\n");
        report.append("====================================================\n\n");

        report.append("PENDING RECEIPTS\n");
        report.append("-----------------------------------------\n");
        report.append("Count : ").append(pendingReceipts.size()).append("\n\n");

        if (!pendingReceipts.isEmpty()) {
            report.append(String.format("%-15s %-15s\n", "Receipt No", "Customer ID"));
            for (JobStatus r : pendingReceipts) {
                report.append(String.format("%-15s %-15s\n", value(r.getReceiptNumber()), value(r.getAccountNumber())));
            }
            report.append("\n");
        }

        report.append("FAILED RECEIPTS\n");
        report.append("-----------------------------------------\n");
        report.append("Count : ").append(failedReceipts.size()).append("\n\n");

        if (!failedReceipts.isEmpty()) {
            report.append(String.format("%-15s %-15s %-20s\n", "Receipt No", "Customer ID", "Error Message"));
            for (JobStatus r : failedReceipts) {
                report.append(String.format("%-15s %-15s %-20s\n", value(r.getReceiptNumber()), value(r.getAccountNumber()), value(r.getErrorMessage())));
            }
            report.append("\n");
        }

        report.append("Job End Time\n");
        report.append("-----------------------------------------\n");
        
        String endTime = value(summary.getEndDate());
        if (!"-".equals(endTime) && summary.getEndTime() != null && !summary.getEndTime().isEmpty()) {
            endTime += " " + summary.getEndTime();
        }
        report.append(endTime).append("\n");
        report.append("====================================================\n");

        return report.toString();
    }

    private static String value(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }
}