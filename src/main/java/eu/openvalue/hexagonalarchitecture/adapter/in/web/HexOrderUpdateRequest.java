package eu.openvalue.hexagonalarchitecture.adapter.in.web;

import java.util.List;

public record HexOrderUpdateRequest(
        String customerName,
        String shippingAddress,
        List<OrderItemRequest> items
) {
}
