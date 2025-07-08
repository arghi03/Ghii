import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

// Import JFreeChart
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

// Import Apache PDFBox
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

// Import Apache POI (Excel)
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


// Asumsi kelas-kelas lain sudah ada
// ... (Kelas asumsi tetap sama)

public class StatisticsScreen extends JFrame {

    // ... (field dan palet warna tetap sama)
    private User adminUser;
    private UserDAO userDAO;
    private BookDAO bookDAO;
    private LoanDAO loanDAO;
    private final Color backgroundColor = new Color(240, 242, 245);
    private final Color primaryColor = new Color(30, 58, 138);
    private final Color successColor = new Color(22, 163, 74);
    private final Color warningColor = new Color(245, 158, 11);
    private final Color neutralColor = new Color(107, 114, 128);
    private final Color dangerColor = new Color(220, 38, 38);

    private JFreeChart lineChart;
    private JFreeChart barChart;
    private JFreeChart pieChart;


    public StatisticsScreen(User admin) {
        // ... (constructor tetap sama)
        this.adminUser = admin;
        java.sql.Connection conn = DBConnection.getConnection();
        this.userDAO = new UserDAO(conn);
        this.bookDAO = new BookDAO(conn);
        this.loanDAO = new LoanDAO(conn);
        setTitle("Statistik Aplikasi - LiteraSpace");
        setMinimumSize(new Dimension(800, 600)); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
        pack(); 
        setVisible(true);
    }

    private void initComponents() {
        // ... (semua layout di sini tetap sama)
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(backgroundColor);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel titleLabel = new JLabel("Dashboard Statistik", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(primaryColor);
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;
        JPanel statsCardPanel = new JPanel(new GridLayout(1, 3, 15, 15));
        statsCardPanel.setOpaque(false);
        statsCardPanel.add(createStatCard("Total Pengguna", String.valueOf(userDAO.getTotalUsers()), successColor));
        statsCardPanel.add(createStatCard("Total Judul Buku", String.valueOf(bookDAO.getTotalBooksCount()), primaryColor));
        statsCardPanel.add(createStatCard("Total Peminjaman", String.valueOf(loanDAO.getTotalLoans()), warningColor));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.weighty = 0.0;
        contentPanel.add(statsCardPanel, gbc);
        this.lineChart = createLineChart();
        this.barChart = createBarChart();
        this.pieChart = createPieChart();
        ChartPanel lineChartPanel = new ChartPanel(this.lineChart);
        lineChartPanel.setPreferredSize(new Dimension(800, 300));
        gbc.gridy = 1; gbc.gridwidth = 2; gbc.weighty = 0.5;
        contentPanel.add(lineChartPanel, gbc);
        ChartPanel barChartPanel = new ChartPanel(this.barChart);
        barChartPanel.setPreferredSize(new Dimension(400, 250));
        gbc.gridy = 2; gbc.gridwidth = 1; gbc.weightx = 0.5; gbc.weighty = 0.5;
        contentPanel.add(barChartPanel, gbc);
        ChartPanel pieChartPanel = new ChartPanel(this.pieChart);
        pieChartPanel.setPreferredSize(new Dimension(400, 250));
        gbc.gridx = 1;
        contentPanel.add(pieChartPanel, gbc);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        JPanel leftBottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftBottomPanel.setOpaque(false);
        JButton refreshButton = new JButton("Refresh");
        styleBottomButton(refreshButton, neutralColor.darker(), 120, 40);
        refreshButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Data direfresh."));
        JButton exportPdfButton = new JButton("Ekspor ke PDF");
        styleBottomButton(exportPdfButton, new Color(200, 30, 30), 160, 40);
        exportPdfButton.addActionListener(e -> exportToPdf());
        JButton exportExcelButton = new JButton("Ekspor ke Excel");
        styleBottomButton(exportExcelButton, new Color(16, 128, 64), 160, 40);
        exportExcelButton.addActionListener(e -> exportToExcel());
        leftBottomPanel.add(refreshButton);
        leftBottomPanel.add(exportPdfButton);
        leftBottomPanel.add(exportExcelButton);
        JPanel rightBottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightBottomPanel.setOpaque(false);
        JButton backButton = new JButton("Kembali ke Dashboard");
        styleBottomButton(backButton, neutralColor, 200, 40);
        backButton.addActionListener(e -> dispose());
        rightBottomPanel.add(backButton);
        bottomPanel.add(leftBottomPanel, BorderLayout.WEST);
        bottomPanel.add(rightBottomPanel, BorderLayout.EAST);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }
    
    // ... (method createStatCard, styleBottomButton, createBarChart, dan createLineChart tetap sama)
    private JPanel createStatCard(String title, String value, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        card.setBackground(bgColor);
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 36));
        valueLabel.setForeground(Color.WHITE);
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        titleLabel.setForeground(Color.WHITE);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(titleLabel, BorderLayout.SOUTH);
        return card;
    }
    private void styleBottomButton(JButton button, Color bgColor, int width, int height) {
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(width, height));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
    }
    private JFreeChart createBarChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Integer> topBooks = loanDAO.getTopBorrowedBooks(5); 
        if (!topBooks.isEmpty()){
            for (Map.Entry<String, Integer> entry : topBooks.entrySet()) {
                dataset.addValue(entry.getValue(), "Jumlah Peminjaman", entry.getKey());
            }
        }
        JFreeChart barChart = ChartFactory.createBarChart("Top 5 Buku Terpopuler", null, "Jumlah", dataset, PlotOrientation.VERTICAL, false, true, false);
        barChart.setBackgroundPaint(Color.WHITE); barChart.getTitle().setFont(new Font("Arial", Font.BOLD, 14));
        CategoryPlot plot = barChart.getCategoryPlot(); plot.setBackgroundPaint(Color.WHITE); plot.setRangeGridlinePaint(new Color(220,220,220)); plot.getDomainAxis().setTickLabelFont(new Font("Arial", Font.PLAIN, 10)); plot.getRangeAxis().setTickLabelFont(new Font("Arial", Font.PLAIN, 10));
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis(); rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        BarRenderer renderer = (BarRenderer) plot.getRenderer(); renderer.setSeriesPaint(0, primaryColor); renderer.setDrawBarOutline(false);
        return barChart;
    }
    private JFreeChart createLineChart() {
        final int DAYS_TO_SHOW = 30;
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<LocalDate, Integer> dailyCounts = loanDAO.getDailyLoanCounts(DAYS_TO_SHOW);
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        for (int i = DAYS_TO_SHOW - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Integer count = dailyCounts.getOrDefault(date, 0);
            dataset.addValue(count, "Jumlah Peminjaman", date.format(formatter));
        }
        JFreeChart lineChart = ChartFactory.createLineChart("Aktivitas Peminjaman (" + DAYS_TO_SHOW + " Hari Terakhir)", null, "Jumlah", dataset, PlotOrientation.VERTICAL, false, true, false);
        lineChart.setBackgroundPaint(Color.WHITE); lineChart.getTitle().setFont(new Font("Arial", Font.BOLD, 14));
        CategoryPlot plot = lineChart.getCategoryPlot(); plot.setBackgroundPaint(Color.WHITE); plot.setRangeGridlinePaint(new Color(220,220,220));
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis(); rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        CategoryAxis domainAxis = plot.getDomainAxis(); domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45); domainAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 9)); rangeAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 10));
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer(); renderer.setSeriesStroke(0, new BasicStroke(2.0f)); renderer.setSeriesPaint(0, primaryColor); renderer.setSeriesShapesVisible(0, false);
        return lineChart;
    }

    // ✅✅✅ PERUBAHAN HANYA DI DALAM METHOD INI ✅✅✅
    private JFreeChart createPieChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        Map<String, Integer> statusCounts = loanDAO.getLoanStatusCounts();
        if (!statusCounts.isEmpty()){
             for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
                dataset.setValue(entry.getKey(), entry.getValue());
            }
        }
        JFreeChart pieChart = ChartFactory.createPieChart("Komposisi Status Peminjaman", dataset, true, true, false); 
        pieChart.setBackgroundPaint(Color.WHITE); 
        pieChart.getTitle().setFont(new Font("Arial", Font.BOLD, 14));
        PiePlot plot = (PiePlot) pieChart.getPlot(); 
        plot.setBackgroundPaint(Color.WHITE); 
        plot.setOutlineVisible(false); 
        plot.setShadowPaint(null); 
        plot.setLabelFont(new Font("Arial", Font.PLAIN, 10)); 
        plot.setLabelBackgroundPaint(null); 
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setSectionPaint("Approved", successColor); 
        plot.setSectionPaint("Pending", warningColor); 
        plot.setSectionPaint("Returned", neutralColor); 
        plot.setSectionPaint("Rejected", dangerColor);
        
        // ✅ PERUBAHAN DI SINI: Memisahkan kurung untuk jumlah dan persen
        // Format: {0} = Nama, {1} = Jumlah, {2} = Persen
        PieSectionLabelGenerator labelGenerator = new StandardPieSectionLabelGenerator(
            "{0} ({1}) ({2})", new DecimalFormat("0"), new DecimalFormat("0.0%")
        );
        plot.setLabelGenerator(labelGenerator);
        
        return pieChart;
    }
    
    // ... (semua method ekspor dan helpernya tetap sama)
    private void exportToPdf() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan Laporan PDF");
        fileChooser.setSelectedFile(new File("Laporan_Statistik_LiteraSpace.pdf"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Documents", "pdf"));
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getAbsolutePath().endsWith(".pdf")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".pdf");
            }
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                final float margin = 40;
                final float V_GAP = 15;
                final PDRectangle mediaBox = page.getMediaBox();
                final float contentWidth = mediaBox.getWidth() - 2 * margin;
                float currentY = mediaBox.getHeight() - margin;
                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    float titleWidth = (new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD).getStringWidth("Laporan Statistik LiteraSpace") / 1000.0f) * 18;
                    contentStream.beginText();
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
                    contentStream.newLineAtOffset((mediaBox.getWidth() - titleWidth) / 2, currentY);
                    contentStream.showText("Laporan Statistik LiteraSpace");
                    contentStream.endText();
                    currentY -= (18 + V_GAP);
                    float cardHeight = 50;
                    drawStatCardsToPdf(contentStream, currentY, contentWidth, margin, cardHeight);
                    currentY -= (cardHeight + V_GAP);
                    float lineChartHeight = 180;
                    drawChartToPdf(document, contentStream, this.lineChart, margin, currentY, contentWidth, lineChartHeight);
                    currentY -= (lineChartHeight + V_GAP);
                    float detailChartHeight = 150;
                    float halfWidth = (contentWidth - V_GAP) / 2;
                    drawChartToPdf(document, contentStream, this.barChart, margin, currentY, halfWidth, detailChartHeight);
                    drawChartToPdf(document, contentStream, this.pieChart, margin + halfWidth + V_GAP, currentY, halfWidth, detailChartHeight);
                }
                document.save(fileToSave);
                JOptionPane.showMessageDialog(this, "Laporan PDF berhasil disimpan di:\n" + fileToSave.getAbsolutePath(), "Ekspor Berhasil", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal menyimpan file PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private void drawStatCardsToPdf(PDPageContentStream contentStream, float y, float totalWidth, float startX, float cardHeight) throws IOException {
        float cardGap = 10;
        float cardWidth = (totalWidth - (2 * cardGap)) / 3;
        String[] titles = {"Total Pengguna", "Total Judul Buku", "Total Peminjaman"};
        String[] values = {String.valueOf(userDAO.getTotalUsers()), String.valueOf(bookDAO.getTotalBooksCount()), String.valueOf(loanDAO.getTotalLoans())};
        Color[] colors = {successColor, primaryColor, warningColor};
        for (int i=0; i < titles.length; i++) {
            float x = startX + i * (cardWidth + cardGap);
            contentStream.setNonStrokingColor(colors[i]);
            contentStream.addRect(x, y - cardHeight, cardWidth, cardHeight);
            contentStream.fill();
            float valueFontSize = 18;
            PDType1Font valueFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            float valueWidth = (valueFont.getStringWidth(values[i]) / 1000.0f) * valueFontSize;
            contentStream.beginText();
            contentStream.setFont(valueFont, valueFontSize);
            contentStream.setNonStrokingColor(Color.WHITE);
            contentStream.newLineAtOffset(x + (cardWidth - valueWidth) / 2, y - 25);
            contentStream.showText(values[i]);
            contentStream.endText();
            float titleFontSize = 9;
            PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            float titleWidth = (titleFont.getStringWidth(titles[i]) / 1000.0f) * titleFontSize;
            contentStream.beginText();
            contentStream.setFont(titleFont, titleFontSize);
            contentStream.setNonStrokingColor(Color.WHITE);
            contentStream.newLineAtOffset(x + (cardWidth - titleWidth) / 2, y - 40);
            contentStream.showText(titles[i]);
            contentStream.endText();
        }
    }
    private void drawChartToPdf(PDDocument document, PDPageContentStream contentStream, JFreeChart chart, float x, float y, float width, float height) throws IOException {
        BufferedImage chartImage = chart.createBufferedImage((int)width, (int)height);
        PDImageXObject pdImage = LosslessFactory.createFromImage(document, chartImage);
        contentStream.drawImage(pdImage, x, y - height, width, height);
    }
    private void exportToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan Laporan Excel");
        fileChooser.setSelectedFile(new File("Laporan_Statistik_LiteraSpace.xlsx"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx"));
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getAbsolutePath().endsWith(".xlsx")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".xlsx");
            }
            try (Workbook workbook = new XSSFWorkbook()) {
                CellStyle totalStyle = workbook.createCellStyle();
                XSSFFont totalFont = ((XSSFWorkbook) workbook).createFont();
                totalFont.setBold(true);
                totalStyle.setFont(totalFont);
                Sheet sheetTopBuku = workbook.createSheet("Top 5 Buku Terpopuler");
                createHeaderRow(sheetTopBuku, "Judul Buku", "Jumlah Peminjaman");
                Map<String, Integer> topBooks = loanDAO.getTopBorrowedBooks(5);
                int rowNum = 1;
                int totalPeminjamanBuku = 0;
                for (Map.Entry<String, Integer> entry : topBooks.entrySet()) {
                    Row row = sheetTopBuku.createRow(rowNum++);
                    row.createCell(0).setCellValue(entry.getKey());
                    row.createCell(1).setCellValue(entry.getValue());
                    totalPeminjamanBuku += entry.getValue();
                }
                sheetTopBuku.createRow(rowNum++);
                Row totalBukuRow = sheetTopBuku.createRow(rowNum);
                Cell totalBukuLabel = totalBukuRow.createCell(0);
                totalBukuLabel.setCellValue("Total Peminjaman (Top 5)");
                totalBukuLabel.setCellStyle(totalStyle);
                Cell totalBukuValue = totalBukuRow.createCell(1);
                totalBukuValue.setCellValue(totalPeminjamanBuku);
                totalBukuValue.setCellStyle(totalStyle);
                sheetTopBuku.autoSizeColumn(0);
                sheetTopBuku.autoSizeColumn(1);
                Sheet sheetStatus = workbook.createSheet("Komposisi Status Peminjaman");
                createHeaderRow(sheetStatus, "Status", "Jumlah");
                Map<String, Integer> statusCounts = loanDAO.getLoanStatusCounts();
                rowNum = 1;
                int totalStatus = 0;
                for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
                    Row row = sheetStatus.createRow(rowNum++);
                    row.createCell(0).setCellValue(entry.getKey());
                    row.createCell(1).setCellValue(entry.getValue());
                    totalStatus += entry.getValue();
                }
                sheetStatus.createRow(rowNum++);
                Row totalStatusRow = sheetStatus.createRow(rowNum);
                Cell totalStatusLabel = totalStatusRow.createCell(0);
                totalStatusLabel.setCellValue("Total Keseluruhan");
                totalStatusLabel.setCellStyle(totalStyle);
                Cell totalStatusValue = totalStatusRow.createCell(1);
                totalStatusValue.setCellValue(totalStatus);
                totalStatusValue.setCellStyle(totalStyle);
                sheetStatus.autoSizeColumn(0);
                sheetStatus.autoSizeColumn(1);
                Sheet sheetTren = workbook.createSheet("Tren Peminjaman Harian");
                createHeaderRow(sheetTren, "Tanggal", "Jumlah Peminjaman");
                Map<LocalDate, Integer> dailyCounts = loanDAO.getDailyLoanCounts(30);
                rowNum = 1;
                int totalPeminjaman30Hari = 0;
                DateTimeFormatter excelDateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
                for (Map.Entry<LocalDate, Integer> entry : dailyCounts.entrySet()) {
                    Row row = sheetTren.createRow(rowNum++);
                    row.createCell(0).setCellValue(entry.getKey().format(excelDateFormatter));
                    row.createCell(1).setCellValue(entry.getValue());
                    totalPeminjaman30Hari += entry.getValue();
                }
                sheetTren.createRow(rowNum++);
                Row totalTrenRow = sheetTren.createRow(rowNum);
                Cell totalTrenLabel = totalTrenRow.createCell(0);
                totalTrenLabel.setCellValue("Total Peminjaman (30 Hari)");
                totalTrenLabel.setCellStyle(totalStyle);
                Cell totalTrenValue = totalTrenRow.createCell(1);
                totalTrenValue.setCellValue(totalPeminjaman30Hari);
                totalTrenValue.setCellStyle(totalStyle);
                sheetTren.autoSizeColumn(0);
                sheetTren.autoSizeColumn(1);
                try (FileOutputStream fileOut = new FileOutputStream(fileToSave)) {
                    workbook.write(fileOut);
                }
                JOptionPane.showMessageDialog(this, "Laporan Excel berhasil disimpan di:\n" + fileToSave.getAbsolutePath(), "Ekspor Berhasil", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal menyimpan file Excel: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private void createHeaderRow(Sheet sheet, String... headers) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        XSSFFont font = ((XSSFWorkbook) sheet.getWorkbook()).createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }
}