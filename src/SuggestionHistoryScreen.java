import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SuggestionHistoryScreen extends JPanel {
    private SuggestionDAO suggestionDAO;
    private User currentUser;
    private JPanel suggestionsPanel;
    // Kita tidak lagi butuh variabel untuk tombol atau dialog

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // Palet warna tetap sama
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
        initComponents();
        loadUserSuggestions();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(backgroundColor);

        JLabel titleLabel = new JLabel("Riwayat Saran Saya", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(primaryColor);
        add(titleLabel, BorderLayout.NORTH);

        suggestionsPanel = new JPanel();
        suggestionsPanel.setLayout(new BoxLayout(suggestionsPanel, BoxLayout.Y_AXIS));
        suggestionsPanel.setBackground(backgroundColor);

        JScrollPane scrollPane = new JScrollPane(suggestionsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBackground(backgroundColor);

        JButton newSuggestionButton = new JButton("Buat Saran Baru");
        newSuggestionButton.addActionListener(e -> openSuggestionDialog());
        bottomPanel.add(newSuggestionButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void loadUserSuggestions() {
        // Method ini tidak berubah
        suggestionsPanel.removeAll();
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
    
    // ✅✅✅ PERUBAHAN TOTAL DI SINI (PENDEKATAN BARU) ✅✅✅
    private void openSuggestionDialog() {
        // 1. Buat panel yang akan menjadi isi dari dialog
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField titleField = new JTextField(25);
        JTextField authorField = new JTextField(25);
        JTextArea notesArea = new JTextArea(4, 25);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Judul Buku:"), gbc);
        gbc.gridx = 1; formPanel.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Penulis (Opsional):"), gbc);
        gbc.gridx = 1; formPanel.add(authorField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Catatan (Opsional):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(new JScrollPane(notesArea), gbc);

        // 2. Tampilkan panel form di dalam sebuah JOptionPane
        int result = JOptionPane.showConfirmDialog(
            this,                          // Parent component
            formPanel,                     // Isi dialognya adalah panel kita
            "Sarankan Buku Baru",          // Judul dialog
            JOptionPane.OK_CANCEL_OPTION,  // Tombol OK dan Cancel
            JOptionPane.PLAIN_MESSAGE      // Tipe dialog
        );

        // 3. Proses hasilnya HANYA jika pengguna menekan "OK"
        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Judul buku tidak boleh kosong.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String author = authorField.getText().trim();
            String notes = notesArea.getText().trim();

            Suggestion newSuggestion = new Suggestion(currentUser.getIdUser(), title, author, notes);
            SuggestionDAO suggestionDAO = new SuggestionDAO(DBConnection.getConnection());
            
            if (suggestionDAO.addSuggestion(newSuggestion)) {
                JOptionPane.showMessageDialog(this, "Terima kasih! Saran Anda telah kami terima.", "Saran Terkirim", JOptionPane.INFORMATION_MESSAGE);
                loadUserSuggestions(); // Refresh daftar setelah berhasil
            } else {
                JOptionPane.showMessageDialog(this, "Gagal mengirim saran. Silakan coba lagi.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Method createHistoryCard dan createStatusBadge tidak berubah
    private JPanel createHistoryCard(Suggestion suggestion) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(cardBorderColor, 1), new EmptyBorder(15, 15, 15, 15)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.setMinimumSize(new Dimension(0, 100));

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