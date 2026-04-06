package eu.openvalue.hexagonalarchitecture.application.port.out;

import eu.openvalue.hexagonalarchitecture.domain.Order;

public interface OrderPersistencePort {

    Order save(Order order);
}
