package com.example.orders.kafka;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Payload of a payment.completed event.
 * Unknown JSON fields are ignored (Jackson 3 default: FAIL_ON_UNKNOWN_PROPERTIES = false).
 */
public record PaymentCompletedEvent(UUID orderId, BigDecimal amount) {
}
