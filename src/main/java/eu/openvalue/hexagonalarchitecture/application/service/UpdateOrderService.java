package eu.openvalue.hexagonalarchitecture.application.service;

import eu.openvalue.hexagonalarchitecture.application.exception.HexOrderNotFoundException;
import eu.openvalue.hexagonalarchitecture.application.port.in.OrderItemCommand;
import eu.openvalue.hexagonalarchitecture.application.port.in.UpdateOrderCommand;
import eu.openvalue.hexagonalarchitecture.application.port.in.UpdateOrderUseCase;
import eu.openvalue.hexagonalarchitecture.application.port.out.OrderLookupPort;
import eu.openvalue.hexagonalarchitecture.application.port.out.OrderPersistencePort;
import eu.openvalue.hexagonalarchitecture.domain.Money;
import eu.openvalue.hexagonalarchitecture.domain.Order;
import eu.openvalue.hexagonalarchitecture.domain.OrderLine;
import eu.openvalue.hexagonalarchitecture.domain.OrderLines;

import java.util.List;

public class UpdateOrderService implements UpdateOrderUseCase {

    private final OrderPersistencePort persistencePort;
    private final OrderLookupPort lookupPort;

    public UpdateOrderService(OrderPersistencePort persistencePort, OrderLookupPort lookupPort) {
        this.persistencePort = persistencePort;
        this.lookupPort = lookupPort;
    }

    @Override
    public Order updateOrder(UpdateOrderCommand command) {
        Order managed = lookupPort.findById(command.orderId())
                .orElseThrow(() -> new HexOrderNotFoundException(command.orderId()));

        if (command.customerName() != null || command.shippingAddress() != null) {
            managed.updateCustomerDetails(command.customerName(), command.shippingAddress());
        }
        if (command.items() != null && !command.items().isEmpty()) {
            managed.replaceLines(toOrderLines(command.items()));
        }

        return persistencePort.save(managed);
    }

    private OrderLines toOrderLines(List<OrderItemCommand> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("At least one order item is required");
        }
        return OrderLines.of(items.stream()
                .map(item -> new OrderLine(
                        item.productCode(),
                        item.productName(),
                        item.quantity(),
                        Money.of(item.unitPrice())
                ))
                .toList());
    }
}
