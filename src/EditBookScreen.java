import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.Map;
import java.util.Vector;

public class EditBookScreen extends JDialog { 
    private BookDAO bookDAO;
    private Book bookToEdit;
    private JTextField titleField, authorField, isbnField;
    private JSpinner ratingSpinner; 
    private JLabel coverImageLabel, bookFileLabel;
    private String coverImagePath, bookFilePath;
    private JComboBox<String> mainClassComboBox;
    private JComboBox<ClassificationItem> subClassComboBox;
    private JTextField specificNumberField;
    
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

        setSize(550, 600);
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
        isbnField.setText(bookToEdit.getIsbn());
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

        selectClassification(bookToEdit.getClassificationCode());
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
        int y=0;
        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("Judul Buku:"), gbc);
        titleField = new JTextField(20);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(titleField, gbc);
        y++; gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Penulis:"), gbc);
        authorField = new JTextField(20);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(authorField, gbc);
        y++; gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 1;
        formPanel.add(new JLabel("ISBN:"), gbc);
        isbnField = new JTextField(20);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(isbnField, gbc);
        y++; gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Klasifikasi DDC:"), gbc);
        JPanel classificationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        classificationPanel.setOpaque(false);
        mainClassComboBox = new JComboBox<>(new Vector<>(DeweyData.getDdcMap().keySet()));
        mainClassComboBox.insertItemAt("-- Pilih Kelas --", 0);
        mainClassComboBox.setSelectedIndex(0);
        subClassComboBox = new JComboBox<>();
        subClassComboBox.setEnabled(false);
        specificNumberField = new JTextField(5);
        classificationPanel.add(mainClassComboBox);
        classificationPanel.add(subClassComboBox);
        classificationPanel.add(specificNumberField);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(classificationPanel, gbc);
        y++; gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Rating (0.0 - 5.0):"), gbc);
        SpinnerNumberModel ratingModel = new SpinnerNumberModel(0.0, 0.0, 5.0, 0.1);
        ratingSpinner = new JSpinner(ratingModel);
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(ratingSpinner, "0.0");
        ratingSpinner.setEditor(editor);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(ratingSpinner, gbc);
        y++; gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Gambar Sampul:"), gbc);
        JButton chooseCoverButton = createStyledButton("Pilih Gambar...", secondaryColor, 120, 30);
        gbc.gridx = 1; gbc.gridwidth = 1;
        formPanel.add(chooseCoverButton, gbc);
        coverImageLabel = new JLabel("Tidak diubah");
        coverImageLabel.setForeground(neutralColor);
        gbc.gridx = 2; gbc.gridwidth = 1;
        formPanel.add(coverImageLabel, gbc);
        y++; gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 1;
        formPanel.add(new JLabel("File Buku (PDF):"), gbc);
        JButton chooseBookFileButton = createStyledButton("Pilih File PDF...", secondaryColor, 120, 30);
        gbc.gridx = 1; gbc.gridwidth = 1;
        formPanel.add(chooseBookFileButton, gbc);
        bookFileLabel = new JLabel("Tidak diubah");
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
        mainClassComboBox.addItemListener(e -> { if (e.getStateChange() == ItemEvent.SELECTED) updateSubClassComboBox(true); });
        chooseCoverButton.addActionListener(e -> chooseFile("Pilih Gambar Sampul", "Image files", new String[]{"jpg", "png", "jpeg"}, coverImageLabel, "cover"));
        chooseBookFileButton.addActionListener(e -> chooseFile("Pilih File Buku", "PDF files", new String[]{"pdf"}, bookFileLabel, "book"));
        saveButton.addActionListener(e -> saveChanges());
        backButton.addActionListener(e -> dispose());
        add(mainPanel);
    }
    
    private void updateSubClassComboBox(boolean clearSpecificField) {
        if (mainClassComboBox.getSelectedIndex() <= 0) {
            subClassComboBox.removeAllItems();
            subClassComboBox.setEnabled(false);
            if(clearSpecificField) specificNumberField.setText("");
            return;
        }
        
        String selectedMainClass = (String) mainClassComboBox.getSelectedItem();
        Map<String, String> subClasses = DeweyData.getDdcMap().get(selectedMainClass);
        
        subClassComboBox.removeAllItems();
        subClassComboBox.addItem(new ClassificationItem("-- Pilih Sub-Kelas --", ""));
        if (subClasses != null) {
            for (Map.Entry<String, String> entry : subClasses.entrySet()) {
                subClassComboBox.addItem(new ClassificationItem(entry.getValue(), entry.getKey()));
            }
        }
        subClassComboBox.setEnabled(true);
        if(clearSpecificField) specificNumberField.setText("");
    }

    private void saveChanges() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String isbn = isbnField.getText().trim();
        String classificationCode = "";
        
        if (subClassComboBox.getSelectedIndex() > 0) {
            ClassificationItem selectedSubClass = (ClassificationItem) subClassComboBox.getSelectedItem();
            String specificNum = specificNumberField.getText().trim();
            if (!specificNum.isEmpty() && !specificNum.startsWith(".")) specificNum = "." + specificNum;
            classificationCode = selectedSubClass.getCode() + specificNum;
        } else if (bookToEdit.getClassificationCode() != null && !bookToEdit.getClassificationCode().isEmpty()) {
            classificationCode = bookToEdit.getClassificationCode();
            String specificNum = specificNumberField.getText().trim();
            if(!specificNum.isEmpty()) {
                 String baseCode = classificationCode.split("\\.")[0];
                 if (!specificNum.startsWith(".")) specificNum = "." + specificNum;
                 classificationCode = baseCode + specificNum;
            }
        }

        if (title.isEmpty() || author.isEmpty() || isbn.isEmpty() || classificationCode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Judul, Penulis, ISBN, dan Klasifikasi tidak boleh kosong!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        bookToEdit.setTitle(title);
        bookToEdit.setAuthor(author);
        bookToEdit.setIsbn(isbn);
        bookToEdit.setClassificationCode(classificationCode);
        bookToEdit.setRating(((Double) ratingSpinner.getValue()).floatValue());
        bookToEdit.setCoverImagePath(coverImagePath);
        bookToEdit.setBookFilePath(bookFilePath);

        if (bookDAO.updateBook(bookToEdit)) {
            JOptionPane.showMessageDialog(this, "Data buku berhasil diperbarui!");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal memperbarui data buku!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void selectClassification(String code) {
        if (code == null || code.isEmpty()) return;
        
        try {
            String mainCodePrefix = code.substring(0, 1) + "00";
            String subCode = code.split("\\.")[0];
            String specificNum = code.contains(".") ? code.substring(code.indexOf('.')) : "";

            for (int i = 0; i < mainClassComboBox.getItemCount(); i++) {
                if (mainClassComboBox.getItemAt(i).startsWith(mainCodePrefix)) {
                    mainClassComboBox.setSelectedIndex(i);
                    break;
                }
            }
            
            updateSubClassComboBox(false);
            
            SwingUtilities.invokeLater(() -> {
                for (int i = 0; i < subClassComboBox.getItemCount(); i++) {
                    if (subClassComboBox.getItemAt(i).getCode().equals(subCode)) {
                        subClassComboBox.setSelectedIndex(i);
                        break;
                    }
                }
                specificNumberField.setText(specificNum);
            });
        } catch (Exception e) {
            System.err.println("Gagal mem-parsing kode klasifikasi: " + code + " - " + e.getMessage());
            specificNumberField.setText(code);
        }
    }

    private void chooseFile(String dialogTitle, String fileDesc, String[] extensions, JLabel label, String fileType) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(dialogTitle);
        fileChooser.setFileFilter(new FileNameExtensionFilter(fileDesc, extensions));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String selectedPath = fileChooser.getSelectedFile().getAbsolutePath();
            if ("cover".equals(fileType)) { coverImagePath = selectedPath; } 
            else if ("book".equals(fileType)) { bookFilePath = selectedPath; }
            label.setText(fileChooser.getSelectedFile().getName()); 
            label.setToolTipText(selectedPath);
            label.setForeground(Color.BLACK);
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

    private static class ClassificationItem {
        private String code;
        private String description;

        public ClassificationItem(String description, String code) {
            this.description = description;
            this.code = code;
        }
        
        public String getCode() { 
            return code; 
        }
        
        @Override
        public String toString() {
            if (code.isEmpty()) return description;
            return code + " - " + description;
        }
    }
}