package eu.openvalue.hexagonalarchitecture.application.service;

import eu.openvalue.hexagonalarchitecture.application.port.in.OrderItemCommand;
import eu.openvalue.hexagonalarchitecture.application.port.in.PlaceOrderCommand;
import eu.openvalue.hexagonalarchitecture.application.port.out.OrderPersistencePort;
import eu.openvalue.hexagonalarchitecture.domain.Order;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceOrderServiceTest {

    @Test
    void placeOrderFreeShippingWhenSubtotalAtThreshold() {
        InMemoryPersistence persistence = new InMemoryPersistence();
        PlaceOrderService service = new PlaceOrderService(persistence);

        Order saved = service.placeOrder(new PlaceOrderCommand(
                "hex@example.com",
                "Hex Customer",
                "Hex Street",
                List.of(new OrderItemCommand("SKU-1", "Digital", 1, new BigDecimal("50.00")))
        ));

        assertThat(saved.money().shippingCost().asBigDecimal()).isEqualByComparingTo("0.00");
        assertThat(saved.money().totalDue().asBigDecimal()).isEqualByComparingTo("50.00");
        assertThat(persistence.saved).hasSize(1);
    }

    @Test
    void placeOrderAddsShippingBelowThreshold() {
        InMemoryPersistence persistence = new InMemoryPersistence();
        PlaceOrderService service = new PlaceOrderService(persistence);

        Order saved = service.placeOrder(new PlaceOrderCommand(
                "hex@example.com",
                "Hex Customer",
                "Hex Street",
                List.of(new OrderItemCommand("SKU-1", "Digital", 1, new BigDecimal("10.00")))
        ));

        assertThat(saved.money().shippingCost().asBigDecimal()).isEqualByComparingTo("5.00");
        assertThat(saved.money().totalDue().asBigDecimal()).isEqualByComparingTo("15.00");
        assertThat(persistence.saved.getFirst()).isSameAs(saved);
    }

    private static final class InMemoryPersistence implements OrderPersistencePort {
        private final List<Order> saved = new ArrayList<>();

        @Override
        public Order save(Order order) {
            saved.add(order);
            return order;
        }
    }
}
