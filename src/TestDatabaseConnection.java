import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestDatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/clinic_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            if (connection != null) {
                System.out.println("Database connected successfully!");
                connection.close();
            }
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL Driver not found");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}