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

    // ... (method getPendingLoans, updateLoanStatus, addLoan, isLoanApproved, getLoanHistoryByUser tetap sama) ...

    public List<Loan> getLoanHistoryByUser(int idUser) {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT l.id_loan, l.id_user, l.id_book, l.status, l.approved_by, " +
                     "l.request_date, l.approved_date, l.return_date, " +
                     "u.nama AS username, l.book_title " + 
                     "FROM loans l " +
                     "LEFT JOIN users u ON l.id_user = u.id_user " +
                     "WHERE l.id_user = ? ORDER BY l.request_date DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUser);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                loans.add(new Loan(
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
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching loan history for user ID " + idUser + ": " + e.getMessage());
            e.printStackTrace();
        }
        return loans;
    }

    /**
     * METHOD BARU: Mengubah status peminjaman menjadi 'returned' dan mencatat tanggal kembali.
     * @param loanId ID peminjaman yang akan dikembalikan.
     * @return true jika berhasil, false jika gagal.
     */
    public boolean returnBook(int loanId) {
        if (conn == null) {
            System.err.println("Koneksi null di returnBook.");
            return false;
        }

        // Query untuk update status dan tanggal kembali
        String sql = "UPDATE loans SET status = 'returned', return_date = NOW() WHERE id_loan = ? AND (status = 'approved' OR status = 'borrowed')";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, loanId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Buku untuk peminjaman ID " + loanId + " berhasil dikembalikan.");
                return true;
            } else {
                // Ini bisa terjadi jika status buku bukan 'approved'/'borrowed' atau loanId tidak ditemukan
                System.out.println("Gagal mengembalikan buku untuk peminjaman ID " + loanId + ". Mungkin sudah dikembalikan atau status tidak valid.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error saat mengembalikan buku (loan ID " + loanId + "): " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }


    // --- Helper dan method lain yang sudah ada ---
    public boolean isLoanApproved(int userId, int bookId) {
        String sql = "SELECT status FROM loans WHERE id_user = ? AND id_book = ? AND status = 'approved'";
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

    public List<Loan> getPendingLoans() { /* ... kode asli ... */ return new ArrayList<>(); }
    public boolean updateLoanStatus(int idLoan, String status, int approvedBy) { /* ... kode asli ... */ return false; }
    public boolean addLoan(int idUser, int idBook) { /* ... kode asli ... */ return false; }

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
