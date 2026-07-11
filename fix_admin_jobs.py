import re
import sys

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. Remove unnecessary line comments, keeping meaningful ones (like TODOs or Javadocs)
    # We will remove lines that just contain `// ` followed by some text, especially if they are separators like `// ───...`
    # Let's target specific useless comments or aggressively remove `// ` comments.
    lines = content.split('\n')
    new_lines = []
    in_javadoc = False
    
    meaningful_keywords = ['TODO', 'FIXME', 'NOTE:']
    
    for line in lines:
        stripped = line.strip()
        
        # Keep javadocs and block comments
        if stripped.startswith('/*'):
            in_javadoc = True
            
        if in_javadoc:
            new_lines.append(line)
            if '*/' in stripped:
                in_javadoc = False
            continue
            
        # Handle line comments
        if stripped.startswith('//'):
            # Check if it contains meaningful keywords
            if any(kw in stripped for kw in meaningful_keywords):
                new_lines.append(line)
            # Otherwise skip
            continue
            
        # Remove trailing comments if they exist and are not URLs (like http://)
        # It's safer to just skip whole-line comments
        
        new_lines.append(line)

    content = '\n'.join(new_lines)
    
    # 2. Fix findScrollableContainer
    old_find_scrollable = """    private WebElement findScrollableContainer(WebElement modal) {
        List<WebElement> candidates = modal.findElements(By.xpath(
            ".//*[self::mat-dialog-content"
            + " or contains(@class,'mat-dialog-content')"
            + " or contains(@class,'dialog-content')"
            + " or contains(@class,'modal-body')"
            + " or contains(@class,'cdk-scrollable')"
            + " or contains(@class,'scrollable')]"));
        for (WebElement c : candidates) {
            if (isScrollable(c)) return c;
        }
        for (WebElement c : modal.findElements(By.xpath(".//*"))) {
            if (isScrollable(c)) return c;
        }
        return modal;
    }"""
    
    new_find_scrollable = """    private WebElement findScrollableContainer(WebElement modal) {
        List<WebElement> candidates = modal.findElements(By.xpath(
            ".//*[self::mat-dialog-content"
            + " or contains(@class,'mat-dialog-content')"
            + " or contains(@class,'dialog-content')"
            + " or contains(@class,'modal-body')"
            + " or contains(@class,'cdk-scrollable')"
            + " or contains(@class,'scrollable')]"));
        for (WebElement c : candidates) {
            if (c.isDisplayed()) return c;
        }
        return modal;
    }"""
    
    content = content.replace(old_find_scrollable, new_find_scrollable)
    
    # 3. Fix scrollContainerTo to make sure it executes reliably
    old_scroll_container = """    private void scrollContainerTo(WebElement container, String position) {
        try {
            if ("bottom".equals(position)) {
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollTop = arguments[0].scrollHeight;", container);
            } else {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollTop = 0;", container);
            }
        } catch (Exception ignored) {}
    }"""
    
    new_scroll_container = """    private void scrollContainerTo(WebElement container, String position) {
        try {
            if ("bottom".equals(position)) {
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollTo({ top: arguments[0].scrollHeight, behavior: 'smooth' });" +
                    "arguments[0].scrollTop = arguments[0].scrollHeight;", container);
            } else {
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollTo({ top: 0, behavior: 'smooth' });" +
                    "arguments[0].scrollTop = 0;", container);
            }
        } catch (Exception ignored) {}
    }"""
    
    content = content.replace(old_scroll_container, new_scroll_container)

    # 4. Fix readModalDetails to capture all relevant details (Job Type, Duration, Next Fire Time, etc)
    old_read_modal = """            scrollContainerTo(scrollable, "top");
            waitMillis(50);
            String uiStatus = readField(modalEl, scrollable, "Status", "Execution Status", "Result");

            scrollContainerTo(scrollable, "bottom");
            waitMillis(50);
            String uiReason = readField(modalEl, scrollable,
                "Failure Reason", "Reason", "Error Message", "Error");

            scrollContainerTo(scrollable, "top");
            waitMillis(50);
            String uiStartDate = readField(modalEl, scrollable, "Start Date");
            String uiStartTime = readField(modalEl, scrollable, "Start Time");
            String uiEndDate   = readField(modalEl, scrollable, "End Date");
            String uiEndTime   = readField(modalEl, scrollable, "End Time");

            if (!uiStatus.isBlank()) {
                status.setStatus(normalizeStatus(uiStatus));
                status.setCurrentStatus(status.getStatus());
            }
            if (!uiStartDate.isBlank()) status.setStartDate(uiStartDate);
            if (!uiStartTime.isBlank()) status.setStartTime(uiStartTime);
            if (!uiEndDate.isBlank())   status.setEndDate(uiEndDate);
            if (!uiEndTime.isBlank())   status.setEndTime(uiEndTime);
            if (!uiReason.isBlank())    status.setFailureReason(uiReason);"""
            
    new_read_modal = """            scrollContainerTo(scrollable, "top");
            waitMillis(100);
            String uiJobType = readField(modalEl, scrollable, "Job Type");
            String uiStatus = readField(modalEl, scrollable, "Status", "Execution Status", "Result");
            String uiStartDate = readField(modalEl, scrollable, "Start Date");
            String uiStartTime = readField(modalEl, scrollable, "Start Time");

            scrollContainerTo(scrollable, "bottom");
            waitMillis(100);
            String uiEndDate   = readField(modalEl, scrollable, "End Date");
            String uiEndTime   = readField(modalEl, scrollable, "End Time");
            String uiDuration  = readField(modalEl, scrollable, "Duration", "Time Duration");
            String uiNextFireTime = readField(modalEl, scrollable, "Next Fire Time");
            String uiReason = readField(modalEl, scrollable,
                "Failure Reason", "Reason", "Error Message", "Error");

            if (!uiJobType.isBlank()) status.setJobType(uiJobType);
            if (!uiStatus.isBlank()) {
                status.setStatus(normalizeStatus(uiStatus));
                status.setCurrentStatus(status.getStatus());
            }
            if (!uiStartDate.isBlank()) status.setStartDate(uiStartDate);
            if (!uiStartTime.isBlank()) status.setStartTime(uiStartTime);
            if (!uiEndDate.isBlank())   status.setEndDate(uiEndDate);
            if (!uiEndTime.isBlank())   status.setEndTime(uiEndTime);
            if (!uiDuration.isBlank())  status.setDuration(uiDuration);
            if (!uiNextFireTime.isBlank()) status.setNextFireTime(uiNextFireTime);
            if (!uiReason.isBlank())    status.setFailureReason(uiReason);"""
            
    content = content.replace(old_read_modal, new_read_modal)
    
    # 5. Fix readField so that it doesn't skip elements that have isDisplayed() == false
    # because they might just be scrolled out of view!
    old_read_field = """                for (WebElement labelEl : labelEls) {
                    if (!labelEl.isDisplayed()) continue;
                    scrollLabelIntoView(labelEl);
                    waitMillis(30);
                    String value = getModalFieldValue(labelEl);
                    if (!value.isBlank()) return value;
                }"""
                
    new_read_field = """                for (WebElement labelEl : labelEls) {
                    scrollLabelIntoView(labelEl);
                    waitMillis(50);
                    if (!labelEl.isDisplayed()) continue;
                    String value = getModalFieldValue(labelEl);
                    if (!value.isBlank()) return value;
                }"""
                
    content = content.replace(old_read_field, new_read_field)

    # 6. Remove isScrollable entirely as it's no longer used
    content = re.sub(r'    private boolean isScrollable\(WebElement el\) \{[\s\S]*?    \}\n', '', content)

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

if __name__ == '__main__':
    process_file(r'd:\EncorepayProject\EncorepayProject\src\main\java\com\encorepay\pages\AdminJobsPage.java')
