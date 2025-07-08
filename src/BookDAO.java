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
        if (conn == null) {
            System.err.println("Koneksi database null! Tidak bisa menghapus buku.");
            return false;
        }
        String sql = "UPDATE books SET is_deleted = 1 WHERE id_book = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error saat soft delete buku: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ✅ DIPERBARUI UNTUK MENAMBAHKAN ISBN
    public boolean addBook(Book book) {
        if (conn == null) return false;
        String sql = "INSERT INTO books (id_book, title, author, isbn, cover_image_path, book_file_path, rating, is_deleted) VALUES (?, ?, ?, ?, ?, ?, ?, 0)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, book.getIdBook());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, book.getIsbn()); // Tambahan
            stmt.setString(5, book.getCoverImagePath());
            stmt.setString(6, book.getBookFilePath());
            stmt.setFloat(7, book.getRating());
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ✅ DIPERBARUI UNTUK MENGUPDATE ISBN
    public boolean updateBook(Book book) {
        if (conn == null) {
            System.err.println("Koneksi null, tidak bisa update buku.");
            return false;
        }
        String sql = "UPDATE books SET title = ?, author = ?, isbn = ?, rating = ?, cover_image_path = ?, book_file_path = ? WHERE id_book = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getIsbn()); // Tambahan
            stmt.setFloat(4, book.getRating());
            stmt.setString(5, book.getCoverImagePath());
            stmt.setString(6, book.getBookFilePath());
            stmt.setInt(7, book.getIdBook()); // ID buku untuk WHERE clause

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error saat update buku: " + e.getMessage());
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
            } else {
                return 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Gagal generate ID buku baru: " + e.getMessage());
        }
    }

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE is_deleted = 0"; 
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
 
    // ✅ DIPERBARUI DENGAN PENCARIAN BERDASARKAN ISBN
    public List<Book> searchBooks(String keyword) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE (LOWER(title) LIKE ? OR LOWER(author) LIKE ? OR LOWER(isbn) LIKE ?) AND is_deleted = 0";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String searchTerm = "%" + keyword.toLowerCase() + "%";
            stmt.setString(1, searchTerm);
            stmt.setString(2, searchTerm);
            stmt.setString(3, searchTerm); // Tambahan
            
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
        String sql = "SELECT * FROM books WHERE id_book = ? AND is_deleted = 0";
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
    
    // Method di bawah ini tidak berhubungan langsung dengan data buku utama, jadi tidak diubah
    public List<Loan> getLoanHistoryByUser(int idUser) { /* ... (kode tidak berubah) ... */ return new ArrayList<>(); }
    public boolean updateBookRating(int idBook, float newRating) { /* ... (kode tidak berubah) ... */ return false; }
    public float getBookRating(int idBook) { /* ... (kode tidak berubah) ... */ return 0.0f; }

    // ✅ METHOD INI ADALAH KUNCI UTAMA, DIPERBARUI UNTUK MEMBACA ISBN
    private Book mapRowToBook(ResultSet rs) throws SQLException {
        return new Book(
            rs.getInt("id_book"),
            rs.getString("title"),
            rs.getString("author"),
            rs.getString("isbn"), // Tambahan
            rs.getString("cover_image_path"),
            rs.getString("book_file_path"),
            rs.getFloat("rating")
        );
    }
}