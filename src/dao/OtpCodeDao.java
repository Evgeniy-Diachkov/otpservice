package dao;

import model.OtpCode;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OtpCodeDao {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/otpdb";
    private static final String DB_USERNAME = "postgres";
    private static final String DB_PASSWORD = "your_password_here";

    public void save(OtpCode otpCode) {
        String sql = "INSERT INTO otp_codes (operation_id, code, created_at, status) VALUES (?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, otpCode.operationId());
            statement.setString(2, otpCode.code());
            statement.setObject(3, otpCode.createdAt());
            statement.setString(4, otpCode.status());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save OTP code to database", e);
        }
    }

    public OtpCode findByOperationId(String operationId) {
        String sql = "SELECT operation_id, code, created_at, status FROM otp_codes WHERE operation_id = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, operationId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new OtpCode(
                            rs.getString("operation_id"),
                            rs.getString("code"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getString("status")
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find OTP code by operationId", e);
        }
    }

    public void updateStatus(String operationId, String newStatus) {
        String sql = "UPDATE otp_codes SET status = ? WHERE operation_id = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, newStatus);
            statement.setString(2, operationId);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update OTP code status", e);
        }
    }

    public List<OtpCode> findAllActiveCodes() {
        String sql = "SELECT operation_id, code, created_at, status FROM otp_codes WHERE status = 'ACTIVE'";
        List<OtpCode> activeCodes = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                OtpCode code = new OtpCode(
                        rs.getString("operation_id"),
                        rs.getString("code"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getString("status")
                );
                activeCodes.add(code);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve active OTP codes", e);
        }

        return activeCodes;
    }
    public void deleteByOperationIdPattern(String username) {
        String sql = "DELETE FROM otp_codes WHERE operation_id LIKE ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username + "%"); // Предполагаем что operationId начинается с username
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete OTP codes by operation ID pattern", e);
        }
    }

}
