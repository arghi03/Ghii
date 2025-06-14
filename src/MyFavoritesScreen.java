import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.io.File; 
import java.awt.Desktop; 
import java.io.IOException; 


public class MyFavoritesScreen extends JFrame {
    private User currentUser;
    private FavoriteDAO favoriteDAO;
    private BookDAO bookDAO; 
    private LoanDAO loanDAO; 

    private JPanel listPanel; 
    private JLabel emptyFavoritesLabel; 

    private Color primaryColor = new Color(30, 58, 138);
    private Color secondaryColor = new Color(59, 130, 246);
    private Color backgroundColor = new Color(240, 242, 245);
    private Color cardBackgroundColor = Color.WHITE;
    private Color neutralColor = new Color(107, 114, 128);
    private Color successColor = new Color(76, 175, 80);
    private Color favoriteColor = new Color(220, 53, 69); 

    public MyFavoritesScreen(User user) {
        this.currentUser = user;
        this.favoriteDAO = new FavoriteDAO(DBConnection.getConnection());
        this.bookDAO = new BookDAO(DBConnection.getConnection()); 
        this.loanDAO = new LoanDAO(DBConnection.getConnection()); 

        setTitle("Buku Favorit Saya - " + currentUser.getNama());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        loadFavoriteBooks(); 

        setVisible(true);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(backgroundColor);

        JLabel titleLabel = new JLabel("Buku Favorit Saya", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(primaryColor);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(cardBackgroundColor);
        listPanel.setBorder(BorderFactory.createLineBorder(new Color(224, 224, 224)));
        
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JButton backButton = new JButton("Kembali ke Dashboard");
        styleActionButton(backButton, neutralColor, 180, 35);
        backButton.addActionListener(e -> dispose()); 

        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        bottomButtonPanel.setBackground(backgroundColor);
        bottomButtonPanel.add(backButton);
        mainPanel.add(bottomButtonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    // ✅ Logika dirombak total jadi lebih simpel dan aman
    private void loadFavoriteBooks() {
        // 1. Hapus semua komponen yang ada di listPanel
        listPanel.removeAll();

        // 2. Ambil data buku favorit terbaru
        List<Book> favoriteBooks = favoriteDAO.getUserFavoriteBooks(currentUser.getIdUser());

        // 3. Cek apakah daftar favoritnya kosong
        if (favoriteBooks.isEmpty()) {
            // Jika kosong, tampilkan label "belum ada favorit"
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setOpaque(false);
            emptyFavoritesLabel = new JLabel("Anda belum memiliki buku favorit.");
            emptyFavoritesLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            emptyFavoritesLabel.setForeground(neutralColor);
            emptyPanel.add(emptyFavoritesLabel);
            listPanel.add(emptyPanel);
        } else {
            // Jika ada isinya, loop dan tambahkan setiap buku ke panel
            for (Book book : favoriteBooks) {
                listPanel.add(createBookEntryPanel(book));
                // Tambahkan separator antar buku
                listPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }

        // 4. Perbarui UI
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createBookEntryPanel(Book book) {
        JPanel bookEntryPanel = new JPanel(new BorderLayout(10, 0));
        bookEntryPanel.setBackground(cardBackgroundColor);
        bookEntryPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(224, 224, 224)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // ✅ HAPUS BARIS INI -> Penyebab masalah "gepeng"
        // bookEntryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, bookEntryPanel.getPreferredSize().height + 10));


        JLabel coverLabel = new JLabel();
        coverLabel.setPreferredSize(new Dimension(50, 70));
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

        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actionButtonsPanel.setOpaque(false);

        JButton removeFavoriteButton = new JButton("💔 Hapus"); 
        styleActionButton(removeFavoriteButton, favoriteColor, 100, 30);
        removeFavoriteButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus \"" + book.getTitle() + "\" dari favorit?", "Konfirmasi Hapus Favorit", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = favoriteDAO.removeFavorite(currentUser.getIdUser(), book.getIdBook());
                if (success) {
                    // Muat ulang daftar favorit untuk merefresh tampilan
                    loadFavoriteBooks(); 
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menghapus buku dari favorit.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        actionButtonsPanel.add(removeFavoriteButton);

        JButton detailButton = new JButton("Detail");
        styleActionButton(detailButton, primaryColor, 70, 30);
        detailButton.addActionListener(e -> {
            new BookDetailScreen(book.getIdBook(), bookDAO, currentUser, favoriteDAO).setVisible(true);
        });
        actionButtonsPanel.add(detailButton);
        
        JButton borrowButton = new JButton("Pinjam");
        styleActionButton(borrowButton, successColor, 70, 30);
        borrowButton.addActionListener(e -> {
            boolean success = loanDAO.addLoan(currentUser.getIdUser(), book.getIdBook());
            if (success) {
                JOptionPane.showMessageDialog(this, "Pengajuan peminjaman untuk buku \"" + book.getTitle() + "\" berhasil!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Gagal mengajukan peminjaman untuk buku \"" + book.getTitle() + "\".", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        actionButtonsPanel.add(borrowButton);

        bookEntryPanel.add(actionButtonsPanel, BorderLayout.EAST);
        return bookEntryPanel;
    }

    private void styleActionButton(JButton button, Color bgColor, int width, int height) {
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 10));
        button.setMargin(new Insets(2, 5, 2, 5)); 
        button.setPreferredSize(new Dimension(width, height));
        button.setOpaque(true);
        button.setBorderPainted(false);
    }
}