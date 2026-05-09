package coffeeshopkiosk.dao;

import coffeeshopkiosk.database.DatabaseConnection;
import coffeeshopkiosk.model.CartItem;
import coffeeshopkiosk.model.TransactionRecord;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TransactionDAO {

    public int saveTransaction(List<CartItem> cartItems, int userId, BigDecimal subtotal, BigDecimal discount,
            BigDecimal total, BigDecimal cashReceived, BigDecimal changeAmount) throws SQLException {
        String transactionSql = "INSERT INTO transactions "
                + "(user_id, subtotal, discount, total, cash_received, change_amount, payment_method, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, 'CASH', 'COMPLETED')";
        String itemSql = "INSERT INTO transaction_items "
                + "(transaction_id, product_id, quantity, unit_price, line_total) VALUES (?, ?, ?, ?, ?)";
        String stockSql = "UPDATE products SET stock_quantity = stock_quantity - ? "
                + "WHERE product_id = ? AND stock_quantity >= ?";
        String inventorySql = "UPDATE inventory i SET quantity_on_hand = i.quantity_on_hand - (pi.quantity_required * ?) "
                + "FROM product_ingredients pi WHERE i.ingredient_id = pi.ingredient_id AND pi.product_id = ?";
        String logSql = "INSERT INTO inventory_logs "
                + "(ingredient_id, transaction_id, user_id, log_type, change_quantity, remarks) "
                + "SELECT ingredient_id, ?, ?, 'SALE', -(quantity_required * ?), 'Automatic deduction from POS sale' "
                + "FROM product_ingredients WHERE product_id = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement transactionStatement = connection.prepareStatement(transactionSql, Statement.RETURN_GENERATED_KEYS);
                    PreparedStatement itemStatement = connection.prepareStatement(itemSql);
                    PreparedStatement stockStatement = connection.prepareStatement(stockSql);
                    PreparedStatement inventoryStatement = connection.prepareStatement(inventorySql);
                    PreparedStatement logStatement = connection.prepareStatement(logSql)) {

                transactionStatement.setInt(1, userId);
                transactionStatement.setBigDecimal(2, subtotal);
                transactionStatement.setBigDecimal(3, discount);
                transactionStatement.setBigDecimal(4, total);
                transactionStatement.setBigDecimal(5, cashReceived);
                transactionStatement.setBigDecimal(6, changeAmount);
                transactionStatement.executeUpdate();

                int transactionId;
                try (ResultSet keys = transactionStatement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Transaction ID was not generated.");
                    }
                    transactionId = keys.getInt(1);
                }

                for (CartItem item : cartItems) {
                    itemStatement.setInt(1, transactionId);
                    itemStatement.setInt(2, item.getProductId());
                    itemStatement.setInt(3, item.getQuantity());
                    itemStatement.setBigDecimal(4, item.getUnitPrice());
                    itemStatement.setBigDecimal(5, item.getLineTotal());
                    itemStatement.addBatch();

                    stockStatement.setInt(1, item.getQuantity());
                    stockStatement.setInt(2, item.getProductId());
                    stockStatement.setInt(3, item.getQuantity());
                    if (stockStatement.executeUpdate() == 0) {
                        throw new SQLException("Not enough product stock for " + item.getProductName());
                    }

                    inventoryStatement.setInt(1, item.getQuantity());
                    inventoryStatement.setInt(2, item.getProductId());
                    inventoryStatement.executeUpdate();

                    logStatement.setInt(1, transactionId);
                    logStatement.setInt(2, userId);
                    logStatement.setInt(3, item.getQuantity());
                    logStatement.setInt(4, item.getProductId());
                    logStatement.executeUpdate();
                }
                itemStatement.executeBatch();
                connection.commit();
                return transactionId;
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }

    public ObservableList<TransactionRecord> findRecent() throws SQLException {
        String sql = "SELECT t.*, u.full_name AS cashier_name, vu.full_name AS voided_by_name "
                + "FROM transactions t "
                + "JOIN users u ON u.user_id = t.user_id "
                + "LEFT JOIN users vu ON vu.user_id = t.voided_by "
                + "ORDER BY t.transaction_date DESC LIMIT 100";
        ObservableList<TransactionRecord> records = FXCollections.observableArrayList();
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                TransactionRecord record = new TransactionRecord();
                record.setTransactionId(resultSet.getInt("transaction_id"));
                Timestamp timestamp = resultSet.getTimestamp("transaction_date");
                record.setTransactionDate(timestamp.toLocalDateTime());
                record.setCashierName(resultSet.getString("cashier_name"));
                record.setSubtotal(resultSet.getBigDecimal("subtotal"));
                record.setDiscount(resultSet.getBigDecimal("discount"));
                record.setTotal(resultSet.getBigDecimal("total"));
                record.setCashReceived(resultSet.getBigDecimal("cash_received"));
                record.setChangeAmount(resultSet.getBigDecimal("change_amount"));
                record.setStatus(resultSet.getString("status"));
                record.setVoidedByName(resultSet.getString("voided_by_name"));
                Timestamp voidedAt = resultSet.getTimestamp("voided_at");
                if (voidedAt != null) {
                    record.setVoidedAt(voidedAt.toLocalDateTime());
                }
                record.setVoidReason(resultSet.getString("void_reason"));
                records.add(record);
            }
        }
        return records;
    }

    public void voidTransaction(int transactionId, int userId, String reason) throws SQLException {
        String statusSql = "SELECT status FROM transactions WHERE transaction_id = ? FOR UPDATE";
        String restoreProductSql = "UPDATE products p SET stock_quantity = p.stock_quantity + ti.quantity "
                + "FROM transaction_items ti WHERE ti.product_id = p.product_id AND ti.transaction_id = ?";
        String restoreInventorySql = "UPDATE inventory i SET quantity_on_hand = i.quantity_on_hand + restored.quantity "
                + "FROM ("
                + "SELECT pi.ingredient_id, SUM(pi.quantity_required * ti.quantity) AS quantity "
                + "FROM transaction_items ti "
                + "JOIN product_ingredients pi ON pi.product_id = ti.product_id "
                + "WHERE ti.transaction_id = ? GROUP BY pi.ingredient_id"
                + ") restored WHERE restored.ingredient_id = i.ingredient_id";
        String logSql = "INSERT INTO inventory_logs "
                + "(ingredient_id, transaction_id, user_id, log_type, change_quantity, remarks) "
                + "SELECT pi.ingredient_id, ?, ?, 'VOID', SUM(pi.quantity_required * ti.quantity), ? "
                + "FROM transaction_items ti "
                + "JOIN product_ingredients pi ON pi.product_id = ti.product_id "
                + "WHERE ti.transaction_id = ? GROUP BY pi.ingredient_id";
        String voidSql = "UPDATE transactions SET status = 'VOIDED', voided_by = ?, voided_at = CURRENT_TIMESTAMP, "
                + "void_reason = ? WHERE transaction_id = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statusStatement = connection.prepareStatement(statusSql);
                    PreparedStatement productStatement = connection.prepareStatement(restoreProductSql);
                    PreparedStatement inventoryStatement = connection.prepareStatement(restoreInventorySql);
                    PreparedStatement logStatement = connection.prepareStatement(logSql);
                    PreparedStatement voidStatement = connection.prepareStatement(voidSql)) {

                statusStatement.setInt(1, transactionId);
                try (ResultSet resultSet = statusStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new SQLException("Transaction was not found.");
                    }
                    if ("VOIDED".equalsIgnoreCase(resultSet.getString("status"))) {
                        throw new SQLException("This transaction is already voided.");
                    }
                }

                productStatement.setInt(1, transactionId);
                productStatement.executeUpdate();

                inventoryStatement.setInt(1, transactionId);
                inventoryStatement.executeUpdate();

                logStatement.setInt(1, transactionId);
                logStatement.setInt(2, userId);
                logStatement.setString(3, "Void transaction: " + reason);
                logStatement.setInt(4, transactionId);
                logStatement.executeUpdate();

                voidStatement.setInt(1, userId);
                voidStatement.setString(2, reason);
                voidStatement.setInt(3, transactionId);
                voidStatement.executeUpdate();

                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }
}
