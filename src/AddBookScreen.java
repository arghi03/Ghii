import javax.swing.*;
import java.awt.*;

public class AddBookScreen extends JFrame {
    private BookDAO bookDAO;
    private User currentUser;

    public AddBookScreen(User user) {
        this.currentUser = user;
        this.bookDAO = new BookDAO(DBConnection.getConnection());

        setTitle("Tambah Buku - " + currentUser.getNama());
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Tambah Buku Baru", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        JLabel titleFieldLabel = new JLabel("Judul:");
        JTextField titleField = new JTextField();
        JLabel authorFieldLabel = new JLabel("Penulis:");
        JTextField authorField = new JTextField();

        formPanel.add(titleFieldLabel);
        formPanel.add(titleField);
        formPanel.add(authorFieldLabel);
        formPanel.add(authorField);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("Simpan");
        JButton backButton = new JButton("Kembali");

        saveButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            if (title.isEmpty() || author.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Judul dan penulis tidak boleh kosong!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int newBookId = bookDAO.getNewBookId();
            Book book = new Book(newBookId, title, author);
            boolean success = bookDAO.addBook(book);
            if (success) {
                JOptionPane.showMessageDialog(this, "Buku berhasil ditambahkan!");
                titleField.setText("");
                authorField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menambahkan buku!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        backButton.addActionListener(e -> {
            new Dashboard(currentUser.getNama(), currentUser.getEmail(), currentUser.getIdRole()).setVisible(true);
            dispose();
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(backButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }
}