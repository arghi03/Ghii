import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class EditBookScreen extends JDialog { 
    private BookDAO bookDAO;
    private Book bookToEdit;
    private JTextField titleField, authorField, isbnField; // ✅ FIELD BARU UNTUK ISBN
    private JSpinner ratingSpinner; 
    private JLabel coverImageLabel, bookFileLabel;
    private String coverImagePath, bookFilePath;

    private Color primaryColor = new Color(30, 58, 138); 
    private Color secondaryColor = new Color(59, 130, 246);
    private Color backgroundColor = new Color(245, 245, 245); 
    private Color successColor = new Color(76, 175, 80); 
    private Color neutralColor = new Color(107, 114, 128);

    public EditBookScreen(Frame owner, int bookId) {
        super(owner, "Edit Detail Buku", true);
        
        this.bookDAO = new BookDAO(DBConnection.getConnection());
        this.bookToEdit = bookDAO.getBookById(bookId);

        if (bookToEdit == null) {
            JOptionPane.showMessageDialog(owner, "Gagal memuat data buku dengan ID: " + bookId, "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        setSize(550, 500); // Perbesar sedikit tinggi frame untuk field baru
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(owner);
        setResizable(false);

        initComponents();
        populateForm();

        setVisible(true);
    }
    
    private void populateForm() {
        titleField.setText(bookToEdit.getTitle());
        authorField.setText(bookToEdit.getAuthor());
        isbnField.setText(bookToEdit.getIsbn()); // ✅ ISI FIELD ISBN
        ratingSpinner.setValue((double) bookToEdit.getRating());
        
        this.coverImagePath = bookToEdit.getCoverImagePath();
        this.bookFilePath = bookToEdit.getBookFilePath();

        if (coverImagePath != null && !coverImagePath.isEmpty()) {
            coverImageLabel.setText(new File(coverImagePath).getName());
            coverImageLabel.setForeground(Color.BLACK);
        }
        
        if (bookFilePath != null && !bookFilePath.isEmpty()) {
            bookFileLabel.setText(new File(bookFilePath).getName());
            bookFileLabel.setForeground(Color.BLACK);
        }
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(backgroundColor);

        JLabel titleLabel = new JLabel("Edit Detail Buku", SwingConstants.CENTER);
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

        // ... (UI untuk pilih file tidak berubah)
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

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(backgroundColor);
        JButton saveButton = createStyledButton("Simpan Perubahan", successColor, 160, 35);
        JButton backButton = createStyledButton("Batal", neutralColor, 120, 35);

        buttonPanel.add(saveButton);
        buttonPanel.add(backButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        chooseCoverButton.addActionListener(e -> chooseFile("Pilih Gambar Sampul", "Image files", new String[]{"jpg", "png", "jpeg"}, coverImageLabel, "cover"));
        chooseBookFileButton.addActionListener(e -> chooseFile("Pilih File Buku", "PDF files", new String[]{"pdf"}, bookFileLabel, "book"));
        
        saveButton.addActionListener(e -> saveChanges());
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

    private void saveChanges() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String isbn = isbnField.getText().trim(); // ✅ AMBIL NILAI ISBN
        float rating = ((Double) ratingSpinner.getValue()).floatValue();

        if (title.isEmpty() || author.isEmpty() || isbn.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Judul, penulis, dan ISBN tidak boleh kosong!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // ✅ UPDATE SEMUA FIELD DI OBJEK BUKU
        bookToEdit.setTitle(title);
        bookToEdit.setAuthor(author);
        bookToEdit.setIsbn(isbn);
        bookToEdit.setRating(rating);
        bookToEdit.setCoverImagePath(coverImagePath);
        bookToEdit.setBookFilePath(bookFilePath);

        boolean success = bookDAO.updateBook(bookToEdit);
        if (success) {
            JOptionPane.showMessageDialog(this, "Data buku '" + title + "' berhasil diperbarui!");
            dispose(); // Tutup dialog setelah berhasil
        } else {
            JOptionPane.showMessageDialog(this, "Gagal memperbarui data buku! Cek konsol atau kemungkinan ISBN duplikat.", "Error", JOptionPane.ERROR_MESSAGE);
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