package eu.openvalue.hexagonalarchitecture.adapter.in.web;

import java.util.List;

public record OrderUpdateRequest(
        String customerName,
        String shippingAddress,
        List<OrderItemRequest> items
) {
}
