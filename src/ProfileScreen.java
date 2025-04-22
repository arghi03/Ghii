
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ProfileScreen extends JFrame {
    private User currentUser;
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JPanel infoPanel;
    private JPanel actionsPanel;
    
    private JLabel lblName;
    private JTextField txtName;
    private JLabel lblNIM;
    private JLabel lblEmail;
    private JLabel lblPhone;
    private JTextField txtPhone;
    private JLabel lblRole;
    
    private JButton btnEdit;
    private JButton btnSave;
    private JButton btnBack;
    
    private boolean editMode = false;
    
    private Color primaryColor = new Color(25, 118, 210); // Material Blue
    private Color backgroundColor = new Color(245, 245, 245);
    private Color cardColor = Color.WHITE;
    private Color textColor = new Color(33, 33, 33);
    private Color secondaryTextColor = new Color(117, 117, 117);
    
    public ProfileScreen(User user) {
        this.currentUser = user;
        
        setTitle("Profile");
        setSize(600, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
        loadUserData();
        
        setVisible(true);
    }
    
    private void initComponents() {
        // Main panel setup with border layout
        mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(backgroundColor);
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        // Header panel - Name
        headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(cardColor);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        
        // Name label
        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        namePanel.setBackground(cardColor);
        namePanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        JLabel nameHeaderLabel = new JLabel("NAMA");
        nameHeaderLabel.setFont(new Font("Arial", Font.BOLD, 12));
        nameHeaderLabel.setForeground(secondaryTextColor);
        nameHeaderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        lblName = new JLabel(currentUser.getNama());
        lblName.setFont(new Font("Arial", Font.BOLD, 24));
        lblName.setForeground(textColor);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        txtName = new JTextField(currentUser.getNama());
        txtName.setFont(new Font("Arial", Font.BOLD, 24));
        txtName.setHorizontalAlignment(JTextField.CENTER);
        txtName.setVisible(false);
        txtName.setMaximumSize(new Dimension(300, 35));
        txtName.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        namePanel.add(nameHeaderLabel);
        namePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        namePanel.add(lblName);
        namePanel.add(txtName);
        
        headerPanel.add(namePanel);
        
        // Info panel - User details
        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(cardColor);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        
        // NIM
        JPanel nimPanel = createInfoPanel("NIM", currentUser.getNim());
        infoPanel.add(nimPanel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Email
        JPanel emailPanel = createInfoPanel("EMAIL", currentUser.getEmail());
        infoPanel.add(emailPanel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Phone
        JPanel phonePanel = new JPanel();
        phonePanel.setLayout(new BoxLayout(phonePanel, BoxLayout.Y_AXIS));
        phonePanel.setBackground(cardColor);
        
        lblPhone = new JLabel("NOMOR TELEPON");
        lblPhone.setFont(new Font("Arial", Font.BOLD, 12));
        lblPhone.setForeground(secondaryTextColor);
        
        txtPhone = new JTextField(currentUser.getNomorTelepon() != null ? currentUser.getNomorTelepon() : "");
        txtPhone.setFont(new Font("Arial", Font.PLAIN, 16));
        txtPhone.setForeground(textColor);
        txtPhone.setBorder(null);
        txtPhone.setEditable(false);
        txtPhone.setBackground(cardColor);
        
        phonePanel.add(lblPhone);
        phonePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        phonePanel.add(txtPhone);
        
        infoPanel.add(phonePanel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Role
        JPanel rolePanel = createInfoPanel("ROLE", currentUser.getRole());
        infoPanel.add(rolePanel);
        
        // Actions panel
        actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        actionsPanel.setBackground(backgroundColor);
        
        btnEdit = createStyledButton("Edit Profil", primaryColor);
        btnSave = createStyledButton("Simpan", new Color(76, 175, 80)); // Green
        btnSave.setVisible(false);
        btnBack = createStyledButton("Kembali", new Color(117, 117, 117)); // Grey
        
        actionsPanel.add(btnEdit);
        actionsPanel.add(btnSave);
        actionsPanel.add(btnBack);
        
        // Add all panels to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(infoPanel, BorderLayout.CENTER);
        mainPanel.add(actionsPanel, BorderLayout.SOUTH);
        
        // Add main panel to frame
        add(mainPanel);
        
        // Add action listeners
        btnEdit.addActionListener(e -> toggleEditMode());
        btnSave.addActionListener(e -> saveProfile());
        btnBack.addActionListener(e -> {
            System.out.println("Kembali ke dashboard untuk: " + currentUser.getNama());
            new Dashboard(currentUser.getNama(), currentUser.getEmail()).setVisible(true);
            dispose();
        });
    }
    
    private JPanel createInfoPanel(String label, String value) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(cardColor);
        
        JLabel lblTitle = new JLabel(label);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 12));
        lblTitle.setForeground(secondaryTextColor);
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Arial", Font.PLAIN, 16));
        lblValue.setForeground(textColor);
        
        panel.add(lblTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(lblValue);
        
        return panel;
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(120, 40));
        
        return button;
    }
    
    private void loadUserData() {
        System.out.println("Memuat data profil untuk: " + currentUser.getNama());
        lblName.setText(currentUser.getNama());
        txtName.setText(currentUser.getNama());
        txtPhone.setText(currentUser.getNomorTelepon() != null ? currentUser.getNomorTelepon() : "");
    }
    
    private void toggleEditMode() {
        editMode = !editMode;
        
        System.out.println("Mode edit: " + (editMode ? "aktif" : "nonaktif"));
        if (editMode) {
            lblName.setVisible(false);
            txtName.setVisible(true);
            txtPhone.setEditable(true);
            txtPhone.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
            btnEdit.setVisible(false);
            btnSave.setVisible(true);
        } else {
            lblName.setVisible(true);
            txtName.setVisible(false);
            txtPhone.setEditable(false);
            txtPhone.setBorder(null);
            btnEdit.setVisible(true);
            btnSave.setVisible(false);
        }
    }
    
    private void saveProfile() {
        String newName = txtName.getText().trim();
        String newPhone = txtPhone.getText().trim();
        
        if (newName.isEmpty()) {
            System.out.println("Gagal simpan: Nama kosong");
            JOptionPane.showMessageDialog(this, "Nama tidak boleh kosong!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Update user object
        currentUser.setNama(newName);
        currentUser.setNomorTelepon(newPhone);
        
        // Update database
        boolean success = UserDAO.updateUser(currentUser);
        if (success) {
            System.out.println("Profil berhasil diperbarui untuk: " + newName);
            JOptionPane.showMessageDialog(this, "Profil berhasil diperbarui!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            // Refresh UI
            lblName.setText(newName);
            toggleEditMode();
        } else {
            System.out.println("Gagal memperbarui profil untuk: " + newName);
            JOptionPane.showMessageDialog(this, "Gagal memperbarui profil!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}