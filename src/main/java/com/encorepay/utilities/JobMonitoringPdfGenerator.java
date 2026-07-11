package com.encorepay.utilities;

import com.encorepay.config.ClientConfig;
import com.encorepay.config.ConfigLoader;
import com.encorepay.models.JobStatus;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class JobMonitoringPdfGenerator {

    private static final String POST_RECEIPTS_JOB    = "Post Receipts Job";
    private static final String COLLECTION_ITEMS_JOB = "Encore Download Collection Items Job";
    private static final String UPCOMING_DEMANDS_JOB = "Encore Upcoming Demands Job";

    private static final DeviceRgb TITLE_BG   = new DeviceRgb( 13,  43,  85);
    private static final DeviceRgb SECTION_BG = new DeviceRgb( 20,  66, 110);
    private static final DeviceRgb COL_HDR_BG = new DeviceRgb( 22,  61, 107);
    private static final DeviceRgb COL_HDR_FG = new DeviceRgb(187, 222, 251);
    private static final DeviceRgb ROW_ODD    = new DeviceRgb(234, 242, 251);
    private static final DeviceRgb ROW_EVEN   = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb BORDER_CLR = new DeviceRgb(176, 190, 197);
    private static final DeviceRgb GREEN_FG   = new DeviceRgb( 27,  94,  32);
    private static final DeviceRgb GREEN_BG   = new DeviceRgb(200, 230, 201);
    private static final DeviceRgb ORANGE_FG  = new DeviceRgb(191,  54,  12);
    private static final DeviceRgb ORANGE_BG  = new DeviceRgb(255, 204, 188);
    private static final DeviceRgb RED_FG     = new DeviceRgb(183,  28,  28);
    private static final DeviceRgb RED_BG     = new DeviceRgb(255, 205, 210);
    private static final DeviceRgb GRAY_FG    = new DeviceRgb( 55,  71,  79);
    private static final DeviceRgb GRAY_BG    = new DeviceRgb(236, 239, 241);
    private static final DeviceRgb WHITE      = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb DARK_TEXT  = new DeviceRgb( 13,  27,  42);
    private static final DeviceRgb FOOTER_CLR = new DeviceRgb( 84, 110, 122);
    private static final DeviceRgb LIGHT_BLUE = new DeviceRgb(144, 202, 249);

    private static final DateTimeFormatter DATE_DISPLAY   = DateTimeFormatter.ofPattern("d MMM yyyy");
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private JobMonitoringPdfGenerator() {}

    public static String generatePdfReport(List<JobStatus> statuses) {
        try {
            String dir = "reports";
            Files.createDirectories(Paths.get(dir));
            String timestamp = LocalDateTime.now().format(FILE_TIMESTAMP);
            String path = dir + "/Daily_Job_Status_Report_" + timestamp + ".pdf";
            Files.write(Paths.get(path), generate(statuses));
            return path;
        } catch (IOException e) {
            throw new RuntimeException("Failed to write Daily Job Status PDF", e);
        }
    }

    public static byte[] generate(List<JobStatus> statuses) throws IOException {

        List<ClientConfig> clients = ConfigLoader.getInstance().getClients();
        
        // Handle dynamic mode - derive client names directly from captured statuses when not configured.
        if (clients.isEmpty() && !statuses.isEmpty()) {
            clients = createClientConfigsFromStatuses(statuses);
            if (clients.isEmpty()) {
                String fallbackClient = extractFirstClientName(statuses);
                if (!fallbackClient.isEmpty()) {
                    clients = createSingleClientConfig(fallbackClient);
                }
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (PdfWriter   writer = new PdfWriter(out);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document    doc    = new Document(pdfDoc, PageSize.A4.rotate())) {

            doc.setMargins(20, 20, 20, 20);
            PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            String reportDate = LocalDate.now().format(DATE_DISPLAY);

            titleBanner(doc, bold, regular, reportDate);
            gap(doc, 6);

            sectionBanner(doc, bold, "1.  POST RECEIPT JOB", clients.size());
            postReceiptTable(doc, bold, regular, clients, statuses);
            gap(doc, 8);

            Table jobs23 = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .useAllAvailableWidth();

            Cell cell2 = new Cell().setBorder(Border.NO_BORDER).setPaddingRight(4);
            buildClientStatusSection(cell2, bold, regular,
                "2.  ENCORE DOWNLOAD COLLECTION ITEMS JOB", clients, statuses, COLLECTION_ITEMS_JOB);

            Cell cell3 = new Cell().setBorder(Border.NO_BORDER).setPaddingLeft(4);
            buildClientStatusSection(cell3, bold, regular,
                "3.  ENCORE UPCOMING DEMANDS JOB", clients, statuses, UPCOMING_DEMANDS_JOB);

            jobs23.addCell(cell2);
            jobs23.addCell(cell3);
            doc.add(jobs23);
            gap(doc, 8);

            footer(doc, regular, reportDate);
        }

        return out.toByteArray();
    }

    private static void titleBanner(Document doc, PdfFont bold, PdfFont regular, String reportDate) {
        Table t = pctTable(100);
        t.addCell(new Cell()
                .add(new Paragraph("DAILY JOB STATUS REPORT")
                        .setFont(bold).setFontSize(16).setFontColor(WHITE)
                        .setTextAlignment(TextAlignment.LEFT))
                .add(new Paragraph(reportDate + "   |   Production Environment   |   EncorePay Platform")
                        .setFont(regular).setFontSize(9).setFontColor(LIGHT_BLUE)
                        .setTextAlignment(TextAlignment.LEFT))
                .setBackgroundColor(TITLE_BG).setBorder(Border.NO_BORDER)
                .setPaddingTop(12).setPaddingBottom(12).setPaddingLeft(14).setPaddingRight(14));
        doc.add(t);
    }

    private static void sectionBanner(Document doc, PdfFont bold, String text, int clientCount) {
        Table t = pctTable(85, 15);
        t.addCell(new Cell()
                .add(new Paragraph(text)
                        .setFont(bold).setFontSize(9).setFontColor(WHITE))
                .setBackgroundColor(SECTION_BG).setBorder(Border.NO_BORDER)
                .setPaddingTop(6).setPaddingBottom(6).setPaddingLeft(10));
        t.addCell(new Cell()
                .add(new Paragraph(clientCount + " Clients")
                        .setFont(bold).setFontSize(8).setFontColor(LIGHT_BLUE)
                        .setTextAlignment(TextAlignment.RIGHT))
                .setBackgroundColor(SECTION_BG).setBorder(Border.NO_BORDER)
                .setPaddingTop(6).setPaddingBottom(6).setPaddingRight(10));
        doc.add(t);
    }

    private static void sectionBannerInCell(Cell container, PdfFont bold, String text, int clientCount) {
        Table t = pctTable(80, 20);
        t.addCell(new Cell()
                .add(new Paragraph(text)
                        .setFont(bold).setFontSize(8.5f).setFontColor(WHITE))
                .setBackgroundColor(SECTION_BG).setBorder(Border.NO_BORDER)
                .setPaddingTop(6).setPaddingBottom(6).setPaddingLeft(8));
        t.addCell(new Cell()
                .add(new Paragraph(clientCount + " Clients")
                        .setFont(bold).setFontSize(7.5f).setFontColor(LIGHT_BLUE)
                        .setTextAlignment(TextAlignment.RIGHT))
                .setBackgroundColor(SECTION_BG).setBorder(Border.NO_BORDER)
                .setPaddingTop(6).setPaddingBottom(6).setPaddingRight(8));
        container.add(t);
    }

    private static void postReceiptTable(Document doc, PdfFont bold, PdfFont regular,
                                          List<ClientConfig> clients, List<JobStatus> statuses) {
        Table t = pctTable(20, 32, 12, 14, 22);

        tableHdr(t, bold, "Client");
        tableHdr(t, bold, "Status");
        tableHdr(t, bold, "Failed\nCount");
        tableHdr(t, bold, "Pending\nto Be Posted");
        tableHdr(t, bold, "Date & Time");

        for (int i = 0; i < clients.size(); i++) {
            ClientConfig cc = clients.get(i);
            JobStatus    js = findForClient(statuses, cc.getName(), POST_RECEIPTS_JOB);
            DeviceRgb    bg = (i % 2 == 0) ? ROW_ODD : ROW_EVEN;

            dataCell(t, bold, cc.getName(), bg, DARK_TEXT, TextAlignment.LEFT);
            postReceiptStatusCell(t, bold, js);
            dataCell(t, regular, count(js == null ? null : js.getFailedCount()), bg, DARK_TEXT, TextAlignment.CENTER);
            pendingCell(t, bold, regular, js, bg);
            dataCell(t, regular, dateTimeVal(js), bg, DARK_TEXT, TextAlignment.LEFT);
        }

        t.setBorder(new SolidBorder(BORDER_CLR, 0.7f));
        doc.add(t);
    }

    private static void buildClientStatusSection(Cell container, PdfFont bold, PdfFont regular,
                                                   String title, List<ClientConfig> clients,
                                                   List<JobStatus> statuses, String jobName) {
        sectionBannerInCell(container, bold, title, clients.size());

        Table t = pctTable(35, 30, 35);
        tableHdrSmall(t, bold, "Client");
        tableHdrSmall(t, bold, "Status");
        tableHdrSmall(t, bold, "Date & Time");

        for (int i = 0; i < clients.size(); i++) {
            ClientConfig cc = clients.get(i);
            JobStatus    js = findForClient(statuses, cc.getName(), jobName);
            DeviceRgb    bg = (i % 2 == 0) ? ROW_ODD : ROW_EVEN;

            dataCellSmall(t, bold, cc.getName(), bg, DARK_TEXT, TextAlignment.LEFT);
            statusBadgeCellSmall(t, bold, js, bg);
            dataCellSmall(t, regular, dateTimeVal(js), bg, DARK_TEXT, TextAlignment.LEFT);
        }

        t.setBorder(new SolidBorder(BORDER_CLR, 0.7f));
        container.add(t);
    }

    private static void statusBadgeCell(Table t, PdfFont bold, JobStatus js, DeviceRgb rowBg) {
        String    label = statusLabelWithReason(js);
        DeviceRgb fg    = statusFg(label);
        DeviceRgb bg    = statusBg(label);

        Table badge = new Table(UnitValue.createPercentArray(new float[]{100})).useAllAvailableWidth();
        badge.addCell(new Cell()
                .add(new Paragraph(label).setFont(bold).setFontSize(7.5f)
                        .setFontColor(fg).setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(bg)
                .setBorder(Border.NO_BORDER)
                .setPaddingTop(3).setPaddingBottom(3));

        t.addCell(new Cell().add(badge)
                .setBackgroundColor(rowBg)
                .setBorder(new SolidBorder(BORDER_CLR, 0.4f))
                .setPaddingTop(4).setPaddingBottom(4).setPaddingLeft(4).setPaddingRight(4));
    }

    private static void statusBadgeCellSmall(Table t, PdfFont bold, JobStatus js, DeviceRgb rowBg) {
        String    label = statusLabelWithReason(js);
        DeviceRgb fg    = statusFg(label);
        DeviceRgb bg    = statusBg(label);

        Table badge = new Table(UnitValue.createPercentArray(new float[]{100})).useAllAvailableWidth();
        badge.addCell(new Cell()
                .add(new Paragraph(label).setFont(bold).setFontSize(7f)
                        .setFontColor(fg).setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(bg)
                .setBorder(Border.NO_BORDER)
                .setPaddingTop(2).setPaddingBottom(2));

        t.addCell(new Cell().add(badge)
                .setBackgroundColor(rowBg)
                .setBorder(new SolidBorder(BORDER_CLR, 0.4f))
                .setPaddingTop(3).setPaddingBottom(3).setPaddingLeft(3).setPaddingRight(3));
    }

    private static void postReceiptStatusCell(Table t, PdfFont bold, JobStatus js) {
        String label = statusLabelWithReason(js);
        DeviceRgb fg = statusFg(label);
        DeviceRgb bg = statusBg(label);

        Cell cell = new Cell()
                .setBackgroundColor(bg)
                .setBorder(new SolidBorder(BORDER_CLR, 0.4f))
                .setPaddingTop(4).setPaddingBottom(4).setPaddingLeft(6).setPaddingRight(6);

        cell.add(new Paragraph(label)
                .setFont(bold).setFontSize(7.4f).setFontColor(fg)
                .setTextAlignment(TextAlignment.CENTER)
                .setMargin(0));

        t.addCell(cell);
    }

   private static void pendingCell(Table t, PdfFont bold, PdfFont regular, JobStatus js, DeviceRgb bg) {
       String val = count(js == null ? null : js.getPendingCount());
       boolean high = js != null && js.getPendingCount() > 0;
       PdfFont font = high ? bold : regular;
       DeviceRgb fg = high ? ORANGE_FG : DARK_TEXT;
       dataCell(t, font, val, bg, fg, TextAlignment.CENTER);
   }

    private static void tableHdr(Table t, PdfFont bold, String text) {
        t.addCell(new Cell()
                .add(new Paragraph(text).setFont(bold).setFontSize(8f)
                        .setFontColor(COL_HDR_FG).setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(COL_HDR_BG)
                .setBorder(new SolidBorder(BORDER_CLR, 0.5f))
                .setPadding(7));
    }

    private static void tableHdrSmall(Table t, PdfFont bold, String text) {
        t.addCell(new Cell()
                .add(new Paragraph(text).setFont(bold).setFontSize(7.5f)
                        .setFontColor(COL_HDR_FG).setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(COL_HDR_BG)
                .setBorder(new SolidBorder(BORDER_CLR, 0.5f))
                .setPadding(6));
    }

    private static void dataCell(Table t, PdfFont font, String text,
                                  DeviceRgb bg, DeviceRgb fg, TextAlignment align) {
        t.addCell(new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(8f)
                        .setFontColor(fg).setTextAlignment(align))
                .setBackgroundColor(bg)
                .setBorder(new SolidBorder(BORDER_CLR, 0.4f))
                .setPaddingTop(5).setPaddingBottom(5)
                .setPaddingLeft(6).setPaddingRight(6));
    }

    private static void dataCellSmall(Table t, PdfFont font, String text,
                                       DeviceRgb bg, DeviceRgb fg, TextAlignment align) {
        t.addCell(new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(7.5f)
                        .setFontColor(fg).setTextAlignment(align))
                .setBackgroundColor(bg)
                .setBorder(new SolidBorder(BORDER_CLR, 0.4f))
                .setPaddingTop(4).setPaddingBottom(4)
                .setPaddingLeft(5).setPaddingRight(5));
    }

    private static void footer(Document doc, PdfFont regular, String reportDate) {
        doc.add(new Paragraph(
                "EncorePay Platform   |   Daily Job Monitoring Report   |   Report Date: " + reportDate)
                .setFont(regular).setFontSize(7.5f).setFontColor(FOOTER_CLR)
                .setTextAlignment(TextAlignment.LEFT)
                .setBorderTop(new SolidBorder(BORDER_CLR, 0.5f)).setPaddingTop(5).setMarginTop(4));
    }

    private static Table pctTable(float... widths) {
        return new Table(UnitValue.createPercentArray(widths)).useAllAvailableWidth();
    }

    private static void gap(Document doc, float pts) {
        doc.add(new Paragraph("").setFontSize(pts).setMargin(0));
    }

    private static JobStatus findForClient(List<JobStatus> statuses, String clientName, String jobName) {
        if (statuses == null || clientName == null) return null;
        return statuses.stream()
                .filter(s -> sameJob(s.getJobName(), jobName))
                .filter(s -> clientMatches(s, clientName))
                .findFirst()
                .orElse(null);
    }

    private static boolean clientMatches(JobStatus s, String clientName) {
        String target = clientName.toLowerCase(Locale.ROOT).trim();
        String byName = s.getClientName()    == null ? "" : s.getClientName().toLowerCase(Locale.ROOT).trim();
        String byAcct = s.getAccountNumber() == null ? "" : s.getAccountNumber().toLowerCase(Locale.ROOT).trim();
        return byName.contains(target) || target.contains(byName)
            || byAcct.contains(target) || target.contains(byAcct);
    }

    private static String statusLabel(JobStatus js) {
        if (js == null) return "N/A";
        // Use the exact raw text from the UI — reportStatus was set directly from
        // what the View modal showed, so return it as-is (trim only).
        String raw = firstNonBlank(js.getReportStatus(), js.getStatus(), js.getJobStatus());
        return isBlank(raw) ? "N/A" : raw.trim();
    }

    private static String statusLabel(String rawStatus) {
        // Used only for badge-color lookup — keep the raw value, do not convert.
        return isBlank(rawStatus) ? "N/A" : rawStatus.trim();
    }

    private static DeviceRgb statusFg(String label) {
        String s = label == null ? "" : label.toLowerCase(Locale.ROOT);
        if (s.contains("successful") || s.contains("completed")) return GREEN_FG;
        if (s.contains("failed"))                                 return RED_FG;
        if (s.contains("pending"))                                return ORANGE_FG;
        if (s.contains("not started") || s.contains("running"))   return ORANGE_FG;
        return GRAY_FG;
    }

    private static DeviceRgb statusBg(String label) {
        String s = label == null ? "" : label.toLowerCase(Locale.ROOT);
        if (s.contains("successful") || s.contains("completed")) return GREEN_BG;
        if (s.contains("failed"))                                 return RED_BG;
        if (s.contains("pending"))                                return ORANGE_BG;
        if (s.contains("not started") || s.contains("running"))   return ORANGE_BG;
        return GRAY_BG;
    }

    private static String timeVal(String v)  { return isBlank(v) ? "—" : v.trim(); }
    private static String dateTimeVal(JobStatus status) {
        if (status == null) {
            return "—";
        }
        String endDate = timeVal(status.getEndDate());
        String endTime = timeVal(status.getEndTime());
        if ("—".equals(endDate) && "—".equals(endTime)) {
            return "—";
        }
        if ("—".equals(endTime)) {
            return endDate;
        }
        if ("—".equals(endDate)) {
            return endTime;
        }
        if (!"—".equals(endTime) && endDate.toLowerCase(Locale.ROOT).contains(endTime.toLowerCase(Locale.ROOT))) {
            return endDate;
        }
        return endDate + " " + endTime;
    }

    private static String failureReason(JobStatus status) {
        if (status == null) {
            return "";
        }
        String reason = firstNonBlank(status.getFailureReason(), status.getErrorMessage());
        if (reason.isBlank()) {
            return "";
        }
        return reason.replaceAll("\\s+", " ").trim();
    }

    private static String statusLabelWithReason(JobStatus status) {
        String label = statusLabel(status);
        String reason = failureReason(status);
        if (!label.toLowerCase(Locale.ROOT).contains("failed") || reason.isBlank()) {
            return label;
        }
        return label + " (" + compactReason(reason) + ")";
    }

    private static String compactReason(String reason) {
        String value = reason == null ? "" : reason.trim().replaceAll("\\s+", " ");
        if (value.length() <= 70) {
            return value;
        }
        return value.substring(0, 67) + "...";
    }

    private static String count(Object v)    { return v == null  ? "0" : String.valueOf(v); }
    private static String val(String v)      { return isBlank(v) ? "-" : v.trim(); }
    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return "";
    }
    private static boolean isBlank(String v) { return v == null || v.trim().isEmpty(); }

    private static boolean sameJob(String actual, String expected) {
        String l = normalize(actual), r = normalize(expected);
        return !l.isBlank() && (l.equals(r) || l.contains(r) || r.contains(l));
    }

    private static String normalize(String v) {
        return val(v).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
    }
    
    private static String extractFirstClientName(List<JobStatus> statuses) {
        return statuses.stream()
            .map(JobStatus::getClientName)
            .filter(name -> name != null && !name.trim().isEmpty())
            .filter(name -> !looksLikeJobName(name))
            .findFirst().orElse("");
    }
    
    private static List<ClientConfig> createSingleClientConfig(String clientName) {
        List<ClientConfig> configs = new java.util.ArrayList<>();
        configs.add(new ClientConfig(1, clientName, ConfigLoader.getInstance().getURL()));
        return configs;
    }

    private static List<ClientConfig> createClientConfigsFromStatuses(List<JobStatus> statuses) {
        Set<String> uniqueClients = new LinkedHashSet<>();
        for (JobStatus status : statuses) {
            String clientName = status.getClientName();
            if (clientName != null && !clientName.trim().isEmpty() && !looksLikeJobName(clientName)) {
                uniqueClients.add(clientName.trim());
            }
        }

        List<ClientConfig> configs = new java.util.ArrayList<>();
        int index = 1;
        for (String clientName : uniqueClients) {
            configs.add(new ClientConfig(index++, clientName, ConfigLoader.getInstance().getURL()));
        }
        return configs;
    }

    private static boolean looksLikeJobName(String value) {
        String normalized = normalize(value);
        return normalized.equals(normalize(POST_RECEIPTS_JOB))
            || normalized.equals(normalize(COLLECTION_ITEMS_JOB))
            || normalized.equals(normalize(UPCOMING_DEMANDS_JOB))
            || normalized.contains("job");
    }
}