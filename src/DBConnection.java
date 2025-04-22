import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    public static Connection connect() {
        try {
            String url = "jdbc:mysql://localhost:3306/register_perpustakaan";
            String user = "root";
            String pass = "";
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, pass);
            System.out.println("Koneksi ke database berhasil!");
            return conn;
        } catch (Exception e) {
            System.err.println("Koneksi gagal: " + e.getMessage());
            e.printStackTrace(); // Cetak stack trace untuk detail error
            return null;
        }
    }
}