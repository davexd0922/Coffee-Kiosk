package coffeeshopkiosk.controller;

import coffeeshopkiosk.AppNavigator;
import coffeeshopkiosk.dao.UserDAO;
import coffeeshopkiosk.model.User;
import coffeeshopkiosk.util.AlertUtil;
import coffeeshopkiosk.util.Session;
import coffeeshopkiosk.util.ValidationUtil;
import java.sql.SQLException;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private void login() {
        if (ValidationUtil.isBlank(usernameField.getText()) || ValidationUtil.isBlank(passwordField.getText())) {
            AlertUtil.warning("Missing Login", "Enter both username and password.");
            return;
        }

        try {
            Optional<User> user = userDAO.authenticate(usernameField.getText(), passwordField.getText());
            if (!user.isPresent()) {
                AlertUtil.error("Login Failed", "Invalid username or password.");
                return;
            }
            Session.login(user.get());
            AppNavigator.loadScene("/coffeeshopkiosk/view/Dashboard.fxml");
        } catch (SQLException ex) {
            AlertUtil.error("Database Error", ex.getMessage());
        }
    }
}
