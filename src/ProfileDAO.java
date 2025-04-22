import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfileDAO {
    private Connection conn;

    public ProfileDAO(Connection conn) {
        this.conn = conn;
    }

    // INSERT Profile ke database
    public boolean insertProfile(Profile profile) {
        String sql = "INSERT INTO profile (id_user, verification, nama, nim, email, nomor_telepon) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, profile.getIdUser());
            stmt.setBoolean(2, profile.isVerification());
            stmt.setString(3, profile.getNama());
            stmt.setString(4, profile.getNim());
            stmt.setString(5, profile.getEmail());
            stmt.setString(6, profile.getNomorTelepon());
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Ambil semua profile dari database
    public List<Profile> getAllProfiles() {
        List<Profile> profileList = new ArrayList<>();
        String sql = "SELECT * FROM profile";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Profile profile = new Profile();
                profile.setProfileId(rs.getInt("profile_id"));
                profile.setIdUser(rs.getInt("id_user"));
                profile.setVerification(rs.getBoolean("verification"));
                profile.setNama(rs.getString("nama"));
                profile.setNim(rs.getString("nim"));
                profile.setEmail(rs.getString("email"));
                profile.setNomorTelepon(rs.getString("nomor_telepon"));
                profile.setCreatedAt(rs.getString("created_at"));
                profileList.add(profile);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return profileList;
    }

    // Tambahan: Get profile by user ID (optional)
    public Profile getProfileByUserId(int idUser) {
        String sql = "SELECT * FROM profile WHERE id_user = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUser);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Profile profile = new Profile();
                profile.setProfileId(rs.getInt("profile_id"));
                profile.setIdUser(rs.getInt("id_user"));
                profile.setVerification(rs.getBoolean("verification"));
                profile.setNama(rs.getString("nama"));
                profile.setNim(rs.getString("nim"));
                profile.setEmail(rs.getString("email"));
                profile.setNomorTelepon(rs.getString("nomor_telepon"));
                profile.setCreatedAt(rs.getString("created_at"));
                return profile;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
