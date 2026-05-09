package coffeeshopkiosk.util;

import coffeeshopkiosk.model.CartItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class ReceiptPrinter {

    private ReceiptPrinter() {
    }

    public static String buildReceipt(List<CartItem> items, BigDecimal subtotal, BigDecimal discount,
            BigDecimal total, BigDecimal cash, BigDecimal change) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("BREWPOINT COFFEE\n");
        receipt.append("Coffee Shop POS-IMS\n");
        receipt.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
        receipt.append("--------------------------------\n");
        for (CartItem item : items) {
            receipt.append(item.getProductName())
                    .append(" x").append(item.getQuantity())
                    .append("  ").append(MoneyUtil.format(item.getLineTotal()))
                    .append("\n");
        }
        receipt.append("--------------------------------\n");
        receipt.append("Subtotal: ").append(MoneyUtil.format(subtotal)).append("\n");
        receipt.append("Discount: ").append(MoneyUtil.format(discount)).append("\n");
        receipt.append("Total:    ").append(MoneyUtil.format(total)).append("\n");
        receipt.append("Cash:     ").append(MoneyUtil.format(cash)).append("\n");
        receipt.append("Change:   ").append(MoneyUtil.format(change)).append("\n");
        receipt.append("--------------------------------\n");
        receipt.append("Thank you!\n");
        return receipt.toString();
    }
}
