import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

// ... (Import JFreeChart tetap sama)
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


// Asumsi kelas-kelas lain sudah ada
// ... (Kelas asumsi tetap sama)

public class StatisticsScreen extends JFrame {

    // ... (Semua field dan palet warna tetap sama)
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


    public StatisticsScreen(User admin) {
        this.adminUser = admin;
        java.sql.Connection conn = DBConnection.getConnection();
        this.userDAO = new UserDAO(conn);
        this.bookDAO = new BookDAO(conn);
        this.loanDAO = new LoanDAO(conn);

        setTitle("Statistik Aplikasi - LiteraSpace");
        // Kita gunakan pack() untuk ukuran yang lebih adaptif di akhir
        // setSize(1100, 768); 
        setMinimumSize(new Dimension(800, 600)); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        pack(); // Biarkan Swing yang menentukan ukuran optimal berdasarkan preferredSize
        setVisible(true);
    }

    private void initComponents() {
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

        // --- 1. Panel Kartu Statistik ---
        JPanel statsCardPanel = new JPanel(new GridLayout(1, 3, 15, 15));
        statsCardPanel.setOpaque(false);
        statsCardPanel.add(createStatCard("Total Pengguna", String.valueOf(userDAO.getTotalUsers()), successColor));
        statsCardPanel.add(createStatCard("Total Judul Buku", String.valueOf(bookDAO.getTotalBooksCount()), primaryColor));
        statsCardPanel.add(createStatCard("Total Peminjaman", String.valueOf(loanDAO.getTotalLoans()), warningColor));
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Biarkan kartu memenuhi 2 kolom
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        contentPanel.add(statsCardPanel, gbc);

        // --- 2. Panel Diagram Garis ---
        JPanel lineChartPanel = createLineChartPanel();
        gbc.gridy = 1;
        gbc.weighty = 0.5;
        contentPanel.add(lineChartPanel, gbc);

        // --- 3. Panel Chart Detail (Bar & Pie) ---
        // Kita akan letakkan berdampingan di baris ke-2
        JPanel barChartPanel = createBarChartPanel();
        gbc.gridy = 2;
        gbc.gridwidth = 1; // Kembali ke 1 kolom
        gbc.weightx = 0.5; // Bagi ruang horizontal 50%
        gbc.weighty = 0.5;
        contentPanel.add(barChartPanel, gbc);

        JPanel pieChartPanel = createPieChartPanel();
        gbc.gridx = 1;
        // gridy tetap 2
        // weightx tetap 0.5
        // weighty tetap 0.5
        contentPanel.add(pieChartPanel, gbc);


        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        JButton backButton = new JButton("Kembali ke Dashboard");
        backButton.setBackground(neutralColor);
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("Arial", Font.BOLD, 12));
        backButton.addActionListener(e -> dispose());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        bottomPanel.add(backButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }
    
    private JPanel createStatCard(String title, String value, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        // ... (sisanya sama)
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
    
    private JPanel createBarChartPanel() {
        // ... (tidak ada perubahan di method ini)
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Integer> topBooks = loanDAO.getTopBorrowedBooks(5); 
        if (topBooks.isEmpty()) return createEmptyChartPanel("Buku Terpopuler", "Data peminjaman belum tersedia.");
        for (Map.Entry<String, Integer> entry : topBooks.entrySet()) {
            dataset.addValue(entry.getValue(), "Jumlah Peminjaman", entry.getKey());
        }
        JFreeChart barChart = ChartFactory.createBarChart("Top 5 Buku Terpopuler", null, "Jumlah", dataset, PlotOrientation.VERTICAL, false, true, false);
        barChart.setBackgroundPaint(Color.WHITE);
        barChart.getTitle().setFont(new Font("Arial", Font.BOLD, 14));
        CategoryPlot plot = barChart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(220,220,220));
        plot.getDomainAxis().setTickLabelFont(new Font("Arial", Font.PLAIN, 10));
        plot.getRangeAxis().setTickLabelFont(new Font("Arial", Font.PLAIN, 10));
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, primaryColor);
        renderer.setDrawBarOutline(false);
        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setPreferredSize(new Dimension(400, 250));
        return chartPanel;
    }

    private JPanel createPieChartPanel() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        Map<String, Integer> statusCounts = loanDAO.getLoanStatusCounts();
        if (statusCounts.isEmpty()) return createEmptyChartPanel("Komposisi Status", "Data status peminjaman tidak tersedia.");
        for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }
        JFreeChart pieChart = ChartFactory.createPieChart("Komposisi Status Peminjaman", dataset, true, true, false); // Legend diaktifkan lagi
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
        
        // ✅ PERUBAHAN DI SINI: Mengubah {1} (jumlah) menjadi {2} (persen)
        PieSectionLabelGenerator labelGenerator = new StandardPieSectionLabelGenerator(
            "{0} ({2})", new DecimalFormat("0"), new DecimalFormat("0.0%")
        );
        plot.setLabelGenerator(labelGenerator);
        // plot.setSimpleLabels(true); // Dimatikan agar label tidak tumpang tindih dengan garis jika terlalu ramai

        ChartPanel chartPanel = new ChartPanel(pieChart);
        chartPanel.setPreferredSize(new Dimension(400, 250));

        return chartPanel;
    }
    
    private JPanel createLineChartPanel() {
        // ... (tidak ada perubahan di method ini)
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
        lineChart.setBackgroundPaint(Color.WHITE);
        lineChart.getTitle().setFont(new Font("Arial", Font.BOLD, 14));
        CategoryPlot plot = lineChart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(220,220,220));
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        domainAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 9));
        rangeAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 10));
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesPaint(0, primaryColor);
        renderer.setSeriesShapesVisible(0, false);
        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setPreferredSize(new Dimension(800, 300));
        return chartPanel;
    }
    
    private JPanel createEmptyChartPanel(String title, String message) {
        // ... (tidak ada perubahan di method ini)
        JPanel emptyPanel = new JPanel(new GridBagLayout());
        emptyPanel.setBackground(Color.WHITE);
        emptyPanel.setBorder(BorderFactory.createTitledBorder(title));
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        emptyPanel.add(messageLabel);
        emptyPanel.setPreferredSize(new Dimension(300, 200));
        return emptyPanel;
    }
}