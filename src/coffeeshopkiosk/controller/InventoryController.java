package coffeeshopkiosk.controller;

import coffeeshopkiosk.dao.InventoryDAO;
import coffeeshopkiosk.model.InventoryItem;
import coffeeshopkiosk.util.AlertUtil;
import coffeeshopkiosk.util.Session;
import coffeeshopkiosk.util.ValidationUtil;
import java.math.BigDecimal;
import java.sql.SQLException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class InventoryController {

    private final InventoryDAO inventoryDAO = new InventoryDAO();

    @FXML
    private Label lowStockLabel;
    @FXML
    private TextField restockQuantityField;
    @FXML
    private TableView<InventoryItem> inventoryTable;
    @FXML
    private TableColumn<InventoryItem, String> ingredientColumn;
    @FXML
    private TableColumn<InventoryItem, String> unitColumn;
    @FXML
    private TableColumn<InventoryItem, BigDecimal> quantityColumn;
    @FXML
    private TableColumn<InventoryItem, BigDecimal> reorderColumn;
    @FXML
    private TableColumn<InventoryItem, String> supplierColumn;
    @FXML
    private TableColumn<InventoryItem, String> statusColumn;

    @FXML
    private void initialize() {
        ingredientColumn.setCellValueFactory(new PropertyValueFactory<>("ingredientName"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantityOnHand"));
        reorderColumn.setCellValueFactory(new PropertyValueFactory<>("reorderLevel"));
        supplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        statusColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().isLowStock() ? "Low Stock" : "OK"));
        inventoryTable.setRowFactory(table -> new TableRow<InventoryItem>() {
            @Override
            protected void updateItem(InventoryItem item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("low-stock-row");
                if (!empty && item != null && item.isLowStock()) {
                    if (!getStyleClass().contains("low-stock-row")) {
                        getStyleClass().add("low-stock-row");
                    }
                }
            }
        });
        loadInventory();
    }

    @FXML
    private void restockSelected() {
        InventoryItem selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.warning("Select Ingredient", "Choose an inventory row to restock.");
            return;
        }
        try {
            BigDecimal quantity = ValidationUtil.parseMoney(restockQuantityField.getText());
            if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                AlertUtil.warning("Invalid Quantity", "Restock quantity must be greater than zero.");
                return;
            }
            int userId = Session.getCurrentUser() == null ? 1 : Session.getCurrentUser().getUserId();
            inventoryDAO.restock(selected.getIngredientId(), quantity, userId);
            restockQuantityField.clear();
            loadInventory();
            AlertUtil.info("Restocked", "Inventory quantity has been updated.");
        } catch (NumberFormatException ex) {
            AlertUtil.warning("Invalid Quantity", "Enter a valid number.");
        } catch (SQLException ex) {
            AlertUtil.error("Database Error", ex.getMessage());
        }
    }

    private void loadInventory() {
        try {
            ObservableList<InventoryItem> items = inventoryDAO.findAll();
            inventoryTable.setItems(items);
            lowStockLabel.setText(inventoryDAO.countLowStock() + " low stock item(s)");
        } catch (SQLException ex) {
            AlertUtil.error("Database Error", ex.getMessage());
        }
    }
}
