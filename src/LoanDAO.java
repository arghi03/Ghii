import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LoanDAO {
    private Connection conn;
    // Definisikan formatter di sini agar bisa dipakai ulang
    private static final DateTimeFormatter DB_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public LoanDAO(Connection conn) {
        this.conn = conn;
        if (this.conn == null) {
            System.err.println("Koneksi database null saat inisialisasi LoanDAO!");
        }
    }

    public List<Loan> getPendingLoans() {
        List<Loan> loans = new ArrayList<>();
        // Ambil juga return_date jika ada di tabel loans dan diperlukan untuk objek Loan
        String sql = "SELECT l.id_loan, l.id_user, l.id_book, l.status, l.approved_by, " +
                     "l.request_date, l.approved_date, l.return_date, " + // Tambahkan l.return_date
                     "u.nama AS username, l.book_title " + // Ambil book_title dari tabel loans
                     "FROM loans l " +
                     "LEFT JOIN users u ON l.id_user = u.id_user " + // Gunakan LEFT JOIN untuk username jika user mungkin tidak ada
                     "WHERE l.status = 'pending' ORDER BY l.request_date DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                LocalDateTime requestDate = parseDateTime(rs.getString("request_date"));
                LocalDateTime approvedDate = parseDateTime(rs.getString("approved_date"));
                LocalDateTime returnDate = parseDateTime(rs.getString("return_date")); // Parse return_date

                loans.add(new Loan(
                    rs.getInt("id_loan"),
                    rs.getInt("id_user"),
                    rs.getInt("id_book"),
                    rs.getString("status"),
                    rs.getInt("approved_by"),
                    requestDate,
                    approvedDate,
                    returnDate, // Masukkan returnDate ke konstruktor
                    rs.getString("username"),
                    rs.getString("book_title")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching pending loans: " + e.getMessage());
            e.printStackTrace();
        }
        return loans;
    }

    public boolean updateLoanStatus(int idLoan, String status, int approvedBy) {
        String sql;
        // Jika status 'returned', kita juga set return_date
        if ("approved".equalsIgnoreCase(status)) {
            sql = "UPDATE loans SET status = ?, approved_by = ?, approved_date = NOW() WHERE id_loan = ?";
        } else if ("returned".equalsIgnoreCase(status)) {
            sql = "UPDATE loans SET status = ?, return_date = NOW() WHERE id_loan = ?";
        }
         else { // Untuk status lain seperti 'rejected' atau 'cancelled'
            sql = "UPDATE loans SET status = ?, approved_by = ? WHERE id_loan = ?"; // approved_by mungkin ID admin yang menolak
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            if ("approved".equalsIgnoreCase(status) || !"returned".equalsIgnoreCase(status)) { // Hanya set approved_by jika bukan 'returned' atau jika 'approved'
                 stmt.setInt(2, approvedBy);
                 stmt.setInt(3, idLoan);
            } else { // Untuk 'returned', parameter kedua adalah idLoan
                 stmt.setInt(2, idLoan);
            }
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating loan status for loan ID " + idLoan + " to " + status + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean addLoan(int idUser, int idBook) {
        String sqlBook = "SELECT title FROM books WHERE id_book = ?";
        String bookTitle;

        // 1. Dapatkan judul buku dari tabel books
        try (PreparedStatement stmtBook = conn.prepareStatement(sqlBook)) {
            stmtBook.setInt(1, idBook);
            ResultSet rsBook = stmtBook.executeQuery();
            if (rsBook.next()) {
                bookTitle = rsBook.getString("title");
            } else {
                System.err.println("Buku dengan id " + idBook + " tidak ditemukan saat mencoba meminjam!");
                return false; // Buku tidak ditemukan
            }
        } catch (SQLException e) {
            System.err.println("Error fetching book title for loan: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        // 2. Masukkan data peminjaman ke tabel loans
        // Diasumsikan 'approved_by' bisa NULL jika status 'pending' atau 'borrowed' (langsung)
        // request_date akan diisi NOW()
        // book_title diambil dari langkah 1
        // status awal adalah 'pending' jika butuh approval, atau 'borrowed' jika langsung
        // Untuk contoh ini, kita set 'pending'
        String sql = "INSERT INTO loans (id_user, id_book, status, request_date, book_title) " +
                     "VALUES (?, ?, 'pending', NOW(), ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUser);
            stmt.setInt(2, idBook);
            stmt.setString(3, bookTitle);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Peminjaman untuk buku '" + bookTitle + "' oleh user ID " + idUser + " berhasil diajukan.");
                return true;
            } else {
                System.out.println("Gagal menambahkan data peminjaman ke database, tidak ada baris yang terpengaruh.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error adding loan: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean isLoanApproved(int userId, int bookId) {
        String sql = "SELECT status FROM loans WHERE id_user = ? AND id_book = ? AND status = 'approved'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // True jika ada baris yang cocok (berarti sudah approved)
        } catch (SQLException e) {
            System.err.println("Error checking if loan is approved: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // METHOD BARU YANG DITAMBAHKAN
    public List<Loan> getLoanHistoryByUser(int idUser) {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT l.id_loan, l.id_user, l.id_book, l.status, l.approved_by, " +
                     "l.request_date, l.approved_date, l.return_date, " +
                     "u.nama AS username, l.book_title " + // Menggunakan book_title dari tabel loans
                     "FROM loans l " +
                     "LEFT JOIN users u ON l.id_user = u.id_user " + // Untuk mendapatkan nama user yang meminjam (jika perlu)
                                                                  // atau bisa juga JOIN users u_approver ON l.approved_by = u_approver.id_user untuk nama approver
                     "WHERE l.id_user = ? ORDER BY l.request_date DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUser);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                LocalDateTime requestDate = parseDateTime(rs.getString("request_date"));
                LocalDateTime approvedDate = parseDateTime(rs.getString("approved_date"));
                LocalDateTime returnDate = parseDateTime(rs.getString("return_date"));

                // Pastikan konstruktor Loan sesuai
                loans.add(new Loan(
                    rs.getInt("id_loan"),
                    rs.getInt("id_user"),
                    rs.getInt("id_book"),
                    rs.getString("status"),
                    rs.getInt("approved_by"),
                    requestDate,
                    approvedDate,
                    returnDate,
                    rs.getString("username"), // Ini nama user yang meminjam
                    rs.getString("book_title") // Ini judul buku dari tabel loans
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching loan history for user ID " + idUser + ": " + e.getMessage());
            e.printStackTrace();
        }
        return loans;
    }

    // Helper method untuk parse string tanggal dari DB ke LocalDateTime
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        try {
            // Jika format di DB adalah DATETIME (misal '2023-10-27 10:15:30.0')
            // .0 di akhir mungkin perlu di-handle jika ada, atau pastikan DB_DATETIME_FORMATTER sesuai
            if (dateTimeStr.endsWith(".0")) {
                 dateTimeStr = dateTimeStr.substring(0, dateTimeStr.length() - 2);
            }
            return LocalDateTime.parse(dateTimeStr, DB_DATETIME_FORMATTER);
        } catch (Exception e) { // Tangkap semua exception parsing
            System.err.println("Failed to parse date-time string: " + dateTimeStr + " - " + e.getMessage());
            return null; // Kembalikan null jika parsing gagal
        }
    }
}
