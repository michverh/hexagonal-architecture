package eu.openvalue.hexagonalarchitecture.order.service;

import eu.openvalue.hexagonalarchitecture.order.exception.OrderNotFoundException;
import eu.openvalue.hexagonalarchitecture.order.exception.OrderOperationException;
import eu.openvalue.hexagonalarchitecture.order.model.FulfillmentType;
import eu.openvalue.hexagonalarchitecture.order.model.Order;
import eu.openvalue.hexagonalarchitecture.order.model.OrderItem;
import eu.openvalue.hexagonalarchitecture.order.model.OrderStatus;
import eu.openvalue.hexagonalarchitecture.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class OrderService {

    private static final BigDecimal LOYALTY_THRESHOLD = new BigDecimal("500.00");
    private static final BigDecimal LOYALTY_DISCOUNT_RATE = new BigDecimal("0.05");
    private static final BigDecimal SHIPPING_BASE = new BigDecimal("5.00");
    private static final BigDecimal SHIPPING_PER_UNIT = BigDecimal.ONE;
    private static final Set<OrderStatus> OPEN_STATUSES = EnumSet.of(OrderStatus.NEW, OrderStatus.FULFILLMENT_PENDING);

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order placeOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order body is required");
        }
        validateCustomer(order);
        sanitizeItems(order);
        validateItems(order.getItems());
        enforceOpenOrderLimit(order.getCustomerEmail());

        Instant now = Instant.now();
        order.setId(null);
        order.setVersion(null);
        order.setStatus(OrderStatus.NEW);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        order.setCancelledAt(null);

        applyFinancials(order);
        return orderRepository.save(order);
    }

    public Order updateOrder(Long id, Order updated) {
        Objects.requireNonNull(id, "Order id is required");
        if (updated == null) {
            throw new IllegalArgumentException("Order body is required");
        }

        Order managed = findOrder(id);
        if (!managed.isEditable()) {
            throw new OrderOperationException("Order " + id + " can no longer be edited");
        }

        overwriteMutableFields(managed, updated);
        sanitizeItems(managed);
        validateItems(managed.getItems());
        applyFinancials(managed);
        managed.setUpdatedAt(Instant.now());
        return managed;
    }

    public Order cancelOrder(Long id) {
        Order managed = findOrder(id);
        if (!managed.isCancellable()) {
            throw new OrderOperationException("Order " + id + " can no longer be cancelled");
        }
        managed.setStatus(OrderStatus.CANCELLED);
        managed.setCancelledAt(Instant.now());
        managed.setUpdatedAt(Instant.now());
        return managed;
    }

    public Order markOrderPaid(Long id) {
        Order managed = findOrder(id);
        if (managed.getStatus() != OrderStatus.NEW) {
            throw new OrderOperationException("Only NEW orders can be marked as paid");
        }
        managed.setStatus(managed.hasPhysicalItems() ? OrderStatus.PAID : OrderStatus.FULFILLMENT_PENDING);
        managed.setUpdatedAt(Instant.now());
        return managed;
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long id) {
        return findOrder(id);
    }

    @Transactional(readOnly = true)
    public List<Order> listOrders(Optional<OrderStatus> statusFilter) {
        return statusFilter.map(orderRepository::findByStatus)
                .orElseGet(orderRepository::findAll);
    }

    private void validateCustomer(Order order) {
        if (!StringUtils.hasText(order.getCustomerEmail())) {
            throw new IllegalArgumentException("Customer email is required");
        }
        if (!StringUtils.hasText(order.getCustomerName())) {
            throw new IllegalArgumentException("Customer name is required");
        }
        if (!StringUtils.hasText(order.getShippingAddress())) {
            throw new IllegalArgumentException("Shipping address is required");
        }
    }

    private void sanitizeItems(Order order) {
        List<OrderItem> current = order.getItems() == null ? List.of() : new ArrayList<>(order.getItems());
        order.setItems(current);
    }

    private void validateItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("At least one order item is required");
        }
        items.forEach(item -> {
            if (!StringUtils.hasText(item.getProductCode())) {
                throw new IllegalArgumentException("Item product code is required");
            }
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Item quantity must be positive");
            }
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Item unit price must be positive");
            }
            if (item.getFulfillmentType() == null) {
                item.setFulfillmentType(FulfillmentType.DIGITAL);
            }
        });
    }

    private void enforceOpenOrderLimit(String customerEmail) {
        long openOrders = orderRepository.countByCustomerEmailAndStatusIn(customerEmail, OPEN_STATUSES);
        if (openOrders >= 3) {
            throw new OrderOperationException("Customer " + customerEmail + " already has too many open orders");
        }
    }

    private void overwriteMutableFields(Order target, Order source) {
        if (StringUtils.hasText(source.getCustomerName())) {
            target.setCustomerName(source.getCustomerName());
        }
        if (StringUtils.hasText(source.getShippingAddress())) {
            target.setShippingAddress(source.getShippingAddress());
        }
        if (source.getItems() != null && !source.getItems().isEmpty()) {
            target.setItems(source.getItems());
        }
    }

    private void applyFinancials(Order order) {
        BigDecimal merchandiseTotal = order.getItems().stream()
                .map(OrderItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = merchandiseTotal.compareTo(LOYALTY_THRESHOLD) >= 0
                ? merchandiseTotal.multiply(LOYALTY_DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal shipping = calculateShipping(order);
        BigDecimal totalDue = merchandiseTotal.subtract(discount).add(shipping);

        order.setMerchandiseTotal(merchandiseTotal);
        order.setDiscountTotal(discount);
        order.setShippingCost(shipping);
        order.setTotalDue(totalDue);
    }

    private BigDecimal calculateShipping(Order order) {
        long physicalUnits = order.getItems().stream()
                .filter(item -> item.getFulfillmentType() == FulfillmentType.PHYSICAL)
                .mapToLong(OrderItem::getQuantity)
                .sum();
        if (physicalUnits == 0) {
            return BigDecimal.ZERO;
        }
        return SHIPPING_BASE.add(BigDecimal.valueOf(physicalUnits).multiply(SHIPPING_PER_UNIT));
    }

    private Order findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
