import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class SuggestionDAO {
    private Connection conn;

    public SuggestionDAO(Connection conn) {
        this.conn = conn;
    }

    public boolean addSuggestion(Suggestion suggestion) {
        if (conn == null) {
            System.err.println("Koneksi null, tidak bisa menambah saran.");
            return false;
        }
        String sql = "INSERT INTO book_suggestions (id_user, suggested_title, suggested_author, notes, status) VALUES (?, ?, ?, ?, 'pending')";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, suggestion.getUserId());
            stmt.setString(2, suggestion.getTitle());
            stmt.setString(3, suggestion.getAuthor());
            stmt.setString(4, suggestion.getNotes());
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error saat menambah saran: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
     
    public List<Suggestion> getAllSuggestions() {
        List<Suggestion> suggestions = new ArrayList<>();
        String sql = "SELECT s.*, u.nama as username " +
                     "FROM book_suggestions s " +
                     "JOIN users u ON s.id_user = u.id_user " +
                     "ORDER BY s.created_at DESC";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Suggestion suggestion = new Suggestion(
                    rs.getInt("id_suggestion"),
                    rs.getInt("id_user"),
                    rs.getString("username"),
                    rs.getString("suggested_title"),
                    rs.getString("suggested_author"),
                    rs.getString("notes"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at").toLocalDateTime()
                );
                suggestions.add(suggestion);
            }
        } catch (SQLException e) {
            System.err.println("Error saat mengambil semua saran: " + e.getMessage());
            e.printStackTrace();
        }
        return suggestions;
    }

    // ✅✅✅ METHOD BARU UNTUK HALAMAN RIWAYAT SARAN USER ✅✅✅
    /**
     * Mengambil semua saran buku dari SATU pengguna spesifik.
     * @param userId ID dari pengguna yang sarannya ingin diambil.
     * @return List dari objek Suggestion milik pengguna tersebut.
     */
    public List<Suggestion> getSuggestionsByUser(int userId) {
        List<Suggestion> suggestions = new ArrayList<>();
        // Query sama seperti getAllSuggestions, tapi dengan tambahan WHERE clause
        String sql = "SELECT s.*, u.nama as username " +
                     "FROM book_suggestions s " +
                     "JOIN users u ON s.id_user = u.id_user " +
                     "WHERE s.id_user = ? " +
                     "ORDER BY s.created_at DESC";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId); // Set parameter user ID
            
            try(ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Suggestion suggestion = new Suggestion(
                        rs.getInt("id_suggestion"),
                        rs.getInt("id_user"),
                        rs.getString("username"),
                        rs.getString("suggested_title"),
                        rs.getString("suggested_author"),
                        rs.getString("notes"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    suggestions.add(suggestion);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saat mengambil saran by user: " + e.getMessage());
            e.printStackTrace();
        }
        return suggestions;
    }

    public boolean updateSuggestionStatus(int suggestionId, String newStatus) {
        if (conn == null) return false;
        String sql = "UPDATE book_suggestions SET status = ? WHERE id_suggestion = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, suggestionId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error saat update status saran: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSuggestion(int suggestionId) {
        if (conn == null) return false;
        String sql = "DELETE FROM book_suggestions WHERE id_suggestion = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, suggestionId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error saat menghapus saran: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}