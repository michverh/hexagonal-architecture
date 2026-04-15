package eu.openvalue.hexagonalarchitecture.application.port.in;

import java.util.List;
import java.util.Objects;

public record UpdateOrderCommand(
        Long orderId,
        String customerName,
        String shippingAddress,
        List<OrderItemCommand> items
) {

    public UpdateOrderCommand {
        Objects.requireNonNull(orderId, "Order id is required");
    }
}
