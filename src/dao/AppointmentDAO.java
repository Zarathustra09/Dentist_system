package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    private static final List<Runnable> changeListeners = new ArrayList<>();

    // Method to add change listeners
    public static void addChangeListener(Runnable listener) {
        changeListeners.add(listener);
    }

    // Method to notify all registered listeners
    private static void notifyChangeListeners() {
        for (Runnable listener : changeListeners) {
            listener.run();
        }
    }

    public static List<Object[]> getAllAppointments() throws SQLException {
        List<Object[]> appointments = new ArrayList<>();
        String sql = "SELECT a.id, CONCAT(p.first_name, ' ', p.last_name) AS patient_name, " +
                "a.procedure_name, a.appointment_date, a.cost, a.charge, a.description " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.id " +
                "ORDER BY a.appointment_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Object[] appointment = {
                        rs.getInt("id"),
                        rs.getString("patient_name"),
                        rs.getString("procedure_name"),
                        rs.getString("appointment_date"),
                        rs.getDouble("cost"),
                        rs.getDouble("charge"),
                        rs.getString("description")
                };
                appointments.add(appointment);
            }
        }
        return appointments;
    }

    public static Object[] getAppointmentById(int id) throws SQLException {
        String sql = "SELECT a.*, p.id as patient_id FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.id WHERE a.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Object[] {
                            rs.getInt("id"),
                            rs.getInt("patient_id"),
                            rs.getString("procedure_name"),
                            rs.getString("appointment_date"),
                            rs.getDouble("cost"),
                            rs.getDouble("charge"),
                            rs.getString("description")
                    };
                }
            }
        }
        return null;
    }

    public static List<Object[]> searchAppointments(String keyword) throws SQLException {
        List<Object[]> appointments = new ArrayList<>();
        String sql = "SELECT a.id, CONCAT(p.first_name, ' ', p.last_name) AS patient_name, " +
                "a.procedure_name, a.appointment_date, a.cost, a.charge, a.description " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.id " +
                "WHERE p.first_name LIKE ? OR p.last_name LIKE ? OR a.procedure_name LIKE ? " +
                "ORDER BY a.appointment_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Object[] appointment = {
                            rs.getInt("id"),
                            rs.getString("patient_name"),
                            rs.getString("procedure_name"),
                            rs.getString("appointment_date"),
                            rs.getDouble("cost"),
                            rs.getDouble("charge"),
                            rs.getString("description")
                    };
                    appointments.add(appointment);
                }
            }
        }
        return appointments;
    }

    public static boolean updateAppointment(int id, String patientId, String procedure,
                                            String date, String cost, String charge, String description) throws SQLException {
        String sql = "UPDATE appointments SET patient_id = ?, procedure_name = ?, appointment_date = ?, " +
                "cost = ?, charge = ?, description = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, Integer.parseInt(patientId));
            stmt.setString(2, procedure);
            stmt.setString(3, date);
            stmt.setDouble(4, Double.parseDouble(cost));
            stmt.setDouble(5, Double.parseDouble(charge));
            stmt.setString(6, description);
            stmt.setInt(7, id);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                notifyChangeListeners(); // Notify listeners
            }
            return rowsAffected > 0;
        }
    }

    public static boolean deleteAppointment(int id) throws SQLException {
        String sql = "DELETE FROM appointments WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                notifyChangeListeners(); // Notify listeners
            }
            return rowsAffected > 0;
        }
    }

    public static boolean addAppointment(int patientId, String procedure,
                                         String dateTime, double cost,
                                         double charge, String description) throws SQLException {
        String sql = "INSERT INTO appointments (patient_id, procedure_name, appointment_date, " +
                "cost, charge, description) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.setString(2, procedure);
            stmt.setString(3, dateTime);
            stmt.setDouble(4, cost);
            stmt.setDouble(5, charge);
            stmt.setString(6, description);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                notifyChangeListeners(); // Notify listeners
            }
            return rowsAffected > 0;
        }
    }

    public static boolean scheduleAppointment(int patientId, String procedure, String dateTime, String cost, String charge, String description) throws SQLException {
        String query = "INSERT INTO appointments (patient_id, `procedure_name`, appointment_date, cost, charge, description) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, patientId);
            stmt.setString(2, procedure);
            stmt.setString(3, dateTime);
            stmt.setString(4, cost);
            stmt.setString(5, charge);
            stmt.setString(6, description);

            boolean success = stmt.executeUpdate() > 0;
            if (success) {
                notifyChangeListeners(); // Notify listeners
            }
            return success;
        }
    }
}