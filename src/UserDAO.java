import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
        if (conn == null) {
            System.err.println("Koneksi database null! Tidak bisa register.");
            return false;
        }
        // Saat register, is_active di-set ke TRUE secara default oleh database
        String sqlUser = "INSERT INTO users (id_user, nama, nim, email, nomor_telepon, password, is_verified) VALUES (?, ?, ?, ?, ?, ?, ?)";
        ProfileDAO profileDAO = new ProfileDAO(conn);

        try {
            conn.setAutoCommit(false); 
            System.out.println("Mendaftarkan user: " + user.getEmail());

            try (PreparedStatement stmtUser = conn.prepareStatement(sqlUser)) {
                stmtUser.setInt(1, user.getIdUser());
                stmtUser.setString(2, user.getNama());
                stmtUser.setString(3, user.getNim());
                stmtUser.setString(4, user.getEmail());
                stmtUser.setString(5, user.getNomorTelepon());
                stmtUser.setString(6, user.getPassword());
                stmtUser.setBoolean(7, false); // is_verified
                stmtUser.executeUpdate();
            }

            // Asumsi ProfileDAO dan Profile class ada
            Profile profile = new Profile(); 
            profile.setIdUser(user.getIdUser());
            profile.setNama(user.getNama());
            profile.setNim(user.getNim());
            profile.setEmail(user.getEmail());
            profile.setNomorTelepon(user.getNomorTelepon());
            profile.setIdRole(user.getIdRole()); 
            boolean profileSuccess = profileDAO.insertProfile(profile); 
            if (!profileSuccess) {
                throw new SQLException("Gagal insert ke tabel profile");
            }

            conn.commit();
            System.out.println("Registrasi berhasil untuk: " + user.getEmail());
            return true;
        } catch (SQLException e) {
            System.err.println("Registration failed: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
            return false;
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Failed to reset auto-commit: " + e.getMessage());
            }
        }
    }

    public User login(String nama, String password) {
        if (conn == null) {
            System.err.println("Koneksi database null! Tidak bisa login.");
            return null;
        }
        String sql = "SELECT u.id_user, u.nama, u.nim, u.email, u.nomor_telepon, u.password, u.is_verified, u.is_active, p.id_role, u.otp_code, u.otp_expiry " +
                     "FROM users u " +
                     "JOIN profile p ON u.id_user = p.id_user " +
                     "WHERE u.nama = ? AND u.password = ? AND u.is_active = true"; // HANYA USER AKTIF YANG BISA LOGIN
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nama);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = createUserFromResultSet(rs);
                System.out.println("Login berhasil untuk: " + nama + ", isVerified: " + user.isVerified() + ", isActive: " + user.isActive());
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
        if (conn == null) {
            System.err.println("Koneksi database null! Tidak bisa updateUser.");
            return false;
        }
        // Transaksi untuk update tabel users dan profile
        String sqlUser = "UPDATE users SET nama = ?, nim = ?, email = ?, nomor_telepon = ?, is_verified = ?, is_active = ?, otp_code = ?, otp_expiry = ? WHERE id_user = ?";
        String sqlProfile = "UPDATE profile SET id_role = ? WHERE id_user = ?";

        try {
            conn.setAutoCommit(false);
            
            // Update tabel users
            try (PreparedStatement stmtUser = conn.prepareStatement(sqlUser)) {
                stmtUser.setString(1, user.getNama());
                stmtUser.setString(2, user.getNim());
                stmtUser.setString(3, user.getEmail());
                stmtUser.setString(4, user.getNomorTelepon());
                stmtUser.setBoolean(5, user.isVerified());
                stmtUser.setBoolean(6, user.isActive()); // Set status aktif
                stmtUser.setString(7, user.getOtpCode());
                if (user.getOtpExpiry() != null) {
                    stmtUser.setString(8, user.getOtpExpiry().format(DB_DATETIME_FORMATTER));
                } else {
                    stmtUser.setNull(8, Types.TIMESTAMP);
                }
                stmtUser.setInt(9, user.getIdUser());
                stmtUser.executeUpdate();
            }

            // Update tabel profile (khusus untuk role)
            try (PreparedStatement stmtProfile = conn.prepareStatement(sqlProfile)) {
                stmtProfile.setInt(1, user.getIdRole());
                stmtProfile.setInt(2, user.getIdUser());
                stmtProfile.executeUpdate();
            }
            
            conn.commit();
            System.out.println("Update user dan profile berhasil untuk ID: " + user.getIdUser());
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        if (conn == null) {
            System.err.println("Koneksi null di getAllUsers.");
            return users;
        }
        String sql = "SELECT u.id_user, u.nama, u.nim, u.email, u.nomor_telepon, u.password, u.is_verified, u.is_active, p.id_role, u.otp_code, u.otp_expiry " +
                     "FROM users u " +
                     "LEFT JOIN profile p ON u.id_user = p.id_user " +
                     "ORDER BY u.id_user";
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(createUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all users: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }
    
    public User getUserById(int idUser) {
        if (conn == null) {
            System.err.println("Koneksi database null! Tidak bisa getUserById.");
            return null;
        }
        String sql = "SELECT u.id_user, u.nama, u.nim, u.email, u.nomor_telepon, u.password, u.is_verified, u.is_active, p.id_role, u.otp_code, u.otp_expiry " +
                     "FROM users u LEFT JOIN profile p ON u.id_user = p.id_user WHERE u.id_user = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUser);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return createUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving user by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public User getUserByNameAndEmail(String nama, String email) {
        if (conn == null) return null;
        String sql = "SELECT u.id_user, u.nama, u.nim, u.email, u.nomor_telepon, u.password, u.is_verified, u.is_active, p.id_role, u.otp_code, u.otp_expiry " +
                     "FROM users u JOIN profile p ON u.id_user = p.id_user WHERE u.nama = ? AND u.email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nama);
            stmt.setString(2, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return createUserFromResultSet(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<User> getUnverifiedUsers() {
        List<User> users = new ArrayList<>();
        if (conn == null) return users;
        String sql = "SELECT u.id_user, u.nama, u.nim, u.email, u.nomor_telepon, u.password, u.is_verified, u.is_active, p.id_role " +
                     "FROM users u JOIN profile p ON u.id_user = p.id_user WHERE u.is_verified = false ORDER BY u.id_user";
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(createUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching unverified users: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    public boolean approveUser(int idUser) {
        User user = getUserById(idUser);
        if (user != null) {
            user.setVerified(true);
            return updateUser(user); // Gunakan updateUser untuk konsistensi
        }
        return false;
    }

    public boolean rejectUser(int idUser) {
        // ... (kode rejectUser yang sudah ada sebelumnya) ...
        try {
            conn.setAutoCommit(false);
            String sqlLoans = "DELETE FROM loans WHERE id_user = ?"; // Hapus juga dari loans
             try (PreparedStatement stmtLoans = conn.prepareStatement(sqlLoans)) {
                stmtLoans.setInt(1, idUser);
                stmtLoans.executeUpdate();
            }
            String sqlFavorites = "DELETE FROM favorites WHERE id_user = ?"; // Hapus juga dari favorites
             try (PreparedStatement stmtFavorites = conn.prepareStatement(sqlFavorites)) {
                stmtFavorites.setInt(1, idUser);
                stmtFavorites.executeUpdate();
            }
            String sqlProfile = "DELETE FROM profile WHERE id_user = ?";
            try (PreparedStatement stmtProfile = conn.prepareStatement(sqlProfile)) {
                stmtProfile.setInt(1, idUser);
                stmtProfile.executeUpdate();
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

    // --- METHOD YANG DITAMBAHKAN KEMBALI ---
    public int getNewUserId() {
        if (conn == null) {
            System.err.println("Koneksi database null! Tidak bisa getNewUserId.");
            throw new IllegalStateException("Koneksi database tidak tersedia");
        }
        String sql = "SELECT MAX(id_user) + 1 AS new_id FROM users";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int newId = rs.getInt("new_id");
                return (newId <= 0) ? 1 : newId; // Jika tabel kosong, newId jadi 1
            } else {
                return 1; // Jika tabel benar-benar kosong
            }
        } catch (SQLException e) {
            System.err.println("Error generating new user ID: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Gagal generate ID user baru: " + e.getMessage(), e);
        }
    }

    public void saveOtp(User user, String otpCode) {
        if (user == null) {
            System.err.println("User tidak boleh null untuk menyimpan OTP.");
            return;
        }
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10); // OTP berlaku 10 menit
        user.setOtpCode(otpCode);
        user.setOtpExpiry(expiry);
        // Panggil updateUser untuk menyimpan perubahan OTP ke database
        if (!updateUser(user)) { 
            System.err.println("Gagal menyimpan OTP untuk user: " + user.getEmail());
        } else {
            System.out.println("OTP berhasil disimpan untuk user: " + user.getEmail());
        }
    }
    // --- AKHIR METHOD YANG DITAMBAHKAN KEMBALI ---

    // Helper method untuk membuat objek User dari ResultSet agar tidak duplikat kode
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
        // Cek jika kolom OTP ada sebelum mengambilnya
        if(hasColumn(rs, "otp_code")) {
             user.setOtpCode(rs.getString("otp_code"));
        }
        if(hasColumn(rs, "otp_expiry")) {
            String otpExpiryStr = rs.getString("otp_expiry");
            if (otpExpiryStr != null && !otpExpiryStr.trim().isEmpty()) {
                user.setOtpExpiry(LocalDateTime.parse(otpExpiryStr, DB_DATETIME_FORMATTER));
            }
        }
        return user;
    }

    // Helper untuk cek apakah kolom ada di ResultSet
    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();
        for (int x = 1; x <= columns; x++) {
            if (columnName.equals(rsmd.getColumnName(x))) {
                return true;
            }
        }
        return false;
    }
}
