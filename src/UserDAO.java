import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public static boolean register(User user) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.connect();
            if (conn == null) {
                System.err.println("Registrasi gagal: Koneksi database null");
                return false;
            }

            String query = "INSERT INTO users (Nama, NIM, Nomor_Telepon, Email, Password, role) VALUES (?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, user.getNama());
            stmt.setString(2, user.getNim());
            stmt.setString(3, user.getNomorTelepon());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getPassword());
            stmt.setString(6, user.getRole());
            
            System.out.println("Menyimpan user: " + user.getNama() + ", " + user.getEmail() + ", role: " + user.getRole());
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Registrasi berhasil, baris terpengaruh: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Gagal registrasi: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    public static boolean login(String nama, String email, String password) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.connect();
            if (conn == null) {
                System.err.println("Login gagal: Koneksi database null");
                return false;
            }

            String query = "SELECT * FROM users WHERE Nama = ? AND Email = ? AND Password = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, nama);
            stmt.setString(2, email);
            stmt.setString(3, password);
            
            System.out.println("Mencoba login: Nama=" + nama + ", Email=" + email);
            rs = stmt.executeQuery();
            boolean success = rs.next();
            System.out.println("Login " + (success ? "berhasil" : "gagal") + " untuk " + nama);
            return success;
        } catch (SQLException e) {
            System.err.println("Gagal login: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    public static User getUserByNameAndEmail(String nama, String email) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.connect();
            if (conn == null) {
                System.err.println("Gagal mengambil data pengguna: Koneksi database null");
                return null;
            }

            String query = "SELECT id_user, Nama, NIM, Nomor_Telepon, Email, Password, role FROM users WHERE Nama = ? AND Email = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, nama);
            stmt.setString(2, email);
            
            System.out.println("Mengambil data pengguna: Nama=" + nama + ", Email=" + email);
            rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User(
                    rs.getInt("id_user"),
                    rs.getString("Nama"),
                    rs.getString("NIM"),
                    rs.getString("Nomor_Telepon"),
                    rs.getString("Email"),
                    rs.getString("Password"),
                    rs.getString("role")
                );
                System.out.println("Data pengguna ditemukan: " + user.getNama() + ", role: " + user.getRole());
                return user;
            } else {
                System.out.println("Pengguna tidak ditemukan: " + nama);
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengambil data pengguna: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    public static boolean updateUser(User user) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.connect();
            if (conn == null) {
                System.err.println("Gagal memperbarui pengguna: Koneksi database null");
                return false;
            }

            String query = "UPDATE users SET Nama = ?, Nomor_Telepon = ? WHERE id_user = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, user.getNama());
            stmt.setString(2, user.getNomorTelepon());
            stmt.setInt(3, user.getIdUser());
            
            System.out.println("Memperbarui pengguna: " + user.getNama() + ", id_user=" + user.getIdUser());
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Pembaruan berhasil, baris terpengaruh: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Gagal memperbarui pengguna: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
}