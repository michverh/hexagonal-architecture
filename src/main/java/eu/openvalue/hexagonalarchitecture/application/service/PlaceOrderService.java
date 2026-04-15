package eu.openvalue.hexagonalarchitecture.application.service;

import eu.openvalue.hexagonalarchitecture.application.port.in.OrderItemCommand;
import eu.openvalue.hexagonalarchitecture.application.port.in.PlaceOrderCommand;
import eu.openvalue.hexagonalarchitecture.application.port.in.PlaceOrderUseCase;
import eu.openvalue.hexagonalarchitecture.application.port.out.OrderPersistencePort;
import eu.openvalue.hexagonalarchitecture.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PlaceOrderService implements PlaceOrderUseCase {

    private final OrderPersistencePort persistencePort;

    public PlaceOrderService(OrderPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    @Override
    public Order placeOrder(PlaceOrderCommand command) {
        CustomerInfo customer = new CustomerInfo(
                command.customerEmail(),
                command.customerName(),
                command.shippingAddress()
        );
        OrderLines lines = toOrderLines(command.items());
        Order order = Order.newOrder(customer, lines);
        return persistencePort.save(order);
    }

    private OrderLines toOrderLines(List<OrderItemCommand> items) {
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
