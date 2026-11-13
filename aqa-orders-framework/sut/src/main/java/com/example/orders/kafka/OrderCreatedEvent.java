package com.example.orders.kafka;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderCreatedEvent(UUID orderId, String status, BigDecimal total) {}
