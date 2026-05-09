package coffeeshopkiosk.model;

import java.math.BigDecimal;

public class SalesReportRow {

    private String label;
    private int orders;
    private BigDecimal revenue;

    public SalesReportRow(String label, int orders, BigDecimal revenue) {
        this.label = label;
        this.orders = orders;
        this.revenue = revenue;
    }

    public String getLabel() {
        return label;
    }

    public int getOrders() {
        return orders;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }
}
