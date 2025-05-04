package dao;

import model.OtpConfig;

import java.sql.*;

public class OtpConfigDao {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/otpdb";
    private static final String DB_USERNAME = "postgres";
    private static final String DB_PASSWORD = "your_password_here";

    public void saveOrUpdate(OtpConfig config) {
        String deleteSql = "DELETE FROM otp_config";
        String insertSql = "INSERT INTO otp_config (code_length, code_lifetime_seconds) VALUES (?, ?)";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             Statement deleteStmt = connection.createStatement();
             PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {

            connection.setAutoCommit(false);

            deleteStmt.executeUpdate(deleteSql);  // <-- Вот тут исправление!
            insertStmt.setInt(1, config.codeLength());
            insertStmt.setInt(2, config.codeLifetimeSeconds());
            insertStmt.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save or update OTP config", e);
        }
    }


    public OtpConfig getConfig() {
        String sql = "SELECT code_length, code_lifetime_seconds FROM otp_config LIMIT 1";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            if (rs.next()) {
                return new OtpConfig(
                        rs.getInt("code_length"),
                        rs.getInt("code_lifetime_seconds")
                );
            } else {
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get OTP config", e);
        }
    }
}
