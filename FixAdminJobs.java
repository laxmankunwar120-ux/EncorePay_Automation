import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;

public class FixAdminJobs {
    public static void main(String[] args) throws Exception {
        String filepath = "src/main/java/com/encorepay/pages/AdminJobsPage.java";
        List<String> lines = Files.readAllLines(Paths.get(filepath));
        List<String> newLines = new ArrayList<>();
        
        for (String line : lines) {
            String stripped = line.trim();
            
            // Keep TODOs or FIXME, remove all other full-line comments `// ...`
            if (stripped.startsWith("//")) {
                if (!stripped.contains("TODO") && !stripped.contains("FIXME") && !stripped.contains("NOTE:")) {
                    continue; // drop unnecessary comments
                }
            }
            
            newLines.add(line);
        }
        
        String content = String.join("\n", newLines);
        
        // --- 1. Fix findScrollableContainer
        String oldFindScrollable = "    private WebElement findScrollableContainer(WebElement modal) {\n" +
            "        List<WebElement> candidates = modal.findElements(By.xpath(\n" +
            "            \".//*[self::mat-dialog-content\"\n" +
            "            + \" or contains(@class,'mat-dialog-content')\"\n" +
            "            + \" or contains(@class,'dialog-content')\"\n" +
            "            + \" or contains(@class,'modal-body')\"\n" +
            "            + \" or contains(@class,'cdk-scrollable')\"\n" +
            "            + \" or contains(@class,'scrollable')]\"));\n" +
            "        for (WebElement c : candidates) {\n" +
            "            if (isScrollable(c)) return c;\n" +
            "        }\n" +
            "        for (WebElement c : modal.findElements(By.xpath(\".//*\"))) {\n" +
            "            if (isScrollable(c)) return c;\n" +
            "        }\n" +
            "        return modal;\n" +
            "    }";
            
        String newFindScrollable = "    private WebElement findScrollableContainer(WebElement modal) {\n" +
            "        List<WebElement> candidates = modal.findElements(By.xpath(\n" +
            "            \".//*[self::mat-dialog-content\"\n" +
            "            + \" or contains(@class,'mat-dialog-content')\"\n" +
            "            + \" or contains(@class,'dialog-content')\"\n" +
            "            + \" or contains(@class,'modal-body')\"\n" +
            "            + \" or contains(@class,'cdk-scrollable')\"\n" +
            "            + \" or contains(@class,'scrollable')]\"));\n" +
            "        for (WebElement c : candidates) {\n" +
            "            if (c.isDisplayed()) return c;\n" +
            "        }\n" +
            "        return modal;\n" +
            "    }";
        content = content.replace(oldFindScrollable, newFindScrollable);
        
        // --- 2. Fix scrollContainerTo
        String oldScrollContainer = "    private void scrollContainerTo(WebElement container, String position) {\n" +
            "        try {\n" +
            "            if (\"bottom\".equals(position)) {\n" +
            "                ((JavascriptExecutor) driver).executeScript(\n" +
            "                    \"arguments[0].scrollTop = arguments[0].scrollHeight;\", container);\n" +
            "            } else {\n" +
            "                ((JavascriptExecutor) driver).executeScript(\"arguments[0].scrollTop = 0;\", container);\n" +
            "            }\n" +
            "        } catch (Exception ignored) {}\n" +
            "    }";
            
        String newScrollContainer = "    private void scrollContainerTo(WebElement container, String position) {\n" +
            "        try {\n" +
            "            if (\"bottom\".equals(position)) {\n" +
            "                ((JavascriptExecutor) driver).executeScript(\n" +
            "                    \"arguments[0].scrollTo({ top: arguments[0].scrollHeight, behavior: 'smooth' });\" +\n" +
            "                    \"arguments[0].scrollTop = arguments[0].scrollHeight;\", container);\n" +
            "            } else {\n" +
            "                ((JavascriptExecutor) driver).executeScript(\n" +
            "                    \"arguments[0].scrollTo({ top: 0, behavior: 'smooth' });\" +\n" +
            "                    \"arguments[0].scrollTop = 0;\", container);\n" +
            "            }\n" +
            "        } catch (Exception ignored) {}\n" +
            "    }";
        content = content.replace(oldScrollContainer, newScrollContainer);
        
        // --- 3. Fix readModalDetails
        String oldReadModal = "            scrollContainerTo(scrollable, \"top\");\n" +
            "            waitMillis(50);\n" +
            "            String uiStatus = readField(modalEl, scrollable, \"Status\", \"Execution Status\", \"Result\");\n" +
            "\n" +
            "            scrollContainerTo(scrollable, \"bottom\");\n" +
            "            waitMillis(50);\n" +
            "            String uiReason = readField(modalEl, scrollable,\n" +
            "                \"Failure Reason\", \"Reason\", \"Error Message\", \"Error\");\n" +
            "\n" +
            "            scrollContainerTo(scrollable, \"top\");\n" +
            "            waitMillis(50);\n" +
            "            String uiStartDate = readField(modalEl, scrollable, \"Start Date\");\n" +
            "            String uiStartTime = readField(modalEl, scrollable, \"Start Time\");\n" +
            "            String uiEndDate   = readField(modalEl, scrollable, \"End Date\");\n" +
            "            String uiEndTime   = readField(modalEl, scrollable, \"End Time\");\n" +
            "\n" +
            "            if (!uiStatus.isBlank()) {\n" +
            "                status.setStatus(normalizeStatus(uiStatus));\n" +
            "                status.setCurrentStatus(status.getStatus());\n" +
            "            }\n" +
            "            if (!uiStartDate.isBlank()) status.setStartDate(uiStartDate);\n" +
            "            if (!uiStartTime.isBlank()) status.setStartTime(uiStartTime);\n" +
            "            if (!uiEndDate.isBlank())   status.setEndDate(uiEndDate);\n" +
            "            if (!uiEndTime.isBlank())   status.setEndTime(uiEndTime);\n" +
            "            if (!uiReason.isBlank())    status.setFailureReason(uiReason);";
            
        String newReadModal = "            scrollContainerTo(scrollable, \"top\");\n" +
            "            waitMillis(100);\n" +
            "            String uiJobType = readField(modalEl, scrollable, \"Job Type\");\n" +
            "            String uiStatus = readField(modalEl, scrollable, \"Status\", \"Execution Status\", \"Result\");\n" +
            "            String uiStartDate = readField(modalEl, scrollable, \"Start Date\");\n" +
            "            String uiStartTime = readField(modalEl, scrollable, \"Start Time\");\n" +
            "\n" +
            "            scrollContainerTo(scrollable, \"bottom\");\n" +
            "            waitMillis(100);\n" +
            "            String uiEndDate   = readField(modalEl, scrollable, \"End Date\");\n" +
            "            String uiEndTime   = readField(modalEl, scrollable, \"End Time\");\n" +
            "            String uiDuration  = readField(modalEl, scrollable, \"Duration\", \"Time Duration\");\n" +
            "            String uiNextFireTime = readField(modalEl, scrollable, \"Next Fire Time\");\n" +
            "            String uiReason = readField(modalEl, scrollable,\n" +
            "                \"Failure Reason\", \"Reason\", \"Error Message\", \"Error\");\n" +
            "\n" +
            "            if (!uiJobType.isBlank()) status.setJobType(uiJobType);\n" +
            "            if (!uiStatus.isBlank()) {\n" +
            "                status.setStatus(normalizeStatus(uiStatus));\n" +
            "                status.setCurrentStatus(status.getStatus());\n" +
            "            }\n" +
            "            if (!uiStartDate.isBlank()) status.setStartDate(uiStartDate);\n" +
            "            if (!uiStartTime.isBlank()) status.setStartTime(uiStartTime);\n" +
            "            if (!uiEndDate.isBlank())   status.setEndDate(uiEndDate);\n" +
            "            if (!uiEndTime.isBlank())   status.setEndTime(uiEndTime);\n" +
            "            if (!uiDuration.isBlank())  status.setDuration(uiDuration);\n" +
            "            if (!uiNextFireTime.isBlank()) status.setNextFireTime(uiNextFireTime);\n" +
            "            if (!uiReason.isBlank())    status.setFailureReason(uiReason);";
            
        content = content.replace(oldReadModal, newReadModal);
        
        // --- 4. Fix readField so that it doesn't skip elements that have isDisplayed() == false before scroll
        String oldReadField = "                for (WebElement labelEl : labelEls) {\n" +
            "                    if (!labelEl.isDisplayed()) continue;\n" +
            "                    scrollLabelIntoView(labelEl);\n" +
            "                    waitMillis(30);\n" +
            "                    String value = getModalFieldValue(labelEl);\n" +
            "                    if (!value.isBlank()) return value;\n" +
            "                }";
            
        String newReadField = "                for (WebElement labelEl : labelEls) {\n" +
            "                    scrollLabelIntoView(labelEl);\n" +
            "                    waitMillis(50);\n" +
            "                    if (!labelEl.isDisplayed()) continue;\n" +
            "                    String value = getModalFieldValue(labelEl);\n" +
            "                    if (!value.isBlank()) return value;\n" +
            "                }";
        content = content.replace(oldReadField, newReadField);
        
        // Remove isScrollable method safely
        content = content.replaceAll("(?s)    private boolean isScrollable\\(WebElement el\\) \\{.*?    \\}\\n*", "");

        Files.write(Paths.get(filepath), content.getBytes());
        System.out.println("Done processing.");
    }
}
