import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class BookDetailScreen extends JFrame {
    private BookDAO bookDAO;

    public BookDetailScreen(int idBook, BookDAO bookDAO) {
        this.bookDAO = bookDAO;

        setTitle("Detail Buku");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        Book book = getBookById(idBook);
        if (book != null) {
            JLabel titleLabel = new JLabel("Judul: " + book.getTitle());
            JLabel authorLabel = new JLabel("Penulis: " + book.getAuthor());
            JLabel coverLabel = new JLabel("Sampul: " + (book.getCoverImagePath() != null ? book.getCoverImagePath() : "Tidak ada"));
            JLabel fileLabel = new JLabel("File: " + (book.getBookFilePath() != null ? book.getBookFilePath() : "Tidak ada"));

            titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            panel.add(titleLabel);
            panel.add(authorLabel);
            panel.add(coverLabel);
            panel.add(fileLabel);
        } else {
            JLabel errorLabel = new JLabel("Buku tidak ditemukan!");
            errorLabel.setFont(new Font("Arial", Font.BOLD, 16));
            panel.add(errorLabel);
        }

        JButton backButton = new JButton("Kembali");
        backButton.addActionListener(e -> dispose());
        panel.add(Box.createVerticalStrut(20));
        panel.add(backButton);

        add(panel);
        setVisible(true);
    }

    private Book getBookById(int idBook) {
        String sql = "SELECT * FROM books WHERE id_book = ?";
        try (Connection conn = bookDAO.getConnection(); // Ambil conn via getter
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idBook);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Book(
                    rs.getInt("id_book"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("cover_image_path"),
                    rs.getString("book_file_path")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching book: " + e.getMessage());
        }
        return null;
    }
}

// Tambah getter di BookDAO
class BookDAO {
    private Connection conn;

    public BookDAO(Connection conn) {
        this.conn = conn;
        if (this.conn == null) {
            System.err.println("Koneksi database null saat inisialisasi BookDAO!");
        }
    }

    public Connection getConnection() {
        return conn;
    }

    // (Lainnya tetap sama, cuma tambah getter ini)
}