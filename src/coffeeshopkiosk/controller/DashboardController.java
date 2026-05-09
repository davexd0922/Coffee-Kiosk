package coffeeshopkiosk.controller;

import coffeeshopkiosk.AppNavigator;
import coffeeshopkiosk.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class DashboardController {

    @FXML
    private Label userLabel;
    @FXML
    private Button productsButton;
    @FXML
    private Button inventoryButton;
    @FXML
    private Button reportsButton;
    @FXML
    private Button cashiersButton;
    @FXML
    private StackPane contentPane;

    @FXML
    private void initialize() {
        if (Session.getCurrentUser() != null) {
            userLabel.setText(Session.getCurrentUser().getFullName() + " - " + Session.getCurrentUser().getRoleName());
        }
        boolean admin = Session.isAdmin();
        productsButton.setVisible(admin);
        productsButton.setManaged(admin);
        inventoryButton.setVisible(admin);
        inventoryButton.setManaged(admin);
        reportsButton.setVisible(admin);
        reportsButton.setManaged(admin);
        cashiersButton.setVisible(admin);
        cashiersButton.setManaged(admin);
        showPOS();
    }

    @FXML
    private void showPOS() {
        AppNavigator.loadInto(contentPane, "/coffeeshopkiosk/view/Pos.fxml");
    }

    @FXML
    private void showInventory() {
        AppNavigator.loadInto(contentPane, "/coffeeshopkiosk/view/Inventory.fxml");
    }

    @FXML
    private void showProducts() {
        AppNavigator.loadInto(contentPane, "/coffeeshopkiosk/view/ProductManagement.fxml");
    }

    @FXML
    private void showReports() {
        AppNavigator.loadInto(contentPane, "/coffeeshopkiosk/view/Reports.fxml");
    }

    @FXML
    private void showCashiers() {
        AppNavigator.loadInto(contentPane, "/coffeeshopkiosk/view/CashierManagement.fxml");
    }

    @FXML
    private void showHistory() {
        AppNavigator.loadInto(contentPane, "/coffeeshopkiosk/view/TransactionHistory.fxml");
    }

    @FXML
    private void logout() {
        Session.logout();
        AppNavigator.loadScene("/coffeeshopkiosk/view/Login.fxml");
    }
}
