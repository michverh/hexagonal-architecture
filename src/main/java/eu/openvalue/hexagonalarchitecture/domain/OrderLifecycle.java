package eu.openvalue.hexagonalarchitecture.domain;

public class OrderLifecycle {

    private OrderStatus status;

    private OrderLifecycle(OrderStatus status) {
        this.status = status;
    }

    public static OrderLifecycle newLifecycle() {
        return new OrderLifecycle(OrderStatus.NEW);
    }

    public static OrderLifecycle restore(OrderStatus status) {
        return new OrderLifecycle(status);
    }

    public OrderStatus status() {
        return status;
    }
}
