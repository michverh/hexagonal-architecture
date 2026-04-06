package eu.openvalue.hexagonalarchitecture.adapter.in.web;

import java.math.BigDecimal;

public record OrderItemRequest(
        String productCode,
        String productName,
        int quantity,
        BigDecimal unitPrice
) {
}
