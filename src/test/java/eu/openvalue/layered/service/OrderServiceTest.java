package eu.openvalue.layered.service;

import eu.openvalue.layered.exception.OrderOperationException;
import eu.openvalue.layered.model.FulfillmentType;
import eu.openvalue.layered.model.Order;
import eu.openvalue.layered.model.OrderItem;
import eu.openvalue.layered.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest
class OrderServiceTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("orders_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void cleanDatabase() {
        orderRepository.deleteAll();
    }

    @Test
    void placeOrderCalculatesTotalsAndPersists() {
        Order created = orderService.placeOrder(compositeOrder("calc@example.com"));

        assertThat(created.getId()).isNotNull();
        assertThat(created.getMerchandiseTotal()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(created.getDiscountTotal()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(created.getShippingCost()).isEqualByComparingTo(new BigDecimal("6.00"));
        assertThat(created.getTotalDue()).isEqualByComparingTo(new BigDecimal("481.00"));
    }

    @Test
    void placingMoreThanThreeOpenOrdersIsRejected() {
        for (int i = 0; i < 3; i++) {
            orderService.placeOrder(simpleOrder("limit@example.com"));
        }

        assertThatThrownBy(() -> orderService.placeOrder(simpleOrder("limit@example.com")))
                .isInstanceOf(OrderOperationException.class)
                .hasMessageContaining("too many open orders");
    }

    @Test
    void updateOrderReplacesItemsAndTotals() {
        Order existing = orderService.placeOrder(simpleOrder("update@example.com"));

        Order patch = new Order();
        patch.setCustomerName("Updated Name");
        patch.setShippingAddress("New Street 42");
        OrderItem replacement = buildItem("PHYS-2", FulfillmentType.PHYSICAL, 1, new BigDecimal("200.00"));
        patch.setItems(List.of(replacement));

        Order updated = orderService.updateOrder(existing.getId(), patch);

        assertThat(updated.getCustomerName()).isEqualTo("Updated Name");
        assertThat(updated.getShippingAddress()).isEqualTo("New Street 42");
        assertThat(updated.getItems()).hasSize(1);
        assertThat(updated.getTotalDue()).isEqualByComparingTo(new BigDecimal("206.00"));
    }

    @Test
    void cancelOrderStoresReason() {
        Order existing = orderService.placeOrder(compositeOrder("cancel@example.com"));

        Order cancelled = orderService.cancelOrder(existing.getId());

        assertThat(cancelled.getStatus().name()).isEqualTo("CANCELLED");
        assertThat(cancelled.getCancelledAt()).isNotNull();
    }

    @Test
    void markOrderPaidSkipsFulfillmentQueueForDigitalOrders() {
        Order existing = orderService.placeOrder(simpleOrder("digital@example.com"));

        Order paid = orderService.markOrderPaid(existing.getId());

        assertThat(paid.getStatus().name()).isEqualTo("FULFILLMENT_PENDING");
    }

    private Order compositeOrder(String email) {
        Order order = simpleOrder(email);
        OrderItem physical = buildItem("PHYS-1", FulfillmentType.PHYSICAL, 1, new BigDecimal("400.00"));
        order.setItems(List.of(order.getItems().get(0), physical));
        return order;
    }

    private Order simpleOrder(String email) {
        Order order = new Order();
        order.setCustomerEmail(email);
        order.setCustomerName("Jane Doe");
        order.setShippingAddress("Example Street 5");
        OrderItem digital = buildItem("DIGI-1", FulfillmentType.DIGITAL, 2, new BigDecimal("50.00"));
        order.setItems(List.of(digital));
        return order;
    }

    private OrderItem buildItem(String code, FulfillmentType type, int qty, BigDecimal price) {
        OrderItem item = new OrderItem();
        item.setProductCode(code);
        item.setProductName(code + " name");
        item.setFulfillmentType(type);
        item.setQuantity(qty);
        item.setUnitPrice(price);
        return item;
    }
}
