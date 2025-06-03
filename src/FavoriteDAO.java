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
            System.out.println("Buku (ID: " + bookId + ") sudah ada di favorit user (ID: " + userId + ").");
            return true; 
        }

        String sql = "INSERT INTO favorites (id_user, id_book, favorited_at) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis())); // Waktu saat ini
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Buku (ID: " + bookId + ") berhasil ditambahkan ke favorit user (ID: " + userId + ")");
                return true;
            }
        } catch (SQLException e) {
    
            if (e.getErrorCode() == 1062) { // Error code for duplicate entry in MySQL
                 System.out.println("Gagal menambahkan favorit: Buku (ID: " + bookId + ") sudah ada di favorit user (ID: " + userId + ") - dicegat oleh DB.");
            } else {
                System.err.println("Error adding favorite: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }

  
    public boolean removeFavorite(int userId, int bookId) {
        if (conn == null) {
            System.err.println("Koneksi null di removeFavorite.");
            return false;
        }
        String sql = "DELETE FROM favorites WHERE id_user = ? AND id_book = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Buku (ID: " + bookId + ") berhasil dihapus dari favorit user (ID: " + userId + ")");
                return true;
            } else {
                 System.out.println("Tidak ada buku (ID: " + bookId + ") di favorit user (ID: " + userId + ") untuk dihapus.");
                return false; // Tidak ada baris yang terhapus (mungkin memang tidak ada)
            }
        } catch (SQLException e) {
            System.err.println("Error removing favorite: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

   
    public boolean isFavorite(int userId, int bookId) {
        if (conn == null) {
            System.err.println("Koneksi null di isFavorite.");
            return false;
        }
        String sql = "SELECT id_favorite FROM favorites WHERE id_user = ? AND id_book = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Jika ada baris, berarti sudah favorit
            }
        } catch (SQLException e) {
            System.err.println("Error checking if book is favorite: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

   
    public List<Book> getUserFavoriteBooks(int userId) {
        List<Book> favoriteBooks = new ArrayList<>();
        if (conn == null) {
            System.err.println("Koneksi null di getUserFavoriteBooks.");
            return favoriteBooks;
        }
        // Query untuk mengambil detail buku yang difavoritkan
        // JOIN antara tabel favorites dan books
        String sql = "SELECT b.id_book, b.title, b.author, b.cover_image_path, b.book_file_path, b.rating " +
                     "FROM favorites f " +
                     "JOIN books b ON f.id_book = b.id_book " +
                     "WHERE f.id_user = ? " +
                     "ORDER BY f.favorited_at DESC"; // Urutkan berdasarkan kapan difavoritkan (terbaru dulu)

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                   
                    Book book = new Book(
                        rs.getInt("id_book"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("cover_image_path"),
                        rs.getString("book_file_path"),
                        rs.getFloat("rating")
                    );
                    favoriteBooks.add(book);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user favorite books: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("User (ID: " + userId + ") memiliki " + favoriteBooks.size() + " buku favorit.");
        return favoriteBooks;
    }
}
