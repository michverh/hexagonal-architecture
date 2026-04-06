package eu.openvalue.hexagonalarchitecture.adapter.out.persistence;

import eu.openvalue.hexagonalarchitecture.application.port.out.OrderLookupPort;
import eu.openvalue.hexagonalarchitecture.application.port.out.OrderPersistencePort;
import eu.openvalue.hexagonalarchitecture.domain.Order;
import eu.openvalue.hexagonalarchitecture.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements OrderPersistencePort, OrderLookupPort {

    private final JpaOrderRepository orderRepository;
    private final HexOrderPersistenceMapper mapper;

    @Override
    public Order save(Order order) {
        JpaOrderEntity entity = mapper.toEntity(order);
        JpaOrderEntity persisted = orderRepository.save(entity);
        return mapper.toDomain(persisted);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream().map(mapper::toDomain).toList();
    }
}
