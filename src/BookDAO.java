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
        }
    }

    public Connection getConnection() {
        return conn;
    }

    public boolean addBook(Book book) {
        if (conn == null) {
            System.err.println("Koneksi database null! Tidak bisa menambah buku.");
            return false;
        }

        String sql = "INSERT INTO books (id_book, title, author, cover_image_path, book_file_path, rating) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, book.getIdBook());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, book.getCoverImagePath());
            stmt.setString(5, book.getBookFilePath());
            stmt.setFloat(6, book.getRating());
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
                    rs.getString("book_file_path"),
                    rs.getFloat("rating")
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

    public List<Book> searchBooks(String keyword) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String searchTerm = "%" + keyword + "%";
            stmt.setString(1, searchTerm);
            stmt.setString(2, searchTerm);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Book book = new Book(
                    rs.getInt("id_book"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("cover_image_path"),
                    rs.getString("book_file_path"),
                    rs.getFloat("rating")
                );
                books.add(book);
            }
        } catch (SQLException e) {
            System.err.println("Error searching books: " + e.getMessage());
        }
        return books;
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
                LocalDateTime requestDate = rs.getTimestamp("request_date") != null ? 
                    rs.getTimestamp("request_date").toLocalDateTime() : null;
                LocalDateTime returnDate = rs.getTimestamp("return_date") != null ? 
                    rs.getTimestamp("return_date").toLocalDateTime() : null;
                LocalDateTime approvedDate = rs.getTimestamp("approved_date") != null ? 
                    rs.getTimestamp("approved_date").toLocalDateTime() : null;

                Loan loan = new Loan(
                    rs.getInt("id_loan"),
                    idUser,
                    rs.getInt("id_book"),
                    rs.getString("status"),
                    rs.getInt("approved_by"),
                    requestDate,
                    approvedDate,
                    returnDate,
                    null, // username gak diambil dari DB, bisa diisi via UserDAO kalo perlu
                    rs.getString("title")
                );
                loans.add(loan);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching loan history: " + e.getMessage());
        }
        return loans;
    }

    // Method baru untuk ambil buku berdasarkan ID
    public Book getBookById(int idBook) {
        if (conn == null) {
            System.err.println("Koneksi database null! Tidak bisa ambil buku.");
            return null;
        }

        String sql = "SELECT * FROM books WHERE id_book = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idBook);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Book(
                    rs.getInt("id_book"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("cover_image_path"),
                    rs.getString("book_file_path"),
                    rs.getFloat("rating")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching book by ID: " + e.getMessage());
        }
        return null;
    }

    // Method untuk update rating
    public boolean updateBookRating(int idBook, float newRating) {
        if (conn == null) {
            System.err.println("Koneksi database null! Tidak bisa update rating.");
            return false;
        }

        String sql = "UPDATE books SET rating = (SELECT AVG(r.rating) FROM (SELECT ? AS rating) r) " +
                     "WHERE id_book = ? AND (rating IS NULL OR (SELECT COUNT(*) FROM loans WHERE id_book = ? AND status = 'approved') > 0)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setFloat(1, newRating);
            stmt.setInt(2, idBook);
            stmt.setInt(3, idBook);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Rating buku berhasil diupdate: " + idBook);
                return true;
            } else {
                System.out.println("Gagal update rating buku: " + idBook);
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error updating book rating: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Method untuk ambil rating rata-rata
    public float getBookRating(int idBook) {
        if (conn == null) {
            System.err.println("Koneksi database null! Tidak bisa ambil rating.");
            return 0.0f;
        }

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
}