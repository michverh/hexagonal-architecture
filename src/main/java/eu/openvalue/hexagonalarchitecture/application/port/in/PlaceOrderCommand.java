package eu.openvalue.hexagonalarchitecture.application.port.in;

import java.util.List;
import java.util.Objects;

public record PlaceOrderCommand(
        String customerEmail,
        String customerName,
        String shippingAddress,
        List<OrderItemCommand> items
) {

    public PlaceOrderCommand(String customerEmail,
                             String customerName,
                             String shippingAddress,
                             List<OrderItemCommand> items) {
        if (customerEmail == null || customerEmail.isBlank()) {
            throw new IllegalArgumentException("Customer email is required");
        }
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("Customer name is required");
        }
        if (shippingAddress == null || shippingAddress.isBlank()) {
            throw new IllegalArgumentException("Shipping address is required");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("At least one order item is required");
        }
        this.customerEmail = customerEmail;
        this.customerName = customerName;
        this.shippingAddress = shippingAddress;
        this.items = List.copyOf(items);
    }
}
