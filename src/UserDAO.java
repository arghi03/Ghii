import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// Asumsi kelas User sudah ada di project Anda
// public class User { private int idUser,idRole; private String nama,nim,email,nomorTelepon,password,otpCode; private boolean isVerified,isActive; private LocalDateTime otpExpiry; public User(int a,String b,String c,String d,String e,String f,int g,boolean h,boolean i){} public int getIdUser(){return 1;} public String getNama(){return "User";} public String getNim(){return "123";} public String getEmail(){return "user@mail.com";} public String getNomorTelepon(){return "08123";} public String getPassword(){return "pass";} public int getIdRole(){return 1;} public boolean isVerified(){return true;} public boolean isActive(){return true;} public String getOtpCode(){return "1234";} public LocalDateTime getOtpExpiry(){return null;} public void setOtpCode(String c){} public void setOtpExpiry(LocalDateTime d){} public void setVerified(boolean b){} }

public class UserDAO {
    private Connection conn;
    private static final DateTimeFormatter DB_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public UserDAO(Connection conn) {
        this.conn = conn;
        if (this.conn == null) {
            System.err.println("Koneksi database null saat inisialisasi UserDAO!");
        }
    }

    public boolean register(User user) {
        if (conn == null) return false;
        
        String sql = "INSERT INTO users (id_user, nama, nim, email, nomor_telepon, password, id_role, is_verified, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, 0, 1)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, user.getIdUser());
            stmt.setString(2, user.getNama());
            stmt.setString(3, user.getNim());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getNomorTelepon());
            stmt.setString(6, user.getPassword());
            stmt.setInt(7, user.getIdRole());
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Registration failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public User login(String nama, String password) {
        if (conn == null) return null;
        
        String sql = "SELECT * FROM users WHERE nama = ? AND password = ? AND is_active = true";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nama);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = createUserFromResultSet(rs);
                System.out.println("Login berhasil untuk: " + nama + ", isVerified: " + user.isVerified());
                return user;
            } else {
                System.out.println("Login gagal: User tidak ditemukan, password salah, atau akun tidak aktif.");
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error saat login: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateUser(User user) {
        if (conn == null) return false;

        String sql = "UPDATE users SET nama = ?, nim = ?, email = ?, nomor_telepon = ?, id_role = ?, is_verified = ?, is_active = ?, otp_code = ?, otp_expiry = ? WHERE id_user = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getNama());
            stmt.setString(2, user.getNim());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getNomorTelepon());
            stmt.setInt(5, user.getIdRole());
            stmt.setBoolean(6, user.isVerified());
            stmt.setBoolean(7, user.isActive());
            stmt.setString(8, user.getOtpCode());
            if (user.getOtpExpiry() != null) {
                stmt.setString(9, user.getOtpExpiry().format(DB_DATETIME_FORMATTER));
            } else {
                stmt.setNull(9, Types.TIMESTAMP);
            }
            stmt.setInt(10, user.getIdUser());
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUserRole(int userId, int newRoleId) {
        if (conn == null) {
            System.err.println("Tidak bisa update role, koneksi DB null.");
            return false;
        }

        String sql = "UPDATE users SET id_role = ? WHERE id_user = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newRoleId);
            stmt.setInt(2, userId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Gagal mengubah role untuk user ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void saveOtp(User user, String otpCode) {
        if (user == null) {
            System.err.println("User tidak boleh null untuk menyimpan OTP.");
            return;
        }
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);
        user.setOtpCode(otpCode);
        user.setOtpExpiry(expiry);
        if (!updateUser(user)) { 
            System.err.println("Gagal menyimpan OTP untuk user: " + user.getEmail());
        } else {
            System.out.println("OTP berhasil disimpan untuk user: " + user.getEmail());
        }
    }

    public boolean approveUser(int idUser) {
        User user = getUserById(idUser);
        if (user != null) {
            user.setVerified(true);
            return updateUser(user);
        }
        return false;
    }

    public boolean rejectUser(int idUser) {
        try {
            conn.setAutoCommit(false);

            String[] deleteQueries = {
                "DELETE FROM book_suggestions WHERE id_user = ?",
                "DELETE FROM favorites WHERE id_user = ?",
                "DELETE FROM loans WHERE id_user = ?"
            };

            for (String query : deleteQueries) {
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, idUser);
                    stmt.executeUpdate();
                }
            }
            
            String sqlUser = "DELETE FROM users WHERE id_user = ?";
            try (PreparedStatement stmtUser = conn.prepareStatement(sqlUser)) {
                stmtUser.setInt(1, idUser);
                int affectedRows = stmtUser.executeUpdate();
                if (affectedRows > 0) {
                    conn.commit();
                    return true;
                }
            }
            
            conn.rollback();
            return false;

        } catch (SQLException e) {
            System.err.println("Error rejecting user: " + e.getMessage());
            try { if(conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { if(conn != null) conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }
    
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        if (conn == null) return users;
        String sql = "SELECT * FROM users ORDER BY id_user";
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(createUserFromResultSet(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }

    // ✅✅✅ METHOD BARU UNTUK STATISTIK ✅✅✅
    public int getTotalUsers() {
        if (conn == null) return 0;
        String sql = "SELECT COUNT(*) AS total FROM users";
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public User getUserById(int idUser) {
        if (conn == null) return null;
        String sql = "SELECT * FROM users WHERE id_user = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUser);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return createUserFromResultSet(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public User getUserByNameAndEmail(String nama, String email) {
        if (conn == null) return null;
        String sql = "SELECT * FROM users WHERE nama = ? AND email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nama);
            stmt.setString(2, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return createUserFromResultSet(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
    
    public int getNewUserId() {
        if (conn == null) throw new IllegalStateException("Koneksi database tidak tersedia");
        String sql = "SELECT MAX(id_user) + 1 AS new_id FROM users";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int newId = rs.getInt("new_id");
                return (newId <= 0) ? 1 : newId;
            } else {
                return 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Gagal generate ID user baru: " + e.getMessage());
        }
    }

    private User createUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User(
            rs.getInt("id_user"),
            rs.getString("nama"),
            rs.getString("nim"),
            rs.getString("email"),
            rs.getString("nomor_telepon"),
            rs.getString("password"),
            rs.getInt("id_role"),
            rs.getBoolean("is_verified"),
            rs.getBoolean("is_active")
        );
        user.setOtpCode(rs.getString("otp_code"));
        String otpExpiryStr = rs.getString("otp_expiry");
        if (otpExpiryStr != null && !otpExpiryStr.trim().isEmpty()) {
            user.setOtpExpiry(LocalDateTime.parse(otpExpiryStr, DB_DATETIME_FORMATTER));
        }
        return user;
    }
}
