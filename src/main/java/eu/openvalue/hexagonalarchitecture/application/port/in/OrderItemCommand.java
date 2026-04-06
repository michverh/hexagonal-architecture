package eu.openvalue.hexagonalarchitecture.application.port.in;

import java.math.BigDecimal;

public record OrderItemCommand(
        String productCode,
        String productName,
        int quantity,
        BigDecimal unitPrice
) {

    public OrderItemCommand {
        if (productCode == null || productCode.isBlank()) {
            throw new IllegalArgumentException("Product code is required");
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be positive");
        }
    }
}
