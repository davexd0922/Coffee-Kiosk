package coffeeshopkiosk.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private static final String URL = "jdbc:postgresql://localhost:5432/coffee_shop_kiosk";
    private static final String USER = "postgres";
    private static final String PASSWORD = "kwen";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
