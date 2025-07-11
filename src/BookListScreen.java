import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.io.File;

public class BookListScreen extends JFrame {
    private BookDAO bookDAO;
    private LoanDAO loanDAO;
    private FavoriteDAO favoriteDAO;
    private User currentUser;
    
    private JPanel listPanel;
    private JTextField searchField;

    public BookListScreen(User user) {
        this.currentUser = user;
        this.bookDAO = new BookDAO(DBConnection.getConnection());
        this.loanDAO = new LoanDAO(DBConnection.getConnection());
        this.favoriteDAO = new FavoriteDAO(DBConnection.getConnection());

        setTitle("Daftar Buku Tersedia - " + currentUser.getNama());
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        displayBooks(bookDAO.getAllBooks()); 

        setVisible(true);
    }

    private void initComponents() {
        Color primaryColor = new Color(30, 58, 138);
        Color backgroundColor = new Color(240, 242, 245);
        Color neutralColor = new Color(107, 114, 128);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(backgroundColor);

        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        topPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Daftar Buku Tersedia", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(primaryColor);
        topPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setOpaque(false);
        searchField = new JTextField();
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.addActionListener(e -> performSearch()); 
        
        JButton searchButton = new JButton("Cari");
        searchButton.setBackground(primaryColor);
        searchButton.setForeground(Color.WHITE);
        searchButton.addActionListener(e -> performSearch());

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        topPanel.add(searchPanel, BorderLayout.CENTER);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);
        listPanel.setBorder(BorderFactory.createLineBorder(new Color(224, 224, 224)));

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JButton backButton = new JButton("Kembali ke Dashboard");
        backButton.setBackground(neutralColor);
        backButton.setForeground(Color.WHITE);
        backButton.setPreferredSize(new Dimension(200, 35));
        backButton.addActionListener(e -> dispose());
        
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        bottomButtonPanel.setBackground(backgroundColor);
        bottomButtonPanel.add(backButton);
        mainPanel.add(bottomButtonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void performSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            displayBooks(bookDAO.getAllBooks());
        } else {
            displayBooks(bookDAO.searchBooks(keyword));
        }
    }

    private void displayBooks(List<Book> books) {
        listPanel.removeAll(); 

        if (books == null || books.isEmpty()) {
            JLabel emptyLabel = new JLabel("Buku tidak ditemukan.", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            emptyLabel.setForeground(new Color(107, 114, 128));
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setBackground(Color.WHITE);
            emptyPanel.add(emptyLabel);
            listPanel.add(emptyPanel);
        } else {
            for (Book book : books) {
                listPanel.add(createBookEntryPanel(book));
            }
        }
        listPanel.revalidate();
        listPanel.repaint();
    }
     
    private JPanel createBookEntryPanel(Book book) {
        Color primaryColor = new Color(30, 58, 138); 
        Color secondaryColor = new Color(107, 114, 128);
        Color cardBackgroundColor = Color.WHITE;
        Color successColor = new Color(76, 175, 80); 
        Color neutralColor = new Color(107, 114, 128);
        Color favoriteColor = new Color(220, 53, 69);
        
        JPanel bookEntryPanel = new JPanel(new BorderLayout(15,0)); 
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
                    coverLabel.setText("");
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
        
        JLabel bookIsbnLabel = new JLabel("ISBN: " + (book.getIsbn() != null ? book.getIsbn() : "-"));
        bookIsbnLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        bookIsbnLabel.setForeground(neutralColor);

        JLabel classificationLabel = new JLabel("Klasifikasi: " + (book.getClassificationCode() != null ? book.getClassificationCode() : "-"));
        classificationLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        classificationLabel.setForeground(neutralColor);
        
        JLabel bookRatingLabel = new JLabel(String.format("Rating: %.1f/5.0", book.getRating()));
        bookRatingLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        bookRatingLabel.setForeground(neutralColor);

        bookInfoPanel.add(bookTitleLabel);
        bookInfoPanel.add(bookAuthorLabel);
        bookInfoPanel.add(bookIsbnLabel);
        bookInfoPanel.add(classificationLabel);
        bookInfoPanel.add(bookRatingLabel);
        bookEntryPanel.add(bookInfoPanel, BorderLayout.CENTER);
        
        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5,0)); 
        actionButtonsPanel.setOpaque(false);

        JButton favoriteButton = new JButton();
        updateFavoriteButtonState(favoriteButton, favoriteDAO.isFavorite(currentUser.getIdUser(), book.getIdBook()), favoriteColor, secondaryColor);
        favoriteButton.addActionListener(e -> handleFavoriteAction(favoriteButton, book, favoriteColor, secondaryColor));
        actionButtonsPanel.add(favoriteButton);

        JButton previewButton = new JButton("Detail");
        styleActionButton(previewButton, primaryColor);
        previewButton.addActionListener(e -> new BookDetailScreen(book.getIdBook(), bookDAO, currentUser, favoriteDAO));
        actionButtonsPanel.add(previewButton);

        JButton borrowButton = new JButton("Pinjam");
        styleActionButton(borrowButton, successColor);
        borrowButton.addActionListener(e -> handleBorrowAction(book));
        actionButtonsPanel.add(borrowButton);
        
        JButton readButton = new JButton("Baca");
        styleActionButton(readButton, neutralColor);
        readButton.setEnabled(loanDAO.isLoanApproved(currentUser.getIdUser(), book.getIdBook()));
        readButton.addActionListener(e -> handleReadAction(book));
        actionButtonsPanel.add(readButton);
        
        bookEntryPanel.add(actionButtonsPanel, BorderLayout.EAST);
        
        return bookEntryPanel;
    }

    private void handleFavoriteAction(JButton favoriteButton, Book book, Color favColor, Color notFavColor) {
        boolean isCurrentlyFavorite = favoriteDAO.isFavorite(currentUser.getIdUser(), book.getIdBook());
        boolean success;
        if (isCurrentlyFavorite) {
            success = favoriteDAO.removeFavorite(currentUser.getIdUser(), book.getIdBook());
            if(success) JOptionPane.showMessageDialog(this, "Dihapus dari favorit.");
        } else {
            success = favoriteDAO.addFavorite(currentUser.getIdUser(), book.getIdBook());
            if(success) JOptionPane.showMessageDialog(this, "Ditambahkan ke favorit.");
        }
        if (success) {
            updateFavoriteButtonState(favoriteButton, !isCurrentlyFavorite, favColor, notFavColor);
        } else {
            JOptionPane.showMessageDialog(this, "Aksi favorit gagal.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleBorrowAction(Book book) {
        boolean success = loanDAO.addLoan(currentUser.getIdUser(), book.getIdBook());
        if (success) {
            JOptionPane.showMessageDialog(this, "Pengajuan peminjaman untuk buku \"" + book.getTitle() + "\" berhasil!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Gagal mengajukan peminjaman.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleReadAction(Book book) {
        if (book.getBookFilePath() != null && !book.getBookFilePath().isEmpty()) {
            new PdfReaderScreen(book.getBookFilePath());
        } else {
            JOptionPane.showMessageDialog(this, "File PDF untuk buku ini tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleActionButton(JButton button, Color bgColor) {
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 10));
        button.setMargin(new Insets(4, 10, 4, 10));
    }

    private void updateFavoriteButtonState(JButton favButton, boolean isFavorite, Color favColor, Color notFavColor) {
        if (isFavorite) {
            favButton.setText("Unfav");
            favButton.setBackground(favColor);
            favButton.setToolTipText("Hapus dari Favorit");
        } else {
            favButton.setText("+ Fav");
            favButton.setBackground(notFavColor);
            favButton.setToolTipText("Tambah ke Favorit");
        }
        favButton.setForeground(Color.WHITE);
        favButton.setFocusPainted(false);
        favButton.setFont(new Font("Arial", Font.BOLD, 10));
        favButton.setMargin(new Insets(4, 10, 4, 10));
        favButton.setPreferredSize(new Dimension(65, 28));
    }
}