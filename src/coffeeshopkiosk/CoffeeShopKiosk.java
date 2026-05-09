package coffeeshopkiosk;

import coffeeshopkiosk.controller.DashboardController;
import coffeeshopkiosk.controller.InventoryController;
import coffeeshopkiosk.controller.CashierManagementController;
import coffeeshopkiosk.controller.LoginController;
import coffeeshopkiosk.controller.PosController;
import coffeeshopkiosk.controller.ProductController;
import coffeeshopkiosk.controller.ReportsController;
import coffeeshopkiosk.controller.TransactionHistoryController;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CoffeeShopKiosk extends Application {

    private static final Class<?>[] FXML_CONTROLLERS = {
        LoginController.class,
        DashboardController.class,
        PosController.class,
        InventoryController.class,
        ProductController.class,
        CashierManagementController.class,
        ReportsController.class,
        TransactionHistoryController.class
    };

    @Override
    public void start(Stage stage) throws Exception {
        keepRunFileCompilationComplete();
        AppNavigator.setStage(stage);
        Parent root = javafx.fxml.FXMLLoader.load(AppNavigator.resource("/coffeeshopkiosk/view/Login.fxml"));
        Scene scene = new Scene(root, 1100, 720);
        scene.getStylesheets().add(AppNavigator.resource("/coffeeshopkiosk/css/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Coffee Shop POS-IMS");
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static void keepRunFileCompilationComplete() {
        if (FXML_CONTROLLERS.length == 0) {
            throw new IllegalStateException("FXML controllers are not registered.");
        }
    }
}
