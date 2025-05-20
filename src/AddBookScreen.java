import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;

public class AddBookScreen extends JFrame {
    private BookDAO bookDAO;
    private User currentUser;
    private JTextField titleField, authorField;
    private JLabel coverImageLabel, bookFileLabel;
    private String coverImagePath, bookFilePath;

    public AddBookScreen(User user) {
        this.currentUser = user;
        this.bookDAO = new BookDAO(DBConnection.getConnection());

        setTitle("Tambah Buku - " + currentUser.getNama());
        setSize(450, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        Color primaryColor = new Color(30, 58, 138); // Biru tua (#1E3A8A)
        Color backgroundColor = new Color(245, 245, 245); // Abu-abu terang (#F5F5F5)
        Color successColor = new Color(76, 175, 80); // Hijau (#4CAF50)
        Color neutralColor = new Color(107, 114, 128); // Abu-abu (#6B7280)

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(backgroundColor);

        JLabel titleLabel = new JLabel("Tambah Buku Baru", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(primaryColor);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBackground(backgroundColor);

        JLabel titleFieldLabel = new JLabel("Judul:");
        titleFieldLabel.setForeground(primaryColor);
        titleField = new JTextField();
        titleField.setBorder(BorderFactory.createLineBorder(neutralColor, 1));

        JLabel authorFieldLabel = new JLabel("Penulis:");
        authorFieldLabel.setForeground(primaryColor);
        authorField = new JTextField();
        authorField.setBorder(BorderFactory.createLineBorder(neutralColor, 1));

        JLabel coverImageFieldLabel = new JLabel("Gambar Sampul:");
        coverImageFieldLabel.setForeground(primaryColor);
        JButton chooseCoverButton = new JButton("Pilih Gambar");
        chooseCoverButton.setBackground(primaryColor);
        chooseCoverButton.setForeground(Color.WHITE);
        chooseCoverButton.setFocusPainted(false);
        chooseCoverButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "png"));
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                coverImagePath = fileChooser.getSelectedFile().getAbsolutePath();
                coverImageLabel.setText("Dipilih: " + coverImagePath);
            }
        });
        coverImageLabel = new JLabel("Belum dipilih");
        coverImageLabel.setForeground(neutralColor);

        JLabel bookFileFieldLabel = new JLabel("File Buku (PDF):");
        bookFileFieldLabel.setForeground(primaryColor);
        JButton chooseBookFileButton = new JButton("Pilih File");
        chooseBookFileButton.setBackground(primaryColor);
        chooseBookFileButton.setForeground(Color.WHITE);
        chooseBookFileButton.setFocusPainted(false);
        chooseBookFileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("PDF files", "pdf"));
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                bookFilePath = fileChooser.getSelectedFile().getAbsolutePath();
                bookFileLabel.setText("Dipilih: " + bookFilePath);
            }
        });
        bookFileLabel = new JLabel("Belum dipilih");
        bookFileLabel.setForeground(neutralColor);

        formPanel.add(titleFieldLabel);
        formPanel.add(titleField);
        formPanel.add(authorFieldLabel);
        formPanel.add(authorField);
        formPanel.add(coverImageFieldLabel);
        JPanel coverPanel = new JPanel(new BorderLayout(5, 0));
        coverPanel.setBackground(backgroundColor);
        coverPanel.add(chooseCoverButton, BorderLayout.WEST);
        coverPanel.add(coverImageLabel, BorderLayout.CENTER);
        formPanel.add(coverPanel);
        formPanel.add(bookFileFieldLabel);
        JPanel bookFilePanel = new JPanel(new BorderLayout(5, 0));
        bookFilePanel.setBackground(backgroundColor);
        bookFilePanel.add(chooseBookFileButton, BorderLayout.WEST);
        bookFilePanel.add(bookFileLabel, BorderLayout.CENTER);
        formPanel.add(bookFilePanel);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(backgroundColor);
        JButton saveButton = new JButton("Simpan");
        saveButton.setBackground(successColor);
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        JButton backButton = new JButton("Kembali");
        backButton.setBackground(neutralColor);
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);

        saveButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            if (title.isEmpty() || author.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Judul dan penulis tidak boleh kosong!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (coverImagePath == null || coverImagePath.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Silakan pilih gambar sampul!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (bookFilePath == null || bookFilePath.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Silakan pilih file PDF buku!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int newBookId = bookDAO.getNewBookId();
            Book book = new Book(newBookId, title, author, coverImagePath, bookFilePath);
            boolean success = bookDAO.addBook(book);
            if (success) {
                JOptionPane.showMessageDialog(this, "Buku berhasil ditambahkan!");
                titleField.setText("");
                authorField.setText("");
                coverImageLabel.setText("Belum dipilih");
                bookFileLabel.setText("Belum dipilih");
                coverImagePath = null;
                bookFilePath = null;
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menambahkan buku!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        backButton.addActionListener(e -> {
            new Dashboard(currentUser.getNama(), currentUser.getEmail(), currentUser.getIdRole()).setVisible(true);
            dispose();
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(backButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }
}