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
import java.awt.event.ActionEvent;

public class Dashboard extends JFrame {
    private UserDAO userDAO;
    private User user;
    private JLabel welcomeLabel;
    private JTextField searchField;
    private JTable bookTable;
    private BookDAO bookDAO;

    public Dashboard(String nama, String email, int idRole) {
        // Inisialisasi user dan koneksi
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            userDAO = new UserDAO(conn);
            bookDAO = new BookDAO(conn);
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
        setSize(800, 600);
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

        // Panel pencarian
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Cari Buku");
        searchPanel.add(new JLabel("Cari: "));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        mainPanel.add(searchPanel, BorderLayout.NORTH);

        // Tabel buku
        String[] columns = {"ID", "Judul", "Penulis", "Sampul", "Rating", "Aksi"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Hanya kolom "Aksi" yang bisa diedit
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3) { // Kolom "Sampul"
                    return ImageIcon.class;
                } else if (columnIndex == 5) { // Kolom "Aksi"
                    return JPanel.class;
                }
                return super.getColumnClass(columnIndex);
            }
        };
        bookTable = new JTable(model);
        bookTable.setRowHeight(50);
        bookTable.getColumnModel().getColumn(5).setPreferredWidth(200);

        // Custom renderer untuk kolom "Sampul"
        bookTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel();
                if (value instanceof ImageIcon) {
                    label.setIcon((ImageIcon) value);
                } else {
                    label.setText("Gambar Tidak Ada");
                }
                label.setHorizontalAlignment(SwingConstants.CENTER);
                return label;
            }
        });

        // Custom renderer untuk kolom "Rating"
        bookTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Float) {
                    label.setText(String.format("%.1f/5", (Float) value));
                } else {
                    label.setText("N/A");
                }
                label.setHorizontalAlignment(SwingConstants.CENTER);
                return label;
            }
        });

        // Custom renderer untuk kolom "Aksi"
        bookTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                return (JPanel) value;
            }
        });

        // Custom editor untuk kolom "Aksi"
        bookTable.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(new JTextField()) {
            private JPanel panel;
            private JButton previewButton;
            private JButton borrowButton;
            private JButton readButton;

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
                previewButton = new JButton("Preview");
                borrowButton = new JButton("Pinjam");
                readButton = new JButton("Baca");

                previewButton.setFont(new Font("Arial", Font.PLAIN, 10));
                borrowButton.setFont(new Font("Arial", Font.PLAIN, 10));
                readButton.setFont(new Font("Arial", Font.PLAIN, 10));

                int idBook = (int) table.getValueAt(row, 0);
                Book book = getBookById(idBook);

                // Action untuk Preview
                previewButton.addActionListener(e -> {
                    new BookDetailScreen(idBook, bookDAO).setVisible(true);
                    stopCellEditing();
                });

                // Action untuk Pinjam
                borrowButton.addActionListener(e -> {
                    try (Connection connection = bookDAO.getConnection();
                         PreparedStatement stmt = connection.prepareStatement(
                             "INSERT INTO loans (id_user, id_book, request_date, status) VALUES (?, ?, NOW(), 'borrowed')")) {
                        stmt.setInt(1, user.getIdUser());
                        stmt.setInt(2, idBook);
                        int affectedRows = stmt.executeUpdate();
                        if (affectedRows > 0) {
                            JOptionPane.showMessageDialog(Dashboard.this, "Buku berhasil dipinjam!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(Dashboard.this, "Gagal meminjam buku!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (SQLException ex) {
                        System.err.println("Error borrowing book: " + ex.getMessage());
                        JOptionPane.showMessageDialog(Dashboard.this, "Gagal meminjam buku: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    stopCellEditing();
                });

                // Action untuk Baca
                readButton.addActionListener(e -> {
                    if (book.getBookFilePath() != null && !book.getBookFilePath().isEmpty()) {
                        try {
                            File pdfFile = new File(book.getBookFilePath());
                            if (pdfFile.exists()) {
                                Desktop.getDesktop().open(pdfFile);
                            } else {
                                JOptionPane.showMessageDialog(Dashboard.this, "File PDF tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception ex) {
                            System.err.println("Error opening PDF: " + ex.getMessage());
                            JOptionPane.showMessageDialog(Dashboard.this, "Gagal membuka file PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(Dashboard.this, "File PDF tidak tersedia untuk buku ini!", "Info", JOptionPane.INFORMATION_MESSAGE);
                    }
                    stopCellEditing();
                });

                panel.add(previewButton);
                panel.add(borrowButton);
                panel.add(readButton);
                return panel;
            }

            @Override
            public Object getCellEditorValue() {
                return panel;
            }
        });

        JScrollPane scrollPane = new JScrollPane(bookTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel tombol bawah (role-based)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

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
            JButton borrowBookButton = new JButton("Daftar Buku");
            borrowBookButton.addActionListener(e -> {
                new BookListScreen(user);
                dispose();
            });
            buttonPanel.add(borrowBookButton);
        }

        // Tombol tambahan
        JButton profileButton = new JButton("Profil");
        profileButton.addActionListener(e -> {
            User updatedUser = userDAO.getUserByNameAndEmail(user.getNama(), user.getEmail());
            if (updatedUser != null) {
                this.user = updatedUser;
                new ProfileScreen(this.user);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal memuat profil!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

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

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            System.out.println("Logout: " + user.getNama());
            dispose();
            new Login().setVisible(true);
        });

        buttonPanel.add(profileButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(logoutButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Action listener untuk pencarian
        searchButton.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            List<Book> books = bookDAO.searchBooks(keyword);
            model.setRowCount(0); // Clear tabel
            for (Book book : books) {
                ImageIcon coverIcon = null;
                try {
                    if (book.getCoverImagePath() != null && !book.getCoverImagePath().isEmpty()) {
                        BufferedImage img = ImageIO.read(new File(book.getCoverImagePath()));
                        Image scaledImg = img.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                        coverIcon = new ImageIcon(scaledImg);
                    }
                } catch (Exception ex) {
                    System.err.println("Error loading image for book " + book.getTitle() + ": " + ex.getMessage());
                }

                // Panel untuk tombol action
                JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
                JButton previewButton = new JButton("Preview");
                JButton borrowButton = new JButton("Pinjam");
                JButton readButton = new JButton("Baca");

                previewButton.setFont(new Font("Arial", Font.PLAIN, 10));
                borrowButton.setFont(new Font("Arial", Font.PLAIN, 10));
                readButton.setFont(new Font("Arial", Font.PLAIN, 10));

                actionPanel.add(previewButton);
                actionPanel.add(borrowButton);
                actionPanel.add(readButton);

                model.addRow(new Object[]{
                    book.getIdBook(),
                    book.getTitle(),
                    book.getAuthor(),
                    coverIcon,
                    book.getRating(),
                    actionPanel
                });
            }
        });

        // Muat semua buku saat dashboard dibuka
        List<Book> initialBooks = bookDAO.getAllBooks();
        for (Book book : initialBooks) {
            ImageIcon coverIcon = null;
            try {
                if (book.getCoverImagePath() != null && !book.getCoverImagePath().isEmpty()) {
                    BufferedImage img = ImageIO.read(new File(book.getCoverImagePath()));
                    Image scaledImg = img.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                    coverIcon = new ImageIcon(scaledImg);
                }
            } catch (Exception ex) {
                System.err.println("Error loading image for book " + book.getTitle() + ": " + ex.getMessage());
            }

            // Panel untuk tombol action
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            JButton previewButton = new JButton("Preview");
            JButton borrowButton = new JButton("Pinjam");
            JButton readButton = new JButton("Baca");

            previewButton.setFont(new Font("Arial", Font.PLAIN, 10));
            borrowButton.setFont(new Font("Arial", Font.PLAIN, 10));
            readButton.setFont(new Font("Arial", Font.PLAIN, 10));

            actionPanel.add(previewButton);
            actionPanel.add(borrowButton);
            actionPanel.add(readButton);

            model.addRow(new Object[]{
                book.getIdBook(),
                book.getTitle(),
                book.getAuthor(),
                coverIcon,
                book.getRating(),
                actionPanel
            });
        }

        add(mainPanel);
        setVisible(true);
    }

    // Helper method untuk ambil Book berdasarkan id
    private Book getBookById(int idBook) {
        List<Book> books = bookDAO.getAllBooks();
        for (Book book : books) {
            if (book.getIdBook() == idBook) {
                return book;
            }
        }
        return null;
    }

    // Getter untuk user
    public User getUser() {
        return user;
    }
}