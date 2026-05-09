package coffeeshopkiosk.model;

import java.math.BigDecimal;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CartItem {

    private final int productId;
    private final StringProperty productName;
    private final SimpleObjectProperty<BigDecimal> unitPrice;
    private final IntegerProperty quantity;
    private final int availableStock;

    public CartItem(Product product) {
        this.productId = product.getProductId();
        this.productName = new SimpleStringProperty(product.getName());
        this.unitPrice = new SimpleObjectProperty<>(product.getPrice());
        this.quantity = new SimpleIntegerProperty(1);
        this.availableStock = product.getStockQuantity();
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName.get();
    }

    public StringProperty productNameProperty() {
        return productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice.get();
    }

    public SimpleObjectProperty<BigDecimal> unitPriceProperty() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity.get();
    }

    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public BigDecimal getLineTotal() {
        return getUnitPrice().multiply(BigDecimal.valueOf(getQuantity()));
    }
}
