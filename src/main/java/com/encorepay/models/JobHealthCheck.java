package com.encorepay.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of read-only health monitoring for a single Admin job row.
 */
public class JobHealthCheck {

    private String jobName;
    private String listJobState;
    private String latestRunStatus;
    private boolean warningIconPresent;
    private boolean exclamationIconPresent;
    private boolean errorIconPresent;
    private boolean healthy;
    private final List<String> issues = new ArrayList<>();
    private final List<JobStatus> clientRows = new ArrayList<>();
    private JobStatus capturedStatus;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getListJobState() {
        return listJobState;
    }

    public void setListJobState(String listJobState) {
        this.listJobState = listJobState;
    }

    public String getLatestRunStatus() {
        return latestRunStatus;
    }

    public void setLatestRunStatus(String latestRunStatus) {
        this.latestRunStatus = latestRunStatus;
    }

    public boolean isWarningIconPresent() {
        return warningIconPresent;
    }

    public void setWarningIconPresent(boolean warningIconPresent) {
        this.warningIconPresent = warningIconPresent;
    }

    public boolean isExclamationIconPresent() {
        return exclamationIconPresent;
    }

    public void setExclamationIconPresent(boolean exclamationIconPresent) {
        this.exclamationIconPresent = exclamationIconPresent;
    }

    public boolean isErrorIconPresent() {
        return errorIconPresent;
    }

    public void setErrorIconPresent(boolean errorIconPresent) {
        this.errorIconPresent = errorIconPresent;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    public List<String> getIssues() {
        return Collections.unmodifiableList(issues);
    }

    public void addIssue(String issue) {
        if (issue != null && !issue.isBlank()) {
            issues.add(issue.trim());
        }
    }

    public List<JobStatus> getClientRows() {
        return Collections.unmodifiableList(clientRows);
    }

    public void addClientRow(JobStatus row) {
        if (row != null) {
            clientRows.add(row);
        }
    }

    public JobStatus getCapturedStatus() {
        return capturedStatus;
    }

    public void setCapturedStatus(JobStatus capturedStatus) {
        this.capturedStatus = capturedStatus;
    }

    public String buildFailureMessage() {
        if (issues.isEmpty()) {
            return jobName + ": job health check failed.";
        }
        return jobName + ": " + String.join(" | ", issues);
    }
}
