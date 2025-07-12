import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

public class Dashboard extends JFrame {
    private UserDAO userDAO;
    private LoanDAO loanDAO;
    private BookDAO bookDAO; // Pastikan ini ada
    private User user;
    private JTextField searchField;

    // Manajer layout dan panel utama yang menampung semua halaman
    private JPanel mainCardPanel;
    private CardLayout cardLayout;
    
    // Tombol-tombol sidebar disimpan dalam list untuk di-update
    private List<JButton> sidebarButtons = new ArrayList<>();

    // Deklarasi semua panel halaman sebagai properti class
    private ProfileScreen profilePanel;
    private LoanHistoryScreen loanHistoryPanel;
    private BookListScreen bookListPanel;
    private MyFavoritesScreen favoritesPanel;
    private SuggestionHistoryScreen suggestionHistoryPanel;
    private UserManagementScreen userManagementPanel;
    private LoanManagementScreen loanManagementPanel;
    private BookManagementScreen bookManagementPanel;
    private SuggestionListScreen suggestionListPanel;
    private StatisticsScreen statisticsPanel;

    public Dashboard(String nama, String email, int idRole) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) { throw new SQLException("Koneksi DB gagal."); }
            userDAO = new UserDAO(conn);
            loanDAO = new LoanDAO(conn);
            bookDAO = new BookDAO(conn); // Pastikan ini diinisialisasi
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

        // Inisialisasi CardLayout dan panel utama
        cardLayout = new CardLayout();
        mainCardPanel = new JPanel(cardLayout);
        mainCardPanel.setOpaque(false);

        // Buat instance dari semua panel halaman SEKALI SAJA
        instantiateAllPanels();

        // Tambahkan semua panel sebagai "kartu" ke mainCardPanel
        addPanelsToCardLayout();
        
        contentPanel.add(mainCardPanel, BorderLayout.CENTER);
        mainContainer.add(contentPanel, BorderLayout.CENTER);
        add(mainContainer);
    }

    private void instantiateAllPanels() {
    profilePanel = new ProfileScreen(this.user);
    loanHistoryPanel = new LoanHistoryScreen(this.user);
    bookListPanel = new BookListScreen(this.user);
    favoritesPanel = new MyFavoritesScreen(this.user);
    suggestionHistoryPanel = new SuggestionHistoryScreen(this.user);
    userManagementPanel = new UserManagementScreen(this.user);
    loanManagementPanel = new LoanManagementScreen(this.user);
    bookManagementPanel = new BookManagementScreen(this.user);
    suggestionListPanel = new SuggestionListScreen(this.user);
    statisticsPanel = new StatisticsScreen(this.user);
    }

    private void addPanelsToCardLayout() {
    mainCardPanel.add(createMainContentPanel(), "DASHBOARD_HOME");
    mainCardPanel.add(profilePanel, "PROFILE");
    mainCardPanel.add(loanHistoryPanel, "LOAN_HISTORY");
    mainCardPanel.add(bookListPanel, "BOOK_LIST");
    mainCardPanel.add(favoritesPanel, "FAVORITES");
    mainCardPanel.add(suggestionHistoryPanel, "SUGGESTION_HISTORY");
    mainCardPanel.add(userManagementPanel, "USER_MANAGEMENT");
    mainCardPanel.add(loanManagementPanel, "LOAN_MANAGEMENT");
    mainCardPanel.add(bookManagementPanel, "BOOK_MANAGEMENT");
    mainCardPanel.add(suggestionListPanel, "SUGGESTION_LIST");
    mainCardPanel.add(statisticsPanel, "STATISTICS");
    }

    private void showPanel(String panelName, JButton clickedButton) {
        // Update tampilan semua tombol sidebar
        for (JButton btn : sidebarButtons) {
            btn.setBackground(new Color(242, 237, 232));
            btn.setForeground(new Color(80, 80, 80));
        }
        // "Nyalakan" tombol yang baru diklik
        clickedButton.setBackground(new Color(72, 191, 172));
        clickedButton.setForeground(Color.WHITE);

        // Refresh data pada panel yang akan ditampilkan (jika perlu)
        switch(panelName) {
            case "LOAN_HISTORY": loanHistoryPanel.loadLoanHistory(); break;
            case "BOOK_LIST": bookListPanel.displayBooks(bookDAO.getAllBooks()); break;
            case "FAVORITES": favoritesPanel.loadFavoriteBooks(); break;
            case "SUGGESTION_HISTORY": suggestionHistoryPanel.loadUserSuggestions(); break;
            case "USER_MANAGEMENT": userManagementPanel.loadAllUsers(); break;
            case "LOAN_MANAGEMENT": loanManagementPanel.loadPendingLoans(); break;
            case "BOOK_MANAGEMENT": bookManagementPanel.loadAllBooks(); break;
            case "SUGGESTION_LIST": suggestionListPanel.loadSuggestions(); break;
            case "PROFILE": profilePanel.loadUserData(); break;
        }
        cardLayout.show(mainCardPanel, panelName);
    }

    private JPanel createSidebarPanel() { 
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(new Color(242, 237, 232));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 10));
        sidebarPanel.setPreferredSize(new Dimension(240, 0));
        
        // ✅✅✅ UBAH SEMUA AKSI TOMBOL UNTUK MEMANGGIL showPanel() ✅✅✅
        addSidebarButton(sidebarPanel, "Dashboard", "icons/dashboard.svg", true, "DASHBOARD_HOME");
        addSidebarButton(sidebarPanel, "Profile", "icons/profile.svg", false, "PROFILE");
        addSidebarButton(sidebarPanel, "Book History", "icons/history.svg", false, "LOAN_HISTORY");
        addSidebarButton(sidebarPanel, "Library Catalog", "icons/booklist.svg", false, "BOOK_LIST");
        addSidebarButton(sidebarPanel, "Favorite Book", "icons/favoritebook.svg", false, "FAVORITES");
        
        addRoleSpecificButtons(sidebarPanel);
        
        sidebarPanel.add(Box.createVerticalGlue());
        
        JButton logoutButton = createSidebarButton("Logout", createSVGIcon("icons/logout.svg"), false, null);
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Anda yakin ingin logout?", "Konfirmasi Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new Login().setVisible(true);
            }
        });
        sidebarPanel.add(logoutButton);

        return sidebarPanel;
    }
    
    private void addRoleSpecificButtons(JPanel sidebarPanel) { 
        int role = user.getIdRole();
        
        if (role == 1) { // Admin
            addSidebarButton(sidebarPanel, "Manajemen User", "icons/users.svg", false, "USER_MANAGEMENT");
            addSidebarButton(sidebarPanel, "Lihat Saran Buku", "icons/suggestion-list.svg", false, "SUGGESTION_LIST");
            addSidebarButton(sidebarPanel, "Statistik Aplikasi", "icons/statistics.svg", false, "STATISTICS");
        } 
        else if (role == 2) { // Supervisor
            JButton addBookBtn = createSidebarButton("Tambah Buku", createSVGIcon("icons/add.svg"), false, null);
            addBookBtn.addActionListener(e -> new AddBookScreen(this, user).setVisible(true));
            sidebarPanel.add(addBookBtn);
            sidebarPanel.add(Box.createVerticalStrut(10));

            addSidebarButton(sidebarPanel, "Kelola Peminjaman", "icons/loan.svg", false, "LOAN_MANAGEMENT");
            addSidebarButton(sidebarPanel, "Kelola Buku", "icons/book-management.svg", false, "BOOK_MANAGEMENT");
            addSidebarButton(sidebarPanel, "Lihat Saran Buku", "icons/suggestion-list.svg", false, "SUGGESTION_LIST");
        } 
        else { // User
            addSidebarButton(sidebarPanel, "Saran Buku", "icons/suggestion.svg", false, "SUGGESTION_HISTORY");
        }
    }

    private void addSidebarButton(JPanel panel, String text, String iconPath, boolean isActive, String panelName) {
        JButton button = createSidebarButton(text, createSVGIcon(iconPath), isActive, panelName);
        panel.add(button);
        panel.add(Box.createVerticalStrut(10));
        sidebarButtons.add(button); // Tambahkan ke list untuk manajemen state
    }

    private JButton createSidebarButton(String text, Icon icon, boolean isActive, String panelName) { 
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
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
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (panelName != null) {
            button.addActionListener(e -> showPanel(panelName, (JButton)e.getSource()));
        }

        Color originalBg = new Color(242, 237, 232);
        Color hoverBg = new Color(225, 220, 215);
        Color activeBg = new Color(72, 191, 172);

        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { 
                if (!button.getBackground().equals(activeBg)) button.setBackground(hoverBg);
            }
            @Override public void mouseExited(MouseEvent e) { 
                if (!button.getBackground().equals(activeBg)) button.setBackground(originalBg);
            }
        });
        
        return button;
    }

    // --- SISA METHOD DI BAWAH INI TIDAK ADA PERUBAHAN ---
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
            public void mouseClicked(MouseEvent e) { showPanel("PROFILE", (JButton)sidebarButtons.get(1)); }
        });
        return accountPanel;
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
        JPanel panel = new JPanel(new GridLayout(1, 1, 20, 0)); 
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        panel.add(createLastBookPanel());
        return panel;
    }
 
    private JPanel createLastBookPanel() {
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(Color.WHITE);
        wrapperPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel mainTitle = new JLabel("Continue Reading");
        mainTitle.setFont(new Font("Arial", Font.BOLD, 14));
        mainTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        wrapperPanel.add(mainTitle, BorderLayout.NORTH);

        Book lastReadBook = loanDAO.getLastReadBook(user.getIdUser());

        if (lastReadBook != null) {
            JPanel contentPanel = new JPanel(new BorderLayout(25, 0));
            contentPanel.setOpaque(false);
            JLabel coverLabel = new JLabel();
            coverLabel.setPreferredSize(new Dimension(80, 110));
            coverLabel.setHorizontalAlignment(SwingConstants.CENTER);
            coverLabel.setVerticalAlignment(SwingConstants.CENTER);
            coverLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            if (lastReadBook.getCoverImagePath() != null && !lastReadBook.getCoverImagePath().isEmpty()) {
                try {
                    File coverFile = new File(lastReadBook.getCoverImagePath());
                    if (coverFile.exists()) {
                        ImageIcon imageIcon = new ImageIcon(new ImageIcon(lastReadBook.getCoverImagePath()).getImage().getScaledInstance(80, 110, Image.SCALE_SMOOTH));
                        coverLabel.setIcon(imageIcon);
                        coverLabel.setBorder(null);
                    } else {
                        coverLabel.setText("No File");
                    }
                } catch (Exception e) {
                    coverLabel.setText("Error");
                }
            } else {
                coverLabel.setText("No Cover");
            }
            contentPanel.add(coverLabel, BorderLayout.WEST);
            JPanel infoActionPanel = new JPanel();
            infoActionPanel.setOpaque(false);
            infoActionPanel.setLayout(new BoxLayout(infoActionPanel, BoxLayout.Y_AXIS));
            JLabel title = new JLabel(lastReadBook.getTitle());
            title.setFont(new Font("Arial", Font.BOLD, 18));
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel author = new JLabel("by " + lastReadBook.getAuthor());
            author.setFont(new Font("Arial", Font.ITALIC, 14));
            author.setAlignmentX(Component.LEFT_ALIGNMENT);
            JButton continueButton = new JButton("Lanjut Baca");
            continueButton.setFont(new Font("Arial", Font.BOLD, 12));
            continueButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            continueButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            continueButton.addActionListener(e -> {
                if (lastReadBook.getBookFilePath() != null && !lastReadBook.getBookFilePath().isEmpty()) {
                    new PdfReaderScreen(lastReadBook.getBookFilePath());
                } else {
                    JOptionPane.showMessageDialog(this, "File PDF untuk buku ini tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            infoActionPanel.add(title);
            infoActionPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            infoActionPanel.add(author);
            infoActionPanel.add(Box.createVerticalGlue());
            infoActionPanel.add(continueButton);
            contentPanel.add(infoActionPanel, BorderLayout.CENTER);
            wrapperPanel.add(contentPanel, BorderLayout.CENTER);
        } else {
            JLabel noBookLabel = new JLabel("Anda belum pernah membaca buku apapun.", SwingConstants.CENTER);
            noBookLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            wrapperPanel.add(noBookLabel, BorderLayout.CENTER);
        }
        return wrapperPanel;
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
            JOptionPane.showMessageDialog(this, "Info", "Masukkan kata kunci pencarian.", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        bookListPanel.displayBooks(bookDAO.searchBooks(keyword));
        showPanel("BOOK_LIST", (JButton)sidebarButtons.get(3));
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