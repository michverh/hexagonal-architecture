package eu.openvalue.hexagonalarchitecture.application.port.in;

import eu.openvalue.hexagonalarchitecture.domain.Order;
import eu.openvalue.hexagonalarchitecture.domain.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface ListOrdersUseCase {

    List<Order> listOrders(Optional<OrderStatus> statusFilter);
}
