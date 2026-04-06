package eu.openvalue.hexagonalarchitecture.adapter.in.web;

import java.util.List;

public record HexOrderRequest(
        String customerEmail,
        String customerName,
        String shippingAddress,
        List<OrderItemRequest> items
) {
}
