import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class FavoriteDAO {
    private Connection conn;

    public FavoriteDAO(Connection conn) {
        this.conn = conn;
        if (this.conn == null) {
            System.err.println("Koneksi database null saat inisialisasi FavoriteDAO!");
        }
    }

    public boolean addFavorite(int userId, int bookId) {
        if (conn == null) {
            System.err.println("Koneksi null di addFavorite.");
            return false;
        }
        
        if (isFavorite(userId, bookId)) {
            return true; 
        }

        String sql = "INSERT INTO favorites (id_user, id_book, favorited_at) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { 
                 return true;
            } else {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean removeFavorite(int userId, int bookId) {
        if (conn == null) return false;
        String sql = "DELETE FROM favorites WHERE id_user = ? AND id_book = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
   
    public boolean isFavorite(int userId, int bookId) {
        if (conn == null) return false;
        String sql = "SELECT id_favorite FROM favorites WHERE id_user = ? AND id_book = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Book> getUserFavoriteBooks(int userId) {
        List<Book> favoriteBooks = new ArrayList<>();
        if (conn == null) return favoriteBooks;
        
        // ✅ PERUBAHAN 1: Menambahkan b.classification_code ke query
        String sql = "SELECT b.id_book, b.title, b.author, b.isbn, b.classification_code, b.cover_image_path, b.book_file_path, b.rating " +
                     "FROM favorites f " +
                     "JOIN books b ON f.id_book = b.id_book " +
                     "WHERE f.id_user = ? " +
                     "ORDER BY f.favorited_at DESC";  

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                   
                    // ✅ PERUBAHAN 2: Menyesuaikan konstruktor Book
                    Book book = new Book(
                        rs.getInt("id_book"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getString("classification_code"), // Ditambahkan
                        rs.getString("cover_image_path"),
                        rs.getString("book_file_path"),
                        rs.getFloat("rating")
                    );
                    favoriteBooks.add(book);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return favoriteBooks;
    }
}