import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfileDAO {
    private Connection conn;

    public ProfileDAO(Connection conn) {
        this.conn = conn;
    }

    public boolean insertProfile(Profile profile) {
        String sql = "INSERT INTO profile (id_user, nama, nim, email, nomor_telepon, id_role) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, profile.getIdUser());
            stmt.setString(2, profile.getNama());
            stmt.setString(3, profile.getNim());
            stmt.setString(4, profile.getEmail());
            stmt.setString(5, profile.getNomorTelepon());
            int idRole = profile.getIdRole() == 0 ? 3 : profile.getIdRole();
            if (!isValidRole(idRole)) {
                throw new SQLException("Invalid id_role: " + idRole);
            }
            stmt.setInt(6, idRole);
            int affectedRows = stmt.executeUpdate();
            System.out.println("Profile inserted with id_role: " + idRole);
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting profile: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateProfile(Profile profile) {
        String sql = "UPDATE profile SET nama = ?, nim = ?, email = ?, nomor_telepon = ?, id_role = ? WHERE id_user = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, profile.getNama());
            stmt.setString(2, profile.getNim());
            stmt.setString(3, profile.getEmail());
            stmt.setString(4, profile.getNomorTelepon());
            int idRole = profile.getIdRole() == 0 ? 3 : profile.getIdRole();
            if (!isValidRole(idRole)) {
                throw new SQLException("Invalid id_role: " + idRole);
            }
            stmt.setInt(5, idRole);
            stmt.setInt(6, profile.getIdUser());
            int affectedRows = stmt.executeUpdate();
            System.out.println("Profile updated with id_role: " + idRole);
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating profile: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Profile> getAllProfiles() {
        List<Profile> profileList = new ArrayList<>();
        String sql = "SELECT p.*, r.name AS role_name, r.level AS role_level FROM profile p LEFT JOIN role r ON p.id_role = r.id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Profile profile = new Profile();
                profile.setProfileId(rs.getInt("profile_id"));
                profile.setIdUser(rs.getInt("id_user"));
                profile.setNama(rs.getString("nama"));
                profile.setNim(rs.getString("nim"));
                profile.setEmail(rs.getString("email"));
                profile.setNomorTelepon(rs.getString("nomor_telepon"));
                profile.setIdRole(rs.getInt("id_role"));
                profile.setLevel(rs.getInt("role_level"));
                profile.setCreatedAt(rs.getString("created_at"));
                profileList.add(profile);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching profiles: " + e.getMessage());
            e.printStackTrace();
        }
        return profileList;
    }

    public Profile getProfileByUserId(int idUser) {
        String sql = "SELECT p.*, r.name AS role_name, r.level AS role_level FROM profile p LEFT JOIN role r ON p.id_role = r.id WHERE p.id_user = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUser);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Profile profile = new Profile();
                profile.setProfileId(rs.getInt("profile_id"));
                profile.setIdUser(rs.getInt("id_user"));
                profile.setNama(rs.getString("nama"));
                profile.setNim(rs.getString("nim"));
                profile.setEmail(rs.getString("email"));
                profile.setNomorTelepon(rs.getString("nomor_telepon"));
                profile.setIdRole(rs.getInt("id_role"));
                profile.setLevel(rs.getInt("role_level"));
                profile.setCreatedAt(rs.getString("created_at"));
                return profile;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching profile by user ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateRole(int idUser, int newRoleId) {
        if (!isValidRole(newRoleId)) {
            System.err.println("Invalid role ID: " + newRoleId);
            return false;
        }
        String sql = "UPDATE profile SET id_role = ? WHERE id_user = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newRoleId);
            stmt.setInt(2, idUser);
            int affectedRows = stmt.executeUpdate();
            System.out.println("Role updated to: " + newRoleId + " for user ID: " + idUser);
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating role: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean isValidRole(int idRole) {
        String sql = "SELECT COUNT(*) FROM role WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idRole);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error validating role: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public String getRoleName(int idRole) {
        if (idRole == 0) {
            System.err.println("Warning: idRole is 0, defaulting to User");
            return "User (Level 3)";
        }
        String sql = "SELECT name, level FROM role WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idRole);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String roleName = rs.getString("name");
                int level = rs.getInt("level");
                System.out.println("Role: " + roleName + ", Level: " + level);
                return roleName + " (Level " + level + ")";
            }
        } catch (SQLException e) {
            System.err.println("Error fetching role name: " + e.getMessage());
            e.printStackTrace();
        }
        System.err.println("Invalid idRole: " + idRole);
        return "Unknown";
    }
}