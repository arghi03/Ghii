import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LoanDAO {
    private Connection conn;

    public LoanDAO(Connection conn) {
        this.conn = conn;
        if (this.conn == null) {
            System.err.println("Koneksi database null saat inisialisasi LoanDAO!");
        }
    }

    private LocalDateTime getTimestampAsLocalDateTime(ResultSet rs, String columnName) throws SQLException {
        Timestamp ts = rs.getTimestamp(columnName);
        if (ts != null) {
            return ts.toLocalDateTime();
        }
        return null;
    }

    public void expireUserLoans(int userId) {
        String sql = "UPDATE loans SET status = 'returned', return_date = expiry_date " +
                     "WHERE id_user = ? AND status = 'approved' AND expiry_date < NOW()";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saat mencoba mengupdate pinjaman kadaluwarsa: " + e.getMessage());
        }
    }

    public boolean updateLoanStatus(int idLoan, String status, int approvedBy) {
        if (conn == null) {
            return false;
        }
        String sql;
        if ("approved".equalsIgnoreCase(status)) {
            sql = "UPDATE loans SET status = ?, approved_by = ?, approved_date = NOW(), expiry_date = DATE_ADD(NOW(), INTERVAL 7 DAY) WHERE id_loan = ?";
        } else {
            sql = "UPDATE loans SET status = ?, approved_by = ?, approved_date = NOW() WHERE id_loan = ?";
        }
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, approvedBy);
            stmt.setInt(3, idLoan);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating loan status for loan ID " + idLoan + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean isLoanApproved(int userId, int bookId) {
        String sql = "SELECT status, expiry_date FROM loans WHERE id_user = ? AND id_book = ? AND status = 'approved' AND NOW() < expiry_date ORDER BY approved_date DESC LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Loan> getPendingLoans() {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT l.id_loan, l.id_user, l.id_book, u.nama as username, b.title as book_title, l.request_date " +
                     "FROM loans l " +
                     "LEFT JOIN users u ON l.id_user = u.id_user " +
                     "LEFT JOIN books b ON l.id_book = b.id_book " +
                     "WHERE LOWER(TRIM(l.status)) = 'pending' " +
                     "ORDER BY l.request_date ASC";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Loan loan = new Loan(
                    rs.getInt("id_loan"),
                    rs.getInt("id_user"),
                    rs.getInt("id_book"),
                    "pending",
                    0,
                    getTimestampAsLocalDateTime(rs, "request_date"),
                    null,
                    null,
                    rs.getString("username"),
                    rs.getString("book_title")
                );
                loans.add(loan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loans;
    }

    public List<Loan> getLoanHistoryByUser(int idUser) {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT l.*, u.nama AS username, b.title AS book_title " +
                     "FROM loans l " +
                     "LEFT JOIN users u ON l.id_user = u.id_user " +
                     "LEFT JOIN books b ON l.id_book = b.id_book " +
                     "WHERE l.id_user = ? ORDER BY l.request_date DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUser);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Loan loan = new Loan(
                    rs.getInt("id_loan"),
                    rs.getInt("id_user"),
                    rs.getInt("id_book"),
                    rs.getString("status"),
                    rs.getInt("approved_by"),
                    getTimestampAsLocalDateTime(rs, "request_date"),
                    getTimestampAsLocalDateTime(rs, "approved_date"),
                    getTimestampAsLocalDateTime(rs, "return_date"),
                    rs.getString("username"),
                    rs.getString("book_title")
                );
                loan.setExpiryDate(getTimestampAsLocalDateTime(rs, "expiry_date"));
                loans.add(loan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loans;
    }

    public Book getLastReadBook(int userId) {
        if (conn == null) return null;
        String sql = "SELECT b.* FROM loans l " +
                     "JOIN books b ON l.id_book = b.id_book " +
                     "WHERE l.id_user = ? AND l.status = 'approved' " +
                     "ORDER BY l.approved_date DESC LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Book(
                    rs.getInt("id_book"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("isbn"),
                    rs.getString("cover_image_path"),
                    rs.getString("book_file_path"),
                    rs.getFloat("rating")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ✅✅✅ SEMUA METHOD STATISTIK DIKEMBALIKAN ISINYA ✅✅✅
    public int getTotalLoans() {
        if (conn == null) return 0;
        String sql = "SELECT COUNT(*) AS total FROM loans";
        try (PreparedStatement stmt = conn.prepareStatement(sql); 
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public Map<String, Integer> getTopBorrowedBooks(int limit) {
        Map<String, Integer> topBooks = new LinkedHashMap<>();
        if (conn == null) return topBooks;
        String sql = "SELECT b.title, COUNT(l.id_book) AS loan_count " +
                     "FROM loans l " +
                     "JOIN books b ON l.id_book = b.id_book " +
                     "GROUP BY l.id_book, b.title " +
                     "ORDER BY loan_count DESC " +
                     "LIMIT ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                topBooks.put(rs.getString("title"), rs.getInt("loan_count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topBooks;
    }

    public Map<String, Integer> getLoanStatusCounts() {
        Map<String, Integer> statusCounts = new LinkedHashMap<>();
        if (conn == null) return statusCounts;
        String sql = "SELECT status, COUNT(*) AS count FROM loans GROUP BY status";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String status = rs.getString("status");
                statusCounts.put(status.substring(0, 1).toUpperCase() + status.substring(1), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statusCounts;
    }
    
    public Map<LocalDate, Integer> getDailyLoanCounts(int days) {
        Map<LocalDate, Integer> dailyCounts = new LinkedHashMap<>();
        if (conn == null) return dailyCounts;
        String sql = "SELECT DATE(request_date) AS loan_date, COUNT(*) AS count " +
                     "FROM loans " +
                     "WHERE request_date >= CURDATE() - INTERVAL ? DAY " +
                     "GROUP BY loan_date " +
                     "ORDER BY loan_date ASC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, days);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                dailyCounts.put(rs.getDate("loan_date").toLocalDate(), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dailyCounts;
    }
    
    public boolean addLoan(int idUser, int idBook) {
        String sql = "INSERT INTO loans (id_user, id_book, status, request_date) VALUES (?, ?, 'pending', NOW())";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUser);
            stmt.setInt(2, idBook);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}