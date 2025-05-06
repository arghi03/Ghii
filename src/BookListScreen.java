import javax.swing.*;
import java.awt.*;
import java.util.List;

public class BookListScreen extends JFrame {
    private BookDAO bookDAO;
    private LoanDAO loanDAO;
    private User currentUser;

    public BookListScreen(User user) {
        this.currentUser = user;
        this.bookDAO = new BookDAO(DBConnection.getConnection());
        this.loanDAO = new LoanDAO(DBConnection.getConnection());

        setTitle("Daftar Buku - " + currentUser.getNama());
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Daftar Buku Tersedia", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        List<Book> books = bookDAO.getAllBooks();
        for (Book book : books) {
            JPanel bookPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel bookLabel = new JLabel("ID: " + book.getIdBook() + " | Judul: " + book.getTitle() + " | Penulis: " + book.getAuthor());
            JButton borrowButton = new JButton("Pinjam");

            borrowButton.addActionListener(e -> {
                boolean success = loanDAO.addLoan(currentUser.getIdUser(), book.getIdBook());
                if (success) {
                    JOptionPane.showMessageDialog(this, "Peminjaman untuk buku \"" + book.getTitle() + "\" berhasil diajukan!");
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal mengajukan peminjaman!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            bookPanel.add(bookLabel);
            bookPanel.add(borrowButton);
            listPanel.add(bookPanel);
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JButton backButton = new JButton("Kembali");
        backButton.addActionListener(e -> {
            new Dashboard(currentUser.getNama(), currentUser.getEmail(), currentUser.getIdRole()).setVisible(true);
            dispose();
        });
        mainPanel.add(backButton, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }
}