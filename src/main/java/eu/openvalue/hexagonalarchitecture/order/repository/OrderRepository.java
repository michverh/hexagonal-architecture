package eu.openvalue.hexagonalarchitecture.order.repository;

import eu.openvalue.hexagonalarchitecture.order.model.Order;
import eu.openvalue.hexagonalarchitecture.order.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    long countByCustomerEmailAndStatusIn(String customerEmail, Collection<OrderStatus> statuses);

    List<Order> findByStatus(OrderStatus status);
}
