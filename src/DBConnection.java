import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    public static Connection getConnection() {
        String url = "jdbc:mysql://localhost:3306/register_perpustakaan?useSSL=false";
        String user = "root"; // Ganti dengan username MySQL kamu
        String password = ""; // Ganti dengan password MySQL kamu
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Memuat driver
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Koneksi berhasil!");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC MySQL tidak ditemukan: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Koneksi gagal: " + e.getMessage());
        }
        return conn;
    }
}