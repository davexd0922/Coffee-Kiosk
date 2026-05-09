package coffeeshopkiosk.dao;

import coffeeshopkiosk.database.DatabaseConnection;
import coffeeshopkiosk.model.SalesReportRow;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ReportDAO {

    public BigDecimal totalRevenue(LocalDate start, LocalDate end) throws SQLException {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM transactions "
                + "WHERE transaction_date >= ? AND transaction_date < ? AND status = 'COMPLETED'";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, start);
            statement.setObject(2, end.plusDays(1));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    public ObservableList<SalesReportRow> salesByDay(LocalDate start, LocalDate end) throws SQLException {
        String sql = "SELECT CAST(transaction_date AS DATE) AS sale_day, COUNT(*) AS orders, "
                + "COALESCE(SUM(total), 0) AS revenue FROM transactions "
                + "WHERE transaction_date >= ? AND transaction_date < ? AND status = 'COMPLETED' "
                + "GROUP BY sale_day ORDER BY sale_day";
        ObservableList<SalesReportRow> rows = FXCollections.observableArrayList();
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, start);
            statement.setObject(2, end.plusDays(1));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rows.add(new SalesReportRow(
                            resultSet.getString("sale_day"),
                            resultSet.getInt("orders"),
                            resultSet.getBigDecimal("revenue")));
                }
            }
        }
        return rows;
    }

    public ObservableList<SalesReportRow> bestSellers(LocalDate start, LocalDate end) throws SQLException {
        String sql = "SELECT p.name, SUM(ti.quantity) AS orders, COALESCE(SUM(ti.line_total), 0) AS revenue "
                + "FROM transaction_items ti "
                + "JOIN transactions t ON t.transaction_id = ti.transaction_id "
                + "JOIN products p ON p.product_id = ti.product_id "
                + "WHERE t.transaction_date >= ? AND t.transaction_date < ? AND t.status = 'COMPLETED' "
                + "GROUP BY p.name ORDER BY orders DESC LIMIT 10";
        ObservableList<SalesReportRow> rows = FXCollections.observableArrayList();
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, start);
            statement.setObject(2, end.plusDays(1));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rows.add(new SalesReportRow(
                            resultSet.getString("name"),
                            resultSet.getInt("orders"),
                            resultSet.getBigDecimal("revenue")));
                }
            }
        }
        return rows;
    }
}
