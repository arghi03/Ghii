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
 
    public boolean addBook(Book book) {
        if (conn == null) return false;
        String sql = "INSERT INTO books (id_book, title, author, cover_image_path, book_file_path, rating, is_deleted) VALUES (?, ?, ?, ?, ?, ?, 0)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, book.getIdBook());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, book.getCoverImagePath());
            stmt.setString(5, book.getBookFilePath());
            stmt.setFloat(6, book.getRating());
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

public boolean updateBook(Book book) {
    if (conn == null) {
        System.err.println("Koneksi null, tidak bisa update buku.");
        return false;
    }
    String sql = "UPDATE books SET title = ?, author = ?, rating = ?, cover_image_path = ?, book_file_path = ? WHERE id_book = ?";
    
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, book.getTitle());
        stmt.setString(2, book.getAuthor());
        stmt.setFloat(3, book.getRating());
        stmt.setString(4, book.getCoverImagePath());
        stmt.setString(5, book.getBookFilePath());
        stmt.setInt(6, book.getIdBook()); // ID buku untuk WHERE clause

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
 
    public List<Book> searchBooks(String keyword) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE (title LIKE ? OR author LIKE ?) AND is_deleted = 0";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String searchTerm = "%" + keyword + "%";
            stmt.setString(1, searchTerm);
            stmt.setString(2, searchTerm);
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
 
    public List<Loan> getLoanHistoryByUser(int idUser) {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT l.id_loan, l.id_book, l.request_date, l.return_date, l.approved_date, l.status, l.approved_by, b.title " +
                     "FROM loans l JOIN books b ON l.id_book = b.id_book " +
                     "WHERE l.id_user = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUser);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LocalDateTime requestDate = rs.getTimestamp("request_date") != null ? rs.getTimestamp("request_date").toLocalDateTime() : null;
                LocalDateTime returnDate = rs.getTimestamp("return_date") != null ? rs.getTimestamp("return_date").toLocalDateTime() : null;
                LocalDateTime approvedDate = rs.getTimestamp("approved_date") != null ? rs.getTimestamp("approved_date").toLocalDateTime() : null;

                Loan loan = new Loan(
                    rs.getInt("id_loan"),
                    idUser,
                    rs.getInt("id_book"),
                    rs.getString("status"),
                    rs.getInt("approved_by"),
                    requestDate,
                    approvedDate,
                    returnDate,
                    null,
                    rs.getString("title")
                );
                loans.add(loan);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching loan history: " + e.getMessage());
        }
        return loans;
    }

    public boolean updateBookRating(int idBook, float newRating) {
        if (conn == null) return false;
        String sql = "UPDATE books SET rating = ? WHERE id_book = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setFloat(1, newRating);
            stmt.setInt(2, idBook);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public float getBookRating(int idBook) {
        if (conn == null) return 0.0f;
        String sql = "SELECT rating FROM books WHERE id_book = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idBook);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getFloat("rating");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching book rating: " + e.getMessage());
        }
        return 0.0f;
    }
     
    private Book mapRowToBook(ResultSet rs) throws SQLException {
        return new Book(
            rs.getInt("id_book"),
            rs.getString("title"),
            rs.getString("author"),
            rs.getString("cover_image_path"),
            rs.getString("book_file_path"),
            rs.getFloat("rating")
        );
    }
}