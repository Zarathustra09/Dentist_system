package dao;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PatientDAO {
    private static final Logger LOGGER = Logger.getLogger(PatientDAO.class.getName());

    private static Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    // src/dao/PatientDAO.java
    public static boolean addPatient(String firstName, String middleName, String lastName, String birthday,
                                     String email, String phone, String address, List<String> filePaths) {
        String sql = "INSERT INTO patients (first_name, middle_name, last_name, birthday, email, phone, address) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, firstName);
            stmt.setString(2, middleName);
            stmt.setString(3, lastName);
            stmt.setString(4, birthday);
            stmt.setString(5, email);
            stmt.setString(6, phone);
            stmt.setString(7, address);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                // Get the generated patient ID
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int patientId = generatedKeys.getInt(1);

                    // Insert file paths if provided
                    if (filePaths != null && !filePaths.isEmpty()) {
                        String fileSql = "INSERT INTO patient_files (patient_id, file_path) VALUES (?, ?)";
                        try (PreparedStatement fileStmt = conn.prepareStatement(fileSql)) {
                            for (String filePath : filePaths) {
                                fileStmt.setInt(1, patientId);
                                fileStmt.setString(2, filePath);
                                fileStmt.addBatch();
                            }
                            fileStmt.executeBatch();
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding patient", e);

        }
        return false;
    }

    public static List<String> getPatientFiles(int patientId) {
        String sql = "SELECT file_path FROM patient_files WHERE patient_id = ?";
        List<String> filePaths = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    filePaths.add(rs.getString("file_path"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving patient files", e);
        }
        return filePaths;
    }

    public static List<Object[]> getAllPatients() {
        String sql = "SELECT id, first_name, middle_name, last_name, birthday, email, phone, address FROM patients";
        List<Object[]> patients = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Object[] patient = new Object[8];
                patient[0] = rs.getInt("id");
                patient[1] = rs.getString("first_name");
                patient[2] = rs.getString("middle_name");
                patient[3] = rs.getString("last_name");
                patient[4] = rs.getString("birthday");
                patient[5] = rs.getString("email");
                patient[6] = rs.getString("phone");
                patient[7] = rs.getString("address");
                patients.add(patient);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all patients", e);
        }
        return patients;
    }

    public static boolean updatePatient(int id, String firstName, String middleName, String lastName, String birthday, String email, String phone, String address, List<String> filePaths) {
        String sql = "UPDATE patients SET first_name = ?, middle_name = ?, last_name = ?, birthday = ?, email = ?, phone = ?, address = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, firstName);
            stmt.setString(2, middleName);
            stmt.setString(3, lastName);
            stmt.setString(4, birthday);
            stmt.setString(5, email);
            stmt.setString(6, phone);
            stmt.setString(7, address);
            stmt.setInt(8, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return updatePatientFiles(id, filePaths);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating patient", e);
            return false;
        }
        return false;
    }

    private static boolean updatePatientFiles(int patientId, List<String> filePaths) {
        String deleteSql = "DELETE FROM patient_files WHERE patient_id = ?";
        String insertSql = "INSERT INTO patient_files (patient_id, file_path) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            // Delete existing files
            deleteStmt.setInt(1, patientId);
            deleteStmt.executeUpdate();

            // Insert new files
            for (String filePath : filePaths) {
                insertStmt.setInt(1, patientId);
                insertStmt.setString(2, filePath);
                insertStmt.addBatch();
            }
            insertStmt.executeBatch();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating patient files", e);
            return false;
        }
    }

    public static boolean deletePatient(int patientId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        List<String> filesToDelete = new ArrayList<>();

        try {
            // First get the files to delete
            filesToDelete = getPatientFiles(patientId);

            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Delete associated appointments
            String deleteAppointmentsSql = "DELETE FROM appointments WHERE patient_id = ?";
            stmt = conn.prepareStatement(deleteAppointmentsSql);
            stmt.setInt(1, patientId);
            stmt.executeUpdate();

            // Then delete patient files from database
            String deleteFilesSql = "DELETE FROM patient_files WHERE patient_id = ?";
            stmt = conn.prepareStatement(deleteFilesSql);
            stmt.setInt(1, patientId);
            stmt.executeUpdate();

            // Finally delete the patient
            String deletePatientSql = "DELETE FROM patients WHERE id = ?";
            stmt = conn.prepareStatement(deletePatientSql);
            stmt.setInt(1, patientId);
            int result = stmt.executeUpdate();

            conn.commit(); // Commit transaction

            // Now delete the actual files from the filesystem
            for (String filePath : filesToDelete) {
                if (filePath != null && !filePath.isEmpty()) {
                    File file = new File(filePath);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }

            return result > 0;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (stmt != null) stmt.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public static boolean isPatientExists(int id) {
        String sql = "SELECT id FROM patients WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking if patient exists", e);
        }
        return false;
    }

    public static Object[] getPatientById(int id) {
        String sql = "SELECT id, first_name, middle_name, last_name, birthday, email, phone, address FROM patients WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Object[] patient = new Object[8];
                    patient[0] = rs.getInt("id");
                    patient[1] = rs.getString("first_name");
                    patient[2] = rs.getString("middle_name");
                    patient[3] = rs.getString("last_name");
                    patient[4] = rs.getString("birthday");
                    patient[5] = rs.getString("email");
                    patient[6] = rs.getString("phone");
                    patient[7] = rs.getString("address");
                    return patient;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving patient by ID", e);
        }
        return null;
    }
    // src/dao/PatientDAO.java
    public static List<Object[]> searchPatient(String keyword) {
        String sql = "SELECT id, first_name, middle_name, last_name, birthday, email, phone, address FROM patients WHERE first_name LIKE ? OR last_name LIKE ?";
        List<Object[]> patients = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Object[] patient = new Object[8];
                    patient[0] = rs.getInt("id");
                    patient[1] = rs.getString("first_name");
                    patient[2] = rs.getString("middle_name");
                    patient[3] = rs.getString("last_name");
                    patient[4] = rs.getString("birthday");
                    patient[5] = rs.getString("email");
                    patient[6] = rs.getString("phone");
                    patient[7] = rs.getString("address");
                    patients.add(patient);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching patients", e);
        }
        return patients;
    }
    public static List<Object[]> getPatientAppointments(int patientId) throws SQLException {
        List<Object[]> appointments = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM appointments WHERE patient_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, patientId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] appointment = new Object[]{
                        rs.getInt("id"),
                        rs.getString("appointment_type"),
                        rs.getString("appointment_date")
                        // Add other fields as needed
                };
                appointments.add(appointment);
            }

            return appointments;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }

    public static boolean deletePatientWithAppointments(int patientId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        List<String> filesToDelete = new ArrayList<>();

        try {
            // First get the files to delete
            filesToDelete = getPatientFiles(patientId);

            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Delete appointments
            String deleteAppointmentsSql = "DELETE FROM appointments WHERE patient_id = ?";
            stmt = conn.prepareStatement(deleteAppointmentsSql);
            stmt.setInt(1, patientId);
            stmt.executeUpdate();

            // Delete patient files
            String deleteFilesSql = "DELETE FROM patient_files WHERE patient_id = ?";
            stmt = conn.prepareStatement(deleteFilesSql);
            stmt.setInt(1, patientId);
            stmt.executeUpdate();

            // Delete patient
            String deletePatientSql = "DELETE FROM patients WHERE id = ?";
            stmt = conn.prepareStatement(deletePatientSql);
            stmt.setInt(1, patientId);
            int result = stmt.executeUpdate();

            conn.commit();

            // Now delete the actual files from the filesystem
            for (String filePath : filesToDelete) {
                if (filePath != null && !filePath.isEmpty()) {
                    File file = new File(filePath);
                    if (file.exists()) {
                        boolean deleted = file.delete();
                        if (!deleted) {
                            System.err.println("Could not delete file: " + filePath);
                        }
                    }
                }
            }

            return result > 0;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (stmt != null) stmt.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}