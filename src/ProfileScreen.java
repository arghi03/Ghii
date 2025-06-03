import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
// Pastikan User dan Profile diimport jika mereka ada di package berbeda
// import com.perpustakaan.model.User; 
// import com.perpustakaan.model.Profile;

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
    // private JLabel lblPhone; // Tidak terpakai sebagai field instance
    private JTextField txtPhone;

    private JButton btnEdit;
    private JButton btnSave;
    private JButton btnBack;

    private boolean editMode = false;

    private Color primaryColor = new Color(25, 118, 210); 
    private Color successColor = new Color(76, 175, 80);  
    private Color secondaryColor = new Color(117, 117, 117); 
    private Color backgroundColor = new Color(240, 242, 245); 
    private Color cardColor = Color.WHITE;
    private Color textColor = new Color(33, 33, 33);
    private Color labelColor = new Color(100, 100, 100); 

    public ProfileScreen(User user) {
        this.currentUser = user;
        this.profileDAO = new ProfileDAO(DBConnection.getConnection());
        this.userDAO = new UserDAO(DBConnection.getConnection());

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
        mainPanel = new JPanel(new BorderLayout(0, 20)); 
        mainPanel.setBackground(backgroundColor);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); 

        headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(cardColor);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(224, 224, 224)), 
            new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel nameHeaderTitleLabel = new JLabel("NAMA PENGGUNA");
        nameHeaderTitleLabel.setFont(new Font("Arial", Font.BOLD, 11));
        nameHeaderTitleLabel.setForeground(labelColor);
        nameHeaderTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblName = new JLabel(currentUser.getNama());
        lblName.setFont(new Font("Arial", Font.BOLD, 22));
        lblName.setForeground(textColor);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtName = new JTextField(currentUser.getNama(), 20);
        txtName.setFont(new Font("Arial", Font.BOLD, 20));
        txtName.setHorizontalAlignment(JTextField.CENTER);
        txtName.setVisible(false);
        txtName.setMaximumSize(new Dimension(300, txtName.getPreferredSize().height + 5));
        txtName.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(nameHeaderTitleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        headerPanel.add(lblName);
        headerPanel.add(txtName);

        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(cardColor);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(224, 224, 224)),
            new EmptyBorder(20, 20, 20, 20)
        ));

        infoPanel.add(createInfoPanel("NIM", currentUser.getNim() != null ? currentUser.getNim() : "-"));
        infoPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        infoPanel.add(createInfoPanel("EMAIL", currentUser.getEmail()));
        infoPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel phonePanelContainer = new JPanel();
        phonePanelContainer.setLayout(new BoxLayout(phonePanelContainer, BoxLayout.Y_AXIS));
        phonePanelContainer.setBackground(cardColor);
        JLabel phoneTitleLabel = new JLabel("NOMOR TELEPON");
        phoneTitleLabel.setFont(new Font("Arial", Font.BOLD, 11));
        phoneTitleLabel.setForeground(labelColor);
        txtPhone = new JTextField(currentUser.getNomorTelepon() != null ? currentUser.getNomorTelepon() : "", 20);
        txtPhone.setFont(new Font("Arial", Font.PLAIN, 16));
        txtPhone.setForeground(textColor);
        txtPhone.setEditable(false);
        txtPhone.setBackground(cardColor);
        txtPhone.setBorder(BorderFactory.createEmptyBorder(2,0,2,0)); 
        phonePanelContainer.add(phoneTitleLabel);
        phonePanelContainer.add(Box.createRigidArea(new Dimension(0, 5)));
        phonePanelContainer.add(txtPhone);
        infoPanel.add(phonePanelContainer);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        infoPanel.add(createInfoPanel("ROLE", getRoleName(currentUser.getIdRole())));

        adminPanel = new JPanel();
        adminPanel.setLayout(new BoxLayout(adminPanel, BoxLayout.Y_AXIS));
        adminPanel.setBackground(cardColor);
        adminPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(224, 224, 224)),
            new EmptyBorder(15, 15, 15, 15)
        ));
        adminPanel.setVisible(currentUser.getIdRole() == 1); 

        JLabel adminTitleLabel = new JLabel("Kelola Role Pengguna Lain");
        adminTitleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        adminTitleLabel.setForeground(textColor);
        adminTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<String> userCombo = new JComboBox<>();
        userCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"Admin", "Supervisor", "User"}); 
        roleCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        JButton btnChangeRole = createStyledButton("Ubah Role User", primaryColor, 150, 35);

        final List<Profile> profiles = profileDAO.getAllProfiles(); 
        if (profiles != null) {
            for (Profile p : profiles) {
                if (p.getIdUser() != currentUser.getIdUser()) {
                     userCombo.addItem(p.getNama() + " (ID: " + p.getIdUser() + ")");
                }
            }
        }

        btnChangeRole.addActionListener(e -> {
            int selectedUserDisplayIndex = userCombo.getSelectedIndex();
            if (selectedUserDisplayIndex >= 0) {
                Profile selectedProfileToChange = null;
                int displayIndexCounter = -1;
                for(Profile p : profiles){ // profiles list harus accessible di sini
                    if(p.getIdUser() != currentUser.getIdUser()){
                        displayIndexCounter++;
                        if(displayIndexCounter == selectedUserDisplayIndex){
                            selectedProfileToChange = p;
                            break;
                        }
                    }
                }

                if (selectedProfileToChange == null) {
                     JOptionPane.showMessageDialog(this, "Gagal mendapatkan profil user yang dipilih.", "Error", JOptionPane.ERROR_MESSAGE);
                     return;
                }

                int newRoleId = roleCombo.getSelectedIndex() + 1; 

                if (profileDAO.isValidRole(newRoleId)) { 
                    User userToUpdate = userDAO.getUserById(selectedProfileToChange.getIdUser());

                    if (userToUpdate != null) {
                        userToUpdate.setIdRole(newRoleId); 
                        boolean success = userDAO.updateUser(userToUpdate); 

                        if (success) {
                            JOptionPane.showMessageDialog(this, "Role untuk " + userToUpdate.getNama() + " berhasil diubah menjadi " + getRoleName(newRoleId) + "!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                            if (currentUser.getIdUser() == userToUpdate.getIdUser()) {
                                this.currentUser = userDAO.getUserById(currentUser.getIdUser()); 
                                loadUserData(); 
                            }
                        } else {
                            JOptionPane.showMessageDialog(this, "Gagal mengubah role di database!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                         JOptionPane.showMessageDialog(this, "User dengan ID " + selectedProfileToChange.getIdUser() + " tidak ditemukan untuk update.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Role ID " + newRoleId + " tidak valid!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Pilih user terlebih dahulu!", "Error", JOptionPane.ERROR_MESSAGE);
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

        actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        actionsPanel.setBackground(backgroundColor);

        btnEdit = createStyledButton("Edit Profil Saya", primaryColor, 150, 40);
        btnSave = createStyledButton("Simpan Perubahan", successColor, 160, 40);
        btnSave.setVisible(false);
        btnBack = createStyledButton("Kembali ke Dashboard", secondaryColor, 180, 40);

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

        btnEdit.addActionListener(e -> toggleEditMode());
        btnSave.addActionListener(e -> saveCurrentUserProfile());
        
        // --- PERUBAHAN DI SINI ---
        btnBack.addActionListener(e -> {
            System.out.println("Kembali ke dashboard dari ProfileScreen untuk: " + currentUser.getNama());
            dispose(); // Cukup tutup window ProfileScreen ini
            // Asumsi Dashboard utama masih ada dan akan terlihat kembali
            // Tidak perlu membuat instance Dashboard baru:
            // new Dashboard(currentUser.getNama(), currentUser.getEmail(), currentUser.getIdRole()).setVisible(true); 
        });
    }

    private JPanel createInfoPanel(String label, String value) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(cardColor);
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
    
    private String getRoleName(int idRole) {
        String roleName = profileDAO.getRoleName(idRole);
        return roleName != null ? roleName : "Tidak Diketahui";
    }

    private void loadUserData() {
        User freshCurrentUser = userDAO.getUserById(currentUser.getIdUser());
        if (freshCurrentUser != null) {
            this.currentUser = freshCurrentUser; 
        } else {
            JOptionPane.showMessageDialog(this, "Gagal memuat ulang data user terkini!", "Error", JOptionPane.ERROR_MESSAGE);
            // Jika user tidak ditemukan lagi, kembali ke Dashboard (dengan data lama user) atau Login
            // Untuk amannya, kita bisa saja menutup ProfileScreen
            // dispose(); 
            // new Login().setVisible(true); // atau ke Login
            // Untuk sementara, biarkan user data yang lama
            System.err.println("ProfileScreen: Gagal refresh currentUser, menggunakan data lama.");
        }

        lblName.setText(currentUser.getNama());
        txtName.setText(currentUser.getNama());
        
        infoPanel.removeAll(); 

        infoPanel.add(createInfoPanel("NIM", currentUser.getNim() != null ? currentUser.getNim() : "-"));
        infoPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        infoPanel.add(createInfoPanel("EMAIL", currentUser.getEmail()));
        infoPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        JPanel phonePanelContainer = new JPanel(); 
        phonePanelContainer.setLayout(new BoxLayout(phonePanelContainer, BoxLayout.Y_AXIS));
        phonePanelContainer.setBackground(cardColor);
        JLabel phoneTitleLabel = new JLabel("NOMOR TELEPON");
        phoneTitleLabel.setFont(new Font("Arial", Font.BOLD, 11));
        phoneTitleLabel.setForeground(labelColor);
        txtPhone.setText(currentUser.getNomorTelepon() != null ? currentUser.getNomorTelepon() : "");
        phonePanelContainer.add(phoneTitleLabel);
        phonePanelContainer.add(Box.createRigidArea(new Dimension(0, 5)));
        phonePanelContainer.add(txtPhone);
        infoPanel.add(phonePanelContainer);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        infoPanel.add(createInfoPanel("ROLE", getRoleName(currentUser.getIdRole())));
        
        infoPanel.revalidate();
        infoPanel.repaint();

        adminPanel.setVisible(currentUser.getIdRole() == 1); 
    }

    private void toggleEditMode() {
        editMode = !editMode;
        lblName.setVisible(!editMode);
        txtName.setVisible(editMode);
        txtPhone.setEditable(editMode);
        txtPhone.setBorder(editMode ? BorderFactory.createLineBorder(primaryColor.darker()) : BorderFactory.createEmptyBorder(2,0,2,0));
        txtPhone.setBackground(editMode ? Color.WHITE : cardColor);
        btnEdit.setVisible(!editMode);
        btnSave.setVisible(editMode);
        if (editMode) {
            txtName.requestFocusInWindow();
        }
    }

    private void saveCurrentUserProfile() {
        String newName = txtName.getText().trim();
        String newPhone = txtPhone.getText().trim();
        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama tidak boleh kosong!", "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            return;
        }
        currentUser.setNama(newName);
        currentUser.setNomorTelepon(newPhone);
        boolean success = userDAO.updateUser(currentUser);
        if (success) {
            JOptionPane.showMessageDialog(this, "Profil berhasil diperbarui!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            lblName.setText(newName); 
            // loadUserData(); // Tidak perlu jika hanya nama & telepon, agar tidak refresh semua
            toggleEditMode(); 
        } else {
            JOptionPane.showMessageDialog(this, "Gagal memperbarui profil di database!", "Error", JOptionPane.ERROR_MESSAGE);
            User oldData = userDAO.getUserById(currentUser.getIdUser()); 
            if(oldData != null) {
                currentUser = oldData; // Kembalikan data objek currentUser
                loadUserData(); // Muat ulang tampilan dengan data lama dari DB
            }
        }
    }
}
