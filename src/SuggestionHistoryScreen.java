import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SuggestionHistoryScreen extends JFrame {
    private SuggestionDAO suggestionDAO;
    private User currentUser;
    private JPanel suggestionsPanel;
    private JDialog suggestionDialog; // Untuk mereferensikan dialog

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // Palet Warna
    private final Color primaryColor = new Color(30, 58, 138); 
    private final Color successColor = new Color(22, 163, 74);
    private final Color dangerColor = new Color(220, 38, 38);
    private final Color warningColor = new Color(245, 158, 11);
    private final Color backgroundColor = new Color(240, 242, 245);
    private final Color neutralColor = new Color(107, 114, 128);
    private final Color cardBorderColor = new Color(229, 231, 235);

    public SuggestionHistoryScreen(User user) {
        this.currentUser = user;
        this.suggestionDAO = new SuggestionDAO(DBConnection.getConnection());

        setTitle("Riwayat Saran Buku Saya");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        loadUserSuggestions();

        setVisible(true);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(backgroundColor);

        JLabel titleLabel = new JLabel("Riwayat Saran Saya", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(primaryColor);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        suggestionsPanel = new JPanel();
        suggestionsPanel.setLayout(new BoxLayout(suggestionsPanel, BoxLayout.Y_AXIS));
        suggestionsPanel.setBackground(backgroundColor);

        JScrollPane scrollPane = new JScrollPane(suggestionsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel Tombol Bawah
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(backgroundColor);

        JButton newSuggestionButton = new JButton("Buat Saran Baru");
        newSuggestionButton.addActionListener(e -> openSuggestionDialog());

        JButton backButton = new JButton("Kembali ke Dashboard");
        backButton.addActionListener(e -> dispose());
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);
        leftPanel.add(newSuggestionButton);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(backButton);

        bottomPanel.add(leftPanel, BorderLayout.WEST);
        bottomPanel.add(rightPanel, BorderLayout.EAST);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadUserSuggestions() {
        suggestionsPanel.removeAll();
        // Memanggil method DAO yang baru kita buat
        List<Suggestion> suggestions = suggestionDAO.getSuggestionsByUser(currentUser.getIdUser());

        if (suggestions.isEmpty()) {
            JLabel emptyLabel = new JLabel("Anda belum pernah memberikan saran.", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            emptyLabel.setForeground(neutralColor);
            suggestionsPanel.add(emptyLabel);
        } else {
            for (Suggestion s : suggestions) {
                suggestionsPanel.add(createHistoryCard(s));
                suggestionsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        suggestionsPanel.revalidate();
        suggestionsPanel.repaint();
    }
    
    private void openSuggestionDialog() {
        // Cek jika dialog sudah ada dan terlihat, jangan buat baru
        if (suggestionDialog != null && suggestionDialog.isVisible()) {
            suggestionDialog.toFront();
            return;
        }
        // Buat dialog baru dan tambahkan window listener
        suggestionDialog = new SuggestionDialog(this, currentUser);
        suggestionDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                // Refresh daftar riwayat setelah dialog ditutup
                loadUserSuggestions();
            }
        });
        suggestionDialog.setVisible(true);
    }

    private JPanel createHistoryCard(Suggestion suggestion) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(cardBorderColor, 1), new EmptyBorder(15, 15, 15, 15)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.setMinimumSize(new Dimension(0, 100));

        // Panel Info (Judul, Penulis, Tanggal)
        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(suggestion.getTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

        String author = (suggestion.getAuthor() != null && !suggestion.getAuthor().isEmpty()) ? "oleh " + suggestion.getAuthor() : "Penulis tidak disebutkan";
        JLabel authorLabel = new JLabel(author);
        authorLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        authorLabel.setForeground(neutralColor);

        JLabel dateLabel = new JLabel("Diajukan pada: " + suggestion.getCreatedAt().format(DATETIME_FORMATTER));
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        dateLabel.setForeground(neutralColor);

        infoPanel.add(titleLabel);
        infoPanel.add(authorLabel);
        infoPanel.add(Box.createVerticalGlue());
        infoPanel.add(dateLabel);

        // Panel Kanan (Status)
        JPanel statusPanel = new JPanel(new GridBagLayout());
        statusPanel.setOpaque(false);
        statusPanel.add(createStatusBadge(suggestion.getStatus()));

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(statusPanel, BorderLayout.EAST);

        return card;
    }
    
    private JLabel createStatusBadge(String status) {
        JLabel label = new JLabel(status.toUpperCase());
        label.setOpaque(true);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 10));
        label.setBorder(new EmptyBorder(4, 10, 4, 10));

        switch (status.toLowerCase()) {
            case "pending":
                label.setBackground(warningColor);
                break;
            case "approved":
                label.setBackground(successColor);
                break;
            case "rejected":
                label.setBackground(dangerColor);
                break;
            default:
                label.setBackground(neutralColor);
                break;
        }
        return label;
    }
}