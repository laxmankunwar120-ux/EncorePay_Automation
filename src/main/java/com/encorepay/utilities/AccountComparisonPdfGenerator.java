package com.encorepay.utilities;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Generates the ACCOUNT COMPARISON REPORT (Old vs New server data migration
 * validation) using the same design language as
 * {@link DashboardComparisonPdfGenerator}: identical colour theme, fonts,
 * header/footer style, section banners and alternate-row tables.
 */
public final class AccountComparisonPdfGenerator {

    // ── Design tokens copied verbatim from DashboardComparisonPdfGenerator ──────
    private static final DeviceRgb TITLE_BG   = new DeviceRgb(13, 43, 85);
    private static final DeviceRgb SECTION_BG = new DeviceRgb(20, 66, 110);
    private static final DeviceRgb COL_HDR_BG = new DeviceRgb(22, 61, 107);
    private static final DeviceRgb COL_HDR_FG = new DeviceRgb(187, 222, 251);
    private static final DeviceRgb ROW_ODD    = new DeviceRgb(234, 242, 251);
    private static final DeviceRgb ROW_EVEN   = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb BORDER_CLR = new DeviceRgb(176, 190, 197);
    private static final DeviceRgb GREEN_FG   = new DeviceRgb(27, 94, 32);
    private static final DeviceRgb GREEN_BG   = new DeviceRgb(200, 230, 201);
    private static final DeviceRgb RED_FG     = new DeviceRgb(183, 28, 28);
    private static final DeviceRgb RED_BG     = new DeviceRgb(255, 205, 210);
    private static final DeviceRgb ORANGE_FG  = new DeviceRgb(191, 54, 12);
    private static final DeviceRgb ORANGE_BG  = new DeviceRgb(255, 204, 188);
    private static final DeviceRgb WHITE      = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb DARK_TEXT  = new DeviceRgb(13, 27, 42);
    private static final DeviceRgb LIGHT_BLUE = new DeviceRgb(144, 202, 249);

    private static final DateTimeFormatter DATE_DISPLAY   = DateTimeFormatter.ofPattern("d MMM yyyy");
    private static final DateTimeFormatter TIME_DISPLAY   = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private AccountComparisonPdfGenerator() {}

    public static String generateReport(AccountComparisonReportData data) {
        try {
            String dir = "reports";
            Files.createDirectories(Paths.get(dir));
            String timestamp = LocalDateTime.now().format(FILE_TIMESTAMP);
            String account = sanitize(data.getAccountNumber());
            String fileName = "Account_Comparison_Report"
                    + (account.isBlank() ? "" : "_" + account) + "_" + timestamp + ".pdf";
            String path = dir + "/" + fileName;
            Files.write(Paths.get(path), generate(data));
            return path;
        } catch (IOException e) {
            throw new RuntimeException("Failed to write Account Comparison PDF", e);
        }
    }

    public static byte[] generate(AccountComparisonReportData data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PdfWriter writer = new PdfWriter(out);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document doc = new Document(pdfDoc, PageSize.A4.rotate())) {

            doc.setMargins(20, 20, 20, 20);
            PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            String reportDate = LocalDate.now().format(DATE_DISPLAY);
            String reportTime = LocalDateTime.now().format(TIME_DISPLAY);

            titleBanner(doc, bold, reportDate);
            gap(doc, 6);

            executionDetailsSection(doc, bold, regular, data, reportDate, reportTime);
            gap(doc, 8);

            summarySection(doc, bold, regular, data);
            gap(doc, 8);

            fieldComparisonSection(doc, bold, regular, data);
            gap(doc, 8);

            mismatchSummarySection(doc, bold, regular, data);
            gap(doc, 8);

            oldDataSection(doc, bold, regular, "OLD SERVER DATA", data.getOldData(), new DeviceRgb(21, 101, 192));
            gap(doc, 6);
            newDataSection(doc, bold, regular, "NEW SERVER DATA", data.getNewData(), new DeviceRgb(46, 125, 50));
            gap(doc, 8);

            screenshotsSection(doc, bold, regular, data);
            gap(doc, 8);

            executionTrailerSection(doc, bold, regular, data, reportDate, reportTime);
            gap(doc, 6);

            footer(doc, regular, reportDate);
        }
        return out.toByteArray();
    }

    // ── Sections ───────────────────────────────────────────────────────────────

    private static void titleBanner(Document doc, PdfFont bold, String reportDate) {
        Table t = pctTable(100);
        t.addCell(new Cell()
                .add(new Paragraph("ACCOUNT COMPARISON REPORT")
                        .setFont(bold).setFontSize(16).setFontColor(WHITE)
                        .setTextAlignment(TextAlignment.LEFT))
                .add(new Paragraph(reportDate + "   |   Old vs New Server Data Migration   |   EncorePay Platform")
                        .setFont(bold).setFontSize(9).setFontColor(LIGHT_BLUE)
                        .setTextAlignment(TextAlignment.LEFT))
                .setBackgroundColor(TITLE_BG).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setPaddingTop(12).setPaddingBottom(12).setPaddingLeft(14).setPaddingRight(14));
        doc.add(t);
    }

    private static void executionDetailsSection(Document doc, PdfFont bold, PdfFont regular,
                                                AccountComparisonReportData data, String date, String time) {
        sectionBanner(doc, bold, "EXECUTION DETAILS", 5);
        Table t = pctTable(30, 70);
        addDetailRow(t, bold, regular, "Execution Date", date);
        addDetailRow(t, bold, regular, "Execution Time", time);
        addDetailRow(t, bold, regular, "Account Number", data.getAccountNumber());
        addDetailRow(t, bold, regular, "Old Server URL", data.getOldServerUrl());
        addDetailRow(t, bold, regular, "New Server URL", data.getNewServerUrl());
        t.setBorder(new com.itextpdf.layout.borders.SolidBorder(BORDER_CLR, 0.7f));
        doc.add(t);
    }

    private static void addDetailRow(Table t, PdfFont bold, PdfFont regular, String label, String value) {
        DeviceRgb bg = ROW_ODD;
        dataCell(t, bold, label, bg, DARK_TEXT, TextAlignment.LEFT);
        dataCell(t, regular, value == null ? "" : value, bg, DARK_TEXT, TextAlignment.LEFT);
    }

    private static void summarySection(Document doc, PdfFont bold, PdfFont regular,
                                        AccountComparisonReportData data) {
        Map<String, String> results = data.getComparisonResults();
        int total = results.size();
        long matched = results.values().stream().filter(v -> "MATCHED".equalsIgnoreCase(v)).count();
        long mismatched = results.values().stream().filter(v -> "MISMATCH".equalsIgnoreCase(v)).count();
        long notFound = results.values().stream().filter(v -> "FIELD NOT FOUND".equalsIgnoreCase(v)).count();
        double pct = total == 0 ? 0.0 : (matched * 100.0) / total;

        sectionBanner(doc, bold, "SUMMARY", 4);
        Table t = pctTable(25, 25, 25, 25);
        summaryCard(t, bold, regular, "Total Fields Compared", String.valueOf(total), SECTION_BG, WHITE);
        summaryCard(t, bold, regular, "Matched Fields", String.valueOf(matched), GREEN_BG, GREEN_FG);
        summaryCard(t, bold, regular, "Mismatched Fields", String.valueOf(mismatched), RED_BG, RED_FG);
        summaryCard(t, bold, regular, "Not Found", String.valueOf(notFound), ORANGE_BG, ORANGE_FG);

        Table pctTable = pctTable(100);
        Cell pctCell = new Cell()
                .add(new Paragraph("Match Percentage : " + String.format("%.2f%%", pct))
                        .setFont(bold).setFontSize(11)
                        .setFontColor(pct >= 100 ? GREEN_FG : (pct >= 80 ? DARK_TEXT : RED_FG)))
                .setBackgroundColor(pct >= 100 ? GREEN_BG : (pct >= 80 ? ROW_ODD : RED_BG))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setPaddingTop(6).setPaddingBottom(6).setPaddingLeft(10);
        pctTable.addCell(pctCell);
        doc.add(t);
        gap(doc, 4);
        doc.add(pctTable);
    }

    private static void summaryCard(Table t, PdfFont bold, PdfFont regular, String label, String value,
                                    DeviceRgb bg, DeviceRgb fg) {
        Cell c = new Cell()
                .add(new Paragraph(value).setFont(bold).setFontSize(14).setFontColor(fg)
                        .setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph(label).setFont(regular).setFontSize(8).setFontColor(fg)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(bg)
                .setBorder(new com.itextpdf.layout.borders.SolidBorder(BORDER_CLR, 0.7f))
                .setPaddingTop(6).setPaddingBottom(6);
        t.addCell(c);
    }

    private static void fieldComparisonSection(Document doc, PdfFont bold, PdfFont regular,
                                               AccountComparisonReportData data) {
        Map<String, String> results = data.getComparisonResults();
        Map<String, String> oldData = data.getOldData();
        Map<String, String> newData = data.getNewData();

        sectionBanner(doc, bold, "FIELD COMPARISON", results != null ? results.size() : 0);
        Table t = pctTable(34, 30, 30, 6);
        tableHdr(t, bold, "Field Name");
        tableHdr(t, bold, "Old Server Value");
        tableHdr(t, bold, "New Server Value");
        tableHdr(t, bold, "Status");

        if (results != null) {
            int row = 0;
            for (Map.Entry<String, String> entry : results.entrySet()) {
                DeviceRgb bg = (row % 2 == 0) ? ROW_ODD : ROW_EVEN;
                String status = entry.getValue() == null ? "" : entry.getValue();
                String oldVal = oldData.getOrDefault(entry.getKey(), "");
                String newVal = newData.getOrDefault(entry.getKey(), "");

                dataCell(t, bold, entry.getKey(), bg, DARK_TEXT, TextAlignment.LEFT);
                dataCell(t, regular, oldVal, bg, DARK_TEXT, TextAlignment.LEFT);
                dataCell(t, regular, newVal, bg, DARK_TEXT, TextAlignment.LEFT);

                boolean matched = "MATCHED".equalsIgnoreCase(status);
                Cell statusCell = new Cell()
                        .add(new Paragraph(status.isEmpty() ? "MATCHED" : status)
                                .setFont(bold).setFontSize(8)
                                .setFontColor(matched ? GREEN_FG : RED_FG))
                        .setBackgroundColor(matched ? GREEN_BG : RED_BG)
                        .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                        .setPaddingTop(4).setPaddingBottom(4).setPaddingLeft(6).setPaddingRight(6)
                        .setTextAlignment(TextAlignment.CENTER);
                t.addCell(statusCell);
                row++;
            }
        }
        t.setBorder(new com.itextpdf.layout.borders.SolidBorder(BORDER_CLR, 0.7f));
        doc.add(t);
    }

    private static void mismatchSummarySection(Document doc, PdfFont bold, PdfFont regular,
                                               AccountComparisonReportData data) {
        Map<String, String> results = data.getComparisonResults();
        Map<String, String> oldData = data.getOldData();
        Map<String, String> newData = data.getNewData();

        java.util.List<String> mismatches = new java.util.ArrayList<>();
        if (results != null) {
            for (Map.Entry<String, String> entry : results.entrySet()) {
                String status = entry.getValue() == null ? "" : entry.getValue();
                if (!"MATCHED".equalsIgnoreCase(status)) {
                    mismatches.add(entry.getKey());
                }
            }
        }

        sectionBanner(doc, bold, "MISMATCH SUMMARY", mismatches.size());
        Table t = pctTable(34, 33, 33);
        tableHdr(t, bold, "Field Name");
        tableHdr(t, bold, "Old Value");
        tableHdr(t, bold, "New Value");

        if (mismatches.isEmpty()) {
            int row = 0;
            DeviceRgb bg = (row % 2 == 0) ? ROW_ODD : ROW_EVEN;
            dataCell(t, bold, "No mismatches found", bg, GREEN_FG, TextAlignment.LEFT);
            dataCell(t, regular, "All compared fields matched", bg, DARK_TEXT, TextAlignment.LEFT);
            dataCell(t, regular, "All compared fields matched", bg, DARK_TEXT, TextAlignment.LEFT);
        } else {
            int row = 0;
            for (String key : mismatches) {
                DeviceRgb bg = (row % 2 == 0) ? ROW_ODD : ROW_EVEN;
                String oldVal = oldData.getOrDefault(key, "FIELD NOT FOUND");
                String newVal = newData.getOrDefault(key, "FIELD NOT FOUND");
                dataCell(t, bold, key, bg, DARK_TEXT, TextAlignment.LEFT);
                dataCell(t, regular, oldVal, bg, RED_FG, TextAlignment.LEFT);
                dataCell(t, regular, newVal, bg, RED_FG, TextAlignment.LEFT);
                row++;
            }
        }
        t.setBorder(new com.itextpdf.layout.borders.SolidBorder(BORDER_CLR, 0.7f));
        doc.add(t);
    }

    private static void oldDataSection(Document doc, PdfFont bold, PdfFont regular, String title,
                                       Map<String, String> data, DeviceRgb headerColor) {
        dataSection(doc, bold, regular, title, data, headerColor);
    }

    private static void newDataSection(Document doc, PdfFont bold, PdfFont regular, String title,
                                       Map<String, String> data, DeviceRgb headerColor) {
        dataSection(doc, bold, regular, title, data, headerColor);
    }

    private static void dataSection(Document doc, PdfFont bold, PdfFont regular, String title,
                                    Map<String, String> data, DeviceRgb headerColor) {
        sectionBannerWithColor(doc, bold, title, data != null ? data.size() : 0, headerColor);
        Table t = pctTable(100);
        tableHdr(t, bold, "Field");
        tableHdr(t, bold, "Value");

        if (data != null && !data.isEmpty()) {
            int row = 0;
            for (Map.Entry<String, String> entry : data.entrySet()) {
                DeviceRgb bg = (row % 2 == 0) ? ROW_ODD : ROW_EVEN;
                dataCell(t, bold, entry.getKey(), bg, DARK_TEXT, TextAlignment.LEFT);
                dataCell(t, regular, entry.getValue() == null ? "" : entry.getValue(), bg, DARK_TEXT, TextAlignment.LEFT);
                row++;
            }
        } else {
            DeviceRgb bg = ROW_ODD;
            dataCell(t, bold, "No data collected", bg, DARK_TEXT, TextAlignment.LEFT);
            dataCell(t, regular, "", bg, DARK_TEXT, TextAlignment.LEFT);
        }
        t.setBorder(new com.itextpdf.layout.borders.SolidBorder(BORDER_CLR, 0.7f));
        doc.add(t);
    }

    private static void screenshotsSection(Document doc, PdfFont bold, PdfFont regular,
                                           AccountComparisonReportData data) {
        sectionBanner(doc, bold, "SCREENSHOTS", 2);

        Table t = pctTable(50, 50);

        Cell oldCell = buildScreenshotCell(bold, regular, "OLD SERVER - VIEW ACCOUNT", data.getOldScreenshotPath());
        Cell newCell = buildScreenshotCell(bold, regular, "NEW SERVER - VIEW ACCOUNT", data.getNewScreenshotPath());

        t.addCell(oldCell);
        t.addCell(newCell);
        t.setBorder(new com.itextpdf.layout.borders.SolidBorder(BORDER_CLR, 0.7f));
        doc.add(t);
    }

    private static Cell buildScreenshotCell(PdfFont bold, PdfFont regular, String caption, String path) {
        Cell cell = new Cell();
        cell.add(new Paragraph(caption).setFont(bold).setFontSize(9).setFontColor(SECTION_BG));
        if (path != null && !path.isBlank() && new File(path).exists()) {
            try {
                Image img = new Image(ImageDataFactory.create(path));
                img.setAutoScale(true);
                img.setWidth(UnitValue.createPercentValue(100));
                img.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
                cell.add(img);
            } catch (Exception e) {
                cell.add(new Paragraph("Screenshot unavailable: " + sanitize(path))
                        .setFont(regular).setFontSize(8).setFontColor(RED_FG));
            }
        } else {
            cell.add(new Paragraph("Screenshot not captured.")
                    .setFont(regular).setFontSize(8).setFontColor(RED_FG));
        }
        cell.setBorder(new com.itextpdf.layout.borders.SolidBorder(BORDER_CLR, 0.7f));
        cell.setPadding(6);
        return cell;
    }

    private static void executionTrailerSection(Document doc, PdfFont bold, PdfFont regular,
                                                AccountComparisonReportData data, String date, String time) {
        sectionBanner(doc, bold, "EXECUTION DETAILS", 4);
        Table t = pctTable(25, 25, 25, 25);
        detailCard(t, bold, regular, "Execution Date", date);
        detailCard(t, bold, regular, "Execution Time", time);
        detailCard(t, bold, regular, "Browser", data.getBrowser());
        detailCard(t, bold, regular, "Generated By", data.getGeneratedBy());
        doc.add(t);
    }

    private static void detailCard(Table t, PdfFont bold, PdfFont regular, String label, String value) {
        Cell c = new Cell()
                .add(new Paragraph(value == null ? "" : value).setFont(bold).setFontSize(9).setFontColor(DARK_TEXT)
                        .setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph(label).setFont(regular).setFontSize(7).setFontColor(DARK_TEXT)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(ROW_ODD)
                .setBorder(new com.itextpdf.layout.borders.SolidBorder(BORDER_CLR, 0.7f))
                .setPaddingTop(5).setPaddingBottom(5);
        t.addCell(c);
    }

    // ── Shared rendering helpers (same as DashboardComparisonPdfGenerator) ──────

    private static void sectionBanner(Document doc, PdfFont bold, String text, int count) {
        Table t = pctTable(85, 15);
        t.addCell(new Cell()
                .add(new Paragraph(text).setFont(bold).setFontSize(9).setFontColor(WHITE))
                .setBackgroundColor(SECTION_BG).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setPaddingTop(6).setPaddingBottom(6).setPaddingLeft(10));
        t.addCell(new Cell()
                .add(new Paragraph(count + " Items").setFont(bold).setFontSize(8).setFontColor(LIGHT_BLUE)
                        .setTextAlignment(TextAlignment.RIGHT))
                .setBackgroundColor(SECTION_BG).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setPaddingTop(6).setPaddingBottom(6).setPaddingRight(10));
        doc.add(t);
    }

    private static void sectionBannerWithColor(Document doc, PdfFont bold, String text, int count, DeviceRgb bgColor) {
        Table t = pctTable(85, 15);
        t.addCell(new Cell()
                .add(new Paragraph(text).setFont(bold).setFontSize(9).setFontColor(WHITE))
                .setBackgroundColor(bgColor).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setPaddingTop(6).setPaddingBottom(6).setPaddingLeft(10));
        t.addCell(new Cell()
                .add(new Paragraph(count + " Items").setFont(bold).setFontSize(8).setFontColor(LIGHT_BLUE)
                        .setTextAlignment(TextAlignment.RIGHT))
                .setBackgroundColor(bgColor).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setPaddingTop(6).setPaddingBottom(6).setPaddingRight(10));
        doc.add(t);
    }

    private static void tableHdr(Table t, PdfFont bold, String text) {
        t.addCell(new Cell()
                .add(new Paragraph(text).setFont(bold).setFontSize(9).setFontColor(COL_HDR_FG))
                .setBackgroundColor(COL_HDR_BG).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setPaddingTop(5).setPaddingBottom(5).setPaddingLeft(8));
    }

    private static void dataCell(Table t, PdfFont font, String value, DeviceRgb bg, DeviceRgb fg, TextAlignment align) {
        if (value == null) value = "";
        t.addCell(new Cell()
                .add(new Paragraph(value).setFont(font).setFontSize(8).setFontColor(fg))
                .setBackgroundColor(bg).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setPaddingTop(4).setPaddingBottom(4).setPaddingLeft(8).setTextAlignment(align));
    }

    private static void gap(Document doc, float height) {
        doc.add(new Paragraph(" ").setHeight(height));
    }

    private static void footer(Document doc, PdfFont regular, String reportDate) {
        Table t = pctTable(100);
        t.addCell(new Cell()
                .add(new Paragraph("Generated By : EncorePay Automation Framework   |   " + reportDate)
                        .setFont(regular).setFontSize(8).setFontColor(new DeviceRgb(84, 110, 122))
                        .setTextAlignment(TextAlignment.LEFT))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setPaddingTop(8).setPaddingBottom(4).setPaddingLeft(10));
        doc.add(t);
    }

    private static Table pctTable(float... widths) {
        return new Table(UnitValue.createPercentArray(widths)).useAllAvailableWidth();
    }

    private static String sanitize(String value) {
        if (value == null) return "";
        return value.replaceAll("[\\\\/:*?\"<>|\\r\\n]+", "_").trim();
    }
}
