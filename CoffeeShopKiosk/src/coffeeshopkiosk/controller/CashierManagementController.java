package coffeeshopkiosk.controller;

import coffeeshopkiosk.dao.UserDAO;
import coffeeshopkiosk.model.User;
import coffeeshopkiosk.util.AlertUtil;
import coffeeshopkiosk.util.ValidationUtil;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class CashierManagementController {

    private final UserDAO userDAO = new UserDAO();
    private User selectedCashier;

    @FXML
    private TableView<User> cashierTable;
    @FXML
    private TableColumn<User, String> fullNameColumn;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, Boolean> activeColumn;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private CheckBox activeCheckBox;

    @FXML
    private void initialize() {
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        cashierTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> showCashier(newValue));
        activeCheckBox.setSelected(true);
        loadCashiers();
    }

    @FXML
    private void saveCashier() {
        if (ValidationUtil.isBlank(fullNameField.getText()) || ValidationUtil.isBlank(usernameField.getText())) {
            AlertUtil.warning("Missing Cashier", "Enter the cashier name and username.");
            return;
        }
        if (selectedCashier == null && ValidationUtil.isBlank(passwordField.getText())) {
            AlertUtil.warning("Missing Password", "Enter an initial password for the new cashier.");
            return;
        }

        try {
            User cashier = selectedCashier == null ? new User() : selectedCashier;
            cashier.setFullName(fullNameField.getText().trim());
            cashier.setUsername(usernameField.getText().trim());
            cashier.setRoleName("CASHIER");
            cashier.setActive(activeCheckBox.isSelected());
            userDAO.saveCashier(cashier, passwordField.getText());
            clearForm();
            loadCashiers();
            AlertUtil.info("Saved", "Cashier account has been saved.");
        } catch (SQLException ex) {
            AlertUtil.error("Database Error", ex.getMessage());
        }
    }

    @FXML
    private void changePassword() {
        if (selectedCashier == null) {
            AlertUtil.warning("Select Cashier", "Choose a cashier account first.");
            return;
        }
        if (ValidationUtil.isBlank(passwordField.getText()) || passwordField.getText().length() < 6) {
            AlertUtil.warning("Weak Password", "Enter a new password with at least 6 characters.");
            return;
        }

        try {
            userDAO.changePassword(selectedCashier.getUserId(), passwordField.getText());
            passwordField.clear();
            AlertUtil.info("Password Updated", "The cashier password has been changed.");
        } catch (SQLException ex) {
            AlertUtil.error("Database Error", ex.getMessage());
        }
    }

    @FXML
    private void deactivateCashier() {
        if (selectedCashier == null) {
            AlertUtil.warning("Select Cashier", "Choose a cashier account first.");
            return;
        }
        selectedCashier.setActive(false);
        try {
            userDAO.saveCashier(selectedCashier, "");
            clearForm();
            loadCashiers();
            AlertUtil.info("Deactivated", "Cashier account has been deactivated.");
        } catch (SQLException ex) {
            AlertUtil.error("Database Error", ex.getMessage());
        }
    }

    @FXML
    private void clearForm() {
        selectedCashier = null;
        cashierTable.getSelectionModel().clearSelection();
        fullNameField.clear();
        usernameField.clear();
        passwordField.clear();
        activeCheckBox.setSelected(true);
    }

    private void showCashier(User cashier) {
        selectedCashier = cashier;
        if (cashier == null) {
            return;
        }
        fullNameField.setText(cashier.getFullName());
        usernameField.setText(cashier.getUsername());
        passwordField.clear();
        activeCheckBox.setSelected(cashier.isActive());
    }

    private void loadCashiers() {
        try {
            cashierTable.setItems(userDAO.findCashiers());
        } catch (SQLException ex) {
            AlertUtil.error("Database Error", ex.getMessage());
        }
    }
}
