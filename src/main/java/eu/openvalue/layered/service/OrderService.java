package eu.openvalue.layered.service;

import eu.openvalue.layered.exception.OrderNotFoundException;
import eu.openvalue.layered.exception.OrderOperationException;
import eu.openvalue.layered.model.Order;
import eu.openvalue.layered.model.OrderItem;
import eu.openvalue.layered.model.OrderStatus;
import eu.openvalue.layered.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private static final BigDecimal SHIPPING_BASE = new BigDecimal("5.00");
    private static final BigDecimal SHIPPING_THRESHOLD = new BigDecimal("50.00");
    private final OrderRepository orderRepository;

    public Order placeOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order body is required");
        }
        validateCustomer(order);
        sanitizeItems(order);
        validateItems(order.getItems());
        order.setId(null);
        order.setVersion(null);
        order.setStatus(OrderStatus.NEW);
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
        if (!isEditable(managed)) {
            throw new OrderOperationException("Order " + id + " can no longer be edited");
        }

        overwriteMutableFields(managed, updated);
        sanitizeItems(managed);
        validateItems(managed.getItems());
        applyFinancials(managed);
        return managed;
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
        });
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
        BigDecimal itemsTotal = order.getItems().stream()
                .map(OrderItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shipping = itemsTotal.compareTo(SHIPPING_THRESHOLD) < 0 ? SHIPPING_BASE : BigDecimal.ZERO;
        BigDecimal totalDue = itemsTotal.add(shipping);

        order.setShippingCost(shipping);
        order.setTotalDue(totalDue);
    }

    private Order findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    private boolean isEditable(Order order) {
        return order.getStatus() == OrderStatus.NEW || order.getStatus() == OrderStatus.FULFILLMENT_PENDING;
    }

}
