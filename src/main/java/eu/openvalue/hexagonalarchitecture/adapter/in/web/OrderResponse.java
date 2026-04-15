package eu.openvalue.hexagonalarchitecture.adapter.in.web;

import eu.openvalue.hexagonalarchitecture.domain.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        Long id,
        String customerEmail,
        String customerName,
        String shippingAddress,
        OrderStatus status,
        BigDecimal shippingCost,
        BigDecimal totalDue,
        List<OrderItemResponse> items
) {

    public record OrderItemResponse(
            String productCode,
            String productName,
            int quantity,
            BigDecimal unitPrice
    ) {
    }
}
