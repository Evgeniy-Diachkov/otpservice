package dao;

import model.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;

public class UserDao {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/otpdb";
    private static final String DB_USERNAME = "postgres";
    private static final String DB_PASSWORD = "your_password_here";

    public void save(User user) {
        String sql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.username());
            statement.setString(2, user.passwordHash());
            statement.setString(3, user.role());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save user", e);
        }
    }

    public User findByUsername(String username) {
        String sql = "SELECT username, password_hash, role FROM users WHERE username = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("role")
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user", e);
        }
    }

    public boolean adminExists() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check admin existence", e);
        }
    }

    public List<User> findAllUsersExceptAdmins() {
        String sql = "SELECT username, password_hash, role FROM users WHERE role != 'ADMIN'";
        List<User> users = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                users.add(new User(
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve users", e);
        }

        return users;
    }

    public void deleteByUsername(String username) {
        String sql = "DELETE FROM users WHERE username = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user", e);
        }
    }
}
