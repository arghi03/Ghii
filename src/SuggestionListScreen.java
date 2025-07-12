import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SuggestionListScreen extends JFrame {
    private SuggestionDAO suggestionDAO;
    private User currentUser;
    private JPanel suggestionsPanel; 

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // Palet Warna
    private final Color primaryColor = new Color(30, 58, 138); 
    private final Color successColor = new Color(22, 163, 74);
    private final Color dangerColor = new Color(220, 38, 38);
    private final Color warningColor = new Color(245, 158, 11);
    private final Color backgroundColor = new Color(240, 242, 245);
    private final Color neutralColor = new Color(107, 114, 128);
    private final Color cardBorderColor = new Color(229, 231, 235);

    public SuggestionListScreen(User user) {
        this.currentUser = user;
        this.suggestionDAO = new SuggestionDAO(DBConnection.getConnection());

        setTitle("Daftar Saran Buku - " + currentUser.getNama());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        loadSuggestions();

        setVisible(true);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(backgroundColor);

        JLabel titleLabel = new JLabel("Daftar Saran Buku dari Pengguna", SwingConstants.CENTER);
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

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(backgroundColor);
        JButton backButton = new JButton("Kembali ke Dashboard");
        backButton.addActionListener(e -> dispose());
        bottomPanel.add(backButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadSuggestions() {
        suggestionsPanel.removeAll();
        List<Suggestion> suggestions = suggestionDAO.getAllSuggestions();

        if (suggestions.isEmpty()) {
            JLabel emptyLabel = new JLabel("Belum ada saran yang masuk.", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            emptyLabel.setForeground(neutralColor);
            suggestionsPanel.add(emptyLabel);
        } else {
            for (Suggestion s : suggestions) {
                suggestionsPanel.add(createSuggestionCard(s));
                suggestionsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        suggestionsPanel.revalidate();
        suggestionsPanel.repaint();
    }

    private JPanel createSuggestionCard(Suggestion suggestion) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(cardBorderColor, 1), new EmptyBorder(15, 15, 15, 15)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.setMinimumSize(new Dimension(0, 120));

        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        
        JPanel titleAuthorPanel = new JPanel(new GridBagLayout());
        titleAuthorPanel.setOpaque(false);
        titleAuthorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 2, 5);
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel titleLabel = new JLabel(suggestion.getTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleAuthorPanel.add(titleLabel, gbc);
        gbc.gridy = 1;
        String author = (suggestion.getAuthor() != null && !suggestion.getAuthor().isEmpty()) ? "oleh " + suggestion.getAuthor() : "Penulis tidak disebutkan";
        JLabel authorLabel = new JLabel(author);
        authorLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        authorLabel.setForeground(neutralColor);
        titleAuthorPanel.add(authorLabel, gbc);

        JPanel notesPanel = new JPanel(new GridBagLayout());
        notesPanel.setOpaque(false);
        notesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints gbcNotes = new GridBagConstraints();
        gbcNotes.anchor = GridBagConstraints.NORTHWEST;
        gbcNotes.gridx = 0; gbcNotes.gridy = 0;
        gbcNotes.insets = new Insets(0, 0, 0, 5);
        JLabel notesTitleLabel = new JLabel("Catatan:");
        notesTitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        notesTitleLabel.setForeground(neutralColor);
        notesPanel.add(notesTitleLabel, gbcNotes);
        gbcNotes.gridx = 1; gbcNotes.weightx = 1.0;
        gbcNotes.fill = GridBagConstraints.HORIZONTAL;
        JTextArea notesArea = new JTextArea(suggestion.getNotes().isEmpty() ? "-" : suggestion.getNotes());
        notesArea.setFont(new Font("Arial", Font.PLAIN, 12));
        notesArea.setOpaque(false);
        notesArea.setEditable(false);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesPanel.add(notesArea, gbcNotes);
        
        JLabel suggesterLabel = new JLabel("Disarankan oleh: " + suggestion.getUsername() + " pada " + suggestion.getCreatedAt().format(DATETIME_FORMATTER));
        suggesterLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        suggesterLabel.setForeground(neutralColor);
        suggesterLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(titleAuthorPanel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(notesPanel);
        infoPanel.add(Box.createVerticalGlue());
        infoPanel.add(suggesterLabel);

        JPanel actionWrapperPanel = new JPanel(new BorderLayout(0, 5));
        actionWrapperPanel.setOpaque(false);
        actionWrapperPanel.setPreferredSize(new Dimension(280, 100));
        JLabel statusLabel = createStatusBadge(suggestion.getStatus());
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        statusPanel.setOpaque(false);
        statusPanel.add(statusLabel);
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actionPanel.setOpaque(false);
        boolean isPending = suggestion.getStatus().equalsIgnoreCase("pending");

        // ✅✅✅ PERUBAHAN TEKS TOMBOL ✅✅✅
        JButton prosesButton = new JButton("Proses Jadi Buku");
        styleActionButton(prosesButton, successColor);
        prosesButton.setEnabled(isPending);
        prosesButton.addActionListener(e -> openAddBookFromSuggestion(suggestion));

        JButton rejectButton = new JButton("Tolak");
        styleActionButton(rejectButton, warningColor);
        rejectButton.setEnabled(isPending); 
        rejectButton.addActionListener(e -> handleUpdateStatus(suggestion.getId(), "rejected"));

        JButton deleteButton = new JButton("Hapus");
        styleActionButton(deleteButton, dangerColor);
        deleteButton.addActionListener(e -> handleDelete(suggestion.getId()));
        
        actionPanel.add(prosesButton);
        actionPanel.add(rejectButton);
        actionPanel.add(deleteButton);

        actionWrapperPanel.add(statusPanel, BorderLayout.NORTH);
        actionWrapperPanel.add(actionPanel, BorderLayout.CENTER);

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(actionWrapperPanel, BorderLayout.EAST);

        return card;
    }
    
    private void openAddBookFromSuggestion(Suggestion suggestion) {
        AddBookScreen addBookDialog = new AddBookScreen(this, currentUser, suggestion);
        addBookDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                loadSuggestions();
            }
        });
        addBookDialog.setVisible(true);
    }

    private void handleUpdateStatus(int suggestionId, String newStatus) {
        String action = newStatus.equals("approved") ? "menyetujui" : "menolak";
        int confirm = JOptionPane.showConfirmDialog(this, "Anda yakin ingin " + action + " saran ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (suggestionDAO.updateSuggestionStatus(suggestionId, newStatus)) {
                JOptionPane.showMessageDialog(this, "Saran berhasil di-" + newStatus + ".");
                loadSuggestions();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal mengubah status saran.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDelete(int suggestionId) {
        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus saran ini secara permanen?", "Konfirmasi Hapus", JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (suggestionDAO.deleteSuggestion(suggestionId)) {
                JOptionPane.showMessageDialog(this, "Saran berhasil dihapus.");
                loadSuggestions();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus saran.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private JLabel createStatusBadge(String status) {
        JLabel label = new JLabel(status.toUpperCase());
        label.setOpaque(true);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 10));
        label.setBorder(new EmptyBorder(3, 8, 3, 8));

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

    private void styleActionButton(JButton button, Color color) {
        button.setFont(new Font("Arial", Font.BOLD, 10));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false); 
        button.setPreferredSize(new Dimension(130, 28));
    }
}