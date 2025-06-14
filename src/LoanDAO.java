import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LoanDAO {
    private Connection conn;
    private static final DateTimeFormatter DB_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public LoanDAO(Connection conn) {
        this.conn = conn;
        if (this.conn == null) {
            System.err.println("Koneksi database null saat inisialisasi LoanDAO!");
        }
    }

    /**
     * ✅ METHOD BARU UNTUK OTOMATISASI
     * Method ini akan mengubah status pinjaman dari 'approved' menjadi 'returned'
     * untuk semua buku yang sudah melewati tanggal kadaluwarsa (expiry_date)
     * milik seorang user.
     */
    public void expireUserLoans(int userId) {
        // Query untuk update status buku yang sudah kadaluwarsa
        String sql = "UPDATE loans SET status = 'returned', return_date = expiry_date " +
                     "WHERE id_user = ? AND status = 'approved' AND expiry_date < NOW()";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Berhasil mengupdate " + affectedRows + " pinjaman yang kadaluwarsa untuk user ID: " + userId);
            }
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
                    0
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