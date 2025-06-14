import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.AbstractCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private Color returnButtonColor = new Color(59, 130, 246); // Biru untuk tombol kembalikan

    public LoanHistoryScreen(User user) {
        this.currentUser = user;
        this.loanDAO = new LoanDAO(DBConnection.getConnection());
        this.userDAO = new UserDAO(DBConnection.getConnection()); 

        setTitle("Riwayat Peminjaman - " + currentUser.getNama());
        setSize(950, 500); // Perbesar ukuran frame
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

        String[] columns = {"ID Pinjam", "Judul Buku", "Tgl Pinjam", "Tgl Disetujui", "Tgl Kembali", "Status", "Disetujui Oleh", "Aksi"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Hanya kolom "Aksi" yang bisa di-klik
            }
        };
        loanTable = new JTable(tableModel);
        loanTable.setFillsViewportHeight(true);
        loanTable.setRowHeight(30); 
        loanTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        loanTable.getTableHeader().setBackground(headerColor);
        loanTable.getTableHeader().setOpaque(false);

        // Atur lebar kolom
        TableColumnModel columnModel = loanTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(60);  // ID
        columnModel.getColumn(1).setPreferredWidth(200); // Judul
        columnModel.getColumn(2).setPreferredWidth(120); // Tgl Pinjam
        columnModel.getColumn(3).setPreferredWidth(120); // Tgl Disetujui
        columnModel.getColumn(4).setPreferredWidth(120); // Tgl Kembali
        columnModel.getColumn(5).setPreferredWidth(80);  // Status
        columnModel.getColumn(6).setPreferredWidth(120); // Disetujui Oleh
        columnModel.getColumn(7).setPreferredWidth(120); // Aksi

        // Set custom renderer dan editor untuk kolom "Aksi"
        ActionPanelRendererAndEditor actionHandler = new ActionPanelRendererAndEditor(loanTable);
        columnModel.getColumn(7).setCellRenderer(actionHandler);
        columnModel.getColumn(7).setCellEditor(actionHandler);

        JScrollPane scrollPane = new JScrollPane(loanTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JButton backButton = new JButton("Kembali ke Dashboard");
        backButton.setBackground(neutralColor);
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("Arial", Font.BOLD, 12));
        backButton.setFocusPainted(false);
        // --- PERBAIKAN WARNA TOMBOL ---
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
        tableModel.setRowCount(0); 
        List<Loan> loans = loanDAO.getLoanHistoryByUser(currentUser.getIdUser());

        if (loans.isEmpty()) {
            tableModel.addRow(new Object[]{"-", "Belum ada riwayat peminjaman.", "-", "-", "-", "-", "-", null});
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
                approvedByUsername,
                "Aksi" // Placeholder untuk kolom Aksi
            });
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "-";
        return dateTime.format(DATETIME_FORMATTER);
    }

    // Inner class untuk tombol "Kembalikan"
    class ActionPanelRendererAndEditor extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {
        private JPanel panel;
        private JButton returnButton;
        private JTable table;
        private int currentRow;

        public ActionPanelRendererAndEditor(JTable table) {
            this.table = table;
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 2)); // Sedikit vertical gap
            panel.setOpaque(true);

            returnButton = new JButton("Kembalikan");
            returnButton.setFont(new Font("Arial", Font.PLAIN, 10));
            returnButton.setMargin(new Insets(2, 5, 2, 5));
            returnButton.setActionCommand("return");
            returnButton.addActionListener(this);
            panel.add(returnButton);
        }
        
        private void updateButtonVisibility(int row) {
             Object statusObj = table.getModel().getValueAt(row, 5); // Kolom status adalah indeks ke-5
             String status = (statusObj != null) ? statusObj.toString() : "";

            // Tampilkan tombol hanya jika status 'approved' atau 'borrowed'
            if ("approved".equalsIgnoreCase(status) || "borrowed".equalsIgnoreCase(status)) {
                returnButton.setVisible(true);
                returnButton.setBackground(returnButtonColor);
                returnButton.setForeground(Color.WHITE);
                // --- PERBAIKAN WARNA TOMBOL ---
                returnButton.setOpaque(true);
                returnButton.setBorderPainted(false);
            } else {
                returnButton.setVisible(false);
            }
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
            } else {
                panel.setBackground(table.getBackground());
            }
            updateButtonVisibility(row);
            return panel;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            updateButtonVisibility(row);
            this.currentRow = row;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fireEditingStopped();
            
            Object idLoanObj = table.getModel().getValueAt(this.currentRow, 0); // Kolom ID adalah indeks ke-0
            
            // Pengaman jika baris placeholder diklik
            if(!(idLoanObj instanceof Integer)){
                return;
            }
            int loanId = (int) idLoanObj;

            int confirm = JOptionPane.showConfirmDialog(table, "Anda yakin ingin mengembalikan buku ini?", "Konfirmasi Pengembalian", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = loanDAO.returnBook(loanId);
                if (success) {
                    JOptionPane.showMessageDialog(table, "Buku berhasil dikembalikan.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    loadLoanHistory(); // Muat ulang data untuk refresh tabel
                } else {
                    JOptionPane.showMessageDialog(table, "Gagal mengembalikan buku.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
