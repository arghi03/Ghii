import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class ProfileScreen extends JFrame {
    private User currentUser;
    private ProfileDAO profileDAO;
    private UserDAO userDAO;
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JPanel infoPanel;
    private JPanel actionsPanel;
    private JPanel adminPanel;
    
    private JLabel lblName;
    private JTextField txtName;
    private JLabel lblPhone;
    private JTextField txtPhone;
    
    private JButton btnEdit;
    private JButton btnSave;
    private JButton btnBack;
    
    private boolean editMode = false;
    
    private Color primaryColor = new Color(25, 118, 210);
    private Color backgroundColor = new Color(245, 245, 245);
    private Color cardColor = Color.WHITE;
    private Color textColor = new Color(33, 33, 33);
    private Color secondaryTextColor = new Color(117, 117, 117);
    
    public ProfileScreen(User user) {
        this.currentUser = user;
        this.profileDAO = new ProfileDAO(DBConnection.getConnection());
        this.userDAO = new UserDAO(DBConnection.getConnection());
        
        setTitle("Profile");
        setSize(600, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
        loadUserData();
        
        setVisible(true);
    }
    
    private void initComponents() {
        mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(backgroundColor);
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(cardColor);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        
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
        
        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(cardColor);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        
        JPanel nimPanel = createInfoPanel("NIM", currentUser.getNim());
        infoPanel.add(nimPanel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        JPanel emailPanel = createInfoPanel("EMAIL", currentUser.getEmail());
        infoPanel.add(emailPanel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
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
        
        String roleName = getRoleName(currentUser.getIdRole());
        JPanel rolePanel = createInfoPanel("ROLE", roleName);
        infoPanel.add(rolePanel);
        
        adminPanel = new JPanel();
        adminPanel.setLayout(new BoxLayout(adminPanel, BoxLayout.Y_AXIS));
        adminPanel.setBackground(cardColor);
        adminPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        adminPanel.setVisible(currentUser.getIdRole() == 1);
        
        JLabel adminLabel = new JLabel("Kelola Role User");
        adminLabel.setFont(new Font("Arial", Font.BOLD, 14));
        adminLabel.setForeground(textColor);
        
        JComboBox<String> userCombo = new JComboBox<>();
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"Admin", "Supervisor", "User"});
        JButton btnChangeRole = createStyledButton("Ubah Role", primaryColor);
        
        List<Profile> profiles = profileDAO.getAllProfiles();
        for (Profile p : profiles) {
            userCombo.addItem(p.getNama() + " (" + p.getEmail() + ")");
        }
        
        btnChangeRole.addActionListener(e -> {
            int selectedUserIndex = userCombo.getSelectedIndex();
            if (selectedUserIndex >= 0) {
                int newRoleId = roleCombo.getSelectedIndex() + 1;
                Profile selectedProfile = profiles.get(selectedUserIndex);
                if (profileDAO.isValidRole(newRoleId)) {
                    boolean profileSuccess = profileDAO.updateRole(selectedProfile.getIdUser(), newRoleId);
                    boolean userSuccess = userDAO.updateRole(selectedProfile.getIdUser(), newRoleId);
                    if (profileSuccess && userSuccess) {
                        JOptionPane.showMessageDialog(this, "Role berhasil diubah!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                        loadUserData();
                    } else {
                        JOptionPane.showMessageDialog(this, "Gagal mengubah role!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Role tidak valid!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Pilih user terlebih dahulu!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        adminPanel.add(adminLabel);
        adminPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        adminPanel.add(userCombo);
        adminPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        adminPanel.add(roleCombo);
        adminPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        adminPanel.add(btnChangeRole);
        
        actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        actionsPanel.setBackground(backgroundColor);
        
        btnEdit = createStyledButton("Edit Profil", primaryColor);
        btnSave = createStyledButton("Simpan", new Color(76, 175, 80));
        btnSave.setVisible(false);
        btnBack = createStyledButton("Kembali", new Color(117, 117, 117));
        
        actionsPanel.add(btnEdit);
        actionsPanel.add(btnSave);
        actionsPanel.add(btnBack);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(infoPanel, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(adminPanel, BorderLayout.NORTH);
        bottomPanel.add(actionsPanel, BorderLayout.SOUTH);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        btnEdit.addActionListener(e -> toggleEditMode());
        btnSave.addActionListener(e -> saveProfile());
        btnBack.addActionListener(e -> {
            System.out.println("Kembali ke dashboard untuk: " + currentUser.getNama());
            new Dashboard(currentUser.getNama(), currentUser.getEmail(), currentUser.getIdRole()).setVisible(true);
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
    
    private String getRoleName(int idRole) {
        return profileDAO.getRoleName(idRole);
    }
    
    private void loadUserData() {
        System.out.println("Memuat data profil untuk: " + currentUser.getNama());
        Profile profile = profileDAO.getProfileByUserId(currentUser.getIdUser());
        if (profile != null) {
            lblName.setText(profile.getNama());
            txtName.setText(profile.getNama());
            txtPhone.setText(profile.getNomorTelepon() != null ? profile.getNomorTelepon() : "");
            currentUser.setIdRole(profile.getIdRole());
            JPanel rolePanel = createInfoPanel("ROLE", getRoleName(profile.getIdRole()));
            infoPanel.remove(infoPanel.getComponentCount() - 1);
            infoPanel.add(rolePanel);
            infoPanel.revalidate();
            infoPanel.repaint();
            adminPanel.setVisible(currentUser.getIdRole() == 1);
        } else {
            System.err.println("Profile not found for user ID: " + currentUser.getIdUser());
            JOptionPane.showMessageDialog(this, "Gagal memuat profil!", "Error", JOptionPane.ERROR_MESSAGE);
        }
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
            System.err.println("Gagal simpan: Nama kosong");
            JOptionPane.showMessageDialog(this, "Nama tidak boleh kosong!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        currentUser.setNama(newName);
        currentUser.setNomorTelepon(newPhone);
        
        boolean userSuccess = userDAO.updateUser(currentUser);
        Profile profile = profileDAO.getProfileByUserId(currentUser.getIdUser());
        if (profile != null) {
            profile.setNama(newName);
            profile.setNomorTelepon(newPhone);
            boolean profileSuccess = profileDAO.updateProfile(profile);
            if (userSuccess && profileSuccess) {
                System.out.println("Profil berhasil diperbarui untuk: " + newName);
                JOptionPane.showMessageDialog(this, "Profil berhasil diperbarui!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                lblName.setText(newName);
                toggleEditMode();
            } else {
                System.err.println("Gagal memperbarui profil untuk: " + newName);
                JOptionPane.showMessageDialog(this, "Gagal memperbarui profil!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            System.err.println("Profile not found for user ID: " + currentUser.getIdUser());
            JOptionPane.showMessageDialog(this, "Gagal memuat profil!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}