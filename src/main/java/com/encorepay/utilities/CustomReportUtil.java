package com.encorepay.utilities;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

import org.testng.ITestContext;

public class CustomReportUtil {

    private static final String PROJECT_ROOT = System.getProperty("user.dir");
    private static final String REPORT_PATH =
            PROJECT_ROOT + File.separator + "test-output" + File.separator + "CustomReport.html";

    public static void generateReport(ITestContext context) {

        Map<String, List<Map<String, String>>> allSteps =
                Optional.ofNullable(ScreenshotUtil.getAllSteps()).orElse(new LinkedHashMap<>());

        Map<String, Long> startMap =
                Optional.ofNullable(ScreenshotUtil.getTestStart()).orElse(new HashMap<>());

        Map<String, Long> endMap =
                Optional.ofNullable(ScreenshotUtil.getTestEnd()).orElse(new HashMap<>());

        Map<String, String> statusMap =
                Optional.ofNullable(ScreenshotUtil.getTestStatus()).orElse(new HashMap<>());

        int total   = statusMap.size();
        int passed  = (int) statusMap.values().stream().filter("PASS"::equals).count();
        int failed  = (int) statusMap.values().stream().filter("FAIL"::equals).count();
        int skipped = (int) statusMap.values().stream().filter("SKIP"::equals).count();

        double passRate = total > 0 ? (passed * 100.0 / total) : 0;

        String suiteName  = context != null ? context.getSuite().getName() : "Test Suite";
        String generated  = new SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(new Date());

        StringBuilder html = new StringBuilder();

        // ═══════════════════════════════════════════
        //  HEAD
        // ═══════════════════════════════════════════
        html.append("<!DOCTYPE html>")
            .append("<html lang='en'><head>")
            .append("<meta charset='UTF-8'>")
            .append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
            .append("<title>Automation Report – ").append(escapeHtml(suiteName)).append("</title>")
            .append("<style>")

            // ── Reset & base ──
            .append("*{box-sizing:border-box;margin:0;padding:0;}")
            .append("body{font-family:'Segoe UI',Arial,sans-serif;background:#0d0d0d;color:#e0e0e0;padding:30px;}")
            .append("h1{font-size:26px;font-weight:700;margin-bottom:4px;color:#ffffff;}")
            .append("h2{font-size:18px;font-weight:600;margin-bottom:12px;color:#cccccc;}")
            .append("h3{font-size:15px;font-weight:600;margin-bottom:8px;color:#ffffff;}")
            .append("p{font-size:13px;margin:3px 0;color:#bbbbbb;}")

            // ── Layout ──
            .append(".header{display:flex;justify-content:space-between;align-items:flex-start;")
            .append("background:#1a1a2e;padding:20px 24px;border-radius:10px;margin-bottom:20px;}")
            .append(".header-left .subtitle{font-size:13px;color:#888;margin-top:4px;}")
            .append(".header-right{text-align:right;font-size:12px;color:#777;}")

            // ── Summary grid ──
            .append(".summary{display:grid;grid-template-columns:repeat(5,1fr);gap:14px;margin-bottom:24px;}")
            .append(".stat{background:#1e1e1e;border-radius:10px;padding:16px;text-align:center;")
            .append("border-top:3px solid #333;}")
            .append(".stat .num{font-size:28px;font-weight:700;margin-bottom:4px;}")
            .append(".stat .lbl{font-size:11px;text-transform:uppercase;letter-spacing:1px;color:#888;}")
            .append(".stat.total  .num{color:#90caf9;} .stat.total{border-color:#90caf9;}")
            .append(".stat.pass   .num{color:#66bb6a;} .stat.pass{border-color:#66bb6a;}")
            .append(".stat.fail   .num{color:#ef5350;} .stat.fail{border-color:#ef5350;}")
            .append(".stat.skip   .num{color:#ffa726;} .stat.skip{border-color:#ffa726;}")
            .append(".stat.rate   .num{color:#ab47bc;} .stat.rate{border-color:#ab47bc;}")

            // ── Progress bar ──
            .append(".progress-wrap{background:#1e1e1e;border-radius:10px;padding:16px 20px;margin-bottom:24px;}")
            .append(".progress-wrap p{margin-bottom:8px;font-size:13px;color:#aaa;}")
            .append(".bar-bg{background:#333;border-radius:6px;height:12px;overflow:hidden;}")
            .append(".bar-fill{height:100%;border-radius:6px;")
            .append("background:linear-gradient(90deg,#66bb6a,#43a047);transition:width .4s;}")

            // ── Test cards ──
            .append(".section-title{font-size:14px;text-transform:uppercase;letter-spacing:1px;")
            .append("color:#777;margin-bottom:12px;}")
            .append(".card{background:#1a1a1a;border-radius:10px;margin-bottom:14px;overflow:hidden;")
            .append("border:1px solid #2a2a2a;}")
            .append(".card-header{display:flex;justify-content:space-between;align-items:center;")
            .append("padding:14px 18px;cursor:pointer;user-select:none;}")
            .append(".card-header:hover{background:#222;}")
            .append(".card-title{font-size:14px;font-weight:600;color:#ddd;word-break:break-word;}")
            .append(".card-meta{font-size:11px;color:#666;margin-top:3px;}")
            .append(".badge{font-size:11px;font-weight:700;padding:3px 10px;border-radius:20px;")
            .append("text-transform:uppercase;letter-spacing:.5px;white-space:nowrap;}")
            .append(".badge.PASS{background:#1b5e20;color:#a5d6a7;}")
            .append(".badge.FAIL{background:#b71c1c;color:#ef9a9a;}")
            .append(".badge.SKIP{background:#e65100;color:#ffcc80;}")

            // ── Steps / accordion ──
            .append(".card-body{padding:0 18px 14px;display:none;}")
            .append(".card-body.open{display:block;}")
            .append(".steps{list-style:none;padding:0;margin-top:8px;}")
            .append(".steps li{font-size:12px;color:#aaa;padding:6px 0;border-bottom:1px solid #252525;}")
            .append(".steps li:last-child{border-bottom:none;}")
            .append(".step-label{margin-bottom:4px;}")
            .append("img{width:100%;max-width:520px;border-radius:6px;margin-top:8px;")
            .append("border:1px solid #333;display:block;}")
            .append(".img-missing{color:#ef5350;font-size:11px;margin-top:4px;}")
            .append(".chevron{font-size:12px;color:#555;margin-left:12px;transition:transform .2s;}")
            .append(".chevron.open{transform:rotate(180deg);}")
            .append(".no-data{background:#1e1e1e;border-radius:10px;padding:20px;text-align:center;")
            .append("color:#666;font-size:13px;}")

            .append("</style></head><body>");

        // ═══════════════════════════════════════════
        //  HEADER BANNER
        // ═══════════════════════════════════════════
        html.append("<div class='header'>")
            .append("<div class='header-left'>")
            .append("<h1>&#9654; Automation Report</h1>")
            .append("<div class='subtitle'>Suite: ").append(escapeHtml(suiteName)).append("</div>")
            .append("</div>")
            .append("<div class='header-right'>")
            .append("<div>Generated</div>")
            .append("<div style='color:#aaa;margin-top:2px;'>").append(generated).append("</div>")
            .append("</div>")
            .append("</div>");

        // ═══════════════════════════════════════════
        //  SUMMARY STATS
        // ═══════════════════════════════════════════
        html.append("<div class='summary'>")
            .append("<div class='stat total'><div class='num'>").append(total).append("</div><div class='lbl'>Total</div></div>")
            .append("<div class='stat pass'><div class='num'>").append(passed).append("</div><div class='lbl'>Passed</div></div>")
            .append("<div class='stat fail'><div class='num'>").append(failed).append("</div><div class='lbl'>Failed</div></div>")
            .append("<div class='stat skip'><div class='num'>").append(skipped).append("</div><div class='lbl'>Skipped</div></div>")
            .append("<div class='stat rate'><div class='num'>").append(String.format("%.1f", passRate)).append("%</div><div class='lbl'>Pass Rate</div></div>")
            .append("</div>");

        // ═══════════════════════════════════════════
        //  PROGRESS BAR
        // ═══════════════════════════════════════════
        html.append("<div class='progress-wrap'>")
            .append("<p>Pass Rate – ").append(String.format("%.1f", passRate)).append("% (")
            .append(passed).append(" of ").append(total).append(" tests passed)</p>")
            .append("<div class='bar-bg'>")
            .append("<div class='bar-fill' style='width:").append(String.format("%.1f", passRate)).append("%;'></div>")
            .append("</div>")
            .append("</div>");

        // ═══════════════════════════════════════════
        //  TEST DETAILS
        // ═══════════════════════════════════════════
        html.append("<div class='section-title'>Test Details</div>");

        if (allSteps.isEmpty()) {
            html.append("<div class='no-data'>&#9888; No test data recorded for this run.</div>");
        }

        int cardIndex = 0;

        for (String testName : allSteps.keySet()) {

            List<Map<String, String>> steps =
                    Optional.ofNullable(allSteps.get(testName)).orElse(new ArrayList<>());

            String status = statusMap.getOrDefault(testName, "SKIP");

            long start    = startMap.getOrDefault(testName, 0L);
            long end      = endMap.getOrDefault(testName, 0L);
            long duration = end - start;

            String durationStr = duration > 0
                    ? (duration >= 1000
                        ? String.format("%.2f s", duration / 1000.0)
                        : duration + " ms")
                    : "N/A";

            String cardId = "card-" + cardIndex++;

            html.append("<div class='card'>")

                // ── Clickable header ──
                .append("<div class='card-header' onclick='toggle(\"").append(cardId).append("\")'>")
                .append("<div>")
                .append("<div class='card-title'>").append(escapeHtml(testName)).append("</div>")
                .append("<div class='card-meta'>&#128337; ").append(durationStr)
                .append(" &nbsp;|&nbsp; ").append(steps.size()).append(" step(s)</div>")
                .append("</div>")
                .append("<div style='display:flex;align-items:center;gap:8px;'>")
                .append("<span class='badge ").append(status).append("'>").append(status).append("</span>")
                .append("<span class='chevron' id='chev-").append(cardId).append("'>&#9660;</span>")
                .append("</div>")
                .append("</div>")

                // ── Collapsible body ──
                .append("<div class='card-body' id='").append(cardId).append("'>");

            if (steps.isEmpty()) {
                html.append("<p style='color:#555;font-size:12px;'>No steps recorded.</p>");
            } else {
                html.append("<ul class='steps'>");
                for (Map<String, String> step : steps) {
                    if (step == null) continue;

                    String label = escapeHtml(step.getOrDefault("label", "Step"));
                    String path  = step.getOrDefault("path", "");

                    html.append("<li>")
                        .append("<div class='step-label'>&#10148; ").append(label).append("</div>");

                    if (path != null && !path.isEmpty()) {
                        String base64 = toBase64(path);
                        if (!base64.isEmpty()) {
                            html.append("<img src='").append(base64).append("' alt='").append(label).append("'/>");
                        } else {
                            html.append("<div class='img-missing'>&#10060; Screenshot not found: ").append(escapeHtml(path)).append("</div>");
                        }
                    }

                    html.append("</li>");
                }
                html.append("</ul>");
            }

            html.append("</div>") // card-body
                .append("</div>"); // card
        }

        // ═══════════════════════════════════════════
        //  FOOTER
        // ═══════════════════════════════════════════
        html.append("<div style='text-align:center;margin-top:30px;font-size:11px;color:#444;'>")
            .append("Generated by CustomReportUtil &nbsp;|&nbsp; EncorePay Automation Framework")
            .append("</div>");

        // ═══════════════════════════════════════════
        //  ACCORDION SCRIPT
        // ═══════════════════════════════════════════
        html.append("<script>")
            .append("function toggle(id){")
            .append("var body=document.getElementById(id);")
            .append("var chev=document.getElementById('chev-'+id);")
            .append("if(body.classList.contains('open')){")
            .append("body.classList.remove('open');chev.classList.remove('open');")
            .append("}else{")
            .append("body.classList.add('open');chev.classList.add('open');")
            .append("}}")
            .append("</script>");

        html.append("</body></html>");

        // ═══════════════════════════════════════════
        //  WRITE FILE
        // ═══════════════════════════════════════════
        try {
            File dir = new File(PROJECT_ROOT + File.separator + "test-output");
            if (!dir.exists()) dir.mkdirs();

            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(REPORT_PATH), "UTF-8"));
            writer.write(html.toString());
            writer.close();

            System.out.println("✅ Report Generated: " + REPORT_PATH);

        } catch (Exception e) {
            System.err.println("❌ Failed to write report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════

    private static String toBase64(String relativePath) {
        try {
            File file = new File(PROJECT_ROOT + File.separator + relativePath);
            if (!file.exists()) {
                System.err.println("❌ Missing screenshot: " + file.getAbsolutePath());
                return "";
            }
            byte[] bytes = Files.readAllBytes(file.toPath());
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}