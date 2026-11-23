package com.example.autotests.data;

import com.example.autotests.model.CreateOrderRequest;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class OrderRequestBuilder {

    private String customerId = "customer-" + UUID.randomUUID();
    private BigDecimal total = randomTotal();

    private OrderRequestBuilder() {
    }

    public static OrderRequestBuilder anOrder() {
        return new OrderRequestBuilder();
    }

    public OrderRequestBuilder withCustomerId(String customerId) {
        this.customerId = customerId;
        return this;
    }

    public OrderRequestBuilder withTotal(BigDecimal total) {
        this.total = total;
        return this;
    }

    public CreateOrderRequest build() {
        return new CreateOrderRequest(customerId, total);
    }

    /**
     * 1.00 .. 999.99 with 2 decimals: unscaled long 100..99_999 with scale 2.
     */
    private static BigDecimal randomTotal() {
        long cents = ThreadLocalRandom.current().nextLong(100, 100_000);
        return BigDecimal.valueOf(cents, 2);
    }
}
