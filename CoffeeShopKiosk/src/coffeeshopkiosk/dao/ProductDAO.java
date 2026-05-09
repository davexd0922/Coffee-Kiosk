package coffeeshopkiosk.dao;

import coffeeshopkiosk.database.DatabaseConnection;
import coffeeshopkiosk.model.Product;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ProductDAO {

    public ObservableList<Product> findActive() throws SQLException {
        String sql = "SELECT p.*, c.name AS category_name FROM products p "
                + "JOIN categories c ON c.category_id = p.category_id "
                + "WHERE p.active = TRUE AND c.active = TRUE AND p.status <> 'UNAVAILABLE' "
                + "ORDER BY c.name, p.name";
        return queryProducts(sql, 0);
    }

    public ObservableList<Product> findAll() throws SQLException {
        String sql = "SELECT p.*, c.name AS category_name FROM products p "
                + "JOIN categories c ON c.category_id = p.category_id "
                + "WHERE p.active = TRUE AND c.active = TRUE ORDER BY p.name";
        return queryProducts(sql, 0);
    }

    public ObservableList<Product> findByCategory(int categoryId) throws SQLException {
        String sql = "SELECT p.*, c.name AS category_name FROM products p "
                + "JOIN categories c ON c.category_id = p.category_id "
                + "WHERE p.active = TRUE AND c.active = TRUE AND p.status <> 'UNAVAILABLE' "
                + "AND p.category_id = ? ORDER BY p.name";
        return queryProducts(sql, categoryId);
    }

    public void save(Product product) throws SQLException {
        if (product.getProductId() == 0) {
            insert(product);
        } else {
            update(product);
        }
    }

    public void deactivate(int productId) throws SQLException {
        String sql = "UPDATE products SET active = FALSE WHERE product_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            statement.executeUpdate();
        }
    }

    private ObservableList<Product> queryProducts(String sql, int categoryId) throws SQLException {
        ObservableList<Product> products = FXCollections.observableArrayList();
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            if (categoryId > 0) {
                statement.setInt(1, categoryId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Product product = new Product();
                    product.setProductId(resultSet.getInt("product_id"));
                    product.setCategoryId(resultSet.getInt("category_id"));
                    product.setCategoryName(resultSet.getString("category_name"));
                    product.setName(resultSet.getString("name"));
                    product.setPrice(resultSet.getBigDecimal("price"));
                    product.setStockQuantity(resultSet.getInt("stock_quantity"));
                    product.setImagePath(resultSet.getString("image_path"));
                    product.setStatus(resultSet.getString("status"));
                    product.setActive(resultSet.getBoolean("active"));
                    products.add(product);
                }
            }
        }
        return products;
    }

    private void insert(Product product) throws SQLException {
        String sql = "INSERT INTO products (category_id, name, price, stock_quantity, image_path, status, active) "
                + "VALUES (?, ?, ?, ?, ?, ?, TRUE)";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, product.getCategoryId());
            statement.setString(2, product.getName());
            statement.setBigDecimal(3, product.getPrice());
            statement.setInt(4, product.getStockQuantity());
            statement.setString(5, product.getImagePath());
            statement.setString(6, product.getStatus());
            statement.executeUpdate();
        }
    }

    private void update(Product product) throws SQLException {
        String sql = "UPDATE products SET category_id = ?, name = ?, price = ?, stock_quantity = ?, "
                + "image_path = ?, status = ?, active = ? WHERE product_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, product.getCategoryId());
            statement.setString(2, product.getName());
            statement.setBigDecimal(3, product.getPrice());
            statement.setInt(4, product.getStockQuantity());
            statement.setString(5, product.getImagePath());
            statement.setString(6, product.getStatus());
            statement.setBoolean(7, product.isActive());
            statement.setInt(8, product.getProductId());
            statement.executeUpdate();
        }
    }
}
