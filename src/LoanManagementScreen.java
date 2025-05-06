import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LoanManagementScreen extends JFrame {
    private LoanDAO loanDAO;
    private User currentUser;

    public LoanManagementScreen(User user) {
        this.currentUser = user;
        this.loanDAO = new LoanDAO(DBConnection.getConnection());

        setTitle("Kelola Peminjaman - " + currentUser.getNama());
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Daftar Peminjaman Pending", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        List<Loan> pendingLoans = loanDAO.getPendingLoans();
        for (Loan loan : pendingLoans) {
            JPanel loanPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel loanLabel = new JLabel("ID Loan: " + loan.getIdLoan() + 
                                          " | Peminjam: " + loan.getUsername() + 
                                          " | Buku: " + loan.getBookTitle() + 
                                          " | Request: " + (loan.getRequestDate() != null ? loan.getRequestDate().toString() : "N/A"));
            JButton approveButton = new JButton("Approve");
            JButton rejectButton = new JButton("Reject");

            approveButton.addActionListener(e -> {
                boolean success = loanDAO.updateLoanStatus(loan.getIdLoan(), "approved", currentUser.getIdUser());
                if (success) {
                    JOptionPane.showMessageDialog(this, "Peminjaman disetujui!");
                    listPanel.remove(loanPanel);
                    listPanel.revalidate();
                    listPanel.repaint();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menyetujui peminjaman!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            rejectButton.addActionListener(e -> {
                boolean success = loanDAO.updateLoanStatus(loan.getIdLoan(), "rejected", currentUser.getIdUser());
                if (success) {
                    JOptionPane.showMessageDialog(this, "Peminjaman ditolak!");
                    listPanel.remove(loanPanel);
                    listPanel.revalidate();
                    listPanel.repaint();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menolak peminjaman!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            loanPanel.add(loanLabel);
            loanPanel.add(approveButton);
            loanPanel.add(rejectButton);
            listPanel.add(loanPanel);
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JButton backButton = new JButton("Kembali");
        backButton.addActionListener(e -> {
            new Dashboard(currentUser.getNama(), currentUser.getEmail(), currentUser.getIdRole()).setVisible(true);
            dispose();
        });
        mainPanel.add(backButton, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }
}