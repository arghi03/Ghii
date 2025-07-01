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
    private static final DateTimeFormatter DB_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public LoanDAO(Connection conn) {
        this.conn = conn;
        if (this.conn == null) {
            System.err.println("Koneksi database null saat inisialisasi LoanDAO!");
        }
    }
 
    public void expireUserLoans(int userId) {
        String sql = "UPDATE loans SET status = 'returned', return_date = expiry_date " +
                     "WHERE id_user = ? AND status = 'approved' AND expiry_date < NOW()";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saat mencoba mengupdate pinjaman kadaluwarsa untuk user ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean updateLoanStatus(int idLoan, String status, int approvedBy) {
        if (conn == null) {
            System.err.println("Koneksi null di updateLoanStatus.");
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
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking if loan is approved: " + e.getMessage());
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
                     "WHERE l.status = 'pending' ORDER BY l.request_date ASC";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Loan loan = new Loan(
                    rs.getInt("id_loan"),
                    rs.getInt("id_user"),
                    rs.getInt("id_book"),
                    "pending",
                    0, null, null, null, null, null
                );
                loan.setUsername(rs.getString("username"));
                loan.setBookTitle(rs.getString("book_title"));
                loan.setRequestDate(parseDateTime(rs.getString("request_date")));
                loans.add(loan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loans;
    }


    public List<Loan> getLoanHistoryByUser(int idUser) {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT l.id_loan, l.id_user, l.id_book, l.status, l.approved_by, " +
                     "l.request_date, l.approved_date, l.return_date, l.expiry_date, " +
                     "u.nama AS username, b.title AS book_title " +
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
                    parseDateTime(rs.getString("request_date")),
                    parseDateTime(rs.getString("approved_date")),
                    parseDateTime(rs.getString("return_date")),
                    rs.getString("username"), 
                    rs.getString("book_title")
                );
                loan.setExpiryDate(parseDateTime(rs.getString("expiry_date"))); 
                loans.add(loan);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching loan history for user ID " + idUser + ": " + e.getMessage());
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
                    rs.getString("cover_image_path"),
                    rs.getString("book_file_path"),
                    rs.getFloat("rating")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching last read book for user ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public int getTotalLoans() {
        if (conn == null) return 0;
        String sql = "SELECT COUNT(*) AS total FROM loans";
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
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
                String title = rs.getString("title");
                int count = rs.getInt("loan_count");
                topBooks.put(title, count);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching top borrowed books: " + e.getMessage());
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
                int count = rs.getInt("count");
                String formattedStatus = status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
                statusCounts.put(formattedStatus, count);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching loan status counts: " + e.getMessage());
            e.printStackTrace();
        }
        return statusCounts;
    }

    /**
     *
     * Mengambil data jumlah peminjaman per hari selama rentang waktu tertentu.
     * @param days Jumlah hari ke belakang yang ingin ditarik datanya.
     * @return Map dengan key LocalDate (tanggal) dan value Integer (jumlah peminjaman).
     */
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
                LocalDate date = rs.getDate("loan_date").toLocalDate();
                int count = rs.getInt("count");
                dailyCounts.put(date, count);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching daily loan counts: " + e.getMessage());
            e.printStackTrace();
        }
        return dailyCounts;
    }


    public boolean addLoan(int idUser, int idBook) {
        String sql = "INSERT INTO loans (id_user, id_book, status, request_date) VALUES (?, ?, 'pending', NOW())";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUser);
            stmt.setInt(2, idBook);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        try {
            if (dateTimeStr.endsWith(".0")) {
                dateTimeStr = dateTimeStr.substring(0, dateTimeStr.length() - 2);
            }
            return LocalDateTime.parse(dateTimeStr, DB_DATETIME_FORMATTER);
        } catch (Exception e) { 
            System.err.println("Failed to parse date-time string: " + dateTimeStr + " - " + e.getMessage());
            return null;
        }
    }
}