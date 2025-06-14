import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.AbstractCellEditor;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class UserManagementScreen extends JFrame {
    private UserDAO userDAO;
    private User adminUser; // Admin yang sedang login
    private DefaultTableModel tableModel;
    private JTable usersTable;

    // Palet Warna
    private Color primaryColor = new Color(30, 58, 138); 
    private Color successColor = new Color(28, 132, 74); 
    private Color dangerColor = new Color(220, 53, 69);
    private Color warningColor = new Color(255, 193, 7);
    private Color headerColor = new Color(224, 231, 255); 
    private Color backgroundColor = new Color(240, 242, 245);
    private Color neutralColor = new Color(107, 114, 128);

    public UserManagementScreen(User admin) {
        this.adminUser = admin;
        this.userDAO = new UserDAO(DBConnection.getConnection());

        setTitle("Manajemen Pengguna - Admin: " + adminUser.getNama());
        setSize(950, 600);
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
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(primaryColor);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        String[] columnNames = {"ID", "Nama", "Email", "Role", "Status Verifikasi", "Status Akun", "Aksi"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Kolom "Aksi" bisa di-klik
            }
        };
        usersTable = new JTable(tableModel);
        usersTable.setRowHeight(35);
        usersTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        usersTable.getTableHeader().setBackground(headerColor);
        usersTable.setFont(new Font("Arial", Font.PLAIN, 12));

        TableColumnModel columnModel = usersTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(40);
        columnModel.getColumn(3).setPreferredWidth(80);
        columnModel.getColumn(4).setPreferredWidth(120);
        columnModel.getColumn(5).setPreferredWidth(100);
        columnModel.getColumn(6).setPreferredWidth(220); // Kolom aksi lebih lebar

        // Custom renderer untuk status
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        columnModel.getColumn(0).setCellRenderer(centerRenderer);
        columnModel.getColumn(3).setCellRenderer(centerRenderer);
        columnModel.getColumn(4).setCellRenderer(new StatusRenderer());
        columnModel.getColumn(5).setCellRenderer(new StatusRenderer());
        
        // Custom renderer dan editor untuk kolom "Aksi"
        UserActionPanel actionHandler = new UserActionPanel(usersTable);
        columnModel.getColumn(6).setCellRenderer(actionHandler);
        columnModel.getColumn(6).setCellEditor(actionHandler);

        JScrollPane scrollPane = new JScrollPane(usersTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel Tombol Bawah
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        JButton refreshButton = new JButton("Refresh Daftar");
        styleActionButton(refreshButton, primaryColor, 140, 35);
        refreshButton.addActionListener(e -> loadAllUsers());

        JButton backButton = new JButton("Kembali ke Dashboard");
        styleActionButton(backButton, neutralColor, 180, 35);
        backButton.addActionListener(e -> dispose());

        JPanel leftBottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftBottomPanel.setOpaque(false);
        leftBottomPanel.add(refreshButton);
        
        JPanel rightBottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightBottomPanel.setOpaque(false);
        rightBottomPanel.add(backButton);
        
        bottomPanel.add(leftBottomPanel, BorderLayout.WEST);
        bottomPanel.add(rightBottomPanel, BorderLayout.EAST);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadAllUsers() {
        tableModel.setRowCount(0);
        List<User> allUsers = userDAO.getAllUsers();
        if (allUsers.isEmpty()) {
             tableModel.addRow(new Object[]{"-", "Tidak ada data pengguna.", "-", "-", "-", "-", null});
        } else {
            for (User user : allUsers) {
                tableModel.addRow(new Object[]{
                    user.getIdUser(),
                    user.getNama(),
                    user.getEmail(),
                    getRoleName(user.getIdRole()),
                    user.isVerified() ? "Terverifikasi" : "Belum Diverifikasi",
                    user.isActive() ? "Aktif" : "Nonaktif",
                    user // Kirim objek user ke kolom Aksi untuk diproses renderer/editor
                });
            }
        }
    }
    
    private String getRoleName(int roleId) {
        switch (roleId) {
            case 1: return "Admin";
            case 2: return "Supervisor";
            case 3: return "User";
            default: return "Unknown";
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
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(originalBgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalBgColor);
            }
        });
    }

   
    class StatusRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = (String) value;
            if ("Terverifikasi".equals(status) || "Aktif".equals(status)) {
                c.setForeground(successColor);
            } else if ("Belum Diverifikasi".equals(status)) {
                c.setForeground(warningColor.darker());
            } else { // Nonaktif
                c.setForeground(dangerColor);
            }
            setHorizontalAlignment(SwingConstants.CENTER);
            return c;
        }
    }
    
   
    class UserActionPanel extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {
        private JPanel panel;
        private JButton verifyButton, rejectButton, toggleActiveButton;
        private JTable table;
        private User selectedUser;

        public UserActionPanel(JTable table) {
            this.table = table;
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 2)); // Sedikit vertical gap
            panel.setOpaque(true);

            verifyButton = new JButton("Verify");
            rejectButton = new JButton("Reject");
            toggleActiveButton = new JButton(); 

            styleButtonInTable(verifyButton, successColor);
            styleButtonInTable(rejectButton, dangerColor);
            
            verifyButton.addActionListener(this);
            rejectButton.addActionListener(this);
            toggleActiveButton.addActionListener(this);

            panel.add(verifyButton);
            panel.add(rejectButton);
            panel.add(toggleActiveButton);
        }

        private void styleButtonInTable(JButton button, Color color) {
            button.setFont(new Font("Arial", Font.PLAIN, 10));
            button.setBackground(color);
            button.setForeground(Color.WHITE);
            button.setMargin(new Insets(2,5,2,5));
            button.setOpaque(true);
            button.setBorderPainted(false);
        }

        private void updateButtons(User user) {
            this.selectedUser = user;

            if (user.isVerified()) {
                verifyButton.setVisible(false);
                rejectButton.setVisible(false);
            } else {
                verifyButton.setVisible(true);
                rejectButton.setVisible(true);
            }

            if (user.isActive()) {
                toggleActiveButton.setText("Nonaktifkan");
                styleButtonInTable(toggleActiveButton, warningColor.darker());
                toggleActiveButton.setActionCommand("deactivate");
            } else {
                toggleActiveButton.setText("Aktifkan");
                styleButtonInTable(toggleActiveButton, successColor);
                toggleActiveButton.setActionCommand("activate");
            }
            
            if (user.getIdUser() == adminUser.getIdUser()) {
                toggleActiveButton.setEnabled(false);
                rejectButton.setEnabled(false); 
            } else {
                toggleActiveButton.setEnabled(true);
                rejectButton.setEnabled(true);
            }
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof User) {
                updateButtons((User) value);
            }
            if (isSelected) panel.setBackground(table.getSelectionBackground());
            else panel.setBackground(table.getBackground());
            return panel;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (value instanceof User) {
                updateButtons((User) value);
            }
            return panel;
        }
        
        @Override
        public Object getCellEditorValue() { return null; }

        @Override
        public void actionPerformed(ActionEvent e) {
            fireEditingStopped();
            String command = e.getActionCommand();
            
            if (selectedUser == null) return;

            switch (command) {
                case "verify":
                    if (userDAO.approveUser(selectedUser.getIdUser())) {
                        JOptionPane.showMessageDialog(table, "User " + selectedUser.getNama() + " berhasil diverifikasi.");
                    } else {
                        JOptionPane.showMessageDialog(table, "Gagal memverifikasi user.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    break;
                case "reject":
                     int confirm = JOptionPane.showConfirmDialog(table, "Anda yakin ingin menolak dan menghapus user " + selectedUser.getNama() + "?\nAksi ini tidak dapat dibatalkan.", "Konfirmasi Tolak", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                     if (confirm == JOptionPane.YES_OPTION) {
                         if (userDAO.rejectUser(selectedUser.getIdUser())) {
                             JOptionPane.showMessageDialog(table, "User " + selectedUser.getNama() + " berhasil ditolak (dihapus).");
                         } else {
                             JOptionPane.showMessageDialog(table, "Gagal menolak user.", "Error", JOptionPane.ERROR_MESSAGE);
                         }
                     }
                    break;
                case "activate":
                case "deactivate":
                    boolean newStatus = "activate".equals(command);
                    selectedUser.setActive(newStatus);
                    if (userDAO.updateUser(selectedUser)) {
                        JOptionPane.showMessageDialog(table, "Status akun " + selectedUser.getNama() + " berhasil diubah menjadi " + (newStatus ? "Aktif" : "Nonaktif") + ".");
                    } else {
                        JOptionPane.showMessageDialog(table, "Gagal mengubah status akun.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    break;
            }
            loadAllUsers(); 
        }
    }
}
