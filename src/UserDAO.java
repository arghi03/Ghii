import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private Connection conn;

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
                stmtUser.setBoolean(7, false);
                stmtUser.executeUpdate();
            }

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
                conn.rollback();
                System.out.println("Transaksi rollback untuk: " + user.getEmail());
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
                rollbackEx.printStackTrace();
            }
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Failed to reset auto-commit: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public User login(String nama, String password) {
        if (conn == null) {
            System.err.println("Koneksi database null! Tidak bisa login.");
            return null;
        }

        String sql = "SELECT u.id_user, u.nama, u.nim, u.email, u.nomor_telepon, u.password, u.is_verified, p.id_role, u.otp_code, u.otp_expiry " +
                     "FROM users u JOIN profile p ON u.id_user = p.id_user " +
                     "WHERE u.nama = ? AND u.password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nama);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User(
                    rs.getInt("id_user"),
                    rs.getString("nama"),
                    rs.getString("nim"),
                    rs.getString("email"),
                    rs.getString("nomor_telepon"),
                    rs.getString("password"),
                    rs.getInt("id_role"),
                    rs.getBoolean("is_verified")
                );
                user.setOtpCode(rs.getString("otp_code"));
                String otpExpiryStr = rs.getString("otp_expiry");
                if (otpExpiryStr != null) {
                    user.setOtpExpiry(LocalDateTime.parse(otpExpiryStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
                System.out.println("Login berhasil untuk: " + nama + ", isVerified: " + user.isVerified());
                return user;
            } else {
                System.out.println("User tidak ditemukan untuk nama: " + nama);
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public User getUserByNameAndEmail(String nama, String email) {
        if (conn == null) {
            System.err.println("Koneksi database null! Tidak bisa getUserByNameAndEmail.");
            return null;
        }

        String sql = "SELECT u.id_user, u.nama, u.nim, u.email, u.nomor_telepon, u.password, u.is_verified, p.id_role, u.otp_code, u.otp_expiry " +
                     "FROM users u JOIN profile p ON u.id_user = p.id_user " +
                     "WHERE u.nama = ? AND u.email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nama);
            stmt.setString(2, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User(
                    rs.getInt("id_user"),
                    rs.getString("nama"),
                    rs.getString("nim"),
                    rs.getString("email"),
                    rs.getString("nomor_telepon"),
                    rs.getString("password"),
                    rs.getInt("id_role"),
                    rs.getBoolean("is_verified")
                );
                user.setOtpCode(rs.getString("otp_code"));
                String otpExpiryStr = rs.getString("otp_expiry");
                if (otpExpiryStr != null) {
                    user.setOtpExpiry(LocalDateTime.parse(otpExpiryStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
                return user;
            } else {
                System.out.println("User tidak ditemukan untuk nama: " + nama + ", email: " + email);
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving user by name and email: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public User getUserByName(String nama) { // Tambah method baru
        if (conn == null) {
            System.err.println("Koneksi database null! Tidak bisa getUserByName.");
            return null;
        }

        String sql = "SELECT u.id_user, u.nama, u.nim, u.email, u.nomor_telepon, u.password, u.is_verified, p.id_role, u.otp_code, u.otp_expiry " +
                     "FROM users u JOIN profile p ON u.id_user = p.id_user " +
                     "WHERE u.nama = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nama);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User(
                    rs.getInt("id_user"),
                    rs.getString("nama"),
                    rs.getString("nim"),
                    rs.getString("email"),
                    rs.getString("nomor_telepon"),
                    rs.getString("password"),
                    rs.getInt("id_role"),
                    rs.getBoolean("is_verified")
                );
                user.setOtpCode(rs.getString("otp_code"));
                String otpExpiryStr = rs.getString("otp_expiry");
                if (otpExpiryStr != null) {
                    user.setOtpExpiry(LocalDateTime.parse(otpExpiryStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
                return user;
            } else {
                System.out.println("User tidak ditemukan untuk nama: " + nama);
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving user by name: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateUser(User user) {
        if (conn == null) {
            System.err.println("Koneksi database null! Tidak bisa updateUser.");
            return false;
        }

        String sql = "UPDATE users SET nama = ?, nim = ?, email = ?, nomor_telepon = ?, is_verified = ?, otp_code = ?, otp_expiry = ? WHERE id_user = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getNama());
            stmt.setString(2, user.getNim());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getNomorTelepon());
            stmt.setBoolean(5, user.isVerified());
            stmt.setString(6, user.getOtpCode());
            if (user.getOtpExpiry() != null) {
                stmt.setString(7, user.getOtpExpiry().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } else {
                stmt.setNull(7, Types.TIMESTAMP);
            }
            stmt.setInt(8, user.getIdUser());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Update user berhasil untuk: " + user.getEmail());
                return true;
            } else {
                System.out.println("Tidak ada user yang diupdate untuk id: " + user.getIdUser());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateRole(int idUser, int newRoleId) {
        if (conn == null) {
            System.err.println("Koneksi database null! Tidak bisa updateRole.");
            return false;
        }

        String sql = "UPDATE users SET role_id = ? WHERE id_user = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newRoleId);
            stmt.setInt(2, idUser);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Update role berhasil untuk id_user: " + idUser + ", new role: " + newRoleId);
                return true;
            } else {
                System.out.println("Tidak ada user yang diupdate untuk id_user: " + idUser + ". Mungkin id tidak ditemukan.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error updating role untuk id_user " + idUser + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

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
                if (newId <= 0) {
                    return 1;
                }
                return newId;
            } else {
                return 1;
            }
        } catch (SQLException e) {
            System.err.println("Error generating new user ID: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Gagal generate ID user baru: " + e.getMessage());
        }
    }

    public void saveOtp(User user, String otpCode) {
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);
        user.setOtpCode(otpCode);
        user.setOtpExpiry(expiry);
        updateUser(user);
    }

    public List<User> getUnverifiedUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.id_user, u.nama, u.nim, u.email, u.nomor_telepon, u.password, u.is_verified, p.id_role " +
                     "FROM users u JOIN profile p ON u.id_user = p.id_user " +
                     "WHERE u.is_verified = false";
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(new User(
                    rs.getInt("id_user"),
                    rs.getString("nama"),
                    rs.getString("nim"),
                    rs.getString("email"),
                    rs.getString("nomor_telepon"),
                    rs.getString("password"),
                    rs.getInt("id_role"),
                    rs.getBoolean("is_verified")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching unverified users: " + e.getMessage());
        }
        return users;
    }

    public boolean approveUser(int idUser) {
        String sql = "UPDATE users SET is_verified = true WHERE id_user = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUser);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error approving user: " + e.getMessage());
            return false;
        }
    }

    public boolean rejectUser(int idUser) {
        try {
            conn.setAutoCommit(false);

            // Hapus dari tabel profile dulu
            String sqlProfile = "DELETE FROM profile WHERE id_user = ?";
            try (PreparedStatement stmtProfile = conn.prepareStatement(sqlProfile)) {
                stmtProfile.setInt(1, idUser);
                stmtProfile.executeUpdate();
            }

            // Baru hapus dari tabel users
            String sqlUser = "DELETE FROM users WHERE id_user = ?";
            try (PreparedStatement stmtUser = conn.prepareStatement(sqlUser)) {
                stmtUser.setInt(1, idUser);
                int affectedRows = stmtUser.executeUpdate();
                if (affectedRows > 0) {
                    conn.commit();
                    return true;
                }
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error rejecting user: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Failed to reset auto-commit: " + e.getMessage());
            }
        }
    }
}