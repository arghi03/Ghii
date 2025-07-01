import javax.swing.*;
import java.awt.*;

public class SuggestionDialog extends JDialog {
    private User currentUser;
    private JTextField titleField;
    private JTextField authorField;
    private JTextArea notesArea;

    public SuggestionDialog(Frame owner, User user) {
        super(owner, "Sarankan Buku Baru", true); 
        this.currentUser = user;

        setSize(450, 350);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // Panel utama
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel("Form Saran Buku", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Panel form
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Judul Buku:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        titleField = new JTextField(20);
        formPanel.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Penulis (Opsional):"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        authorField = new JTextField(20);
        formPanel.add(authorField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Catatan (Opsional):"), gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        notesArea = new JTextArea(4, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScrollPane = new JScrollPane(notesArea);
        formPanel.add(notesScrollPane, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Panel Tombol
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton submitButton = new JButton("Kirim Saran");
        JButton cancelButton = new JButton("Batal");

        submitButton.addActionListener(e -> submitSuggestion());
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(submitButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private void submitSuggestion() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Judul buku tidak boleh kosong.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String author = authorField.getText().trim();
        String notes = notesArea.getText().trim();

        Suggestion newSuggestion = new Suggestion(currentUser.getIdUser(), title, author, notes);
        SuggestionDAO suggestionDAO = new SuggestionDAO(DBConnection.getConnection());
        
        boolean success = suggestionDAO.addSuggestion(newSuggestion);

        if (success) {
            JOptionPane.showMessageDialog(this, "Terima kasih! Saran Anda telah kami terima.", "Saran Terkirim", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal mengirim saran. Silakan coba lagi.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}