import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class BookListScreen extends JFrame {
    private BookDAO bookDAO;
    private LoanDAO loanDAO;
    private User currentUser;

    public BookListScreen(User user) {
        this.currentUser = user;
        this.bookDAO = new BookDAO(DBConnection.getConnection());
        this.loanDAO = new LoanDAO(DBConnection.getConnection());

        setTitle("Daftar Buku - " + currentUser.getNama());
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        Color primaryColor = new Color(30, 58, 138); // Biru tua (#1E3A8A)
        Color secondaryColor = new Color(59, 130, 246); // Biru terang (#3B82F6)
        Color backgroundColor = new Color(245, 245, 245); // Abu-abu terang (#F5F5F5)
        Color successColor = new Color(76, 175, 80); // Hijau (#4CAF50)
        Color neutralColor = new Color(107, 114, 128); // Abu-abu (#6B7280)

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(backgroundColor);

        JLabel titleLabel = new JLabel("Daftar Buku Tersedia", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(primaryColor);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(backgroundColor);

        List<Book> books = bookDAO.getAllBooks();
        for (Book book : books) {
            JPanel bookPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            bookPanel.setBackground(backgroundColor);

            JLabel bookLabel = new JLabel("ID: " + book.getIdBook() + " | Judul: " + book.getTitle() + " | Penulis: " + book.getAuthor());
            bookLabel.setForeground(primaryColor);

            JLabel coverLabel = new JLabel();
            if (book.getCoverImagePath() != null && !book.getCoverImagePath().isEmpty()) {
                try {
                    ImageIcon imageIcon = new ImageIcon(book.getCoverImagePath());
                    Image image = imageIcon.getImage().getScaledInstance(50, 70, Image.SCALE_SMOOTH);
                    coverLabel.setIcon(new ImageIcon(image));
                } catch (Exception e) {
                    coverLabel.setText("[Gambar Tidak Ditemukan]");
                    coverLabel.setForeground(neutralColor);
                }
            } else {
                coverLabel.setText("[Tanpa Sampul]");
                coverLabel.setForeground(neutralColor);
            }

            JButton previewButton = new JButton("Preview");
            previewButton.setBackground(primaryColor);
            previewButton.setForeground(Color.WHITE);
            previewButton.setFocusPainted(false);
            previewButton.addActionListener(e -> {
                if (book.getCoverImagePath() != null && !book.getCoverImagePath().isEmpty()) {
                    try {
                        ImageIcon imageIcon = new ImageIcon(book.getCoverImagePath());
                        Image image = imageIcon.getImage().getScaledInstance(300, 400, Image.SCALE_SMOOTH);
                        JLabel previewLabel = new JLabel(new ImageIcon(image));
                        JOptionPane.showMessageDialog(this, previewLabel, "Preview Sampul - " + book.getTitle(), JOptionPane.PLAIN_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Gagal memuat preview!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Tidak ada sampul untuk dilihat!", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            });

            JButton borrowButton = new JButton("Pinjam");
            borrowButton.setBackground(successColor);
            borrowButton.setForeground(Color.WHITE);
            borrowButton.setFocusPainted(false);
            borrowButton.addActionListener(e -> {
                boolean success = loanDAO.addLoan(currentUser.getIdUser(), book.getIdBook());
                if (success) {
                    JOptionPane.showMessageDialog(this, "Peminjaman untuk buku \"" + book.getTitle() + "\" berhasil diajukan!");
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal mengajukan peminjaman!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            JButton readButton = new JButton("Baca");
            readButton.setBackground(secondaryColor);
            readButton.setForeground(Color.WHITE);
            readButton.setFocusPainted(false);
            if (loanDAO.isLoanApproved(currentUser.getIdUser(), book.getIdBook())) {
                readButton.addActionListener(e -> {
                    if (book.getBookFilePath() != null && !book.getBookFilePath().isEmpty()) {
                        try {
                            File pdfFile = new File(book.getBookFilePath());
                            Desktop.getDesktop().open(pdfFile);
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(this, "Gagal membuka PDF!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "File buku tidak tersedia!", "Info", JOptionPane.INFORMATION_MESSAGE);
                    }
                });
            } else {
                readButton.setEnabled(false);
            }

            bookPanel.add(coverLabel);
            bookPanel.add(bookLabel);
            bookPanel.add(previewButton);
            bookPanel.add(borrowButton);
            bookPanel.add(readButton);
            listPanel.add(bookPanel);
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JButton backButton = new JButton("Kembali");
        backButton.setBackground(neutralColor);
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> {
            new Dashboard(currentUser.getNama(), currentUser.getEmail(), currentUser.getIdRole()).setVisible(true);
            dispose();
        });
        mainPanel.add(backButton, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }
}