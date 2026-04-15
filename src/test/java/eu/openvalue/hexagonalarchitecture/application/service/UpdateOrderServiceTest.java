package eu.openvalue.hexagonalarchitecture.application.service;

import eu.openvalue.hexagonalarchitecture.application.exception.HexOrderNotFoundException;
import eu.openvalue.hexagonalarchitecture.application.port.in.OrderItemCommand;
import eu.openvalue.hexagonalarchitecture.application.port.in.UpdateOrderCommand;
import eu.openvalue.hexagonalarchitecture.application.port.out.OrderLookupPort;
import eu.openvalue.hexagonalarchitecture.application.port.out.OrderPersistencePort;
import eu.openvalue.hexagonalarchitecture.domain.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpdateOrderServiceTest {

    @Test
    void updateOrderReplacesItemsAndCustomerData() {
        InMemoryLookup lookup = new InMemoryLookup();
        InMemoryPersistence persistence = new InMemoryPersistence();
        UpdateOrderService service = new UpdateOrderService(persistence, lookup);

        Order existing = orderWithStatus(OrderStatus.NEW);
        lookup.store(existing);

        Order result = service.updateOrder(new UpdateOrderCommand(
                existing.id().value(),
                "Updated",
                "New Street",
                List.of(new OrderItemCommand("SKU-2", "Physical", 1, new BigDecimal("200.00")))
        ));

        assertThat(result.customer().name()).isEqualTo("Updated");
        assertThat(result.customer().shippingAddress()).isEqualTo("New Street");
        assertThat(result.lines().items()).hasSize(1);
        assertThat(result.money().totalDue().asBigDecimal()).isEqualByComparingTo("200.00");
        assertThat(persistence.saved.getFirst()).isSameAs(result);
    }

    @Test
    void updateOrderRejectsNonEditableStatus() {
        InMemoryLookup lookup = new InMemoryLookup();
        UpdateOrderService service = new UpdateOrderService(new InMemoryPersistence(), lookup);

        Order existing = orderWithStatus(OrderStatus.SHIPPED);
        lookup.store(existing);

        assertThatThrownBy(() -> service.updateOrder(new UpdateOrderCommand(existing.id().value(), "Name", null, List.of())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void updateOrderRejectsUnknownId() {
        UpdateOrderService service = new UpdateOrderService(new InMemoryPersistence(), new InMemoryLookup());

        assertThatThrownBy(() -> service.updateOrder(new UpdateOrderCommand(999L, null, null, List.of())))
                .isInstanceOf(HexOrderNotFoundException.class);
    }

    @Test
    void updateOrderWithoutItemsKeepsExistingLines() {
        InMemoryLookup lookup = new InMemoryLookup();
        InMemoryPersistence persistence = new InMemoryPersistence();
        UpdateOrderService service = new UpdateOrderService(persistence, lookup);

        Order existing = orderWithStatus(OrderStatus.NEW);
        lookup.store(existing);

        Order result = service.updateOrder(new UpdateOrderCommand(existing.id().value(), "Updated", null, null));

        assertThat(result.lines().items()).hasSize(1);
        assertThat(result.money().totalDue()).isEqualTo(existing.money().totalDue());
    }

    @Test
    void updateOrderAppliesShippingWhenSubtotalDrops() {
        InMemoryLookup lookup = new InMemoryLookup();
        InMemoryPersistence persistence = new InMemoryPersistence();
        UpdateOrderService service = new UpdateOrderService(persistence, lookup);

        Order existing = orderWithStatus(OrderStatus.NEW);
        lookup.store(existing);

        Order result = service.updateOrder(new UpdateOrderCommand(
                existing.id().value(),
                null,
                null,
                List.of(new OrderItemCommand("SKU-3", "Physical", 1, new BigDecimal("10.00")))
        ));

        assertThat(result.money().shippingCost().asBigDecimal()).isEqualByComparingTo("5.00");
        assertThat(result.money().totalDue().asBigDecimal()).isEqualByComparingTo("15.00");
    }

    private Order orderWithStatus(OrderStatus status) {
        CustomerInfo customer = new CustomerInfo("existing@example.com", "Existing", "Street");
        OrderLines lines = OrderLines.of(List.of(new OrderLine("SKU-1", "Item", 2, Money.of(new BigDecimal("50.00")))));
        OrderMoneySnapshot money = new OrderMoneySnapshot(Money.zero(), Money.of(new BigDecimal("100.00")));
        OrderLifecycle lifecycle = OrderLifecycle.restore(status);
        return Order.restore(new OrderId(new Random().nextLong(1, Long.MAX_VALUE)), customer, lines, money, lifecycle);
    }

    private static final class InMemoryPersistence implements OrderPersistencePort {
        private final List<Order> saved = new ArrayList<>();

        @Override
        public Order save(Order order) {
            saved.add(order);
            return order;
        }
    }

    private static final class InMemoryLookup implements OrderLookupPort {
        private final Map<Long, Order> orders = new HashMap<>();

        void store(Order order) {
            orders.put(order.id().value(), order);
        }

        @Override
        public Optional<Order> findById(Long id) {
            return Optional.ofNullable(orders.get(id));
        }

        @Override
        public List<Order> findAll() {
            return new ArrayList<>(orders.values());
        }

        @Override
        public List<Order> findByStatus(OrderStatus status) {
            return orders.values().stream()
                    .filter(order -> order.status() == status)
                    .toList();
        }
    }
}
