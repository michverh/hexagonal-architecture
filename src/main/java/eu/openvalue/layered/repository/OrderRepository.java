package eu.openvalue.layered.repository;

import eu.openvalue.layered.model.Order;
import eu.openvalue.layered.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    long countByCustomerEmailAndStatusIn(String customerEmail, Collection<OrderStatus> statuses);

    List<Order> findByStatus(OrderStatus status);
}
