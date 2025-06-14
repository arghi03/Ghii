import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.Desktop;


public class Dashboard extends JFrame {
    private UserDAO userDAO;
    private User user; // User yang sedang login
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

        String[] columns = {"ID", "Judul", "Penulis", "Sampul", "Rating", "Aksi"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3) return ImageIcon.class;
                if (columnIndex == 5) return JPanel.class; 
                return super.getColumnClass(columnIndex);
            }
        };
        bookTable = new JTable(model);
        bookTable.setRowHeight(60);
        bookTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        bookTable.setFont(new Font("Arial", Font.PLAIN, 12));

        TableColumnModel columnModelTable = bookTable.getColumnModel();
        columnModelTable.getColumn(0).setPreferredWidth(40);
        columnModelTable.getColumn(1).setPreferredWidth(200);
        columnModelTable.getColumn(2).setPreferredWidth(150);
        columnModelTable.getColumn(3).setPreferredWidth(70);
        columnModelTable.getColumn(4).setPreferredWidth(60);
        columnModelTable.getColumn(5).setPreferredWidth(230); 

        columnModelTable.getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel();
                if (value instanceof ImageIcon) label.setIcon((ImageIcon) value);
                else label.setText("N/A");
                label.setHorizontalAlignment(SwingConstants.CENTER);
                return label;
            }
        });
        columnModelTable.getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Float) label.setText(String.format("%.1f", (Float) value));
                else label.setText("-");
                label.setHorizontalAlignment(SwingConstants.CENTER);
                return label;
            }
        });
        columnModelTable.getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof JPanel) return (JPanel) value;
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        JScrollPane scrollPane = new JScrollPane(bookTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomButtonPanel = new JPanel(new BorderLayout(0,10));
        bottomButtonPanel.setOpaque(false);
        JPanel roleSpecificButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        roleSpecificButtonPanel.setOpaque(false);

        if (user.getIdRole() == 1) { // Admin
            JButton verifyUsersButton = createStyledButton("Manajemen User");
            verifyUsersButton.addActionListener(e -> new UserManagementScreen(user));
            JButton viewReportsButton = createStyledButton("Lihat Laporan");
            viewReportsButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Fitur laporan belum tersedia!", "Info", JOptionPane.INFORMATION_MESSAGE));
            roleSpecificButtonPanel.add(verifyUsersButton);
            roleSpecificButtonPanel.add(viewReportsButton);
        } else if (user.getIdRole() == 2) { // Supervisor
            JButton addBookButton = createStyledButton("Tambah Buku");
            addBookButton.addActionListener(e -> new AddBookScreen(user));
            JButton manageLoansButton = createStyledButton("Kelola Peminjaman");
            manageLoansButton.addActionListener(e -> new LoanManagementScreen(user));
            roleSpecificButtonPanel.add(addBookButton);
            roleSpecificButtonPanel.add(manageLoansButton);
        } else { // User
            JButton browseBooksButton = createStyledButton("Lihat Daftar Buku");
            browseBooksButton.addActionListener(e -> new BookListScreen(user));
            roleSpecificButtonPanel.add(browseBooksButton);
        }
        bottomButtonPanel.add(roleSpecificButtonPanel, BorderLayout.WEST);

        JPanel commonButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        commonButtonPanel.setOpaque(false);

        JButton profileButton = createStyledButton("Profil Saya");
        profileButton.addActionListener(e -> {
            User latestUser = userDAO.getUserById(user.getIdUser());
            if (latestUser != null) {
                this.user = latestUser;
                 new ProfileScreen(this.user);
            } else {
                JOptionPane.showMessageDialog(this, "Gagal memuat profil, data user tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton myFavoritesButton = createStyledButton("Favorit Saya");
        myFavoritesButton.addActionListener(e -> {
            new MyFavoritesScreen(this.user); 
        });
        
        JButton loanHistoryButton = createStyledButton("Riwayat Peminjaman");
        loanHistoryButton.addActionListener(e -> new LoanHistoryScreen(this.user));

        JButton refreshButton = createStyledButton("Refresh Data");
        refreshButton.addActionListener(e -> refreshDashboardData());

        JButton logoutButton = createStyledButton("Logout");
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Anda yakin ingin logout?", "Konfirmasi Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.out.println("Logout: " + user.getNama());
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
        loadInitialBooks();
        add(mainPanel);
        setVisible(true);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        return button;
    }
    
    private void loadInitialBooks() {
        System.out.println("Memuat semua buku ke dashboard...");
        DefaultTableModel model = (DefaultTableModel) bookTable.getModel();
        model.setRowCount(0);
        List<Book> initialBooks = bookDAO.getAllBooks();
        if (initialBooks.isEmpty()) System.out.println("Tidak ada buku yang dimuat ke dashboard.");
        else System.out.println("Berhasil memuat " + initialBooks.size() + " buku ke dashboard.");
        populateBookTable(initialBooks);
    }

    private void searchBooks() {
        String keyword = searchField.getText().trim();
        System.out.println("Mencari buku dengan keyword: " + keyword);
        DefaultTableModel model = (DefaultTableModel) bookTable.getModel();
        model.setRowCount(0);
        List<Book> books = bookDAO.searchBooks(keyword);
        if (books.isEmpty()) {
            System.out.println("Tidak ada buku yang ditemukan untuk keyword: " + keyword);
            JOptionPane.showMessageDialog(this, "Tidak ada buku yang cocok dengan kata kunci '" + keyword + "'.", "Pencarian Kosong", JOptionPane.INFORMATION_MESSAGE);
        } else {
            System.out.println("Ditemukan " + books.size() + " buku untuk keyword: " + keyword);
        }
        populateBookTable(books);
    }

    private void populateBookTable(List<Book> books) {
        DefaultTableModel model = (DefaultTableModel) bookTable.getModel();
        

        for (Book book : books) {
            ImageIcon coverIcon = null;
            if (book.getCoverImagePath() != null && !book.getCoverImagePath().isEmpty()) {
                try {
                    File imgFile = new File(book.getCoverImagePath());
                    if (imgFile.exists() && !imgFile.isDirectory()) {
                        BufferedImage img = ImageIO.read(imgFile);
                        Image scaledImg = img.getScaledInstance(50, 55, Image.SCALE_SMOOTH);
                        coverIcon = new ImageIcon(scaledImg);
                    }
                } catch (Exception ex) {
                    System.err.println("Error loading cover image for table, book " + book.getTitle() + ": " + ex.getMessage());
                }
            }

            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
            actionPanel.setOpaque(false);
            
            JButton previewBtn = new JButton("Lihat");
            JButton borrowBtn = new JButton("Pinjam");
            JButton favoriteBtnTable = new JButton(); 

            Font actionButtonFont = new Font("Arial", Font.PLAIN, 10);
            previewBtn.setFont(actionButtonFont);
            borrowBtn.setFont(actionButtonFont);
            favoriteBtnTable.setFont(actionButtonFont); 

            updateFavoriteButtonForTable(favoriteBtnTable, this.favoriteDAO.isFavorite(this.user.getIdUser(), book.getIdBook()));


            previewBtn.addActionListener(ae -> {
                System.out.println("Preview action for book ID: " + book.getIdBook());
               
                new BookDetailScreen(book.getIdBook(), bookDAO, this.user, this.favoriteDAO).setVisible(true);
            });
            borrowBtn.addActionListener(ae -> {
                System.out.println("Borrow action for book ID: " + book.getIdBook());
                LoanDAO tempLoanDAO = new LoanDAO(DBConnection.getConnection());
                boolean success = tempLoanDAO.addLoan(this.user.getIdUser(), book.getIdBook());
                 if (success) {
                    JOptionPane.showMessageDialog(this, "Pengajuan pinjaman untuk buku \"" + book.getTitle() + "\" berhasil.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal mengajukan peminjaman untuk buku \"" + book.getTitle() + "\".", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            favoriteBtnTable.addActionListener(ae -> {
                boolean isCurrentlyFavorite = this.favoriteDAO.isFavorite(this.user.getIdUser(), book.getIdBook());
                boolean success;
                if (isCurrentlyFavorite) {
                    success = this.favoriteDAO.removeFavorite(this.user.getIdUser(), book.getIdBook());
                    if (success) JOptionPane.showMessageDialog(this, "\""+book.getTitle()+"\" dihapus dari favorit.");
                } else {
                    success = this.favoriteDAO.addFavorite(this.user.getIdUser(), book.getIdBook());
                     if (success) JOptionPane.showMessageDialog(this, "\""+book.getTitle()+"\" ditambahkan ke favorit.");
                }
                if(success) {
                    updateFavoriteButtonForTable(favoriteBtnTable, !isCurrentlyFavorite);
                } else {
                     JOptionPane.showMessageDialog(this, "Aksi favorit gagal.");
                }
            });

            actionPanel.add(favoriteBtnTable); 
            actionPanel.add(previewBtn);
            actionPanel.add(borrowBtn);

            model.addRow(new Object[]{
                book.getIdBook(),
                book.getTitle(),
                book.getAuthor(),
                coverIcon,
                book.getRating(),
                actionPanel
            });
        }
    }
    
    private void updateFavoriteButtonForTable(JButton favButton, boolean isFavorite) {
        if (isFavorite) {
            favButton.setText("❤️ Fav"); 
            favButton.setBackground(new Color(220, 53, 69)); 
        } else {
            favButton.setText("🤍 Add Fav");
            favButton.setBackground(new Color(108, 117, 125)); 
        }
        favButton.setForeground(Color.WHITE);
        favButton.setFocusPainted(false);
        // favButton.setMargin(new Insets(2,5,2,5)); 
    }

    private void refreshDashboardData() {
        System.out.println("Refreshing dashboard data...");
        User latestUser = userDAO.getUserById(this.user.getIdUser());
        if (latestUser != null) {
            this.user = latestUser;
            welcomeLabel.setText("Selamat Datang, " + user.getNama() + "!");
        } else {
            System.err.println("Gagal refresh user data, user tidak ditemukan.");
        }
        loadInitialBooks(); 
        searchField.setText("");
        System.out.println("Dashboard data refreshed.");
    }



    public User getUser() {
        return user;
    }
}
