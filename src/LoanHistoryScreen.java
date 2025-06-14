import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LoanHistoryScreen extends JFrame {
    private User currentUser;
    private LoanDAO loanDAO;
    private UserDAO userDAO;
    private DefaultTableModel tableModel;
    private JTable loanTable;

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private Color primaryColor = new Color(30, 58, 138); 
    private Color headerColor = new Color(224, 231, 255); 
    private Color backgroundColor = Color.WHITE;
    private Color neutralColor = new Color(107, 114, 128);

    public LoanHistoryScreen(User user) {
        this.currentUser = user;
        this.loanDAO = new LoanDAO(DBConnection.getConnection());
        this.userDAO = new UserDAO(DBConnection.getConnection()); 

        setTitle("Riwayat Peminjaman - " + currentUser.getNama());
        setSize(950, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        loadLoanHistory();
        
        setVisible(true);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(backgroundColor);

        JLabel titleLabel = new JLabel("Riwayat Peminjaman Anda", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(primaryColor);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        
        String[] columns = {"ID Pinjam", "Judul Buku", "Tgl Pinjam", "Tgl Disetujui", "Tgl Kembali", "Status", "Disetujui Oleh"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                
                return false;
            }
        };
        loanTable = new JTable(tableModel);
        loanTable.setFillsViewportHeight(true);
        loanTable.setRowHeight(30); 
        loanTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        loanTable.getTableHeader().setBackground(headerColor);
        loanTable.getTableHeader().setOpaque(false);

        
        TableColumnModel columnModel = loanTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(60);  // ID
        columnModel.getColumn(1).setPreferredWidth(200); // Judul
        columnModel.getColumn(2).setPreferredWidth(120); // Tgl Pinjam
        columnModel.getColumn(3).setPreferredWidth(120); // Tgl Disetujui
        columnModel.getColumn(4).setPreferredWidth(120); // Tgl Kembali
        columnModel.getColumn(5).setPreferredWidth(80);  // Status
        columnModel.getColumn(6).setPreferredWidth(120); // Disetujui Oleh

        

        JScrollPane scrollPane = new JScrollPane(loanTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JButton backButton = new JButton("Kembali ke Dashboard");
        backButton.setBackground(neutralColor);
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("Arial", Font.BOLD, 12));
        backButton.setFocusPainted(false);
        backButton.setOpaque(true);
        backButton.setBorderPainted(false);
        backButton.addActionListener(e -> dispose()); 
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10)); 
        buttonPanel.setBackground(backgroundColor);
        buttonPanel.add(backButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadLoanHistory() {
        // ✅ PERUBAHAN DI SINI: Panggil method update otomatis SEBELUM mengambil data
        loanDAO.expireUserLoans(currentUser.getIdUser());

        tableModel.setRowCount(0); 
        List<Loan> loans = loanDAO.getLoanHistoryByUser(currentUser.getIdUser());

        if (loans.isEmpty()) {
            
            tableModel.addRow(new Object[]{"-", "Belum ada riwayat peminjaman.", "-", "-", "-", "-", "-"});
            return; // Keluar dari method setelah menampilkan pesan
        }

        for (Loan loan : loans) {
            String approvedByUsername = "Belum Disetujui";
            if (loan.getApprovedBy() != 0) {
                User approver = userDAO.getUserById(loan.getApprovedBy()); 
                if (approver != null) {
                    approvedByUsername = approver.getNama();
                } else {
                    approvedByUsername = "ID: " + loan.getApprovedBy();
                }
            } else if ("borrowed".equalsIgnoreCase(loan.getStatus())) {
                approvedByUsername = "Langsung Dipinjam";
            }

            
            tableModel.addRow(new Object[]{
                loan.getIdLoan(),
                loan.getBookTitle() != null ? loan.getBookTitle() : "N/A",
                formatDateTime(loan.getRequestDate()),
                formatDateTime(loan.getApprovedDate()),
                formatDateTime(loan.getReturnDate()),
                loan.getStatus(),
                approvedByUsername
            });
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "-";
        return dateTime.format(DATETIME_FORMATTER);
    }

    
}