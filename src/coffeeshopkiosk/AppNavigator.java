package coffeeshopkiosk;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public final class AppNavigator {

    private static Stage stage;

    private AppNavigator() {
    }

    public static void setStage(Stage primaryStage) {
        stage = primaryStage;
    }

    public static Stage getStage() {
        return stage;
    }

    public static void loadScene(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(resource(fxmlPath));
            Scene scene = new Scene(root, 1100, 720);
            scene.getStylesheets().add(resource("/coffeeshopkiosk/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot load screen: " + fxmlPath, ex);
        }
    }

    public static void loadInto(Pane container, String fxmlPath) {
        try {
            Parent content = FXMLLoader.load(resource(fxmlPath));
            container.getChildren().setAll(content);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot load view: " + fxmlPath, ex);
        }
    }

    public static URL resource(String path) {
        URL url = AppNavigator.class.getResource(path);
        if (url != null) {
            return url;
        }

        String normalizedPath = path.startsWith("/") ? path.substring(1) : path;
        Path sourcePath = Paths.get("src").resolve(normalizedPath);
        if (Files.exists(sourcePath)) {
            try {
                return sourcePath.toUri().toURL();
            } catch (IOException ex) {
                throw new IllegalStateException("Cannot load resource from src: " + path, ex);
            }
        }

        throw new IllegalStateException("Resource not found: " + path);
    }
}
