package eu.openvalue.hexagonalarchitecture.domain;

public record CustomerInfo(String email, String name, String shippingAddress) {

    public CustomerInfo {
        if (null == email || email.isEmpty()) {
            throw new IllegalArgumentException("Customer email is required");
        }
        if (null == name || name.isEmpty()) {
            throw new IllegalArgumentException("Customer name is required");
        }
        if (null == shippingAddress || shippingAddress.isEmpty()) {
            throw new IllegalArgumentException("Shipping address is required");
        }
    }
}
