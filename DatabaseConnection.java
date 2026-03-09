import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:mysql://127.0.0.1:3306/library_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static final String USER = "root";
    private static final String PASSWORD = "root@123";  // ← change this

    public static Connection getConnection() {
        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Create and return connection
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library_management", "root", "root@123");

            System.out.println("Database connected successfully!");
            return connection;

        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
            throw new RuntimeException("Driver error");

        } catch (SQLException e) {
            System.out.println("Database connection failed.");
            e.printStackTrace();
            throw new RuntimeException("Connection error");
        }
    }
}