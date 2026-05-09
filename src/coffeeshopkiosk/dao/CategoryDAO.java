package coffeeshopkiosk.dao;

import coffeeshopkiosk.database.DatabaseConnection;
import coffeeshopkiosk.model.Category;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CategoryDAO {

    public ObservableList<Category> findAll() throws SQLException {
        String sql = "SELECT category_id, name FROM categories WHERE active = TRUE ORDER BY name";
        ObservableList<Category> categories = FXCollections.observableArrayList();

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                categories.add(new Category(
                        resultSet.getInt("category_id"),
                        resultSet.getString("name")));
            }
        }
        return categories;
    }

    public void addCategory(String name) throws SQLException {
        String sql = "INSERT INTO categories (name, description, active) VALUES (?, ?, TRUE) "
                + "ON CONFLICT (name) DO UPDATE SET active = TRUE, description = EXCLUDED.description";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, "Added from Product Management");
            statement.executeUpdate();
        }
    }

    public int countProductsInCategory(int categoryId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE category_id = ? AND active = TRUE";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, categoryId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    public void deleteCategoryWithProducts(int categoryId) throws SQLException {
        String deactivateProductsSql = "UPDATE products SET active = FALSE WHERE category_id = ?";
        String deactivateCategorySql = "UPDATE categories SET active = FALSE WHERE category_id = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement productsStatement = connection.prepareStatement(deactivateProductsSql);
                    PreparedStatement categoryStatement = connection.prepareStatement(deactivateCategorySql)) {
                productsStatement.setInt(1, categoryId);
                productsStatement.executeUpdate();

                categoryStatement.setInt(1, categoryId);
                categoryStatement.executeUpdate();
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }
}
