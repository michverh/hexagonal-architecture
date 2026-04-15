package eu.openvalue.hexagonalarchitecture.domain;

import java.util.Objects;

public record OrderId(Long value) {

    public OrderId {
        if (value != null && value < 0) {
            throw new IllegalArgumentException("Order id must be positive");
        }
    }

    public boolean isAssigned() {
        return value != null;
    }

    public static OrderId of(Long value) {
        return new OrderId(value);
    }

    public OrderId assign(Long newValue) {
        Objects.requireNonNull(newValue, "Order id is required");
        return new OrderId(newValue);
    }
}
