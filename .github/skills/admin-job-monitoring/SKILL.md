---
name: admin-job-monitoring
description: Patterns, pitfalls and fixes for the EncorePay AdminJobsPage Selenium flow — LMS filter selection, status capture, receipt counting, and PDF report rendering.
---

# Admin Job Monitoring — Key Learnings

## 1. Angular dropdown: `Select.selectByVisibleText()` silently fails

**Problem:** Selenium's `Select.selectByVisibleText()` changes the DOM value but Angular never detects it. The dropdown visually resets to `-- Choose Status --` and Search runs unfiltered.

**Fix:** After selecting the option, fire Angular's change-detection events via JS:

```java
select.selectByVisibleText(matchedText);
((JavascriptExecutor) driver).executeScript(
    "var el = arguments[0];" +
    "el.dispatchEvent(new Event('input',  {bubbles:true}));" +
    "el.dispatchEvent(new Event('change', {bubbles:true}));" +
    "el.dispatchEvent(new Event('blur',   {bubbles:true}));",
    selectEl);
```

Always confirm with `select.getFirstSelectedOption().getText()` and log it.

---

## 2. Do NOT touch any other field after selecting the LMS dropdown

**Problem:** Calling `selectTodayReceiptDate()` after selecting the LMS dropdown caused Angular to re-render the filter form and reset the dropdown back to `-- Choose Status --`.

**Rule:** The correct sequence is:
1. `clearReceiptFilters()` — reset previous state
2. `ensureFilterPanelOpen()` — open filter only if closed (toggle-safe)
3. `selectPostingStatus("PENDING")` + fire events
4. `clickSearchAndWait("PENDING")` — immediately search, no other field touches
5. Capture rows
6. Repeat for `"FAILED"`

---

## 3. Show Filter button is a toggle — do not call it blindly

**Problem:** Calling `clickShowFilter()` for both PENDING and FAILED passes. If the filter was already open from the first pass, the second call **closed** it, hiding the LMS dropdown.

**Fix:** Use `ensureFilterPanelOpen()` — check if the LMS `<select>` is already visible before clicking Show Filter.

```java
private void ensureFilterPanelOpen() {
    for (WebElement s : driver.findElements(lmsPostingStatus)) {
        if (s.isDisplayed()) return; // already open
    }
    clickShowFilter(); // only click if closed
}
```

---

## 4. LMS dropdown XPath — avoid broad fallbacks

**Problem:** The XPath `(//app-receipts//div//select)[1]` matched a hidden unrelated `<select>`, not the visible LMS Posting Status control. Selection "succeeded" on the wrong element.

**Correct XPath** (narrow, label-proximity based):
```java
By.xpath(
    "//select[@name='lmsPostingStatus']"
    + " | //select[@id='lmsPostingStatus']"
    + " | //*[contains(normalize-space(),'LMS Posting Status')]/following::select[1]"
    + " | //*[contains(normalize-space(),'LMS Posting')]/following-sibling::*//select"
    + " | //*[contains(normalize-space(),'LMS Posting')]/following-sibling::select")
```

---

## 5. Status in the report must be the raw UI text — never normalize

**Problem:** `statusLabel()` in the PDF generator was converting raw values:
- `SUCCESSFUL` → `"Completed"`
- `FAILED` → `"Failed"`

This hid what the UI actually showed.

**Rule:** `jobStatus` is set directly from the View modal's Status field (e.g. `"SUCCESSFUL"`, `"FAILED"`). It must never be overwritten or normalized before being written to the report. `normalizeJobStatus()` is for internal branching logic only.

**`applyPostReceiptBusinessStatus` must NOT overwrite `jobStatus`:**
```java
// WRONG — destroys raw UI value
summary.setJobStatus(normalizedJobStatus);

// CORRECT — raw UI value is preserved; only reportStatus/status are set
summary.setStatus(rawStatus);
summary.setReportStatus(rawStatus);
```

**PDF `statusLabel()` must return raw text:**
```java
private static String statusLabel(JobStatus js) {
    if (js == null) return "N/A";
    String raw = firstNonBlank(js.getReportStatus(), js.getStatus(), js.getJobStatus());
    return isBlank(raw) ? "N/A" : raw.trim(); // NO conversion
}
```

---

## 6. Badge colors must match raw values (not normalized labels)

After removing normalization, `statusFg`/`statusBg` must use `contains()` on lowercase:

```java
private static DeviceRgb statusFg(String label) {
    String s = label == null ? "" : label.toLowerCase(Locale.ROOT);
    if (s.contains("successful") || s.contains("completed")) return GREEN_FG;
    if (s.contains("failed"))                                 return RED_FG;
    if (s.contains("pending"))                                return ORANGE_FG;
    if (s.contains("not started") || s.contains("running"))   return ORANGE_FG;
    return GRAY_FG;
}
```

---

## 7. Post Receipts failure reason comes from receipt error popups, not from the job modal

**Problem:** `applyPostReceiptBusinessStatus` set `failureReason = "Receipt Posting Failure"` (hardcoded generic placeholder).

**Fix:** After `applyPostReceiptBusinessStatus`, collect real reasons from `failedRows`:
```java
if (!failedRows.isEmpty()) {
    List<String> realReasons = new ArrayList<>();
    for (JobStatus fr : failedRows) {
        String r = firstNonBlank(fr.getFailureReason(), fr.getErrorMessage(), fr.getPopupDetails());
        if (!r.isEmpty() && !realReasons.contains(r)) realReasons.add(r);
    }
    if (!realReasons.isEmpty()) {
        summary.setFailureReason(String.join(" | ", realReasons));
    }
}
```

---

## 8. `statuses.addAll(failedRows)` adds duplicate rows to the report

`capturePostReceiptsJob` adds both `summary` and `failedRows` to the statuses list. The PDF/text report must filter to summary-level rows only (those without a receipt number) when rendering the main table:

```java
List<JobStatus> summaryRows = statuses.stream()
    .filter(s -> s.getReceiptNumber() == null || s.getReceiptNumber().isBlank())
    .toList();
```

---

## 9. Text summary must use `reportStatus`, not `jobStatus`

The `JobMonitoringSummaryFormatter` was reading `row.getJobStatus()` which is the raw modal value. For Post Receipts, `reportStatus` is the business-mapped display value. Use:

```java
String displayStatus = firstNonBlank(row.getReportStatus(), row.getStatus(), row.getJobStatus());
```

---

## Report columns — correct meaning

| Column | Source | Notes |
|---|---|---|
| Status | View modal → `Status` field (raw text) | Never normalize |
| Failed Count | Count of FAILED rows from LMS filter search | |
| Pending to Be Posted | Count of PENDING rows from LMS filter search | |
| Date & Time | View modal → `End Date` + `End Time` | |
| Reason (in Status badge) | Failed receipt popup or View modal Reason field | Only shown when FAILED |
