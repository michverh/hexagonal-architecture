package eu.openvalue.hexagonalarchitecture.domain;

public record OrderMoneySnapshot(
        Money shippingCost,
        Money totalDue
) {
    public OrderMoneySnapshot {
        if (shippingCost == null || totalDue == null) {
            throw new IllegalArgumentException("Monetary amounts cannot be null");
        }
    }
}
