package coffeeshopkiosk.controller;

import coffeeshopkiosk.dao.CategoryDAO;
import coffeeshopkiosk.dao.ProductDAO;
import coffeeshopkiosk.dao.TransactionDAO;
import coffeeshopkiosk.model.CartItem;
import coffeeshopkiosk.model.Category;
import coffeeshopkiosk.model.Product;
import coffeeshopkiosk.util.AlertUtil;
import coffeeshopkiosk.util.ImageUtil;
import coffeeshopkiosk.util.MoneyUtil;
import coffeeshopkiosk.util.ReceiptPrinter;
import coffeeshopkiosk.util.Session;
import coffeeshopkiosk.util.ValidationUtil;
import java.math.BigDecimal;
import java.sql.SQLException;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class PosController {

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();

    @FXML
    private ComboBox<Category> categoryComboBox;
    @FXML
    private FlowPane menuFlowPane;
    @FXML
    private TableView<CartItem> cartTable;
    @FXML
    private TableColumn<CartItem, String> itemColumn;
    @FXML
    private TableColumn<CartItem, BigDecimal> priceColumn;
    @FXML
    private TableColumn<CartItem, Integer> quantityColumn;
    @FXML
    private TableColumn<CartItem, BigDecimal> totalColumn;
    @FXML
    private TextField discountField;
    @FXML
    private TextField cashField;
    @FXML
    private Label subtotalLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private Label changeLabel;
    @FXML
    private TextArea receiptArea;

    @FXML
    private void initialize() {
        itemColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        totalColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getLineTotal()));
        cartTable.setItems(cartItems);
        discountField.textProperty().addListener((obs, oldValue, newValue) -> updateTotals());
        cashField.textProperty().addListener((obs, oldValue, newValue) -> updateTotals());
        loadCategories();
        loadProducts();
    }

    @FXML
    private void filterProducts() {
        Category selected = categoryComboBox.getSelectionModel().getSelectedItem();
        try {
            if (selected == null || selected.getCategoryId() == 0) {
                renderProducts(productDAO.findActive());
            } else {
                renderProducts(productDAO.findByCategory(selected.getCategoryId()));
            }
        } catch (SQLException ex) {
            AlertUtil.error("Database Error", ex.getMessage());
        }
    }

    @FXML
    private void increaseQuantity() {
        CartItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (!canAddOneMore(selected)) {
                return;
            }
            selected.setQuantity(selected.getQuantity() + 1);
            warnIfReachedLimit(selected);
            cartTable.refresh();
            updateTotals();
        }
    }

    @FXML
    private void decreaseQuantity() {
        CartItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getQuantity() > 1) {
            selected.setQuantity(selected.getQuantity() - 1);
            cartTable.refresh();
            updateTotals();
        }
    }

    @FXML
    private void removeSelected() {
        CartItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            cartItems.remove(selected);
            updateTotals();
        }
    }

    @FXML
    private void clearCart() {
        cartItems.clear();
        receiptArea.clear();
        updateTotals();
    }

    @FXML
    private void checkout() {
        if (cartItems.isEmpty()) {
            AlertUtil.warning("Empty Cart", "Add at least one product before checkout.");
            return;
        }
        try {
            BigDecimal subtotal = computeSubtotal();
            BigDecimal discount = ValidationUtil.parseMoney(discountField.getText());
            BigDecimal total = subtotal.subtract(discount);
            BigDecimal cash = ValidationUtil.parseMoney(cashField.getText());
            if (discount.compareTo(BigDecimal.ZERO) < 0 || discount.compareTo(subtotal) > 0) {
                AlertUtil.warning("Invalid Discount", "Discount must be between zero and the subtotal.");
                return;
            }
            if (cash.compareTo(total) < 0) {
                AlertUtil.warning("Insufficient Cash", "Cash received is lower than the total amount.");
                return;
            }
            BigDecimal change = cash.subtract(total);
            int userId = Session.getCurrentUser() == null ? 1 : Session.getCurrentUser().getUserId();
            int transactionId = transactionDAO.saveTransaction(cartItems, userId, subtotal, discount, total, cash, change);
            receiptArea.setText("Transaction #" + transactionId + "\n\n"
                    + ReceiptPrinter.buildReceipt(cartItems, subtotal, discount, total, cash, change));
            cartItems.clear();
            discountField.clear();
            cashField.clear();
            loadProducts();
            updateTotals();
            AlertUtil.info("Payment Complete", "Transaction saved successfully.");
        } catch (NumberFormatException ex) {
            AlertUtil.warning("Invalid Amount", "Enter valid numeric values for discount and cash.");
        } catch (SQLException ex) {
            AlertUtil.error("Checkout Failed", ex.getMessage());
        }
    }

    private void loadCategories() {
        try {
            ObservableList<Category> categories = categoryDAO.findAll();
            categories.add(0, new Category(0, "All Categories"));
            categoryComboBox.setItems(categories);
            categoryComboBox.getSelectionModel().selectFirst();
        } catch (SQLException ex) {
            AlertUtil.error("Database Error", ex.getMessage());
        }
    }

    private void loadProducts() {
        try {
            renderProducts(productDAO.findActive());
        } catch (SQLException ex) {
            AlertUtil.error("Database Error", ex.getMessage());
        }
    }

    private void renderProducts(ObservableList<Product> products) {
        menuFlowPane.getChildren().clear();
        for (Product product : products) {
            Button button = new Button();
            button.getStyleClass().add("product-tile");
            button.setPadding(new Insets(12));
            button.setGraphic(buildProductTile(product));
            button.setOnAction(event -> addProductToCart(product));
            button.setDisable(!isSellable(product));
            menuFlowPane.getChildren().add(button);
        }
    }

    private VBox buildProductTile(Product product) {
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER);
        Image image = ImageUtil.loadProductImage(product.getImagePath());
        if (image != null) {
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(118);
            imageView.setFitHeight(74);
            imageView.setPreserveRatio(true);
            imageView.getStyleClass().add("product-image");
            box.getChildren().add(imageView);
        } else {
            Label placeholder = new Label("No Image");
            placeholder.getStyleClass().add("product-image-placeholder");
            box.getChildren().add(placeholder);
        }

        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add("product-name");
        nameLabel.setWrapText(true);
        Label priceLabel = new Label(MoneyUtil.format(product.getPrice()));
        priceLabel.getStyleClass().add("product-price");
        Label stockLabel = new Label(product.getStatus() + " | Stock: " + product.getStockQuantity());
        stockLabel.getStyleClass().add("product-stock");
        box.getChildren().addAll(nameLabel, priceLabel, stockLabel);
        return box;
    }

    private boolean isSellable(Product product) {
        return "AVAILABLE".equalsIgnoreCase(product.getStatus()) && product.getStockQuantity() > 0;
    }

    private void addProductToCart(Product product) {
        for (CartItem item : cartItems) {
            if (item.getProductId() == product.getProductId()) {
                if (!canAddOneMore(item)) {
                    return;
                }
                item.setQuantity(item.getQuantity() + 1);
                warnIfReachedLimit(item);
                cartTable.refresh();
                updateTotals();
                return;
            }
        }
        cartItems.add(new CartItem(product));
        updateTotals();
    }

    private boolean canAddOneMore(CartItem item) {
        if (item.getQuantity() >= item.getAvailableStock()) {
            AlertUtil.warning("Quantity Limit Reached",
                    item.getProductName() + " only has " + item.getAvailableStock() + " item(s) available.");
            return false;
        }
        return true;
    }

    private void warnIfReachedLimit(CartItem item) {
        if (item.getQuantity() == item.getAvailableStock()) {
            AlertUtil.warning("Quantity Limit Reached",
                    item.getProductName() + " has reached the available stock limit.");
        }
    }

    private BigDecimal computeSubtotal() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            subtotal = subtotal.add(item.getLineTotal());
        }
        return subtotal;
    }

    private void updateTotals() {
        BigDecimal subtotal = computeSubtotal();
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal cash = BigDecimal.ZERO;
        try {
            discount = ValidationUtil.parseMoney(discountField.getText());
            cash = ValidationUtil.parseMoney(cashField.getText());
        } catch (NumberFormatException ex) {
            // Keep the UI responsive while the user is typing.
        }
        BigDecimal total = subtotal.subtract(discount);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }
        BigDecimal change = cash.subtract(total);
        if (change.compareTo(BigDecimal.ZERO) < 0) {
            change = BigDecimal.ZERO;
        }
        subtotalLabel.setText(MoneyUtil.format(subtotal));
        totalLabel.setText(MoneyUtil.format(total));
        changeLabel.setText(MoneyUtil.format(change));
    }
}
