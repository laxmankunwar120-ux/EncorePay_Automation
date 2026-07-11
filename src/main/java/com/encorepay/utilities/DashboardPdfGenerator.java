package com.encorepay.utilities;

import com.encorepay.pages.DashboardPage;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public final class DashboardPdfGenerator {

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
    private static final DeviceRgb WHITE      = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb DARK_TEXT  = new DeviceRgb(13, 27, 42);

    private static final DateTimeFormatter DATE_DISPLAY   = DateTimeFormatter.ofPattern("d MMM yyyy");
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private DashboardPdfGenerator() {}

    public static String generateDashboardReport(Map<String, String> sanityResults, Map<String, String> timingData) {
        try {
            String dir = "reports";
            Files.createDirectories(Paths.get(dir));
            String timestamp = LocalDateTime.now().format(FILE_TIMESTAMP);
            String path = dir + "/Dashboard_Sanity_Report_" + timestamp + ".pdf";
            Files.write(Paths.get(path), generate(sanityResults, timingData));
            return path;
        } catch (IOException e) {
            throw new RuntimeException("Failed to write Dashboard PDF report", e);
        }
    }

    public static byte[] generate(Map<String, String> sanityResults, Map<String, String> timingData) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (PdfWriter   writer = new PdfWriter(out);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document    doc    = new Document(pdfDoc, PageSize.A4)) {

            doc.setMargins(20, 20, 20, 20);
            PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            String reportDate = LocalDate.now().format(DATE_DISPLAY);

            titleBanner(doc, bold, reportDate);
            gap(doc, 8);
            performanceSection(doc, bold, regular, timingData);
            gap(doc, 8);
            sanityCheckSection(doc, bold, regular, sanityResults);
            gap(doc, 8);
            footer(doc, regular, reportDate);
        }

        return out.toByteArray();
    }

    private static void titleBanner(Document doc, PdfFont bold, String reportDate) {
        Table t = pctTable(100);
        t.addCell(new Cell()
                .add(new Paragraph("DASHBOARD SANITY & PERFORMANCE REPORT")
                        .setFont(bold).setFontSize(16).setFontColor(WHITE)
                        .setTextAlignment(TextAlignment.LEFT))
                .add(new Paragraph(reportDate + "   |   EncorePay Platform")
                        .setFont(bold).setFontSize(9).setFontColor(new DeviceRgb(144, 202, 249))
                        .setTextAlignment(TextAlignment.LEFT))
                .setBackgroundColor(TITLE_BG).setBorder(Border.NO_BORDER)
                .setPaddingTop(12).setPaddingBottom(12).setPaddingLeft(14).setPaddingRight(14));
        doc.add(t);
    }

    private static void performanceSection(Document doc, PdfFont bold, PdfFont regular, Map<String, String> timingData) {
        sectionBanner(doc, bold, "PERFORMANCE OBSERVATION", timingData.size());
        
        Table t = pctTable(50, 50);
        tableHdr(t, bold, "Metric");
        tableHdr(t, bold, "Value");

        if (timingData != null) {
            for (Map.Entry<String, String> entry : timingData.entrySet()) {
                dataCell(t, bold, entry.getKey(), ROW_ODD, DARK_TEXT, TextAlignment.LEFT);
                dataCell(t, regular, entry.getValue(), ROW_ODD, DARK_TEXT, TextAlignment.RIGHT);
            }
        }

        t.setBorder(new SolidBorder(BORDER_CLR, 0.7f));
        doc.add(t);
    }

    private static void sanityCheckSection(Document doc, PdfFont bold, PdfFont regular, Map<String, String> sanityResults) {
        sectionBanner(doc, bold, "DASHBOARD SANITY CHECK", sanityResults != null ? sanityResults.size() : 0);
        
        Table t = pctTable(70, 30);
        tableHdr(t, bold, "Section / Card");
        tableHdr(t, bold, "Status");

        if (sanityResults != null) {
            int row = 0;
            for (Map.Entry<String, String> entry : sanityResults.entrySet()) {
                DeviceRgb bg = (row % 2 == 0) ? ROW_ODD : ROW_EVEN;
                String value = entry.getValue() != null ? entry.getValue() : "";
                boolean matched = value.equalsIgnoreCase("Matched") || value.equalsIgnoreCase("Present") || !value.isBlank();
                
                dataCell(t, bold, entry.getKey(), bg, DARK_TEXT, TextAlignment.LEFT);
                
                Cell statusCell = new Cell()
                    .add(new Paragraph(matched ? "Matched" : "Missing")
                        .setFont(bold).setFontSize(9)
                        .setFontColor(matched ? GREEN_FG : RED_FG))
                    .setBackgroundColor(matched ? GREEN_BG : RED_BG)
                    .setBorder(Border.NO_BORDER)
                    .setPaddingTop(4).setPaddingBottom(4).setPaddingLeft(6).setPaddingRight(6);
                t.addCell(statusCell);
                row++;
            }
        }

        t.setBorder(new SolidBorder(BORDER_CLR, 0.7f));
        doc.add(t);
    }

    private static void sectionBanner(Document doc, PdfFont bold, String text, int count) {
        Table t = pctTable(85, 15);
        t.addCell(new Cell()
                .add(new Paragraph(text)
                        .setFont(bold).setFontSize(9).setFontColor(WHITE))
                .setBackgroundColor(SECTION_BG).setBorder(Border.NO_BORDER)
                .setPaddingTop(6).setPaddingBottom(6).setPaddingLeft(10));
        t.addCell(new Cell()
                .add(new Paragraph(count + " Items")
                        .setFont(bold).setFontSize(8).setFontColor(new DeviceRgb(144, 202, 249))
                        .setTextAlignment(TextAlignment.RIGHT))
                .setBackgroundColor(SECTION_BG).setBorder(Border.NO_BORDER)
                .setPaddingTop(6).setPaddingBottom(6).setPaddingRight(10));
        doc.add(t);
    }

    private static void tableHdr(Table t, PdfFont bold, String text) {
        t.addCell(new Cell()
                .add(new Paragraph(text).setFont(bold).setFontSize(9).setFontColor(COL_HDR_FG))
                .setBackgroundColor(COL_HDR_BG).setBorder(Border.NO_BORDER)
                .setPaddingTop(5).setPaddingBottom(5).setPaddingLeft(8));
    }

    private static void dataCell(Table t, PdfFont font, String value, DeviceRgb bg, DeviceRgb fg, TextAlignment align) {
        if (value == null) value = "";
        t.addCell(new Cell()
                .add(new Paragraph(value).setFont(font).setFontSize(9).setFontColor(fg))
                .setBackgroundColor(bg).setBorder(Border.NO_BORDER)
                .setPaddingTop(4).setPaddingBottom(4).setPaddingLeft(8));
    }

    private static void gap(Document doc, float height) {
        doc.add(new Paragraph(" ").setHeight(height));
    }

    private static void footer(Document doc, PdfFont regular, String reportDate) {
        Table t = pctTable(100);
        t.addCell(new Cell()
                .add(new Paragraph("Generated By : EncorePay Automation   |   " + reportDate)
                        .setFont(regular).setFontSize(8).setFontColor(new DeviceRgb(84, 110, 122))
                        .setTextAlignment(TextAlignment.LEFT))
                .setBorder(Border.NO_BORDER)
                .setPaddingTop(8).setPaddingBottom(4).setPaddingLeft(10));
        doc.add(t);
    }

    private static Table pctTable(float... widths) {
        return new Table(UnitValue.createPercentArray(widths)).useAllAvailableWidth();
    }
}
