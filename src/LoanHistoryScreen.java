import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer; // Digunakan untuk menengahkan
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LoanHistoryScreen extends JFrame {
    private User currentUser;
    private LoanDAO loanDAO;
    private UserDAO userDAO; 

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    // private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");


    public LoanHistoryScreen(User user) {
        this.currentUser = user;
        this.loanDAO = new LoanDAO(DBConnection.getConnection());
        this.userDAO = new UserDAO(DBConnection.getConnection()); 

        setTitle("Riwayat Peminjaman - " + currentUser.getNama());
        setSize(800, 500); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        Color primaryColor = new Color(30, 58, 138); 
        Color headerColor = new Color(224, 231, 255); 
        Color backgroundColor = Color.WHITE; 

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(backgroundColor);

        JLabel titleLabel = new JLabel("Riwayat Peminjaman Anda", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(primaryColor);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"ID Pinjam", "Judul Buku", "Tgl Pinjam", "Tgl Disetujui", "Tgl Kembali", "Status", "Disetujui Oleh"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        JTable loanTable = new JTable(model);
        loanTable.setFillsViewportHeight(true);
        loanTable.setRowHeight(25); 
        loanTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12)); 
        loanTable.getTableHeader().setBackground(headerColor); 
        loanTable.getTableHeader().setOpaque(false); 

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < columns.length; i++) {
            loanTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(loanTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JButton backButton = new JButton("Kembali ke Dashboard");
        backButton.setBackground(primaryColor);
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("Arial", Font.BOLD, 12));
        backButton.setFocusPainted(false);
        
        backButton.addActionListener(e -> {
            dispose(); 
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10)); 
        buttonPanel.setBackground(backgroundColor);
        buttonPanel.add(backButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        loadLoanHistory(model);

        add(mainPanel);
        setVisible(true); // --- BARIS INI DIAKTIFKAN KEMBALI ---
    }

    private void loadLoanHistory(DefaultTableModel model) {
        model.setRowCount(0); 
        List<Loan> loans = loanDAO.getLoanHistoryByUser(currentUser.getIdUser());

        if (loans.isEmpty()) {
            System.out.println("Tidak ada riwayat peminjaman untuk user: " + currentUser.getNama());
            // Opsional: Tampilkan pesan di UI jika tidak ada data
            // JLabel noDataLabel = new JLabel("Belum ada riwayat peminjaman.", SwingConstants.CENTER);
            // noDataLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            // model.addRow(new Object[]{"-", noDataLabel, "-", "-", "-", "-", "-"}); // Ini akan error karena tipe tidak cocok
            // Cara yang lebih baik adalah menampilkan panel kosong atau pesan di atas tabel.
        }

        for (Loan loan : loans) {
            String approvedByUsername = "Belum Disetujui";
            if (loan.getApprovedBy() != 0) {
                User approver = userDAO.getUserById(loan.getApprovedBy()); 
                if (approver != null) {
                    approvedByUsername = approver.getNama();
                } else {
                    approvedByUsername = "ID: " + loan.getApprovedBy() + " (Tidak Dikenal)";
                }
            } else if ("approved".equalsIgnoreCase(loan.getStatus()) || "returned".equalsIgnoreCase(loan.getStatus()) || "borrowed".equalsIgnoreCase(loan.getStatus())) {
                if ("borrowed".equalsIgnoreCase(loan.getStatus()) && loan.getApprovedBy() == 0 && loan.getApprovedDate() == null) {
                    approvedByUsername = "Langsung Dipinjam"; 
                } else {
                     approvedByUsername = "Sistem/Data Lama";
                }
            }

            model.addRow(new Object[]{
                loan.getIdLoan(),
                loan.getBookTitle() != null ? loan.getBookTitle() : "Judul Tidak Ada",
                formatDateTime(loan.getRequestDate()),
                formatDateTime(loan.getApprovedDate()),
                formatDateTime(loan.getReturnDate()),
                loan.getStatus(),
                approvedByUsername
            });
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "-"; 
        }
        return dateTime.format(DATETIME_FORMATTER);
    }
}
