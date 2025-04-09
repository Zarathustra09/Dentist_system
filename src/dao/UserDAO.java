package dao;

import dao.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class UserDAO {

    public static boolean register(String username, String password, String email) throws SQLException {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, email);

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Duplicate entry error code for MySQL
                throw new SQLException("Username or email already exists.");
            } else {
                throw e;
            }
        }
    }

    public static boolean login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND active = 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Returns true if a matching record is found
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateUser(int userId, String username, String password, String email) throws SQLException {
        String sql = "UPDATE users SET username = ?, password = ?, email = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, email);
            stmt.setInt(4, userId);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static boolean deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static boolean isUsernameTaken(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Returns true if a matching record is found
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static boolean isEmailTaken(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Returns true if a matching record is found
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static boolean isUserActive(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND active = 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Returns true if a matching record is found
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static boolean deactivateUser(int userId) throws SQLException {
        String sql = "UPDATE users SET active = 0 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static boolean activateUser(int userId) throws SQLException {
        String sql = "UPDATE users SET active = 1 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static boolean resetPassword(String username, String newPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPassword);
            stmt.setString(2, username);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static boolean changePassword(int userId, String oldPassword, String newPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE id = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPassword);
            stmt.setInt(2, userId);
            stmt.setString(3, oldPassword);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static boolean isPasswordCorrect(int userId, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Returns true if a matching record is found
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static boolean isUsernameOrEmailTaken(String username, String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? OR email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Returns true if a matching record is found
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static boolean isUserExists(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Returns true if a matching record is found
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static boolean isUserExists(int userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Returns true if a matching record is found
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }
    public static boolean deleteUser(String username) {
        String query = "DELETE FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static String[] getAllUsernames() {
        String sql = "SELECT username FROM users";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // Get result count
            ArrayList<String> usernames = new ArrayList<>();
            while (rs.next()) {
                usernames.add(rs.getString("username"));
            }

            return usernames.toArray(new String[0]);
        } catch (SQLException e) {
            e.printStackTrace();
            return new String[0];
        }
    }
}