    import javax.swing.*;
    import java.awt.*;
    import java.util.List;
    import java.awt.Desktop;
    import java.io.File;
    import java.io.IOException;
    // Pastikan User, Book, BookDAO, LoanDAO, FavoriteDAO, DBConnection diimport jika berada di package berbeda
    // import com.perpustakaan.model.User;
    // import com.perpustakaan.model.Book;
    // import com.perpustakaan.dao.BookDAO;
    // import com.perpustakaan.dao.LoanDAO;
    // import com.perpustakaan.dao.FavoriteDAO; // Tambahkan ini
    // import com.perpustakaan.util.DBConnection;


    public class BookListScreen extends JFrame {
        private BookDAO bookDAO;
        private LoanDAO loanDAO;
        private FavoriteDAO favoriteDAO; // DAO untuk fitur favorit
        private User currentUser;

        public BookListScreen(User user) {
            this.currentUser = user;
            // Inisialisasi DAO
            this.bookDAO = new BookDAO(DBConnection.getConnection());
            this.loanDAO = new LoanDAO(DBConnection.getConnection());
            this.favoriteDAO = new FavoriteDAO(DBConnection.getConnection()); // Inisialisasi FavoriteDAO

            setTitle("Daftar Buku Tersedia - " + currentUser.getNama());
            setSize(800, 600); // Perbesar sedikit untuk tombol tambahan
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setLocationRelativeTo(null);

            // Palet Warna
            Color primaryColor = new Color(30, 58, 138); 
            Color secondaryColor = new Color(59, 130, 246); 
            Color backgroundColor = new Color(240, 242, 245); 
            Color successColor = new Color(76, 175, 80); 
            Color neutralColor = new Color(107, 114, 128);
            Color cardBackgroundColor = Color.WHITE;
            Color favoriteColor = new Color(220, 53, 69); // Warna untuk tombol favorit (merah)

            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            mainPanel.setBackground(backgroundColor);

            JLabel titleLabel = new JLabel("Daftar Buku Tersedia", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
            titleLabel.setForeground(primaryColor);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
            mainPanel.add(titleLabel, BorderLayout.NORTH);

            JPanel listPanel = new JPanel();
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            listPanel.setBackground(cardBackgroundColor); 
            listPanel.setBorder(BorderFactory.createLineBorder(new Color(224,224,224)));

            List<Book> books = bookDAO.getAllBooks(); 
            if (books.isEmpty()) {
                JLabel emptyLabel = new JLabel("Tidak ada buku yang tersedia saat ini.", SwingConstants.CENTER);
                emptyLabel.setFont(new Font("Arial", Font.ITALIC, 16));
                emptyLabel.setForeground(neutralColor);
                emptyLabel.setPreferredSize(new Dimension(500, 100));
                listPanel.add(Box.createVerticalGlue());
                listPanel.add(emptyLabel);
                listPanel.add(Box.createVerticalGlue());
            } else {
                for (Book book : books) {
                    JPanel bookEntryPanel = new JPanel(new BorderLayout(10,0)); 
                    bookEntryPanel.setBackground(cardBackgroundColor);
                    bookEntryPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0,0,1,0, new Color(224,224,224)), 
                        BorderFactory.createEmptyBorder(10,10,10,10) 
                    ));

                    JLabel coverLabel = new JLabel();
                    coverLabel.setPreferredSize(new Dimension(50,70)); 
                    coverLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    coverLabel.setBorder(BorderFactory.createLineBorder(neutralColor));
                    if (book.getCoverImagePath() != null && !book.getCoverImagePath().isEmpty()) {
                        try {
                            File coverFile = new File(book.getCoverImagePath());
                            if (coverFile.exists() && !coverFile.isDirectory()) {
                                ImageIcon imageIcon = new ImageIcon(book.getCoverImagePath());
                                Image image = imageIcon.getImage().getScaledInstance(50, 70, Image.SCALE_SMOOTH);
                                coverLabel.setIcon(new ImageIcon(image));
                                coverLabel.setBorder(null); 
                            } else {
                                coverLabel.setText("X");
                            }
                        } catch (Exception e) {
                            coverLabel.setText("Err");
                            System.err.println("Error loading cover for BookListScreen: " + book.getCoverImagePath() + " - " + e.getMessage());
                        }
                    } else {
                        coverLabel.setText("N/A");
                    }
                    bookEntryPanel.add(coverLabel, BorderLayout.WEST);

                    JPanel bookInfoPanel = new JPanel();
                    bookInfoPanel.setLayout(new BoxLayout(bookInfoPanel, BoxLayout.Y_AXIS));
                    bookInfoPanel.setOpaque(false); 

                    JLabel bookTitleLabel = new JLabel(book.getTitle());
                    bookTitleLabel.setFont(new Font("Arial", Font.BOLD, 14));
                    bookTitleLabel.setForeground(primaryColor);

                    JLabel bookAuthorLabel = new JLabel("Penulis: " + book.getAuthor());
                    bookAuthorLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                    bookAuthorLabel.setForeground(neutralColor);
                    
                    JLabel bookRatingLabel = new JLabel(String.format("Rating: %.1f/5.0", book.getRating()));
                    bookRatingLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                    bookRatingLabel.setForeground(neutralColor);

                    bookInfoPanel.add(bookTitleLabel);
                    bookInfoPanel.add(bookAuthorLabel);
                    bookInfoPanel.add(bookRatingLabel);
                    bookEntryPanel.add(bookInfoPanel, BorderLayout.CENTER);

                    JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5,0)); 
                    actionButtonsPanel.setOpaque(false);

                    JButton favoriteButton = new JButton();
                    final boolean isCurrentlyFavorite = favoriteDAO.isFavorite(currentUser.getIdUser(), book.getIdBook());
                    updateFavoriteButtonState(favoriteButton, isCurrentlyFavorite, favoriteColor, secondaryColor);

                    favoriteButton.addActionListener(e -> {
                        boolean isNowFavorite = favoriteDAO.isFavorite(currentUser.getIdUser(), book.getIdBook());
                        boolean success;
                        if (isNowFavorite) {
                            success = favoriteDAO.removeFavorite(currentUser.getIdUser(), book.getIdBook());
                            if (success) {
                                JOptionPane.showMessageDialog(this, "\"" + book.getTitle() + "\" dihapus dari favorit.", "Favorit Dihapus", JOptionPane.INFORMATION_MESSAGE);
                                updateFavoriteButtonState(favoriteButton, false, favoriteColor, secondaryColor);
                            } else {
                                JOptionPane.showMessageDialog(this, "Gagal menghapus dari favorit.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            success = favoriteDAO.addFavorite(currentUser.getIdUser(), book.getIdBook());
                            if (success) {
                                JOptionPane.showMessageDialog(this, "\"" + book.getTitle() + "\" ditambahkan ke favorit.", "Favorit Ditambahkan", JOptionPane.INFORMATION_MESSAGE);
                                updateFavoriteButtonState(favoriteButton, true, favoriteColor, secondaryColor);
                            } else {
                                JOptionPane.showMessageDialog(this, "Gagal menambahkan ke favorit.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    });
                    actionButtonsPanel.add(favoriteButton); 

                    JButton previewButton = new JButton("Detail");
                    styleActionButton(previewButton, primaryColor);
                    previewButton.addActionListener(e -> {
                        
                        new BookDetailScreen(book.getIdBook(), bookDAO, currentUser, favoriteDAO).setVisible(true);
                    });

                    JButton borrowButton = new JButton("Pinjam");
                    styleActionButton(borrowButton, successColor);
                    borrowButton.addActionListener(e -> {
                        boolean success = loanDAO.addLoan(currentUser.getIdUser(), book.getIdBook());
                        if (success) {
                            JOptionPane.showMessageDialog(this, "Pengajuan peminjaman untuk buku \"" + book.getTitle() + "\" berhasil!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(this, "Gagal mengajukan peminjaman untuk buku \"" + book.getTitle() + "\". Cek konsol untuk detail.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    });

                    JButton readButton = new JButton("Baca");
                    styleActionButton(readButton, secondaryColor);
                    if (loanDAO.isLoanApproved(currentUser.getIdUser(), book.getIdBook())) {
                        readButton.setEnabled(true);
                        readButton.addActionListener(e -> {
                            if (book.getBookFilePath() != null && !book.getBookFilePath().isEmpty()) {
                                try {
                                    File pdfFile = new File(book.getBookFilePath());
                                    if (pdfFile.exists()) {
                                        Desktop.getDesktop().open(pdfFile);
                                    } else {
                                        JOptionPane.showMessageDialog(this, "File PDF tidak ditemukan di: " + book.getBookFilePath(), "Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                } catch (IOException ex) {
                                    JOptionPane.showMessageDialog(this, "Gagal membuka file PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                    ex.printStackTrace();
                                }
                            } else {
                                JOptionPane.showMessageDialog(this, "File buku (PDF) tidak tersedia untuk buku ini.", "Info", JOptionPane.INFORMATION_MESSAGE);
                            }
                        });
                    } else {
                        readButton.setEnabled(false);
                    }

                    actionButtonsPanel.add(previewButton);
                    actionButtonsPanel.add(borrowButton);
                    actionButtonsPanel.add(readButton);
                    bookEntryPanel.add(actionButtonsPanel, BorderLayout.EAST);
                    
                    listPanel.add(bookEntryPanel);
                }
            }

            JScrollPane scrollPane = new JScrollPane(listPanel);
            scrollPane.setBorder(BorderFactory.createEmptyBorder()); 
            mainPanel.add(scrollPane, BorderLayout.CENTER);

            JButton backButton = new JButton("Kembali ke Dashboard");
            styleActionButton(backButton, neutralColor); 
            backButton.setPreferredSize(new Dimension(200, 35)); 

            backButton.addActionListener(e -> {
                dispose(); 
            });
            
            JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
            bottomButtonPanel.setBackground(backgroundColor);
            bottomButtonPanel.add(backButton);
            mainPanel.add(bottomButtonPanel, BorderLayout.SOUTH);

            add(mainPanel);
            setVisible(true); 
        }

        private void styleActionButton(JButton button, Color bgColor) {
            button.setBackground(bgColor);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setFont(new Font("Arial", Font.BOLD, 10)); 
            button.setMargin(new Insets(2, 8, 2, 8)); 
        }

        private void updateFavoriteButtonState(JButton favButton, boolean isFavorite, Color favColor, Color notFavColor) {
            if (isFavorite) {
                favButton.setText("❤️ Hapus Favorit"); 
                favButton.setBackground(favColor); 
                favButton.setToolTipText("Hapus dari daftar favorit Anda");
            } else {
                favButton.setText("🤍 Tambah Favorit"); 
                favButton.setBackground(notFavColor); 
                favButton.setToolTipText("Tambahkan ke daftar favorit Anda");
            }
            favButton.setForeground(Color.WHITE);
            favButton.setFocusPainted(false);
            favButton.setFont(new Font("Arial", Font.BOLD, 10));
            favButton.setMargin(new Insets(2, 8, 2, 8));
        }

    }
