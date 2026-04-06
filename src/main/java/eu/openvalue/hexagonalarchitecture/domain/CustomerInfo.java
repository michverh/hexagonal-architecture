package eu.openvalue.hexagonalarchitecture.domain;

import org.springframework.util.StringUtils;

public record CustomerInfo(String email, String name, String shippingAddress) {

    public CustomerInfo {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Customer email is required");
        }
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Customer name is required");
        }
        if (!StringUtils.hasText(shippingAddress)) {
            throw new IllegalArgumentException("Shipping address is required");
        }
    }
}
