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
    
    // ✅✅✅ METHOD BARU YANG MENGGUNAKAN TRANSAKSI ✅✅✅
    /**
     * Menambahkan buku baru dan secara opsional memenuhi (approve) saran buku dalam satu transaksi.
     * Jika suggestionId adalah 0 atau kurang, hanya buku yang akan ditambahkan.
     * Jika suggestionId valid, buku akan ditambahkan DAN status saran akan diubah menjadi 'approved'.
     * Jika salah satu proses gagal, keduanya akan dibatalkan (rollback).
     *
     * @param book Objek buku yang akan ditambahkan.
     * @param suggestionId ID dari saran yang dipenuhi. Kirim 0 jika tidak ada saran terkait.
     * @return true jika semua operasi berhasil, false jika gagal.
     */
    public boolean addBookAndFulfillSuggestion(Book book, int suggestionId) {
        if (conn == null) return false;

        String addBookSQL = "INSERT INTO books (id_book, title, author, isbn, classification_code, cover_image_path, book_file_path, rating, is_deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)";
        String approveSuggestionSQL = "UPDATE book_suggestions SET status = 'approved' WHERE id_suggestion = ?";
        
        try {
            // 1. Memulai mode transaksi
            conn.setAutoCommit(false);

            // 2. Eksekusi penambahan buku
            try (PreparedStatement addBookStmt = conn.prepareStatement(addBookSQL)) {
                addBookStmt.setInt(1, book.getIdBook());
                addBookStmt.setString(2, book.getTitle());
                addBookStmt.setString(3, book.getAuthor());
                addBookStmt.setString(4, book.getIsbn());
                addBookStmt.setString(5, book.getClassificationCode());
                addBookStmt.setString(6, book.getCoverImagePath());
                addBookStmt.setString(7, book.getBookFilePath());
                addBookStmt.setFloat(8, book.getRating());
                addBookStmt.executeUpdate();
            }

            // 3. Jika ada suggestionId, eksekusi update status saran
            if (suggestionId > 0) {
                try (PreparedStatement approveSuggestionStmt = conn.prepareStatement(approveSuggestionSQL)) {
                    approveSuggestionStmt.setInt(1, suggestionId);
                    approveSuggestionStmt.executeUpdate();
                }
            }
            
            // 4. Jika semua berhasil, commit transaksi
            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Transaksi gagal! Melakukan rollback...");
            e.printStackTrace();
            try {
                // 5. Jika terjadi error, batalkan semua perubahan (rollback)
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Gagal melakukan rollback!");
                rollbackEx.printStackTrace();
            }
            return false;
        } finally {
            try {
                // 6. Kembalikan ke mode auto-commit (sangat penting!)
                conn.setAutoCommit(true);
            } catch (SQLException finalEx) {
                finalEx.printStackTrace();
            }
        }
    }
    
    // Method addBook lama bisa kita hapus atau biarkan jika masih dipakai di tempat lain.
    // Untuk sekarang kita biarkan saja.
    public boolean addBook(Book book) {
        if (conn == null) return false;
        // Ini pada dasarnya adalah memanggil method baru tanpa suggestionId
        return addBookAndFulfillSuggestion(book, 0);
    }
    
    // --- METHOD LAINNYA TIDAK BERUBAH ---

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
    
    public List<Loan> getLoanHistoryByUser(int idUser) { return new ArrayList<>(); }
    public boolean updateBookRating(int idBook, float newRating) { return false; }
    public float getBookRating(int idBook) { return 0.0f; }

    private Book mapRowToBook(ResultSet rs) throws SQLException {
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