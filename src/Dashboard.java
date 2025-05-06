import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

public class Dashboard extends JFrame {
    private UserDAO userDAO;
    private User user;
    private JLabel welcomeLabel;

    public Dashboard(String nama, String email, int idRole) {
        // Inisialisasi user sementara untuk ambil data lengkap
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            userDAO = new UserDAO(conn);
            this.user = userDAO.getUserByNameAndEmail(nama, email);
            if (this.user == null) {
                throw new Exception("User tidak ditemukan!");
            }
        } catch (Exception e) {
            System.err.println("Error connecting to database or fetching user: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal terhubung ke database atau memuat user!", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            new Login().setVisible(true);
            return;
        }

        setTitle("Dashboard Perpustakaan - " + user.getNama());
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel utama
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Label sambutan
        welcomeLabel = new JLabel("Selamat Datang, " + user.getNama() + " (Role ID: " + user.getIdRole() + ")!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(welcomeLabel, BorderLayout.NORTH);

        // Panel tombol
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // Komponen berdasarkan role
        if (user.getIdRole() == 1) { // Admin
            JButton verifyUsersButton = new JButton("Verifikasi User");
            verifyUsersButton.addActionListener(e -> {
                new VerificationListScreen(user);
                dispose();
            });
            JButton viewReportsButton = new JButton("Lihat Laporan");
            viewReportsButton.addActionListener(e -> {
                JOptionPane.showMessageDialog(this, "Fitur laporan belum tersedia!", "Info", JOptionPane.INFORMATION_MESSAGE);
            });
            buttonPanel.add(verifyUsersButton);
            buttonPanel.add(viewReportsButton);
        } else if (user.getIdRole() == 2) { // Supervisor
            JButton addBookButton = new JButton("Tambah Buku");
            addBookButton.addActionListener(e -> {
                new AddBookScreen(user);
                dispose();
            });
            JButton manageLoansButton = new JButton("Kelola Peminjaman");
            manageLoansButton.addActionListener(e -> {
                new LoanManagementScreen(user);
                dispose();
            });
            buttonPanel.add(addBookButton);
            buttonPanel.add(manageLoansButton);
        } else { // User (idRole == 3)
            JButton borrowBookButton = new JButton("Pinjam Buku");
            borrowBookButton.addActionListener(e -> {
                new BookListScreen(user);
                dispose();
            });
            buttonPanel.add(borrowBookButton);
        }

        // Tombol profil (untuk semua role)
        JButton profileButton = new JButton("Profil");
        profileButton.addActionListener(e -> {
            User updatedUser = userDAO.getUserByNameAndEmail(user.getNama(), user.getEmail());
            if (updatedUser != null) {
                this.user = updatedUser;
                System.out.println("Navigasi ke profil untuk: " + user.getNama());
                new ProfileScreen(this.user);
                dispose();
            } else {
                System.out.println("Gagal memuat data profil untuk: " + user.getNama());
                JOptionPane.showMessageDialog(this, "Gagal memuat profil!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Tombol refresh (untuk update role)
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            User updatedUser = userDAO.getUserByNameAndEmail(user.getNama(), user.getEmail());
            if (updatedUser != null) {
                this.user = updatedUser;
                welcomeLabel.setText("Selamat Datang, " + user.getNama() + " (Role ID: " + user.getIdRole() + ")!");
                revalidate();
                repaint();
                new Dashboard(user.getNama(), user.getEmail(), user.getIdRole()).setVisible(true);
                dispose();
            }
        });

        // Tombol logout (untuk semua role)
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            System.out.println("Logout: " + user.getNama());
            dispose();
            new Login().setVisible(true);
        });

        buttonPanel.add(profileButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(logoutButton);

        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        add(mainPanel);
        setVisible(true);
    }

    // Getter untuk user
    public User getUser() {
        return user;
    }
}