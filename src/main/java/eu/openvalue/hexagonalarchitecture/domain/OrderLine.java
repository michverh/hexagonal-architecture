package eu.openvalue.hexagonalarchitecture.domain;

import java.math.BigDecimal;

public class OrderLine {

    private final String productCode;
    private final String productName;
    private final int quantity;
    private final Money unitPrice;

    public OrderLine(String productCode, String productName, int quantity, Money unitPrice) {
        if (null == productCode || productCode.isEmpty()) {
            throw new IllegalArgumentException("Product code is required");
        }
        if (null == productName || productName.isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice == null || unitPrice.asBigDecimal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be positive");
        }
        this.productCode = productCode;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String productCode() {
        return productCode;
    }

    public String productName() {
        return productName;
    }

    public int quantity() {
        return quantity;
    }

    public Money unitPrice() {
        return unitPrice;
    }

    public Money lineTotal() {
        return unitPrice.multiply(quantity);
    }
}
