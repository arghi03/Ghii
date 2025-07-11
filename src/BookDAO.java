import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {
    private Connection conn;

    public BookDAO(Connection conn) {
        this.conn = conn;
        if (this.conn == null) {
            System.err.println("Koneksi database null saat inisialisasi BookDAO!");
        } else {
            System.out.println("Koneksi database berhasil diinisialisasi di BookDAO.");
        }
    }

    public Connection getConnection() {
        return conn;
    }

    public boolean softDeleteBook(int bookId) {
        if (conn == null) { return false; }
        String sql = "UPDATE books SET is_deleted = 1 WHERE id_book = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addBook(Book book) {
        if (conn == null) return false;
        String sql = "INSERT INTO books (id_book, title, author, isbn, classification_code, cover_image_path, book_file_path, rating, is_deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, book.getIdBook());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, book.getIsbn());
            stmt.setString(5, book.getClassificationCode());
            stmt.setString(6, book.getCoverImagePath());
            stmt.setString(7, book.getBookFilePath());
            stmt.setFloat(8, book.getRating());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBook(Book book) {
        if (conn == null) return false;
        String sql = "UPDATE books SET title = ?, author = ?, isbn = ?, classification_code = ?, rating = ?, cover_image_path = ?, book_file_path = ? WHERE id_book = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getIsbn());
            stmt.setString(4, book.getClassificationCode());
            stmt.setFloat(5, book.getRating());
            stmt.setString(6, book.getCoverImagePath());
            stmt.setString(7, book.getBookFilePath());
            stmt.setInt(8, book.getIdBook());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getNewBookId() {
        if (conn == null) throw new IllegalStateException("Koneksi database tidak tersedia");
        String sql = "SELECT MAX(id_book) + 1 AS new_id FROM books";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int newId = rs.getInt("new_id");
                return (newId <= 0) ? 1 : newId;
            }
            return 1;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal generate ID buku baru: " + e.getMessage());
        }
    }

    // ✅ PERBAIKAN: Menggunakan SELECT eksplisit
    private final String ALL_BOOK_COLUMNS = "id_book, title, author, isbn, classification_code, cover_image_path, book_file_path, rating, is_deleted";

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT " + ALL_BOOK_COLUMNS + " FROM books WHERE is_deleted = 0"; 
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                books.add(mapRowToBook(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    public int getTotalBooksCount() {
        String sql = "SELECT COUNT(*) AS total FROM books WHERE is_deleted = 0";
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
 
    public List<Book> searchBooks(String keyword) {
        List<Book> books = new ArrayList<>();
        // ✅ PERBAIKAN: Menggunakan SELECT eksplisit
        String sql = "SELECT " + ALL_BOOK_COLUMNS + " FROM books WHERE (LOWER(title) LIKE ? OR LOWER(author) LIKE ? OR LOWER(isbn) LIKE ? OR LOWER(classification_code) LIKE ?) AND is_deleted = 0";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String searchTerm = "%" + keyword.toLowerCase() + "%";
            stmt.setString(1, searchTerm);
            stmt.setString(2, searchTerm);
            stmt.setString(3, searchTerm);
            stmt.setString(4, searchTerm);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                books.add(mapRowToBook(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }
        
    public Book getBookById(int idBook) {
        if (conn == null) return null;
        // ✅ PERBAIKAN: Menggunakan SELECT eksplisit
        String sql = "SELECT " + ALL_BOOK_COLUMNS + " FROM books WHERE id_book = ? AND is_deleted = 0";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idBook);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToBook(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<Loan> getLoanHistoryByUser(int idUser) { /* ... kode tidak berubah ... */ return new ArrayList<>(); }
    public boolean updateBookRating(int idBook, float newRating) { /* ... kode tidak berubah ... */ return false; }
    public float getBookRating(int idBook) { /* ... kode tidak berubah ... */ return 0.0f; }

    private Book mapRowToBook(ResultSet rs) throws SQLException {
        // Method ini tidak perlu diubah karena nama kolom sudah benar
        return new Book(
            rs.getInt("id_book"),
            rs.getString("title"),
            rs.getString("author"),
            rs.getString("isbn"),
            rs.getString("classification_code"),
            rs.getString("cover_image_path"),
            rs.getString("book_file_path"),
            rs.getFloat("rating")
        );
    }
}