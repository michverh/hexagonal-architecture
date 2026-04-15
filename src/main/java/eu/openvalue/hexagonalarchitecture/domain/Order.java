package eu.openvalue.hexagonalarchitecture.domain;

public class Order {

    private static final Money SHIPPING_BASE = Money.of(new java.math.BigDecimal("5.00"));
    private static final Money SHIPPING_THRESHOLD = Money.of(new java.math.BigDecimal("50.00"));

    private final OrderId id;
    private CustomerInfo customer;
    private OrderLines lines;
    private OrderMoneySnapshot money;
    private final OrderLifecycle lifecycle;

    private Order(OrderId id, CustomerInfo customer, OrderLines lines, OrderMoneySnapshot money, OrderLifecycle lifecycle) {
        this.id = id;
        this.customer = customer;
        this.lines = lines;
        this.money = money;
        this.lifecycle = lifecycle;
    }

    public static Order newOrder(CustomerInfo customer, OrderLines lines) {
        OrderLifecycle lifecycle = OrderLifecycle.newLifecycle();
        Order order = new Order(new OrderId(null), customer, lines,  zeroedMoney(), lifecycle);
        order.refreshFinancials();
        return order;
    }

    public static Order restore(OrderId id,
                                CustomerInfo customer,
                                OrderLines lines,
                                OrderMoneySnapshot money,
                                OrderLifecycle lifecycle) {
        return new Order(id, customer, lines, money, lifecycle);
    }

    public OrderId id() {
        return id;
    }

    public CustomerInfo customer() {
        return customer;
    }

    public OrderLines lines() {
        return lines;
    }

    public OrderMoneySnapshot money() {
        return money;
    }

    public OrderStatus status() {
        return lifecycle.status();
    }

    public void replaceLines(OrderLines newLines) {
        ensureEditable();

        this.lines = newLines;
        refreshFinancials();
    }

    public void updateCustomerDetails(String newName, String newAddress) {
        ensureEditable();
        String name = newName != null ? newName : customer.name();
        String address = newAddress != null ? newAddress : customer.shippingAddress();
        this.customer = new CustomerInfo(customer.email(), name, address);
    }

    private void refreshFinancials() {
        Money lineTotal = lines.merchandiseTotal();
        Money shipping = lineTotal.isLessThan(SHIPPING_THRESHOLD) ? SHIPPING_BASE : Money.zero();
        Money totalDue = lineTotal.add(shipping);
        this.money = new OrderMoneySnapshot(shipping, totalDue);
    }

    private boolean canEdit() {
        return lifecycle.status() == OrderStatus.NEW || lifecycle.status() == OrderStatus.FULFILLMENT_PENDING;
    }

    private void ensureEditable() {
        if (!canEdit()) {
            throw new IllegalStateException("Order can no longer be edited");
        }
    }

    private static OrderMoneySnapshot zeroedMoney() {
        Money zero = Money.zero();
        return new OrderMoneySnapshot(zero, zero);
    }
}
