package eu.openvalue.hexagonalarchitecture.adapter.in.web;

import java.util.List;

public record OrderRequest(
        String customerEmail,
        String customerName,
        String shippingAddress,
        List<OrderItemRequest> items
) {
}
