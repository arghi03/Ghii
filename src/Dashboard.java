import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.sql.Connection;
import java.sql.SQLException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class Dashboard extends JFrame {
    private UserDAO userDAO;
    private User user;
    private JLabel welcomeLabel;
    private JTextField searchField;
    private JTable bookTable;
    private BookDAO bookDAO;
    private FavoriteDAO favoriteDAO; 

    public Dashboard(String nama, String email, int idRole) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) {
                throw new SQLException("Koneksi ke database gagal didapatkan dari DBConnection.");
            }
            userDAO = new UserDAO(conn);
            bookDAO = new BookDAO(conn);
            favoriteDAO = new FavoriteDAO(conn); 

            this.user = userDAO.getUserByNameAndEmail(nama, email);

            if (this.user == null) {
                JOptionPane.showMessageDialog(this, "Gagal memuat data user. User tidak ditemukan dengan nama: " + nama + " dan email: " + email, "Error User", JOptionPane.ERROR_MESSAGE);
                SwingUtilities.invokeLater(() -> new Login().setVisible(true));
                dispose();
                return;
            }
            if (this.user.getIdRole() != idRole) {
                System.out.println("Peringatan: idRole dari DB (" + this.user.getIdRole() + ") berbeda dengan idRole dari argumen (" + idRole + "). Menggunakan dari DB.");
            }

        } catch (Exception e) {
            System.err.println("Error initializing Dashboard: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat Dashboard: " + e.getMessage(), "Error Kritis", JOptionPane.ERROR_MESSAGE);
            SwingUtilities.invokeLater(() -> new Login().setVisible(true));
            dispose();
            return;
        }

        setTitle("Dashboard Perpustakaan - " + user.getNama());
        setSize(850, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(240, 242, 245));

        JPanel topPanel = new JPanel(new BorderLayout(10,5));
        topPanel.setOpaque(false);

        welcomeLabel = new JLabel("Selamat Datang di LiteraSpace, " + user.getNama() + "!", SwingConstants.LEFT);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        topPanel.add(welcomeLabel, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setOpaque(false);
        searchField = new JTextField(25);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        JButton searchButton = new JButton("Cari Buku");
        searchButton.setFont(new Font("Arial", Font.PLAIN, 12));

        searchPanel.add(new JLabel("Cari Buku:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        topPanel.add(searchPanel, BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        JLabel infoLabel = new JLabel("Gunakan menu di bawah untuk navigasi.", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        mainPanel.add(infoLabel, BorderLayout.CENTER);


        JPanel bottomButtonPanel = new JPanel(new BorderLayout(0,10));
        bottomButtonPanel.setOpaque(false);
        JPanel roleSpecificButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        roleSpecificButtonPanel.setOpaque(false);

        if (user.getIdRole() == 1) { // Admin
            JButton verifyUsersButton = createStyledButton("Manajemen User");
            verifyUsersButton.addActionListener(e -> new UserManagementScreen(user));
            JButton viewReportsButton = createStyledButton("Lihat Laporan");
            viewReportsButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Fitur laporan belum tersedia!", "Info", JOptionPane.INFORMATION_MESSAGE));
            
            // ✅ Tombol Saran untuk Admin
            JButton viewSuggestionsButton = createStyledButton("Lihat Saran Buku");
            viewSuggestionsButton.addActionListener(e -> new SuggestionListScreen(user));

            roleSpecificButtonPanel.add(verifyUsersButton);
            roleSpecificButtonPanel.add(viewReportsButton);
            roleSpecificButtonPanel.add(viewSuggestionsButton);

        } else if (user.getIdRole() == 2) { // Supervisor
            JButton addBookButton = createStyledButton("Tambah Buku");
            addBookButton.addActionListener(e -> new AddBookScreen(user));
            
            JButton manageLoansButton = createStyledButton("Kelola Peminjaman");
            manageLoansButton.addActionListener(e -> new LoanManagementScreen(user));

            JButton manageBooksButton = createStyledButton("Kelola Buku");
            manageBooksButton.addActionListener(e -> new BookManagementScreen(user));
            
            // ✅ Tombol Saran untuk Supervisor
            JButton viewSuggestionsButton = createStyledButton("Lihat Saran Buku");
            viewSuggestionsButton.addActionListener(e -> new SuggestionListScreen(user));

            roleSpecificButtonPanel.add(addBookButton);
            roleSpecificButtonPanel.add(manageLoansButton);
            roleSpecificButtonPanel.add(manageBooksButton); 
            roleSpecificButtonPanel.add(viewSuggestionsButton);
            
        } else { // User
            JButton browseBooksButton = createStyledButton("Lihat Daftar Buku");
            browseBooksButton.addActionListener(e -> new BookListScreen(user));
            
            JButton suggestionButton = createStyledButton("Saran Buku");
            suggestionButton.addActionListener(e -> {
                new SuggestionDialog(this, this.user);
            });

            roleSpecificButtonPanel.add(browseBooksButton);
            roleSpecificButtonPanel.add(suggestionButton);
        }
        bottomButtonPanel.add(roleSpecificButtonPanel, BorderLayout.WEST);

        JPanel commonButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        commonButtonPanel.setOpaque(false);

        JButton profileButton = createStyledButton("Profil Saya");
        profileButton.addActionListener(e -> new ProfileScreen(this.user));

        JButton myFavoritesButton = createStyledButton("Favorit Saya");
        myFavoritesButton.addActionListener(e -> new MyFavoritesScreen(this.user)); 
        
        JButton loanHistoryButton = createStyledButton("Riwayat Peminjaman");
        loanHistoryButton.addActionListener(e -> new LoanHistoryScreen(this.user));

        JButton refreshButton = createStyledButton("Refresh Data");
        refreshButton.addActionListener(e -> refreshDashboardData());

        JButton logoutButton = createStyledButton("Logout");
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Anda yakin ingin logout?", "Konfirmasi Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new Login().setVisible(true);
            }
        });

        commonButtonPanel.add(profileButton);
        commonButtonPanel.add(myFavoritesButton); 
        commonButtonPanel.add(loanHistoryButton);
        commonButtonPanel.add(refreshButton);
        commonButtonPanel.add(logoutButton);
        bottomButtonPanel.add(commonButtonPanel, BorderLayout.EAST);
        mainPanel.add(bottomButtonPanel, BorderLayout.SOUTH);

        searchButton.addActionListener(e -> searchBooks());
        
        add(mainPanel);
        setVisible(true);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        return button;
    }

    private void searchBooks() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Masukkan kata kunci untuk mencari.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        } 
        JOptionPane.showMessageDialog(this, "Fitur pencarian tersedia di halaman 'Lihat Daftar Buku'.", "Info", JOptionPane.INFORMATION_MESSAGE);
        new BookListScreen(this.user);
    }

    private void refreshDashboardData() {
        System.out.println("Refreshing dashboard data...");
        User latestUser = userDAO.getUserById(this.user.getIdUser());
        if (latestUser != null) {
            this.user = latestUser;
            welcomeLabel.setText("Selamat Datang di LiteraSpace, " + user.getNama() + "!");
            System.out.println("Dashboard data refreshed.");
        } else {
            System.err.println("Gagal refresh user data, user tidak ditemukan.");
        }
    }

    public User getUser() {
        return user;
    }
}