package eu.openvalue.hexagonalarchitecture.application.port.in;

import eu.openvalue.hexagonalarchitecture.domain.Order;

public interface UpdateOrderUseCase {

    Order updateOrder(UpdateOrderCommand command);
}
