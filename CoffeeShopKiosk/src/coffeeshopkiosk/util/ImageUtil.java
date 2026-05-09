package coffeeshopkiosk.util;

import java.io.File;
import javafx.scene.image.Image;

public final class ImageUtil {

    private ImageUtil() {
    }

    public static Image loadProductImage(String imagePath) {
        if (ValidationUtil.isBlank(imagePath)) {
            return null;
        }

        try {
            File file = new File(imagePath);
            if (file.exists()) {
                return new Image(file.toURI().toString(), true);
            }

            String resourcePath = imagePath.startsWith("/") ? imagePath : "/" + imagePath;
            if (ImageUtil.class.getResource(resourcePath) != null) {
                return new Image(ImageUtil.class.getResource(resourcePath).toExternalForm(), true);
            }

            if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                return new Image(imagePath, true);
            }
        } catch (IllegalArgumentException ex) {
            return null;
        }
        return null;
    }
}
