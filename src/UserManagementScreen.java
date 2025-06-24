import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

// Asumsi kelas-kelas lain sudah ada di project Anda
// public class User { private int idUser, idRole; private String nama, email; private boolean isVerified, isActive; public User() {} public int getIdUser() {return 1;} public String getNama() {return "Admin";} public String getEmail(){return "admin@mail.com";} public int getIdRole(){return 1;} public boolean isVerified(){return true;} public boolean isActive(){return true;} public void setActive(boolean b){} }
// public class UserDAO { public UserDAO(java.sql.Connection c){} public List<User> getAllUsers(){ java.util.List<User> list = new java.util.ArrayList<>(); for(int i=0; i<10; i++) list.add(new User()); return list;} public boolean approveUser(int id){return true;} public boolean rejectUser(int id){return true;} public boolean updateUser(User u){return true;} public boolean updateUserRole(int id, int roleId){return true;} }
// public class DBConnection { public static java.sql.Connection getConnection(){return null;} }

public class UserManagementScreen extends JFrame {
    private UserDAO userDAO;
    private User adminUser;
    private JPanel userCardsPanel; // Panel untuk menampung semua kartu pengguna

    // Palet Warna
    private final Color primaryColor = new Color(30, 58, 138); 
    private final Color successColor = new Color(22, 163, 74);
    private final Color dangerColor = new Color(220, 38, 38);
    private final Color warningColor = new Color(245, 158, 11);
    private final Color headerColor = new Color(224, 231, 255); 
    private final Color backgroundColor = new Color(240, 242, 245);
    private final Color neutralColor = new Color(107, 114, 128);
    private final Color cardBackgroundColor = Color.WHITE;

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
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(backgroundColor);

        JLabel titleLabel = new JLabel("Manajemen Pengguna", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(primaryColor);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0,0,15,0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Panel utama untuk menampung kartu-kartu
        userCardsPanel = new JPanel();
        userCardsPanel.setLayout(new BoxLayout(userCardsPanel, BoxLayout.Y_AXIS));
        userCardsPanel.setBackground(backgroundColor);
        
        // Membuatnya bisa di-scroll
        JScrollPane scrollPane = new JScrollPane(userCardsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Scroll lebih smooth
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Panel Tombol Bawah
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton refreshButton = new JButton("Refresh Daftar");
        styleActionButton(refreshButton, primaryColor, 140, 35);
        refreshButton.addActionListener(e -> loadAllUsers());

        JButton backButton = new JButton("Kembali ke Dashboard");
        styleActionButton(backButton, neutralColor, 180, 35);
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
        userCardsPanel.removeAll(); // Hapus semua kartu yang ada
        List<User> allUsers = userDAO.getAllUsers();
        
        if (allUsers == null || allUsers.isEmpty()) {
            JLabel emptyLabel = new JLabel("Tidak ada data pengguna untuk ditampilkan.", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            userCardsPanel.add(emptyLabel);
        } else {
            for (User user : allUsers) {
                userCardsPanel.add(createUserCard(user)); // Tambahkan kartu baru untuk setiap user
                userCardsPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Beri jarak antar kartu
            }
        }
        
        userCardsPanel.revalidate();
        userCardsPanel.repaint();
    }

    // ✅✅✅ INI ADALAH FUNGSI UTAMA YANG MENGGANTIKAN JTABLE ✅✅✅
    private JPanel createUserCard(User user) {
        JPanel card = new JPanel(new BorderLayout(15, 15));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(cardBackgroundColor);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120)); // Batasi tinggi maksimum kartu
        card.setMinimumSize(new Dimension(0, 120));

        // Panel Info Kiri
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(user.getNama() + " (ID: " + user.getIdUser() + ")");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        infoPanel.add(nameLabel);

        JLabel emailLabel = new JLabel(user.getEmail());
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        emailLabel.setForeground(Color.GRAY);
        infoPanel.add(emailLabel);
        
        infoPanel.add(Box.createVerticalStrut(10));

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        statusPanel.setOpaque(false);
        statusPanel.add(createStatusLabel("Role: " + getRoleName(user.getIdRole()), primaryColor));
        statusPanel.add(createStatusLabel(user.isVerified() ? "Terverifikasi" : "Belum Diverifikasi", user.isVerified() ? successColor : warningColor));
        statusPanel.add(createStatusLabel(user.isActive() ? "Aktif" : "Nonaktif", user.isActive() ? successColor : dangerColor));
        infoPanel.add(statusPanel);
        
        card.add(infoPanel, BorderLayout.CENTER);
        
        // Panel Aksi Kanan
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        actionPanel.setOpaque(false);
        
        boolean isSelf = (user.getIdUser() == adminUser.getIdUser());

        JButton ubahRoleBtn = new JButton("Ubah Role");
        styleButtonInTable(ubahRoleBtn, primaryColor);
        ubahRoleBtn.setEnabled(!isSelf);
        ubahRoleBtn.addActionListener(e -> showRoleChangeDialog(user));
        actionPanel.add(ubahRoleBtn);

        if (!user.isVerified()) {
            JButton verifyBtn = new JButton("Verify");
            styleButtonInTable(verifyBtn, successColor);
            verifyBtn.addActionListener(e -> {
                if (userDAO.approveUser(user.getIdUser())) {
                    JOptionPane.showMessageDialog(this, "User " + user.getNama() + " berhasil diverifikasi.");
                    loadAllUsers();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal memverifikasi user.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            actionPanel.add(verifyBtn);

            JButton rejectBtn = new JButton("Reject");
            styleButtonInTable(rejectBtn, dangerColor);
            rejectBtn.setEnabled(!isSelf);
            rejectBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this, "Anda yakin ingin menolak dan menghapus user " + user.getNama() + "?\nAksi ini tidak dapat dibatalkan.", "Konfirmasi Tolak", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (userDAO.rejectUser(user.getIdUser())) {
                        JOptionPane.showMessageDialog(this, "User " + user.getNama() + " berhasil ditolak (dihapus).");
                        loadAllUsers();
                    } else {
                        JOptionPane.showMessageDialog(this, "Gagal menolak user.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            actionPanel.add(rejectBtn);
        }

        JButton toggleActiveBtn = new JButton(user.isActive() ? "Nonaktifkan" : "Aktifkan");
        styleButtonInTable(toggleActiveBtn, user.isActive() ? warningColor.darker() : successColor);
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
        
        card.add(actionPanel, BorderLayout.EAST);
        
        return card;
    }
    
    private JLabel createStatusLabel(String text, Color bgColor) {
        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setBackground(bgColor);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 10));
        label.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        return label;
    }

    private void styleButtonInTable(JButton button, Color color) {
        button.setFont(new Font("Arial", Font.PLAIN, 11));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setMargin(new Insets(4, 8, 4, 8));
        button.setOpaque(true);
        button.setBorderPainted(false);
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

    private void styleActionButton(JButton button, Color bgColor, int width, int height) {
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(width, height));
        button.setOpaque(true);
        button.setBorderPainted(false);
        
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
