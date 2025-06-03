import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableCellEditor; // IMPORT YANG PERLU DITAMBAHKAN
import javax.swing.AbstractCellEditor;   // IMPORT YANG PERLU DITAMBAHKAN
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// Asumsi kelas User, Loan, LoanDAO, DBConnection sudah ada dan bisa diakses
// import com.perpustakaan.model.User;
// import com.perpustakaan.model.Loan;
// import com.perpustakaan.dao.LoanDAO;
// import com.perpustakaan.util.DBConnection;

public class LoanManagementScreen extends JFrame {
    private LoanDAO loanDAO;
    private User currentUser; // Supervisor yang sedang login
    private DefaultTableModel tableModel;
    private JTable pendingLoansTable;

    // Formatter untuk tanggal dan waktu
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // Palet Warna
    private Color primaryColor = new Color(30, 58, 138); // Biru tua
    private Color successColor = new Color(28, 132, 74); // Hijau lebih tua
    private Color dangerColor = new Color(220, 53, 69);  // Merah
    private Color headerColor = new Color(224, 231, 255); // Biru muda untuk header tabel
    private Color backgroundColor = new Color(240, 242, 245);
    private Color neutralColor = new Color(107, 114, 128);


    public LoanManagementScreen(User user) {
        this.currentUser = user;
        this.loanDAO = new LoanDAO(DBConnection.getConnection());

        setTitle("Kelola Peminjaman Pending - Supervisor: " + currentUser.getNama());
        setSize(900, 550); // Perbesar frame untuk tabel yang lebih informatif
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        loadPendingLoans();

        setVisible(true);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(backgroundColor);

        JLabel titleLabel = new JLabel("Daftar Permintaan Peminjaman Buku", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(primaryColor);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Setup Tabel
        String[] columnNames = {"ID Pinjam", "Nama Peminjam", "Judul Buku", "Tgl Permintaan", "Aksi"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Kolom "Aksi" akan berisi tombol, jadi perlu bisa "diedit" untuk memicu aksi tombol
                return column == 4; 
            }
        };
        pendingLoansTable = new JTable(tableModel);
        pendingLoansTable.setRowHeight(30); // Tinggi baris
        pendingLoansTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        pendingLoansTable.getTableHeader().setBackground(headerColor);
        pendingLoansTable.getTableHeader().setOpaque(false);
        pendingLoansTable.setFont(new Font("Arial", Font.PLAIN, 12));
        pendingLoansTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Pilih satu baris saja

        // Atur lebar kolom
        TableColumnModel columnModel = pendingLoansTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(80);  // ID Pinjam
        columnModel.getColumn(1).setPreferredWidth(150); // Nama Peminjam
        columnModel.getColumn(2).setPreferredWidth(250); // Judul Buku
        columnModel.getColumn(3).setPreferredWidth(120); // Tgl Permintaan
        columnModel.getColumn(4).setPreferredWidth(180); // Aksi (untuk 2 tombol)

        // Set custom renderer dan editor untuk kolom "Aksi"
        ActionPanelRendererAndEditor actionHandler = new ActionPanelRendererAndEditor(pendingLoansTable);
        columnModel.getColumn(4).setCellRenderer(actionHandler);
        columnModel.getColumn(4).setCellEditor(actionHandler);


        JScrollPane scrollPane = new JScrollPane(pendingLoansTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Tombol Refresh dan Kembali
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        JButton refreshButton = new JButton("Refresh Daftar");
        styleActionButton(refreshButton, primaryColor, 140, 35);
        refreshButton.addActionListener(e -> loadPendingLoans());
        
        JButton backButton = new JButton("Kembali ke Dashboard");
        styleActionButton(backButton, neutralColor, 180, 35);
        backButton.addActionListener(e -> dispose()); // Hanya tutup window ini

        JPanel leftBottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftBottomPanel.setOpaque(false);
        leftBottomPanel.add(refreshButton);
        
        JPanel rightBottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightBottomPanel.setOpaque(false);
        rightBottomPanel.add(backButton);

        bottomPanel.add(leftBottomPanel, BorderLayout.WEST);
        bottomPanel.add(rightBottomPanel, BorderLayout.EAST);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadPendingLoans() {
        tableModel.setRowCount(0); // Kosongkan tabel
        List<Loan> pendingLoans = loanDAO.getPendingLoans(); // Asumsi method ini ada dan benar

        if (pendingLoans.isEmpty()) {
            // Opsional: tampilkan pesan jika tidak ada data, bisa dengan menambahkan label ke panel
            tableModel.addRow(new Object[]{"-", "Tidak ada permintaan pending", "-", "-", null});
        } else {
            for (Loan loan : pendingLoans) {
                tableModel.addRow(new Object[]{
                        loan.getIdLoan(),
                        loan.getUsername() != null ? loan.getUsername() : "N/A",
                        loan.getBookTitle() != null ? loan.getBookTitle() : "N/A",
                        loan.getRequestDate() != null ? loan.getRequestDate().format(DATETIME_FORMATTER) : "N/A",
                        "Aksi" // Placeholder, akan di-render oleh ActionPanelRendererAndEditor
                });
            }
        }
    }
    
    private void styleActionButton(JButton button, Color bgColor, int width, int height) {
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(width, height));
        // Efek hover sederhana
        Color originalBgColor = bgColor; 
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(originalBgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalBgColor);
            }
        });
    }

    // Inner class untuk menangani tombol Aksi di dalam tabel
    class ActionPanelRendererAndEditor extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {
        private JPanel panel;
        private JButton approveButton;
        private JButton rejectButton;
        private JTable table;
        private int currentRow; // Menyimpan baris yang sedang di-render/edit

        public ActionPanelRendererAndEditor(JTable table) {
            this.table = table;
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            panel.setOpaque(true); // Agar background bisa di-set

            approveButton = new JButton("Setujui");
            styleActionButton(approveButton, successColor, 80, 25);
            approveButton.setActionCommand("approve");
            approveButton.addActionListener(this);

            rejectButton = new JButton("Tolak");
            styleActionButton(rejectButton, dangerColor, 80, 25);
            rejectButton.setActionCommand("reject");
            rejectButton.addActionListener(this);

            panel.add(approveButton);
            panel.add(rejectButton);
        }

        // Overload method styleActionButton khusus untuk inner class jika perlu style berbeda
        private void styleActionButton(JButton button, Color bgColor, int width, int height) {
            button.setBackground(bgColor);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setFont(new Font("Arial", Font.PLAIN, 10)); // Font lebih kecil untuk tombol di tabel
            button.setPreferredSize(new Dimension(width, height));
            button.setMargin(new Insets(2,2,2,2));
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // Set background panel sesuai dengan status seleksi baris
            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
            } else {
                panel.setBackground(table.getBackground());
            }
            this.currentRow = row;
            return panel;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.currentRow = row;
            // Tidak perlu set background di sini karena panel sudah di-return apa adanya
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return null; // Tidak ada nilai yang perlu disimpan dari editor ini
        }

        @Override
        public boolean isCellEditable(java.util.EventObject anEvent) {
            return true; // Selalu bisa diedit untuk memicu tombol
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            fireEditingStopped(); // Penting untuk menghentikan mode edit sel

            // Dapatkan idLoan dari baris saat ini, kolom pertama (indeks 0)
            // Pastikan model tabelmu memiliki idLoan di kolom 0
            Object idLoanObj = table.getModel().getValueAt(this.currentRow, 0);
            if (idLoanObj == null || idLoanObj.toString().equals("-")) { // Cek jika placeholder
                System.err.println("Tidak bisa melakukan aksi: ID Loan tidak valid di baris " + this.currentRow);
                return;
            }
            
            // Pastikan idLoanObj adalah Integer sebelum di-cast
            if (!(idLoanObj instanceof Integer)) {
                System.err.println("Tidak bisa melakukan aksi: Tipe data ID Loan tidak valid di baris " + this.currentRow + ". Ditemukan: " + idLoanObj.getClass().getName());
                return;
            }
            int idLoan = (int) idLoanObj;

            String command = e.getActionCommand();
            boolean success = false;
            String actionMessage = "";

            if ("approve".equals(command)) {
                success = loanDAO.updateLoanStatus(idLoan, "approved", currentUser.getIdUser());
                actionMessage = success ? "Peminjaman ID " + idLoan + " berhasil disetujui." : "Gagal menyetujui peminjaman ID " + idLoan + ".";
            } else if ("reject".equals(command)) {
                success = loanDAO.updateLoanStatus(idLoan, "rejected", currentUser.getIdUser());
                actionMessage = success ? "Peminjaman ID " + idLoan + " berhasil ditolak." : "Gagal menolak peminjaman ID " + idLoan + ".";
            }

            if (success) {
                JOptionPane.showMessageDialog(table.getParent().getParent(), actionMessage, "Status Update", JOptionPane.INFORMATION_MESSAGE);
                loadPendingLoans(); // Muat ulang data tabel untuk merefleksikan perubahan
            } else {
                JOptionPane.showMessageDialog(table.getParent().getParent(), actionMessage, "Error Update", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
