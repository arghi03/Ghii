import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Vector;

public class ProfileScreen extends JFrame {
    private User currentUser;
    private UserDAO userDAO;
    private RoleDAO roleDAO;

    private JLabel lblNameValue;
    private JTextField txtName, txtNim, txtEmail, txtPhone;
    private JButton btnEdit, btnSave, btnBack;
    
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
        this.roleDAO = new RoleDAO(DBConnection.getConnection());

        setTitle("Profil Pengguna - " + currentUser.getNama());
        setSize(600, 700); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();
        loadUserData(); 

        setVisible(true);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20)); 
        mainPanel.setBackground(backgroundColor);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); 

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

        JPanel adminPanel = createAdminPanel();
        adminPanel.setVisible(currentUser.getIdRole() == 1); 

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        actionsPanel.setBackground(backgroundColor);

        btnEdit = createStyledButton("Edit Profil", primaryColor, 150, 40);
        btnSave = createStyledButton("Simpan", successColor, 160, 40);
        btnSave.setVisible(false);
        btnBack = createStyledButton("Kembali", secondaryColor, 180, 40);

        actionsPanel.add(btnEdit);
        actionsPanel.add(btnSave);
        actionsPanel.add(btnBack);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(infoPanel, BorderLayout.CENTER);

        JPanel bottomOuterPanel = new JPanel(new BorderLayout(0,15)); 
        bottomOuterPanel.setBackground(backgroundColor);
        if(currentUser.getIdRole() == 1) { 
            bottomOuterPanel.add(adminPanel, BorderLayout.NORTH);
        }
        bottomOuterPanel.add(actionsPanel, BorderLayout.SOUTH);
        mainPanel.add(bottomOuterPanel, BorderLayout.SOUTH);

        add(mainPanel);

        btnEdit.addActionListener(e -> toggleEditMode(true));
        btnSave.addActionListener(e -> saveUserProfile());
        btnBack.addActionListener(e -> dispose());
    }

    private JPanel createAdminPanel() {
        JPanel adminPanel = createCardPanel();
        adminPanel.setLayout(new BoxLayout(adminPanel, BoxLayout.Y_AXIS));

        JLabel adminTitleLabel = new JLabel("Kelola Role Pengguna Lain");
        adminTitleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        adminTitleLabel.setForeground(textColor);
        adminTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        List<User> allUsers = userDAO.getAllUsers();
        List<Role> allRoles = roleDAO.getAllRoles();

        Vector<User> userModel = new Vector<>();
        for (User u : allUsers) {
            if (u.getIdUser() != currentUser.getIdUser()) {
                userModel.add(u);
            }
        }

        JComboBox<User> userCombo = new JComboBox<>(userModel);
        userCombo.setRenderer(new UserComboBoxRenderer());

        JComboBox<Role> roleCombo = new JComboBox<>(new Vector<>(allRoles));

        JButton btnChangeRole = createStyledButton("Ubah Role", primaryColor, 150, 35);
        btnChangeRole.addActionListener(e -> {
            User selectedUser = (User) userCombo.getSelectedItem();
            Role selectedRole = (Role) roleCombo.getSelectedItem();

            if (selectedUser == null || selectedRole == null) {
                JOptionPane.showMessageDialog(this, "Pilih user dan role terlebih dahulu!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            selectedUser.setIdRole(selectedRole.getId());
            boolean success = userDAO.updateUser(selectedUser);

            if (success) {
                JOptionPane.showMessageDialog(this, "Role untuk " + selectedUser.getNama() + " berhasil diubah.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Gagal mengubah role.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        adminPanel.add(adminTitleLabel);
        adminPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        adminPanel.add(new JLabel("Pilih User:"));
        adminPanel.add(userCombo);
        adminPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        adminPanel.add(new JLabel("Pilih Role Baru:"));
        adminPanel.add(roleCombo);
        adminPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        adminPanel.add(btnChangeRole);
        return adminPanel;
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
            setTitle("Profil Pengguna - " + newName); // Update window title
            loadUserData();
            toggleEditMode(false);
        } else {
            JOptionPane.showMessageDialog(this, "Gagal memperbarui profil!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadUserData() {
        this.currentUser = userDAO.getUserById(currentUser.getIdUser());
        lblNameValue.setText(currentUser.getNama());
        txtName.setText(currentUser.getNama());
        txtNim.setText(currentUser.getNim());
        txtEmail.setText(currentUser.getEmail());
        txtPhone.setText(currentUser.getNomorTelepon());
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
    
    class UserComboBoxRenderer extends JLabel implements ListCellRenderer<User> {
        public UserComboBoxRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends User> list, 
                                                      User user, 
                                                      int index, 
                                                      boolean isSelected, 
                                                      boolean cellHasFocus) {
            
            if (user != null) {
                setText(user.getNama() + " (ID: " + user.getIdUser() + ")");
            } else {
                setText("");
            }

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }
    }
}