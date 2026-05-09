package coffeeshopkiosk.dao;

import coffeeshopkiosk.database.DatabaseConnection;
import coffeeshopkiosk.model.InventoryItem;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class InventoryDAO {

    public ObservableList<InventoryItem> findAll() throws SQLException {
        String sql = "SELECT i.inventory_id, i.ingredient_id, ing.name AS ingredient_name, ing.unit, "
                + "i.quantity_on_hand, i.reorder_level, COALESCE(s.name, '') AS supplier_name "
                + "FROM inventory i "
                + "JOIN ingredients ing ON ing.ingredient_id = i.ingredient_id "
                + "LEFT JOIN suppliers s ON s.supplier_id = ing.supplier_id "
                + "ORDER BY ing.name";
        ObservableList<InventoryItem> items = FXCollections.observableArrayList();

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                InventoryItem item = new InventoryItem();
                item.setInventoryId(resultSet.getInt("inventory_id"));
                item.setIngredientId(resultSet.getInt("ingredient_id"));
                item.setIngredientName(resultSet.getString("ingredient_name"));
                item.setUnit(resultSet.getString("unit"));
                item.setQuantityOnHand(resultSet.getBigDecimal("quantity_on_hand"));
                item.setReorderLevel(resultSet.getBigDecimal("reorder_level"));
                item.setSupplierName(resultSet.getString("supplier_name"));
                items.add(item);
            }
        }
        return items;
    }

    public int countLowStock() throws SQLException {
        String sql = "SELECT COUNT(*) FROM inventory WHERE quantity_on_hand <= reorder_level";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    public void restock(int ingredientId, BigDecimal quantity, int userId) throws SQLException {
        String updateSql = "UPDATE inventory SET quantity_on_hand = quantity_on_hand + ? WHERE ingredient_id = ?";
        String logSql = "INSERT INTO inventory_logs (ingredient_id, user_id, log_type, change_quantity, remarks) "
                + "VALUES (?, ?, 'RESTOCK', ?, 'Manual restock')";

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement update = connection.prepareStatement(updateSql);
                    PreparedStatement log = connection.prepareStatement(logSql)) {
                update.setBigDecimal(1, quantity);
                update.setInt(2, ingredientId);
                update.executeUpdate();

                log.setInt(1, ingredientId);
                log.setInt(2, userId);
                log.setBigDecimal(3, quantity);
                log.executeUpdate();
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }
}
