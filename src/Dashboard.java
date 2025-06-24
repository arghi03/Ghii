import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

// Asumsi kelas-kelas lain (User, DAO, Screen, dll.) sudah ada di project Anda
// public class UserDAO { public UserDAO(Connection c) {} public User getUserByNameAndEmail(String n, String e) { User u = new User(); u.nama = n; u.idRole = 1; return u; } public User getUserById(int id) { return new User(); } }
// public class BookDAO { public BookDAO(Connection c) {} }
// public class FavoriteDAO { public FavoriteDAO(Connection c) {} }
// public class DBConnection { public static Connection getConnection() { return null; } }
// public class User { public int idRole = 1; public String nama; public String getNama() {return nama;} public int getIdRole() { return idRole; } public int getIdUser() {return 0;} }
// public class Login extends JFrame { public Login() {} }
// public class ProfileScreen extends JFrame { public ProfileScreen(User u) { System.out.println("Profile screen opened for " + u.getNama()); setSize(400,300); setLocationRelativeTo(null); setVisible(true); } }
// public class LoanHistoryScreen extends JFrame { public LoanHistoryScreen(User u) {} }
// public class BookListScreen extends JFrame { public BookListScreen(User u) {} }
// public class MyFavoritesScreen extends JFrame { public MyFavoritesScreen(User u) {} }
// public class UserManagementScreen extends JFrame { public UserManagementScreen(User u) {} }
// public class SuggestionListScreen extends JFrame { public SuggestionListScreen(User u) {System.out.println("Suggestion list screen opened."); setSize(600,400); setLocationRelativeTo(null); setVisible(true);}}
// public class AddBookScreen extends JFrame { public AddBookScreen(User u) {} }
// public class LoanManagementScreen extends JFrame { public LoanManagementScreen(User u) {} }
// public class BookManagementScreen extends JFrame { public BookManagementScreen(User u) {} }
// public class SuggestionDialog extends JDialog { public SuggestionDialog(Frame f, User u) {} }

public class Dashboard extends JFrame {
    private UserDAO userDAO;
    private User user;
    private JTextField searchField;

    public Dashboard(String nama, String email, int idRole) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) { throw new SQLException("Koneksi DB gagal."); }
            userDAO = new UserDAO(conn);
            this.user = userDAO.getUserByNameAndEmail(nama, email);
            if (this.user == null) { throw new Exception("User tidak ditemukan."); }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat Dashboard: " + e.getMessage(), "Error Kritis", JOptionPane.ERROR_MESSAGE);
            SwingUtilities.invokeLater(() -> new Login().setVisible(true));
            dispose();
            return;
        }

        setTitle("LiteraSpace - Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1100, 768));
        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(new Color(242, 237, 232));
        mainContainer.add(createHeaderPanel(), BorderLayout.NORTH);
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.add(createSidebarPanel(), BorderLayout.WEST);
        JScrollPane mainContentScrollPane = new JScrollPane(createMainContentPanel());
        mainContentScrollPane.setBorder(null);
        mainContentScrollPane.getViewport().setBackground(new Color(242, 237, 232));
        contentPanel.add(mainContentScrollPane, BorderLayout.CENTER);
        mainContainer.add(contentPanel, BorderLayout.CENTER);
        add(mainContainer);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(20, 0));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        logoPanel.setOpaque(false);
        logoPanel.add(createLogoIcon());
        JLabel brandName = new JLabel("LiteraSpace");
        brandName.setFont(new Font("Arial", Font.BOLD, 18));
        brandName.setForeground(new Color(147, 112, 219));
        brandName.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        logoPanel.add(brandName);
        
        headerPanel.add(logoPanel, BorderLayout.WEST);
        headerPanel.add(createSearchPanel(), BorderLayout.CENTER);
        headerPanel.add(createAccountPanel(), BorderLayout.EAST);
        
        return headerPanel;
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setOpaque(false);
        
        searchField = new JTextField(30) {
            @Override
            protected void paintComponent(Graphics g) {
                if (!isOpaque() && getBorder() instanceof javax.swing.border.EmptyBorder) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setPaint(getBackground());
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 40, 40));
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        searchField.setOpaque(false);
        searchField.setBackground(new Color(245, 245, 245));
        searchField.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Cari buku, pengarang, atau genre...");
        searchField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, createSVGIcon("icons/search.svg"));
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.addActionListener(e -> searchBooks());
        
        searchPanel.add(searchField);
        return searchPanel;
    }

    private JPanel createAccountPanel() {
        JPanel accountPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        accountPanel.setOpaque(false);
        accountPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel accountLabel = new JLabel("Hi, " + user.getNama());
        accountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        accountLabel.setIcon(createSVGIcon("icons/profile.svg"));
        accountLabel.setIconTextGap(8);
        
        accountPanel.add(accountLabel);
        accountPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { new ProfileScreen(user); }
        });
        return accountPanel;
    }

    private JPanel createSidebarPanel() {
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(new Color(242, 237, 232));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 10));
        sidebarPanel.setPreferredSize(new Dimension(240, 0));
        
        sidebarPanel.add(createSidebarButton("Dashboard", createSVGIcon("icons/dashboard.svg"), true, null));
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Profile", createSVGIcon("icons/profile.svg"), false, () -> new ProfileScreen(this.user)));
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Book History", createSVGIcon("icons/history.svg"), false, () -> new LoanHistoryScreen(this.user)));
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Library Catalog", createSVGIcon("icons/booklist.svg"), false, () -> new BookListScreen(user)));
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Favorite Book", createSVGIcon("icons/favoritebook.svg"), false, () -> new MyFavoritesScreen(this.user)));
        
        addRoleSpecificButtons(sidebarPanel);
        
        sidebarPanel.add(Box.createVerticalGlue());
        sidebarPanel.add(createSidebarButton("Logout", createSVGIcon("icons/logout.svg"), false, () -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Anda yakin ingin logout?", "Konfirmasi Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new Login().setVisible(true);
            }
        }));
        return sidebarPanel;
    }

    // ✅✅✅ METHOD INI DIPERBAIKI ✅✅✅
    private void addRoleSpecificButtons(JPanel sidebarPanel) {
        sidebarPanel.add(Box.createVerticalStrut(10));
        int role = user.getIdRole();
        
        // Menu khusus Admin
        if (role == 1) { 
            sidebarPanel.add(createSidebarButton("Manajemen User", createSVGIcon("icons/users.svg"), false, () -> new UserManagementScreen(user)));
            sidebarPanel.add(Box.createVerticalStrut(10));
            // Tombol yang hilang, ditambahkan kembali
            sidebarPanel.add(createSidebarButton("Lihat Saran Buku", createSVGIcon("icons/suggestion-list.svg"), false, () -> new SuggestionListScreen(user)));
        } 
        // Menu khusus Supervisor
        else if (role == 2) { 
            sidebarPanel.add(createSidebarButton("Tambah Buku", createSVGIcon("icons/add.svg"), false, () -> new AddBookScreen(user)));
            sidebarPanel.add(Box.createVerticalStrut(10));
            sidebarPanel.add(createSidebarButton("Kelola Peminjaman", createSVGIcon("icons/loan.svg"), false, () -> new LoanManagementScreen(user)));
            sidebarPanel.add(Box.createVerticalStrut(10));
            // Tombol yang hilang, ditambahkan kembali
            sidebarPanel.add(createSidebarButton("Lihat Saran Buku", createSVGIcon("icons/suggestion-list.svg"), false, () -> new SuggestionListScreen(user)));
        } 
        // Menu khusus User
        else { 
            sidebarPanel.add(createSidebarButton("Saran Buku", createSVGIcon("icons/suggestion.svg"), false, () -> new SuggestionDialog(this, this.user).setVisible(true)));
        }
    }

    private JButton createSidebarButton(String text, Icon icon, boolean isActive, Runnable action) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if(getModel().isArmed()) {
                    g2.setColor(getBackground().darker());
                } else {
                    g2.setColor(getBackground());
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setIcon(icon);
        button.setIconTextGap(15);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(isActive ? Color.WHITE : new Color(80, 80, 80));
        button.setBackground(isActive ? new Color(72, 191, 172) : new Color(242, 237, 232));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);

        if (action != null) {
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.addActionListener(e -> action.run());
            if (!isActive) {
                Color originalBg = button.getBackground();
                Color hoverBg = new Color(225, 220, 215);
                button.addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { button.setBackground(hoverBg); }
                    @Override public void mouseExited(MouseEvent e) { button.setBackground(originalBg); }
                });
            }
        }
        return button;
    }

    private JPanel createMainContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(createWelcomeSection(), BorderLayout.NORTH);
        panel.add(createBottomSection(), BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createWelcomeSection() {
        JPanel welcomePanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.dispose();
            }
        };
        welcomePanel.setOpaque(false);
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        
        JLabel welcomeTitle = new JLabel("Welcome to LiteraSpace!");
        welcomeTitle.setFont(new Font("Arial", Font.BOLD, 32));
        welcomeTitle.setForeground(new Color(147, 112, 219));
        
        JLabel subtitle = new JLabel("<html><i>\"Your Digital Gateway - A World of Books in One Click\"</i></html>");
        subtitle.setFont(new Font("Arial", Font.ITALIC, 18));
        subtitle.setForeground(new Color(147, 112, 219));
        subtitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        textPanel.add(welcomeTitle);
        textPanel.add(subtitle);
        
        welcomePanel.add(textPanel, BorderLayout.CENTER);
        welcomePanel.add(createIllustrationLabel(), BorderLayout.EAST);
        
        return welcomePanel;
    }

    private JPanel createBottomSection() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        panel.add(createLastBookPanel());
        panel.add(createStatsPanel());
        return panel;
    }

    private JPanel createLastBookPanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Panel Buku Terakhir"));
        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Panel Statistik"));
        return panel;
    }

    private JLabel createLogoIcon() {
        return new JLabel(createSVGIcon("icons/logo.svg", 24, 24));
    }
    
    private JLabel createIllustrationLabel() {
        return new JLabel();
    }

    private void searchBooks() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Masukkan kata kunci pencarian.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        new BookListScreen(this.user);
    }
    
    private Icon createSVGIcon(String path, int width, int height) {
        URL url = getClass().getClassLoader().getResource(path);
        if (url == null) {
            System.err.println("Gagal memuat ikon: " + path + " (File tidak ditemukan)");
            return new ImageIcon(new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB));
        }
        return new FlatSVGIcon(url).derive(width, height);
    }
    
    private FlatSVGIcon createSVGIcon(String path) {
        URL url = getClass().getClassLoader().getResource(path);
        if (url == null) {
            System.err.println("Gagal memuat ikon: " + path + " (File tidak ditemukan)");
            return null;
        }
        
        FlatSVGIcon icon = new FlatSVGIcon(url);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> {
            if(color.equals(Color.black))
                return new Color(80, 80, 80);
            return color;
        }));
        return icon;
    }
}
