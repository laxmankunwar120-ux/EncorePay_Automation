package com.encorepay.utilities;



import com.encorepay.models.JobStatus;



import java.time.LocalDate;

import java.time.format.DateTimeFormatter;

import java.util.List;

import java.util.Locale;



public final class JobMonitoringSummaryFormatter {



    private static final DateTimeFormatter DATE_DISPLAY =

            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);



    private JobMonitoringSummaryFormatter() {

    }



    public static String formatAdminJobMonitoringReport(List<JobStatus> statuses) {



        if (statuses == null || statuses.isEmpty()) {

            return "No job monitoring data available.";

        }



        StringBuilder report = new StringBuilder();



        report.append("📊 DAILY JOB STATUS REPORT\n");

        report.append("Date: ")

                .append(LocalDate.now().format(DATE_DISPLAY))

                .append("\n\n");



        report.append("====================================\n");

        report.append("POST RECEIPTS JOB\n");

        report.append("====================================\n");



        statuses.stream()

                .filter(s -> s.getJobName() != null

                        && s.getJobName().toLowerCase().contains("post"))

                .forEach(s -> {



                    report.append("\nClient: ")

                            .append(value(s.getClientName()));



                    report.append("\nStatus: ")

                            .append(getStatusLabel(s.getStatus()));



                    report.append("\nFailed: ")

                            .append(s.getFailedCount());



                    report.append("\nPending: ")

                            .append(s.getPendingCount());



                    report.append("\nDate & Time: ")

                            .append(value(s.getEndDate()));



                    report.append("\n------------------------------------\n");

                });



        report.append("\n\n====================================\n");

        report.append("ENCORE DOWNLOAD COLLECTION ITEMS JOB\n");

        report.append("====================================\n");



        statuses.stream()

                .filter(s -> s.getJobName() != null

                        && s.getJobName().toLowerCase().contains("collection"))

                .forEach(s -> {



                    report.append("\nClient: ")

                            .append(value(s.getClientName()));



                    report.append("\nStatus: ")

                            .append(getStatusLabel(s.getStatus()));



                    report.append("\nDate & Time: ")

                            .append(value(s.getEndDate()));



                    report.append("\n------------------------------------\n");

                });



        report.append("\n\n====================================\n");

        report.append("ENCORE UPCOMING DEMANDS JOB\n");

        report.append("====================================\n");



        statuses.stream()

                .filter(s -> s.getJobName() != null

                        && s.getJobName().toLowerCase().contains("upcoming"))

                .forEach(s -> {



                    report.append("\nClient: ")

                            .append(value(s.getClientName()));



                    report.append("\nStatus: ")

                            .append(getStatusLabel(s.getStatus()));



                    report.append("\nDate & Time: ")

                            .append(value(s.getEndDate()));



                    report.append("\n------------------------------------\n");

                });



        return report.toString();

    }



    private static String getStatusLabel(String status) {



        if (status == null || status.trim().isEmpty()) {

            return "N/A";

        }



        String s = status.toLowerCase(Locale.ENGLISH);



        if (s.contains("completed") || s.contains("successful")) {

            return "Completed";

        }



        if (s.contains("failed")) {

            return "Failed";

        }



        if (s.contains("pending")) {

            return "Pending";

        }



        if (s.contains("not started") || s.contains("notstarted")) {

            return "Not Started";

        }



        return "N/A";

    }



    private static String value(String value) {

        return value == null || value.trim().isEmpty() ? "-" : value.trim();

    }

}