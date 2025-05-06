import javax.swing.*;
import java.awt.*;
import java.util.List;

public class VerificationListScreen extends JFrame {
    private UserDAO userDAO;
    private User currentUser;

    public VerificationListScreen(User user) {
        this.currentUser = user;
        this.userDAO = new UserDAO(DBConnection.getConnection());

        setTitle("Verifikasi User - " + currentUser.getNama());
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Daftar User Belum Diverifikasi", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        List<User> unverifiedUsers = userDAO.getUnverifiedUsers();
        for (User u : unverifiedUsers) {
            JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel userLabel = new JLabel("ID: " + u.getIdUser() + " | Nama: " + u.getNama() + " | Email: " + u.getEmail());
            JButton approveButton = new JButton("Approve");
            JButton rejectButton = new JButton("Reject");

            approveButton.addActionListener(e -> {
                boolean success = userDAO.approveUser(u.getIdUser());
                if (success) {
                    JOptionPane.showMessageDialog(this, "User " + u.getNama() + " berhasil diverifikasi!");
                    listPanel.remove(userPanel);
                    listPanel.revalidate();
                    listPanel.repaint();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal memverifikasi user!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            rejectButton.addActionListener(e -> {
                boolean success = userDAO.rejectUser(u.getIdUser());
                if (success) {
                    JOptionPane.showMessageDialog(this, "User " + u.getNama() + " berhasil ditolak!");
                    listPanel.remove(userPanel);
                    listPanel.revalidate();
                    listPanel.repaint();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menolak user!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            userPanel.add(userLabel);
            userPanel.add(approveButton);
            userPanel.add(rejectButton);
            listPanel.add(userPanel);
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