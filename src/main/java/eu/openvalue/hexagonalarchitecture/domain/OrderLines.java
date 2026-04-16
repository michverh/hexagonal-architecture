package eu.openvalue.hexagonalarchitecture.domain;

import java.util.Collections;
import java.util.List;

public class OrderLines {

    private final List<OrderLine> items;

    private OrderLines(List<OrderLine> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("At least one order line is required");
        }
        this.items = List.copyOf(items);
    }

    public static OrderLines of(List<OrderLine> items) {
        return new OrderLines(items);
    }

    public List<OrderLine> items() {
        return Collections.unmodifiableList(items);
    }

    public Money merchandiseTotal() {
        return items.stream()
                .map(OrderLine::lineTotal)
                .reduce(Money.zero(), Money::add);
    }

}
