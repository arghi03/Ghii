import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
 
public class UserManagementScreen extends JFrame {
    private UserDAO userDAO;
    private User adminUser;
    private JPanel userCardsPanel;

    private final Color primaryColor = new Color(45, 156, 219);
    private final Color successColor = new Color(34, 197, 94);
    private final Color dangerColor = new Color(239, 68, 68);
    private final Color warningColor = new Color(245, 158, 11);
    private final Color roleColor = new Color(45, 156, 219);
    private final Color backgroundColor = new Color(249, 250, 251);
    private final Color neutralColor = new Color(107, 114, 128);
    private final Color cardBackgroundColor = Color.WHITE;
    private final Color borderColor = new Color(229, 231, 235);

    public UserManagementScreen(User admin) {
        this.adminUser = admin;
        this.userDAO = new UserDAO(DBConnection.getConnection());

        setTitle("Manajemen Pengguna - Admin: " + adminUser.getNama());
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        loadAllUsers();

        setVisible(true);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(backgroundColor);

        JLabel titleLabel = new JLabel("Manajemen Pengguna", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(31, 41, 55));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0,0,20,0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        userCardsPanel = new JPanel();
        userCardsPanel.setLayout(new BoxLayout(userCardsPanel, BoxLayout.Y_AXIS));
        userCardsPanel.setBackground(backgroundColor);
        
        JScrollPane scrollPane = new JScrollPane(userCardsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(backgroundColor);
        scrollPane.getViewport().setBackground(backgroundColor);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JButton refreshButton = new JButton("Refresh List");
        styleBottomButton(refreshButton, primaryColor, 140, 45);
        refreshButton.addActionListener(e -> loadAllUsers());

        JButton backButton = new JButton("Back to Dashboard");
        styleBottomButton(backButton, neutralColor, 180, 45);
        backButton.addActionListener(e -> dispose());

        JPanel leftBottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftBottomPanel.setOpaque(false);
        leftBottomPanel.add(refreshButton);
        
        JPanel rightBottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightBottomPanel.setOpaque(false);
        rightBottomPanel.add(backButton);
        
        bottomPanel.add(leftBottomPanel, BorderLayout.WEST);
        bottomPanel.add(rightBottomPanel, BorderLayout.EAST);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadAllUsers() {
        userCardsPanel.removeAll();
        List<User> allUsers = userDAO.getAllUsers();
        
        if (allUsers == null || allUsers.isEmpty()) {
            JLabel emptyLabel = new JLabel("Tidak ada data pengguna untuk ditampilkan.", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            emptyLabel.setForeground(neutralColor);
            userCardsPanel.add(emptyLabel);
        } else {
            for (User user : allUsers) {
                userCardsPanel.add(createUserCard(user));
                userCardsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            }
        }
        userCardsPanel.revalidate();
        userCardsPanel.repaint();
    }

    private JPanel createUserCard(User user) {
        JPanel card = new JPanel(new BorderLayout(20, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor, 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        card.setBackground(cardBackgroundColor);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        card.setMinimumSize(new Dimension(0, 110));

        JPanel leftSection = new JPanel(new BorderLayout(15, 0));
        leftSection.setOpaque(false);
        
        JLabel avatarLabel = createAvatarLabel(user.getNama());
        leftSection.add(avatarLabel, BorderLayout.CENTER);
        
        JPanel middleSection = new JPanel();
        middleSection.setOpaque(false);
        middleSection.setLayout(new BoxLayout(middleSection, BoxLayout.Y_AXIS));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel(user.getNama() + " ( ID : " + user.getIdUser() + " )");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(new Color(31, 41, 55));
        infoPanel.add(nameLabel);

        JLabel emailLabel = new JLabel(user.getEmail());
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        emailLabel.setForeground(new Color(107, 114, 128));
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(emailLabel);
        
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        statusPanel.setOpaque(false);
        statusPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        statusPanel.add(createStatusBadge("Role : " + getRoleName(user.getIdRole()), roleColor));
        statusPanel.add(createStatusBadge(user.isVerified() ? "Terverifikasi" : "Belum Diverifikasi", user.isVerified() ? successColor : warningColor));
        statusPanel.add(createStatusBadge(user.isActive() ? "Aktif" : "Nonaktif", user.isActive() ? successColor : dangerColor));
        
        middleSection.add(infoPanel);
        middleSection.add(Box.createVerticalStrut(8));
        middleSection.add(statusPanel);
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);
        
        boolean isSelf = (user.getIdUser() == adminUser.getIdUser());
 
        if (!user.isVerified()) { 
            JButton verifyButton = new JButton("Verifikasi");
            styleActionButton(verifyButton, successColor);
            verifyButton.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "Anda yakin ingin memverifikasi pengguna '" + user.getNama() + "'?",
                    "Konfirmasi Verifikasi", 
                    JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    if (userDAO.approveUser(user.getIdUser())) {
                        JOptionPane.showMessageDialog(this, "Pengguna berhasil diverifikasi.");
                        loadAllUsers();  
                    } else {
                        JOptionPane.showMessageDialog(this, "Gagal memverifikasi pengguna.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            actionPanel.add(verifyButton);
        } else { 
            JButton ubahRoleBtn = new JButton("Ubah Role");
            styleActionButton(ubahRoleBtn, primaryColor);
            ubahRoleBtn.setEnabled(!isSelf);
            ubahRoleBtn.addActionListener(e -> showRoleChangeDialog(user));
            actionPanel.add(ubahRoleBtn);

            JButton toggleActiveBtn = new JButton(user.isActive() ? "Nonaktifkan" : "Aktifkan");
            styleActionButton(toggleActiveBtn, user.isActive() ? dangerColor : successColor);
            toggleActiveBtn.setEnabled(!isSelf);
            toggleActiveBtn.addActionListener(e -> {
                boolean newStatus = !user.isActive();
                user.setActive(newStatus);
                if (userDAO.updateUser(user)) {
                    JOptionPane.showMessageDialog(this, "Status akun " + user.getNama() + " berhasil diubah.");
                    loadAllUsers();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal mengubah status akun.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            actionPanel.add(toggleActiveBtn);
        }
        
        card.add(leftSection, BorderLayout.WEST);
        card.add(middleSection, BorderLayout.CENTER);
        card.add(actionPanel, BorderLayout.EAST);
        
        return card;
    }
    
    private JLabel createAvatarLabel(String name) {
        JLabel avatarLabel = new JLabel();
        avatarLabel.setPreferredSize(new Dimension(45, 45));
        avatarLabel.setBackground(new Color(75, 85, 99));
        avatarLabel.setOpaque(true);
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        String initial = name.length() > 0 ? name.substring(0, 1).toUpperCase() : "?";
        avatarLabel.setText(initial);
        avatarLabel.setFont(new Font("Arial", Font.BOLD, 18));
        avatarLabel.setForeground(Color.WHITE);
        
        avatarLabel.setBorder(BorderFactory.createEmptyBorder());
        
        return avatarLabel;
    }
    
    private JLabel createStatusBadge(String text, Color bgColor) {
        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setBackground(bgColor);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 11));
        label.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        return label;
    }

    private void styleActionButton(JButton button, Color color) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(100, 35));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        Color originalColor = color;
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                if (button.isEnabled()) button.setBackground(originalColor.darker());
            }
            public void mouseExited(MouseEvent evt) {
                if (button.isEnabled()) button.setBackground(originalColor);
            }
        });
    }
    
    private void styleBottomButton(JButton button, Color bgColor, int width, int height) {
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(width, height));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        
        Color originalBgColor = bgColor; 
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(originalBgColor.darker());
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(originalBgColor);
            }
        });
    }
    
    private String getRoleName(int roleId) {
        switch (roleId) {
            case 1: return "Admin";
            case 2: return "Supervisor";
            case 3: return "User";
            default: return "Unknown";
        }
    }
    
    private int getRoleId(String roleName) {
        switch (roleName) {
            case "Admin": return 1;
            case "Supervisor": return 2;
            case "User": return 3;
            default: return -1;
        }
    }

    private void showRoleChangeDialog(User userToEdit) {
        String[] roles = {"Admin", "Supervisor", "User"};
        JComboBox<String> roleComboBox = new JComboBox<>(roles);
        roleComboBox.setSelectedItem(getRoleName(userToEdit.getIdRole()));

        Object[] message = {
            "Ubah role untuk pengguna:",
            new JLabel("<html><b>" + userToEdit.getNama() + "</b></html>"),
            "Pilih role baru:",
            roleComboBox
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Ubah Role Pengguna", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String selectedRoleName = (String) roleComboBox.getSelectedItem();
            int newRoleId = getRoleId(selectedRoleName);
            int confirm = JOptionPane.showConfirmDialog(this, "Anda yakin ingin mengubah role '" + userToEdit.getNama() + "' menjadi '" + selectedRoleName + "'?", "Konfirmasi Final", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                if (userDAO.updateUserRole(userToEdit.getIdUser(), newRoleId)) {
                    JOptionPane.showMessageDialog(this, "Role berhasil diubah.");
                    loadAllUsers();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal mengubah role.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}