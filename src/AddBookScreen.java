import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;

public class AddBookScreen extends JFrame {
    private BookDAO bookDAO;
    private User currentUser;
    private JTextField titleField, authorField, isbnField; // ✅ FIELD BARU UNTUK ISBN
    private JSpinner ratingSpinner; 
    private JLabel coverImageLabel, bookFileLabel;
    private String coverImagePath, bookFilePath;

    
    private Color primaryColor = new Color(30, 58, 138); 
    private Color secondaryColor = new Color(59, 130, 246);
    private Color backgroundColor = new Color(245, 245, 245); 
    private Color successColor = new Color(76, 175, 80); 
    private Color neutralColor = new Color(107, 114, 128);

    public AddBookScreen(User user) {
        this.currentUser = user;
        this.bookDAO = new BookDAO(DBConnection.getConnection());

        setTitle("Tambah Buku - " + currentUser.getNama());
        setSize(550, 500); // Perbesar sedikit tinggi frame untuk field baru
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();

        setVisible(true);
    }
    
  
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(backgroundColor);

        JLabel titleLabel = new JLabel("Tambah Buku Baru", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(primaryColor);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Judul
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Judul Buku:"), gbc);
        titleField = new JTextField(20);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(titleField, gbc);
        
        // Penulis
        gbc.gridy++;
        gbc.gridx = 0; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Penulis:"), gbc);
        authorField = new JTextField(20);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(authorField, gbc);

        // ✅ TAMBAHKAN INPUT FIELD UNTUK ISBN
        gbc.gridy++;
        gbc.gridx = 0; gbc.gridwidth = 1;
        formPanel.add(new JLabel("ISBN:"), gbc);
        isbnField = new JTextField(20);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(isbnField, gbc);

        // Rating
        gbc.gridy++;
        gbc.gridx = 0; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Rating (0.0 - 5.0):"), gbc);
        SpinnerNumberModel ratingModel = new SpinnerNumberModel(0.0, 0.0, 5.0, 0.1);
        ratingSpinner = new JSpinner(ratingModel);
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(ratingSpinner, "0.0");
        ratingSpinner.setEditor(editor);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(ratingSpinner, gbc);

        // Gambar Sampul
        gbc.gridy++;
        gbc.gridx = 0; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Gambar Sampul:"), gbc);
        JButton chooseCoverButton = createStyledButton("Pilih Gambar...", secondaryColor, 120, 30);
        gbc.gridx = 1; gbc.gridwidth = 1;
        formPanel.add(chooseCoverButton, gbc);
        coverImageLabel = new JLabel("Belum dipilih");
        coverImageLabel.setForeground(neutralColor);
        gbc.gridx = 2; gbc.gridwidth = 1;
        formPanel.add(coverImageLabel, gbc);
        
        // File Buku (PDF)
        gbc.gridy++;
        gbc.gridx = 0; gbc.gridwidth = 1;
        formPanel.add(new JLabel("File Buku (PDF):"), gbc);
        JButton chooseBookFileButton = createStyledButton("Pilih File PDF...", secondaryColor, 120, 30);
        gbc.gridx = 1; gbc.gridwidth = 1;
        formPanel.add(chooseBookFileButton, gbc);
        bookFileLabel = new JLabel("Belum dipilih");
        bookFileLabel.setForeground(neutralColor);
        gbc.gridx = 2; gbc.gridwidth = 1;
        formPanel.add(bookFileLabel, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Panel Tombol
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(backgroundColor);
        JButton saveButton = createStyledButton("Simpan Buku", successColor, 140, 35);
        JButton backButton = createStyledButton("Kembali", neutralColor, 120, 35);

        buttonPanel.add(saveButton);
        buttonPanel.add(backButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        chooseCoverButton.addActionListener(e -> chooseFile("Pilih Gambar Sampul", "Image files", new String[]{"jpg", "png", "jpeg"}, coverImageLabel, "cover"));
        chooseBookFileButton.addActionListener(e -> chooseFile("Pilih File Buku", "PDF files", new String[]{"pdf"}, bookFileLabel, "book"));
        
        saveButton.addActionListener(e -> saveBook());
        backButton.addActionListener(e -> dispose());

        add(mainPanel);
    }
    
    private void chooseFile(String dialogTitle, String fileDesc, String[] extensions, JLabel label, String fileType) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(dialogTitle);
        fileChooser.setFileFilter(new FileNameExtensionFilter(fileDesc, extensions));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String selectedPath = fileChooser.getSelectedFile().getAbsolutePath();
            if ("cover".equals(fileType)) {
                coverImagePath = selectedPath;
            } else if ("book".equals(fileType)) {
                bookFilePath = selectedPath;
            }
            label.setText(fileChooser.getSelectedFile().getName()); 
            label.setToolTipText(selectedPath);
            label.setForeground(Color.BLACK);
        }
    }

    private void saveBook() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String isbn = isbnField.getText().trim(); // ✅ AMBIL NILAI ISBN DARI FIELD
        double ratingValue = (Double) ratingSpinner.getValue();
        float rating = (float) ratingValue;

        // ✅ VALIDASI ISBN
        if (title.isEmpty() || author.isEmpty() || isbn.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Judul, penulis, dan ISBN tidak boleh kosong!", "Error", JOptionPane.ERROR_MESSAGE);
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
        // ✅ MASUKKAN ISBN SAAT MEMBUAT OBJEK BUKU BARU
        Book book = new Book(newBookId, title, author, isbn, coverImagePath, bookFilePath, rating);
        boolean success = bookDAO.addBook(book);
        
        if (success) {
            JOptionPane.showMessageDialog(this, "Buku '" + title + "' berhasil ditambahkan!");
            // Bersihkan semua field
            titleField.setText("");
            authorField.setText("");
            isbnField.setText(""); // ✅ BERSIHKAN FIELD ISBN
            ratingSpinner.setValue(0.0);
            coverImageLabel.setText("Belum dipilih");
            coverImageLabel.setForeground(neutralColor);
            bookFileLabel.setText("Belum dipilih");
            bookFileLabel.setForeground(neutralColor);
            coverImagePath = null;
            bookFilePath = null;
        } else {
            JOptionPane.showMessageDialog(this, "Gagal menambahkan buku! Cek konsol untuk detail atau kemungkinan ISBN sudah ada.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton createStyledButton(String text, Color bgColor, int width, int height) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(width, height));
        button.setOpaque(true);
        button.setBorderPainted(false);
        return button;
    }
}