package eu.openvalue.hexagonalarchitecture.application.port.out;

import eu.openvalue.hexagonalarchitecture.domain.Order;
import eu.openvalue.hexagonalarchitecture.domain.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderLookupPort {

    Optional<Order> findById(Long id);

    List<Order> findAll();

    List<Order> findByStatus(OrderStatus status);
}
