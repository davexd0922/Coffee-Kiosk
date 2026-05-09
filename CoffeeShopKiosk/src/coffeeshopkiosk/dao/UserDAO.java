package coffeeshopkiosk.dao;

import coffeeshopkiosk.database.DatabaseConnection;
import coffeeshopkiosk.model.User;
import coffeeshopkiosk.util.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class UserDAO {

    public Optional<User> authenticate(String username, String password) throws SQLException {
        String sql = "SELECT u.user_id, u.username, u.full_name, u.password_hash, u.active, r.name AS role_name "
                + "FROM users u JOIN roles r ON r.role_id = u.role_id "
                + "WHERE LOWER(u.username) = LOWER(?) AND u.active = TRUE";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                String storedHash = resultSet.getString("password_hash");
                if (!PasswordUtil.matches(password, storedHash)) {
                    return Optional.empty();
                }

                User user = new User();
                user.setUserId(resultSet.getInt("user_id"));
                user.setUsername(resultSet.getString("username"));
                user.setFullName(resultSet.getString("full_name"));
                user.setRoleName(resultSet.getString("role_name"));
                user.setActive(resultSet.getBoolean("active"));
                return Optional.of(user);
            }
        }
    }

    public ObservableList<User> findCashiers() throws SQLException {
        String sql = "SELECT u.user_id, u.username, u.full_name, u.active, r.name AS role_name "
                + "FROM users u JOIN roles r ON r.role_id = u.role_id "
                + "WHERE r.name = 'CASHIER' ORDER BY u.full_name";
        ObservableList<User> users = FXCollections.observableArrayList();

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
        }
        return users;
    }

    public void saveCashier(User user, String password) throws SQLException {
        if (user.getUserId() == 0) {
            insertCashier(user, password);
        } else {
            updateCashier(user);
        }
    }

    public void changePassword(int userId, String newPassword) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, PasswordUtil.sha256(newPassword));
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
    }

    private void insertCashier(User user, String password) throws SQLException {
        String sql = "INSERT INTO users (role_id, username, password_hash, full_name, active) "
                + "VALUES ((SELECT role_id FROM roles WHERE name = 'CASHIER'), ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, PasswordUtil.sha256(password));
            statement.setString(3, user.getFullName());
            statement.setBoolean(4, user.isActive());
            statement.executeUpdate();
        }
    }

    private void updateCashier(User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, full_name = ?, active = ? WHERE user_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getFullName());
            statement.setBoolean(3, user.isActive());
            statement.setInt(4, user.getUserId());
            statement.executeUpdate();
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setUserId(resultSet.getInt("user_id"));
        user.setUsername(resultSet.getString("username"));
        user.setFullName(resultSet.getString("full_name"));
        user.setRoleName(resultSet.getString("role_name"));
        user.setActive(resultSet.getBoolean("active"));
        return user;
    }
}
