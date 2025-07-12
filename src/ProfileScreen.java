import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// ✅✅✅ PERUBAHAN: extends JFrame -> extends JPanel
public class ProfileScreen extends JPanel {
    private User currentUser;
    private UserDAO userDAO; 

    private JLabel lblNameValue;
    private JTextField txtName, txtNim, txtEmail, txtPhone;
    private JButton btnEdit, btnSave;
    
    // Tombol 'Back' tidak lagi dibutuhkan
    // private JButton btnBack;
    
    private boolean isEditMode = false;

    private Color primaryColor = new Color(30, 58, 138); 
    private Color successColor = new Color(76, 175, 80);  
    private Color secondaryColor = new Color(117, 117, 117); 
    private Color backgroundColor = new Color(240, 242, 245); 
    private Color cardColor = Color.WHITE;
    private Color textColor = new Color(33, 33, 33);
    private Color labelColor = new Color(100, 100, 100); 

    public ProfileScreen(User user) {
        this.currentUser = user;
        this.userDAO = new UserDAO(DBConnection.getConnection()); 

        // ❌ HAPUS KODE PENGATURAN FRAME (setTitle, setSize, dll.)

        initComponents();
        loadUserData(); 

        // ❌ HAPUS setVisible(true)
    }

    private void initComponents() {
        // ✅✅✅ PERUBAHAN: Langsung atur layout untuk 'this'
        setLayout(new BorderLayout(0, 20)); 
        setBackground(backgroundColor);
        setBorder(new EmptyBorder(20, 20, 20, 20)); 

        JPanel headerPanel = createCardPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel nameHeaderTitleLabel = createTitleLabel("NAMA PENGGUNA");
        lblNameValue = new JLabel(currentUser.getNama());
        lblNameValue.setFont(new Font("Arial", Font.BOLD, 22));
        lblNameValue.setForeground(textColor);
        lblNameValue.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtName = new JTextField(currentUser.getNama(), 20);
        txtName.setFont(new Font("Arial", Font.BOLD, 20));
        txtName.setHorizontalAlignment(JTextField.CENTER);
        txtName.setVisible(false);
        txtName.setMaximumSize(new Dimension(300, txtName.getPreferredSize().height + 5));
        txtName.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        headerPanel.add(nameHeaderTitleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        headerPanel.add(lblNameValue);
        headerPanel.add(txtName);

        JPanel infoPanel = createCardPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        txtNim = createInfoTextField(currentUser.getNim());
        txtEmail = createInfoTextField(currentUser.getEmail());
        txtPhone = createInfoTextField(currentUser.getNomorTelepon());

        infoPanel.add(createDetailEntry("NIM", txtNim));
        infoPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        infoPanel.add(createDetailEntry("EMAIL", txtEmail));
        infoPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        infoPanel.add(createDetailEntry("NOMOR TELEPON", txtPhone));
        infoPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        infoPanel.add(createInfoPanel("ROLE", getRoleName(currentUser.getIdRole())));
 
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        actionsPanel.setBackground(backgroundColor);

        btnEdit = createStyledButton("Edit Profil", primaryColor, 150, 40);
        btnSave = createStyledButton("Simpan", successColor, 160, 40);
        btnSave.setVisible(false);
        
        // ❌ Tombol "Kembali" dihapus dari panel
        // btnBack = createStyledButton("Kembali", secondaryColor, 180, 40);

        actionsPanel.add(btnEdit);
        actionsPanel.add(btnSave);
        // actionsPanel.add(btnBack);
        
        add(headerPanel, BorderLayout.NORTH);
        add(infoPanel, BorderLayout.CENTER);
        add(actionsPanel, BorderLayout.SOUTH);  

        btnEdit.addActionListener(e -> toggleEditMode(true));
        btnSave.addActionListener(e -> saveUserProfile());
        // ❌ Action listener untuk tombol kembali dihapus
        // btnBack.addActionListener(e -> dispose());
    }
 
    private void toggleEditMode(boolean isEditing) {
        this.isEditMode = isEditing;
        lblNameValue.setVisible(!isEditing);
        txtName.setVisible(isEditing);
        txtNim.setEditable(isEditing);
        txtEmail.setEditable(isEditing);
        txtPhone.setEditable(isEditing);

        Color fieldBgColor = isEditing ? Color.WHITE : cardColor;
        txtNim.setBackground(fieldBgColor);
        txtEmail.setBackground(fieldBgColor);
        txtPhone.setBackground(fieldBgColor);
        
        btnEdit.setVisible(!isEditing);
        btnSave.setVisible(isEditing);

        if (isEditing) {
            txtName.requestFocusInWindow();
        }
    }

    private void saveUserProfile() {
        String newName = txtName.getText().trim();
        String newNim = txtNim.getText().trim();
        String newEmail = txtEmail.getText().trim();
        String newPhone = txtPhone.getText().trim();

        if (newName.isEmpty() || newEmail.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama dan Email tidak boleh kosong!", "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentUser.setNama(newName);
        currentUser.setNim(newNim);
        currentUser.setEmail(newEmail);
        currentUser.setNomorTelepon(newPhone);
        
        boolean success = userDAO.updateUser(currentUser);
        if (success) {
            JOptionPane.showMessageDialog(this, "Profil berhasil diperbarui!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            loadUserData();
            toggleEditMode(false);
            // ✅✅✅ PERUBAHAN: Update nama di dashboard jika memungkinkan
            // Ini akan membutuhkan komunikasi kembali ke Dashboard, kita siapkan nanti
            // Contoh: ((Dashboard) SwingUtilities.getWindowAncestor(this)).updateUserName(newName);
        } else {
            JOptionPane.showMessageDialog(this, "Gagal memperbarui profil!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadUserData() {
        this.currentUser = userDAO.getUserById(currentUser.getIdUser());
        if (this.currentUser != null) {
            lblNameValue.setText(currentUser.getNama());
            txtName.setText(currentUser.getNama());
            txtNim.setText(currentUser.getNim());
            txtEmail.setText(currentUser.getEmail());
            txtPhone.setText(currentUser.getNomorTelepon());
        }
    }

    private String getRoleName(int idRole) {
        switch (idRole) {
            case 1: return "Admin";
            case 2: return "Supervisor";
            case 3: return "User";
            default: return "Unknown";
        }
    }
    
    private JPanel createDetailEntry(String labelText, JComponent component) {
        JPanel entryPanel = new JPanel();
        entryPanel.setLayout(new BoxLayout(entryPanel, BoxLayout.Y_AXIS));
        entryPanel.setOpaque(false);
        entryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel(labelText);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 11));
        lblTitle.setForeground(labelColor);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        entryPanel.add(lblTitle);
        entryPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        entryPanel.add(component);

        return entryPanel;
    }

    private JPanel createInfoPanel(String label, String value) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT); 

        JLabel lblTitle = new JLabel(label);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 11));
        lblTitle.setForeground(labelColor);

        JLabel lblValue = new JLabel(value != null && !value.isEmpty() ? value : "-");
        lblValue.setFont(new Font("Arial", Font.PLAIN, 16));
        lblValue.setForeground(textColor);

        panel.add(lblTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 3)));
        panel.add(lblValue);

        return panel;
    }

    private JTextField createInfoTextField(String value) {
        JTextField textField = new JTextField(value != null ? value : "", 20);
        textField.setFont(new Font("Arial", Font.PLAIN, 16));
        textField.setForeground(textColor);
        textField.setEditable(false);
        textField.setOpaque(false);
        textField.setBorder(BorderFactory.createEmptyBorder(2,0,2,0));
        return textField;
    }

    private JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(cardColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(224, 224, 224)), 
            new EmptyBorder(20, 20, 20, 20)
        ));
        return panel;
    }

    private JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 11));
        label.setForeground(labelColor);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private JButton createStyledButton(String text, Color bgColor, int width, int height) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false); 
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(width, height));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        return button;
    }
}