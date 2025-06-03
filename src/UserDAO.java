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

        // Asumsi tabel users punya kolom: id_user, nama, nim, email, nomor_telepon, password, is_verified, (mungkin otp_code, otp_expiry)
        // Asumsi tabel profile punya kolom: id_profile (PK), id_user (FK), id_role
        String sqlUser = "INSERT INTO users (id_user, nama, nim, email, nomor_telepon, password, is_verified) VALUES (?, ?, ?, ?, ?, ?, ?)";
        // Jika id_role disimpan di tabel users, tidak perlu insert ke profile untuk role saja.
        // Jika id_role disimpan di tabel profile, maka perlu ProfileDAO.
        // Untuk contoh ini, kita asumsikan id_role ada di tabel profile.
        ProfileDAO profileDAO = new ProfileDAO(conn); // Pastikan ProfileDAO ada dan berfungsi

        try {
            conn.setAutoCommit(false); // Mulai transaksi

            // Insert ke tabel users
            try (PreparedStatement stmtUser = conn.prepareStatement(sqlUser)) {
                stmtUser.setInt(1, user.getIdUser()); // Asumsi id_user sudah di-set sebelumnya (misal dari getNewUserId)
                stmtUser.setString(2, user.getNama());
                stmtUser.setString(3, user.getNim());
                stmtUser.setString(4, user.getEmail());
                stmtUser.setString(5, user.getNomorTelepon());
                stmtUser.setString(6, user.getPassword()); // Sebaiknya password di-hash sebelum disimpan
                stmtUser.setBoolean(7, false); // User baru defaultnya belum terverifikasi
                stmtUser.executeUpdate();
            }

            // Insert ke tabel profile
            // Buat objek Profile berdasarkan data User
            Profile profile = new Profile(); // Asumsi ada kelas Profile
            profile.setIdUser(user.getIdUser());
            profile.setNama(user.getNama()); // Atau field lain yang relevan untuk profile
            profile.setNim(user.getNim());
            profile.setEmail(user.getEmail());
            profile.setNomorTelepon(user.getNomorTelepon());
            profile.setIdRole(user.getIdRole()); // Ambil id_role dari objek User

            boolean profileSuccess = profileDAO.insertProfile(profile); // Asumsi ada method ini di ProfileDAO
            if (!profileSuccess) {
                throw new SQLException("Gagal insert ke tabel profile.");
            }

            conn.commit(); // Commit transaksi jika semua berhasil
            System.out.println("Registrasi berhasil untuk: " + user.getEmail());
            return true;

        } catch (SQLException e) {
            System.err.println("Registrasi gagal: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback(); // Rollback jika terjadi error
                    System.out.println("Transaksi registrasi di-rollback untuk: " + user.getEmail());
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback gagal: " + rollbackEx.getMessage());
                rollbackEx.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true); // Kembalikan ke mode auto-commit
                }
            } catch (SQLException e) {
                System.err.println("Gagal mereset auto-commit: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public User login(String nama, String password) {
        if (conn == null) {
            System.err.println("Koneksi database null! Tidak bisa login.");
            return null;
        }
        // Ambil id_role dari tabel profile
        String sql = "SELECT u.id_user, u.nama, u.nim, u.email, u.nomor_telepon, u.password, u.is_verified, p.id_role, u.otp_code, u.otp_expiry " +
                     "FROM users u " +
                     "JOIN profile p ON u.id_user = p.id_user " +
                     "WHERE u.nama = ? AND u.password = ?"; // Password sebaiknya diverifikasi dengan hash
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
                    rs.getString("password"), // Sebaiknya tidak menyimpan password plain text di objek User setelah login
                    rs.getInt("id_role"),
                    rs.getBoolean("is_verified")
                );
                user.setOtpCode(rs.getString("otp_code"));
                String otpExpiryStr = rs.getString("otp_expiry");
                if (otpExpiryStr != null) {
                    user.setOtpExpiry(parseDateTime(otpExpiryStr));
                }
                System.out.println("Login berhasil untuk: " + nama + ", isVerified: " + user.isVerified());
                return user;
            } else {
                System.out.println("User tidak ditemukan atau password salah untuk nama: " + nama);
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error saat login: " + e.getMessage());
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
                     "FROM users u " +
                     "JOIN profile p ON u.id_user = p.id_user " +
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
                    user.setOtpExpiry(parseDateTime(otpExpiryStr));
                }
                return user;
            } else {
                System.out.println("User tidak ditemukan untuk nama: " + nama + " dan email: " + email);
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error mengambil user berdasarkan nama dan email: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public User getUserByName(String nama) {
        if (conn == null) {
            System.err.println("Koneksi database null! Tidak bisa getUserByName.");
            return null;
        }
        String sql = "SELECT u.id_user, u.nama, u.nim, u.email, u.nomor_telepon, u.password, u.is_verified, p.id_role, u.otp_code, u.otp_expiry " +
                     "FROM users u " +
                     "JOIN profile p ON u.id_user = p.id_user " +
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
                    user.setOtpExpiry(parseDateTime(otpExpiryStr));
                }
                return user;
            } else {
                System.out.println("User tidak ditemukan untuk nama: " + nama);
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error mengambil user berdasarkan nama: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // METHOD BARU YANG DITAMBAHKAN
    public User getUserById(int idUser) {
        if (conn == null) {
            System.err.println("Koneksi database null! Tidak bisa getUserById.");
            return null;
        }
        String sql = "SELECT u.id_user, u.nama, u.nim, u.email, u.nomor_telepon, u.password, u.is_verified, p.id_role, u.otp_code, u.otp_expiry " +
                     "FROM users u " +
                     "LEFT JOIN profile p ON u.id_user = p.id_user " + // LEFT JOIN untuk kasus user belum ada di profile
                     "WHERE u.id_user = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUser);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User(
                    rs.getInt("id_user"),
                    rs.getString("nama"),
                    rs.getString("nim"),
                    rs.getString("email"),
                    rs.getString("nomor_telepon"),
                    rs.getString("password"), // Sebaiknya tidak mengambil password untuk info umum
                    rs.getInt("id_role"),     // Diambil dari profile table
                    rs.getBoolean("is_verified")
                );
                user.setOtpCode(rs.getString("otp_code"));
                String otpExpiryStr = rs.getString("otp_expiry");
                if (otpExpiryStr != null) {
                    user.setOtpExpiry(parseDateTime(otpExpiryStr));
                }
                return user;
            } else {
                System.out.println("User dengan ID " + idUser + " tidak ditemukan.");
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error mengambil user berdasarkan ID " + idUser + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    public boolean updateUser(User user) {
        if (conn == null) {
            System.err.println("Koneksi database null! Tidak bisa updateUser.");
            return false;
        }
        // Update tabel users
        String sqlUser = "UPDATE users SET nama = ?, nim = ?, email = ?, nomor_telepon = ?, is_verified = ?, otp_code = ?, otp_expiry = ? WHERE id_user = ?";
        // Update juga tabel profile jika ada perubahan role atau data lain di profile
        String sqlProfile = "UPDATE profile SET id_role = ? WHERE id_user = ?"; // Contoh jika hanya role yang diupdate di profile

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement stmtUser = conn.prepareStatement(sqlUser)) {
                stmtUser.setString(1, user.getNama());
                stmtUser.setString(2, user.getNim());
                stmtUser.setString(3, user.getEmail());
                stmtUser.setString(4, user.getNomorTelepon());
                stmtUser.setBoolean(5, user.isVerified());
                stmtUser.setString(6, user.getOtpCode());
                if (user.getOtpExpiry() != null) {
                    stmtUser.setString(7, user.getOtpExpiry().format(DB_DATETIME_FORMATTER));
                } else {
                    stmtUser.setNull(7, Types.TIMESTAMP);
                }
                stmtUser.setInt(8, user.getIdUser());
                stmtUser.executeUpdate(); // Tidak perlu cek affectedRows di sini jika mau lanjut ke profile
            }

            try (PreparedStatement stmtProfile = conn.prepareStatement(sqlProfile)) {
                stmtProfile.setInt(1, user.getIdRole());
                stmtProfile.setInt(2, user.getIdUser());
                stmtProfile.executeUpdate();
            }
            
            conn.commit();
            System.out.println("Update user dan profile berhasil untuk: " + user.getEmail());
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

    // Method updateRole yang lama mungkin tidak diperlukan jika updateRole sudah dihandle di updateUser
    // public boolean updateRole(int idUser, int newRoleId) { ... }


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
                return (newId <= 0) ? 1 : newId; // Jika tabel kosong atau MAX(id_user) null, new_id bisa 0 atau 1.
            } else {
                return 1; // Tabel kosong
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

    public List<User> getUnverifiedUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.id_user, u.nama, u.nim, u.email, u.nomor_telepon, u.password, u.is_verified, p.id_role " +
                     "FROM users u " +
                     "JOIN profile p ON u.id_user = p.id_user " +
                     "WHERE u.is_verified = false ORDER BY u.id_user"; // Tambah urutan
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
            e.printStackTrace();
        }
        return users;
    }

    public boolean approveUser(int idUser) {
        // Sebaiknya ini jadi bagian dari updateUser jika hanya mengubah is_verified
        User userToApprove = getUserById(idUser);
        if (userToApprove != null) {
            userToApprove.setVerified(true);
            return updateUser(userToApprove);
        }
        System.err.println("Gagal approve: User dengan ID " + idUser + " tidak ditemukan.");
        return false;
    }

    public boolean rejectUser(int idUser) {
        // Menghapus user melibatkan penghapusan dari beberapa tabel terkait (loans, profile, users)
        // Perlu hati-hati dengan foreign key constraints
        // Urutan penghapusan: loans (yang terkait user ini), profile, baru users.
        String sqlDeleteLoans = "DELETE FROM loans WHERE id_user = ?";
        String sqlDeleteProfile = "DELETE FROM profile WHERE id_user = ?";
        String sqlDeleteUser = "DELETE FROM users WHERE id_user = ?";

        try {
            conn.setAutoCommit(false);

            // Hapus dari loans
            try (PreparedStatement stmtLoans = conn.prepareStatement(sqlDeleteLoans)) {
                stmtLoans.setInt(1, idUser);
                stmtLoans.executeUpdate(); 
                // Tidak masalah jika tidak ada loans, proses lanjut
            }
            
            // Hapus dari profile
            try (PreparedStatement stmtProfile = conn.prepareStatement(sqlDeleteProfile)) {
                stmtProfile.setInt(1, idUser);
                stmtProfile.executeUpdate();
                // Tidak masalah jika tidak ada profile (meskipun seharusnya ada), proses lanjut
            }

            // Hapus dari users
            try (PreparedStatement stmtUser = conn.prepareStatement(sqlDeleteUser)) {
                stmtUser.setInt(1, idUser);
                int affectedRows = stmtUser.executeUpdate();
                if (affectedRows > 0) {
                    conn.commit();
                    System.out.println("User dengan ID " + idUser + " berhasil ditolak (dihapus).");
                    return true;
                } else {
                    System.out.println("Gagal menghapus user dengan ID " + idUser + " dari tabel users (mungkin tidak ditemukan).");
                    conn.rollback(); // Rollback jika user utama tidak terhapus
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error rejecting (deleting) user ID " + idUser + ": " + e.getMessage());
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
    
    // Helper method untuk parse string tanggal dari DB ke LocalDateTime
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        try {
            if (dateTimeStr.endsWith(".0")) { // Handle jika ada .0 di akhir timestamp
                 dateTimeStr = dateTimeStr.substring(0, dateTimeStr.length() - 2);
            }
            return LocalDateTime.parse(dateTimeStr, DB_DATETIME_FORMATTER);
        } catch (Exception e) {
            System.err.println("Failed to parse date-time string: " + dateTimeStr + " - " + e.getMessage());
            return null;
        }
    }
}
