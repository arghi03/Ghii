import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    public static Connection getConnection() {
        String url = "jdbc:mysql://localhost:3306/register_perpustakaan?useSSL=false";
        String user = "root";  
        String password = ""; 
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver JDBC MySQL berhasil dimuat.");
            
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Koneksi ke database berhasil! URL: " + url);
            
            
            conn.setAutoCommit(true);
            

        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC MySQL tidak ditemukan: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Koneksi gagal: " + e.getMessage());
            e.printStackTrace();
        }
        if (conn == null) {
            System.err.println("Koneksi database mengembalikan null!");
        }
        return conn;
    }
}