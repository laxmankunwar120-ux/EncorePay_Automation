package com.encorepay.models;

public class JobStatus {

    private String jobName;
    private String clientName;
    private String jobStatus;
    private String receiptStatus;
    private String reportStatus;
    private String status;
    private int failedCount;
    private int pendingCount;
    private String dateTime;
    private String runName;
    private String startDate;
    private String endDate;
    private String startTime;
    private String endTime;
    private String duration;
    private String nextFireTime;
    private String jobType;
    private String previousFireTime;
    private String currentStatus;
    private String lastRunTime;
    private String executionResult;
    private String errorMessage;
    private String failureReason;
    private String remarks;
    private String accountNumber;
    private String receiptNumber;
    private String amount;
    private String branch;
    private String lmsResponse;
    private String popupDetails;
    private String pendingRecords;

    public JobStatus() {
    }

    public JobStatus(String jobName, String status, int failedCount, int pendingCount, String dateTime) {
        this.jobName = jobName;
        this.status = status;
        this.failedCount = failedCount;
        this.pendingCount = pendingCount;
        this.dateTime = dateTime;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public String getReceiptStatus() {
        return receiptStatus;
    }

    public void setReceiptStatus(String receiptStatus) {
        this.receiptStatus = receiptStatus;
    }

    public String getReportStatus() {
        return reportStatus;
    }

    public void setReportStatus(String reportStatus) {
        this.reportStatus = reportStatus;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public int getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(int pendingCount) {
        this.pendingCount = pendingCount;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getRunName() {
        return runName;
    }

    public void setRunName(String runName) {
        this.runName = runName;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getNextFireTime() {
        return nextFireTime;
    }

    public void setNextFireTime(String nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getPreviousFireTime() {
        return previousFireTime;
    }

    public void setPreviousFireTime(String previousFireTime) {
        this.previousFireTime = previousFireTime;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getLastRunTime() {
        return lastRunTime;
    }

    public void setLastRunTime(String lastRunTime) {
        this.lastRunTime = lastRunTime;
    }

    public String getExecutionResult() {
        return executionResult;
    }

    public void setExecutionResult(String executionResult) {
        this.executionResult = executionResult;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getLmsResponse() {
        return lmsResponse;
    }

    public void setLmsResponse(String lmsResponse) {
        this.lmsResponse = lmsResponse;
    }

    public String getPopupDetails() {
        return popupDetails;
    }

    public void setPopupDetails(String popupDetails) {
        this.popupDetails = popupDetails;
    }

    public String getPendingRecords() {
        return pendingRecords;
    }

    public void setPendingRecords(String pendingRecords) {
        this.pendingRecords = pendingRecords;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    @Override
    public String toString() {
        return "JobStatus{"
            + "jobName='" + jobName + '\''
            + ", clientName='" + clientName + '\''
            + ", jobStatus='" + jobStatus + '\''
            + ", receiptStatus='" + receiptStatus + '\''
            + ", reportStatus='" + reportStatus + '\''
            + ", status='" + status + '\''
            + ", failedCount=" + failedCount
            + ", pendingCount=" + pendingCount
            + ", dateTime='" + dateTime + '\''
            + ", runName='" + runName + '\''
            + ", startDate='" + startDate + '\''
            + ", endDate='" + endDate + '\''
            + ", startTime='" + startTime + '\''
            + ", endTime='" + endTime + '\''
            + ", duration='" + duration + '\''
            + ", nextFireTime='" + nextFireTime + '\''
            + ", jobType='" + jobType + '\''
            + ", previousFireTime='" + previousFireTime + '\''
            + ", currentStatus='" + currentStatus + '\''
            + ", lastRunTime='" + lastRunTime + '\''
            + ", executionResult='" + executionResult + '\''
            + ", errorMessage='" + errorMessage + '\''
            + ", failureReason='" + failureReason + '\''
            + ", remarks='" + remarks + '\''
            + ", accountNumber='" + accountNumber + '\''
            + ", receiptNumber='" + receiptNumber + '\''
            + ", amount='" + amount + '\''
            + ", branch='" + branch + '\''
            + ", lmsResponse='" + lmsResponse + '\''
            + ", popupDetails='" + popupDetails + '\''
            + ", pendingRecords='" + pendingRecords + '\''
            + '}';
    }
}
