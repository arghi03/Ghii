import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoleDAO {
    private Connection conn;

    public RoleDAO(Connection conn) {
        this.conn = conn;
    }

    public List<Role> getAllRoles() {
        List<Role> roles = new ArrayList<>();
        // Mengambil dari tabel 'rolenew'
        String sql = "SELECT id_role, nama_role FROM rolenew"; 
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                roles.add(new Role(rs.getInt("id_role"), rs.getString("nama_role")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roles;
    }
}