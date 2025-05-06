import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LoanDAO {
    private Connection conn;

    public LoanDAO(Connection conn) {
        this.conn = conn;
    }

    public List<Loan> getPendingLoans() {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT id_loan, id_user, id_book, status, approved_by, request_date, approved_date, username, book_title " +
                     "FROM loans WHERE status = 'pending'";
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String requestDateStr = rs.getString("request_date");
                String approvedDateStr = rs.getString("approved_date");
                LocalDateTime requestDate = requestDateStr != null ?
                    LocalDateTime.parse(requestDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
                LocalDateTime approvedDate = approvedDateStr != null ?
                    LocalDateTime.parse(approvedDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
                loans.add(new Loan(
                    rs.getInt("id_loan"),
                    rs.getInt("id_user"),
                    rs.getInt("id_book"),
                    rs.getString("status"),
                    rs.getInt("approved_by"),
                    requestDate,
                    approvedDate,
                    rs.getString("username"),
                    rs.getString("book_title")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching pending loans: " + e.getMessage());
        }
        return loans;
    }

    public boolean updateLoanStatus(int idLoan, String status, int approvedBy) {
        String sql;
        if ("approved".equals(status)) {
            sql = "UPDATE loans SET status = ?, approved_by = ?, approved_date = NOW() WHERE id_loan = ?";
        } else {
            sql = "UPDATE loans SET status = ?, approved_by = ? WHERE id_loan = ?";
        }
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, approvedBy);
            if ("approved".equals(status)) {
                stmt.setInt(3, idLoan);
            } else {
                stmt.setInt(3, idLoan);
            }
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating loan status: " + e.getMessage());
            return false;
        }
    }

    public boolean addLoan(int idUser, int idBook) {
        String sqlUser = "SELECT nama FROM users WHERE id_user = ?";
        String sqlBook = "SELECT title FROM books WHERE id_book = ?";
        String username;
        String bookTitle;

        // Ambil nama user
        try (PreparedStatement stmtUser = conn.prepareStatement(sqlUser)) {
            stmtUser.setInt(1, idUser);
            ResultSet rsUser = stmtUser.executeQuery();
            if (rsUser.next()) {
                username = rsUser.getString("nama");
            } else {
                System.err.println("User dengan id " + idUser + " tidak ditemukan!");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching username: " + e.getMessage());
            return false;
        }

        // Ambil judul buku
        try (PreparedStatement stmtBook = conn.prepareStatement(sqlBook)) {
            stmtBook.setInt(1, idBook);
            ResultSet rsBook = stmtBook.executeQuery();
            if (rsBook.next()) {
                bookTitle = rsBook.getString("title");
            } else {
                System.err.println("Buku dengan id " + idBook + " tidak ditemukan!");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching book title: " + e.getMessage());
            return false;
        }

        // Insert ke tabel loans dengan username dan book_title
        String sql = "INSERT INTO loans (id_user, id_book, status, approved_by, request_date, username, book_title) " +
                     "VALUES (?, ?, 'pending', NULL, NOW(), ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUser);
            stmt.setInt(2, idBook);
            stmt.setString(3, username);
            stmt.setString(4, bookTitle);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error adding loan: " + e.getMessage());
            return false;
        }
    }
}