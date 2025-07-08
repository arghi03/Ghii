import javax.swing.*;
import java.awt.*;
import java.io.File;

public class BookDetailScreen extends JFrame {
    private BookDAO bookDAO;
    private LoanDAO loanDAO;
    private FavoriteDAO favoriteDAO;
    private User currentUser;
    private Book currentBook;

    private Color primaryColor = new Color(30, 58, 138);
    private Color secondaryColor = new Color(59, 130, 246);
    private Color successColor = new Color(76, 175, 80);
    private Color favoriteActiveColor = new Color(220, 53, 69);
    private Color backgroundColor = Color.WHITE;
    private Color neutralColor = new Color(107, 114, 128);
    private Color labelColor = new Color(100, 100, 100);
    private Color textColor = new Color(33, 33, 33);

    private JButton favoriteButton; 

    public BookDetailScreen(int idBook, BookDAO bookDAO, User currentUser, FavoriteDAO favoriteDAO) {
        this.bookDAO = bookDAO;
        this.currentUser = currentUser;
        this.favoriteDAO = favoriteDAO;
        this.loanDAO = new LoanDAO(DBConnection.getConnection());
        this.currentBook = this.bookDAO.getBookById(idBook); 

        if (currentBook != null) {
            setTitle("Detail Buku - " + currentBook.getTitle());
        } else {
            setTitle("Detail Buku - Tidak Ditemukan");
        }
        
        setSize(500, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents(); 

        if (currentBook != null) {
            updateFavoriteButtonState(); 
        }
        
        setVisible(true);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(backgroundColor);

        if (currentBook != null) {
            JLabel titleLabel = new JLabel(currentBook.getTitle());
            titleLabel.setFont(new Font("Arial", Font.BOLD, 22)); 
            titleLabel.setForeground(textColor);
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT); 
            mainPanel.add(titleLabel);
            mainPanel.add(Box.createVerticalStrut(15)); 

            JLabel coverImageLabel = new JLabel();
            coverImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            if (currentBook.getCoverImagePath() != null && !currentBook.getCoverImagePath().isEmpty()) {
                try {
                    File coverFile = new File(currentBook.getCoverImagePath());
                    if (coverFile.exists() && !coverFile.isDirectory()) {
                        ImageIcon coverIcon = new ImageIcon(currentBook.getCoverImagePath());
                        Image image = coverIcon.getImage().getScaledInstance(200, 300, Image.SCALE_SMOOTH); 
                        coverImageLabel.setIcon(new ImageIcon(image));
                    } else {
                        setPlaceholderOrErrorText(coverImageLabel, "Gambar sampul tidak ditemukan.");
                    }
                } catch (Exception e) {
                    setPlaceholderOrErrorText(coverImageLabel, "Gagal memuat gambar sampul.");
                    System.err.println("Error loading cover image for BookDetailScreen: " + e.getMessage());
                }
            } else {
                setPlaceholderOrErrorText(coverImageLabel, "Sampul tidak tersedia.");
            }
            mainPanel.add(coverImageLabel);
            mainPanel.add(Box.createVerticalStrut(20)); 

            Font detailHeaderFont = new Font("Arial", Font.BOLD, 11);
            Font detailValueFont = new Font("Arial", Font.PLAIN, 14);
            
            mainPanel.add(createDetailEntry("PENULIS", currentBook.getAuthor(), detailHeaderFont, detailValueFont));
            mainPanel.add(Box.createVerticalStrut(10));
            mainPanel.add(createDetailEntry("ISBN", currentBook.getIsbn(), detailHeaderFont, detailValueFont));
            mainPanel.add(Box.createVerticalStrut(10));
            mainPanel.add(createDetailEntry("RATING", String.format("%.1f / 5.0", currentBook.getRating()), detailHeaderFont, detailValueFont));
            mainPanel.add(Box.createVerticalStrut(20)); 

            mainPanel.add(Box.createVerticalGlue()); 
            
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
            actionPanel.setBackground(backgroundColor); 

            favoriteButton = new JButton(); 
            favoriteButton.addActionListener(e -> toggleFavoriteStatus());
            
            JButton readButton = new JButton("Baca Buku");
            styleActionButton(readButton, successColor, 120, 35);
            readButton.addActionListener(e -> {
                if (loanDAO.isLoanApproved(currentUser.getIdUser(), currentBook.getIdBook())) {
                    new PdfReaderScreen(currentBook.getBookFilePath());
                } else {
                    JOptionPane.showMessageDialog(this, "Anda harus meminjam buku ini dan menunggu persetujuan untuk membacanya.", "Akses Ditolak", JOptionPane.WARNING_MESSAGE);
                }
            });

            JButton backButton = new JButton("Kembali");
            styleActionButton(backButton, neutralColor, 100, 35);
            backButton.addActionListener(e -> dispose()); 
            
            actionPanel.add(favoriteButton);
            actionPanel.add(readButton);
            actionPanel.add(backButton);
            
            mainPanel.add(actionPanel);

        } else {
            JLabel errorLabel = new JLabel("Detail buku tidak dapat ditemukan.");
            errorLabel.setFont(new Font("Arial", Font.BOLD, 16));
            errorLabel.setForeground(Color.RED);
            errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            mainPanel.add(errorLabel);
        }
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); 
        add(scrollPane);
    }
    
    // ✅✅✅ SEMUA METHOD HELPER DI BAWAH INI SUDAH DIKEMBALIKAN ISINYA ✅✅✅

    private void setPlaceholderOrErrorText(JLabel label, String text) {
        label.setText("<html><div style='text-align: center;'>" + text + "</div></html>");
        label.setPreferredSize(new Dimension(200, 300)); 
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    }

    private JPanel createDetailEntry(String labelText, String valueText, Font headerFont, Font valueFont) {
        JPanel entryPanel = new JPanel();
        entryPanel.setLayout(new BoxLayout(entryPanel, BoxLayout.Y_AXIS));
        entryPanel.setBackground(backgroundColor); 
        entryPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblHeader = new JLabel(labelText);
        lblHeader.setFont(headerFont);
        lblHeader.setForeground(labelColor);
        lblHeader.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblValue = new JLabel(valueText != null && !valueText.isEmpty() ? "<html><div style='text-align: center;'>" + valueText + "</div></html>" : "-");
        lblValue.setFont(valueFont);
        lblValue.setForeground(textColor);
        lblValue.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        entryPanel.add(lblHeader);
        entryPanel.add(Box.createRigidArea(new Dimension(0,3)));
        entryPanel.add(lblValue);
        return entryPanel;
    }
    
    private void styleActionButton(JButton button, Color bgColor, int width, int height) {
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(width, height));
        Color originalBgColor = bgColor; 
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(originalBgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalBgColor);
            }
        });
    }

    private void updateFavoriteButtonState() {
        if (currentBook == null || favoriteDAO == null || currentUser == null || favoriteButton == null) return;

        boolean isFav = favoriteDAO.isFavorite(currentUser.getIdUser(), currentBook.getIdBook());
        if (isFav) {
            favoriteButton.setText("❤️ Hapus Favorit");
            styleActionButton(favoriteButton, favoriteActiveColor, 150, 35); 
            favoriteButton.setToolTipText("Hapus dari daftar favorit Anda");
        } else {
            favoriteButton.setText("🤍 Tambah Favorit");
            styleActionButton(favoriteButton, secondaryColor, 160, 35); 
            favoriteButton.setToolTipText("Tambahkan ke daftar favorit Anda");
        }
    }

    private void toggleFavoriteStatus() {
        if (currentBook == null || favoriteDAO == null || currentUser == null) return;

        boolean isCurrentlyFavorite = favoriteDAO.isFavorite(currentUser.getIdUser(), currentBook.getIdBook());
        boolean success;
        if (isCurrentlyFavorite) {
            success = favoriteDAO.removeFavorite(currentUser.getIdUser(), currentBook.getIdBook());
            if (success) {
                JOptionPane.showMessageDialog(this, "\"" + currentBook.getTitle() + "\" dihapus dari favorit.", "Favorit Dihapus", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus dari favorit.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            success = favoriteDAO.addFavorite(currentUser.getIdUser(), currentBook.getIdBook());
            if (success) {
                JOptionPane.showMessageDialog(this, "\"" + currentBook.getTitle() + "\" ditambahkan ke favorit.", "Favorit Ditambahkan", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menambahkan ke favorit.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        if (success) {
            updateFavoriteButtonState(); 
        }
    }
}