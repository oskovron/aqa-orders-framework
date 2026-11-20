package com.example.autotests.model;

import java.math.BigDecimal;

public record CreateOrderRequest(String customerId, BigDecimal total) {
}
