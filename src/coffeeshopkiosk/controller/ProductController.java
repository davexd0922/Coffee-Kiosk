package coffeeshopkiosk.controller;

import coffeeshopkiosk.AppNavigator;
import coffeeshopkiosk.dao.CategoryDAO;
import coffeeshopkiosk.dao.ProductDAO;
import coffeeshopkiosk.model.Category;
import coffeeshopkiosk.model.Product;
import coffeeshopkiosk.util.AlertUtil;
import coffeeshopkiosk.util.ImageUtil;
import coffeeshopkiosk.util.ValidationUtil;
import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

public class ProductController {

    private static final String STATUS_AVAILABLE = "AVAILABLE";
    private static final String STATUS_OUT_OF_STOCK = "OUT_OF_STOCK";
    private static final String STATUS_UNAVAILABLE = "UNAVAILABLE";

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private Product selectedProduct;

    @FXML
    private TextField nameField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField stockField;
    @FXML
    private TextField imagePathField;
    @FXML
    private TextField newCategoryField;
    @FXML
    private ImageView imagePreview;
    @FXML
    private ComboBox<Category> categoryComboBox;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, String> nameColumn;
    @FXML
    private TableColumn<Product, String> categoryColumn;
    @FXML
    private TableColumn<Product, BigDecimal> priceColumn;
    @FXML
    private TableColumn<Product, Integer> stockColumn;
    @FXML
    private TableColumn<Product, String> statusColumn;

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> showProduct(newValue));
        imagePathField.textProperty().addListener((obs, oldValue, newValue) -> updateImagePreview());
        statusComboBox.getItems().setAll(STATUS_AVAILABLE, STATUS_OUT_OF_STOCK, STATUS_UNAVAILABLE);
        statusComboBox.getSelectionModel().select(STATUS_AVAILABLE);
        loadCategories();
        loadProducts();
    }

    @FXML
    private void saveProduct() {
        if (ValidationUtil.isBlank(nameField.getText()) || categoryComboBox.getSelectionModel().getSelectedItem() == null) {
            AlertUtil.warning("Missing Product", "Enter product name and category.");
            return;
        }
        try {
            Product product = selectedProduct == null ? new Product() : selectedProduct;
            product.setName(nameField.getText().trim());
            product.setCategoryId(categoryComboBox.getSelectionModel().getSelectedItem().getCategoryId());
            product.setPrice(ValidationUtil.parseMoney(priceField.getText()));
            product.setStockQuantity(ValidationUtil.parseInt(stockField.getText()));
            product.setImagePath(imagePathField.getText());
            product.setStatus(statusComboBox.getSelectionModel().getSelectedItem());
            product.setActive(true);
            if (product.getPrice().signum() < 0 || product.getStockQuantity() < 0) {
                AlertUtil.warning("Invalid Product", "Price and stock cannot be negative.");
                return;
            }
            if (product.getStockQuantity() == 0 && STATUS_AVAILABLE.equals(product.getStatus())) {
                product.setStatus(STATUS_OUT_OF_STOCK);
            }
            productDAO.save(product);
            clearForm();
            loadProducts();
            AlertUtil.info("Saved", "Product has been saved.");
        } catch (NumberFormatException ex) {
            AlertUtil.warning("Invalid Input", "Price and stock must be valid numbers.");
        } catch (SQLException ex) {
            AlertUtil.error("Database Error", ex.getMessage());
        }
    }

    @FXML
    private void addCategory() {
        if (ValidationUtil.isBlank(newCategoryField.getText())) {
            AlertUtil.warning("Missing Category", "Enter a category name.");
            return;
        }
        try {
            String categoryName = newCategoryField.getText().trim();
            categoryDAO.addCategory(categoryName);
            newCategoryField.clear();
            loadCategories();
            selectCategoryByName(categoryName);
            AlertUtil.info("Category Added", "The new category is ready to use.");
        } catch (SQLException ex) {
            if ("23505".equals(ex.getSQLState())) {
                AlertUtil.warning("Duplicate Category", "That category already exists.");
            } else {
                AlertUtil.error("Database Error", ex.getMessage());
            }
        }
    }

    @FXML
    private void deleteSelectedCategory() {
        Category selected = categoryComboBox.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.warning("Select Category", "Choose a category to delete.");
            return;
        }

        try {
            int productCount = categoryDAO.countProductsInCategory(selected.getCategoryId());
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Category");
            confirm.setHeaderText("Delete category: " + selected.getName());
            confirm.setContentText("This will also remove " + productCount
                    + " active product(s) under this category from the POS menu. Continue?");
            Optional<ButtonType> result = confirm.showAndWait();
            if (!result.isPresent() || result.get() != ButtonType.OK) {
                return;
            }

            categoryDAO.deleteCategoryWithProducts(selected.getCategoryId());
            clearForm();
            loadCategories();
            loadProducts();
            AlertUtil.info("Category Deleted", "The category and its products were removed from active use.");
        } catch (SQLException ex) {
            AlertUtil.error("Database Error", ex.getMessage());
        }
    }

    @FXML
    private void deleteProduct() {
        if (selectedProduct == null) {
            AlertUtil.warning("Select Product", "Choose a product to delete.");
            return;
        }
        try {
            productDAO.deactivate(selectedProduct.getProductId());
            clearForm();
            loadProducts();
            AlertUtil.info("Deleted", "Product has been deactivated.");
        } catch (SQLException ex) {
            AlertUtil.error("Database Error", ex.getMessage());
        }
    }

    @FXML
    private void chooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Product Image");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = chooser.showOpenDialog(AppNavigator.getStage());
        if (file != null) {
            imagePathField.setText(file.getAbsolutePath());
            updateImagePreview();
        }
    }

    @FXML
    private void clearForm() {
        selectedProduct = null;
        productTable.getSelectionModel().clearSelection();
        nameField.clear();
        priceField.clear();
        stockField.clear();
        imagePathField.clear();
        imagePreview.setImage(null);
        statusComboBox.getSelectionModel().select(STATUS_AVAILABLE);
        if (!categoryComboBox.getItems().isEmpty()) {
            categoryComboBox.getSelectionModel().selectFirst();
        }
    }

    private void showProduct(Product product) {
        selectedProduct = product;
        if (product == null) {
            return;
        }
        nameField.setText(product.getName());
        priceField.setText(product.getPrice().toPlainString());
        stockField.setText(String.valueOf(product.getStockQuantity()));
        imagePathField.setText(product.getImagePath());
        updateImagePreview();
        statusComboBox.getSelectionModel().select(product.getStatus());
        selectCategoryById(product.getCategoryId());
    }

    private void loadCategories() {
        try {
            categoryComboBox.setItems(categoryDAO.findAll());
        } catch (SQLException ex) {
            AlertUtil.error("Database Error", ex.getMessage());
        }
    }

    private void loadProducts() {
        try {
            ObservableList<Product> products = productDAO.findAll();
            productTable.setItems(products);
        } catch (SQLException ex) {
            AlertUtil.error("Database Error", ex.getMessage());
        }
    }

    private void updateImagePreview() {
        Image image = ImageUtil.loadProductImage(imagePathField.getText());
        imagePreview.setImage(image);
    }

    private void selectCategoryById(int categoryId) {
        for (Category category : categoryComboBox.getItems()) {
            if (category.getCategoryId() == categoryId) {
                categoryComboBox.getSelectionModel().select(category);
                return;
            }
        }
    }

    private void selectCategoryByName(String categoryName) {
        for (Category category : categoryComboBox.getItems()) {
            if (category.getName().equalsIgnoreCase(categoryName)) {
                categoryComboBox.getSelectionModel().select(category);
                return;
            }
        }
    }
}
