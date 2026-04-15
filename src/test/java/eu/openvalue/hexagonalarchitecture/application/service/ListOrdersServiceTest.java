package eu.openvalue.hexagonalarchitecture.application.service;

import eu.openvalue.hexagonalarchitecture.application.port.out.OrderLookupPort;
import eu.openvalue.hexagonalarchitecture.domain.CustomerInfo;
import eu.openvalue.hexagonalarchitecture.domain.Money;
import eu.openvalue.hexagonalarchitecture.domain.Order;
import eu.openvalue.hexagonalarchitecture.domain.OrderId;
import eu.openvalue.hexagonalarchitecture.domain.OrderLifecycle;
import eu.openvalue.hexagonalarchitecture.domain.OrderLine;
import eu.openvalue.hexagonalarchitecture.domain.OrderLines;
import eu.openvalue.hexagonalarchitecture.domain.OrderMoneySnapshot;
import eu.openvalue.hexagonalarchitecture.domain.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ListOrdersServiceTest {

    @Test
    void listOrdersWithStatusUsesFilteredResults() {
        RecordingLookup lookup = new RecordingLookup();
        ListOrdersService service = new ListOrdersService(lookup);

        OrderStatus status = OrderStatus.NEW;
        Order order = sampleOrder(1L);
        lookup.setFindByStatusResult(List.of(order));

        assertThat(service.listOrders(Optional.of(status))).containsExactly(order);
    }

    @Test
    void listOrdersWithoutStatusReturnsAll() {
        RecordingLookup lookup = new RecordingLookup();
        ListOrdersService service = new ListOrdersService(lookup);

        List<Order> allOrders = List.of(sampleOrder(2L));
        lookup.setFindAllResult(allOrders);

        assertThat(service.listOrders(Optional.empty())).isEqualTo(allOrders);
    }

    private Order sampleOrder(Long id) {
        CustomerInfo customer = new CustomerInfo("customer" + id + "@example.com", "Customer" + id, "Street" + id);
        OrderLines lines = OrderLines.of(List.of(new OrderLine("SKU-" + id, "Item", 1, Money.of(BigDecimal.TEN))));
        OrderMoneySnapshot money = new OrderMoneySnapshot(Money.of(BigDecimal.valueOf(5)), Money.of(BigDecimal.valueOf(15)));
        return Order.restore(OrderId.of(id), customer, lines, money, OrderLifecycle.restore(OrderStatus.NEW));
    }

    private static final class RecordingLookup implements OrderLookupPort {
        private List<Order> findAllResult = new ArrayList<>();
        private List<Order> findByStatusResult = new ArrayList<>();

        void setFindAllResult(List<Order> orders) {
            this.findAllResult = new ArrayList<>(orders);
        }

        void setFindByStatusResult(List<Order> orders) {
            this.findByStatusResult = new ArrayList<>(orders);
        }

        @Override
        public List<Order> findAll() {
            return findAllResult;
        }

        @Override
        public List<Order> findByStatus(OrderStatus status) {
            return findByStatusResult;
        }

        @Override
        public Optional<Order> findById(Long id) {
            return Optional.empty();
        }
    }
}
