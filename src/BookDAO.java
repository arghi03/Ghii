import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {
    private Connection conn;

    public BookDAO(Connection conn) {
        this.conn = conn;
        if (this.conn == null) {
            System.err.println("Koneksi database null saat inisialisasi BookDAO!");
        }
    }

    public boolean addBook(Book book) {
        if (conn == null) {
            System.err.println("Koneksi database null! Tidak bisa menambah buku.");
            return false;
        }

        String sql = "INSERT INTO books (id_book, title, author, cover_image_path, book_file_path) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, book.getIdBook());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, book.getCoverImagePath());
            stmt.setString(5, book.getBookFilePath());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Buku berhasil ditambahkan: " + book.getTitle());
                return true;
            } else {
                System.out.println("Gagal menambah buku: " + book.getTitle());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error adding book: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public int getNewBookId() {
        if (conn == null) {
            System.err.println("Koneksi database null! Tidak bisa getNewBookId.");
            throw new IllegalStateException("Koneksi database tidak tersedia");
        }

        String sql = "SELECT MAX(id_book) + 1 AS new_id FROM books";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int newId = rs.getInt("new_id");
                if (newId <= 0) {
                    return 1;
                }
                return newId;
            } else {
                return 1;
            }
        } catch (SQLException e) {
            System.err.println("Error generating new book ID: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Gagal generate ID buku baru: " + e.getMessage());
        }
    }

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books";
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Book book = new Book(
                    rs.getInt("id_book"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("cover_image_path"),
                    rs.getString("book_file_path")
                );
                books.add(book);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching books: " + e.getMessage());
        }
        return books;
    }

    public int getTotalBooksCount() {
        String sql = "SELECT COUNT(*) AS total FROM books";
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching total books count: " + e.getMessage());
        }
        return 0;
    }
}