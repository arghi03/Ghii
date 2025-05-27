import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LoanHistoryScreen extends JFrame {
    private User user;
    private BookDAO bookDAO;

    public LoanHistoryScreen(User user) {
        this.user = user;
        this.bookDAO = new BookDAO(DBConnection.getConnection());

        setTitle("Riwayat Peminjaman - " + user.getNama());
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Label judul
        JLabel titleLabel = new JLabel("Riwayat Peminjaman Anda", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Tabel riwayat peminjaman
        String[] columns = {"ID Peminjaman", "Judul Buku", "Tanggal Pinjam", "Tanggal Disetujui", "Tanggal Kembali", "Status", "Disetujui Oleh"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable loanTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(loanTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Tombol kembali
        JButton backButton = new JButton("Kembali");
        backButton.addActionListener(e -> {
            dispose();
            new Dashboard(user.getNama(), user.getEmail(), user.getIdRole()).setVisible(true);
        });
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(backButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Muat data riwayat peminjaman
        List<Loan> loans = bookDAO.getLoanHistoryByUser(user.getIdUser());
        for (Loan loan : loans) {
            model.addRow(new Object[]{
                loan.getIdLoan(),
                loan.getBookTitle(),
                loan.getRequestDate(),
                loan.getApprovedDate(),
                loan.getReturnDate(),
                loan.getStatus(),
                loan.getApprovedBy() == 0 ? "Belum Disetujui" : loan.getApprovedBy()
            });
        }

        add(mainPanel);
        setVisible(true);
    }
}