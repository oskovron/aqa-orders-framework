package com.example.autotests.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Body returned by POST /orders (201) and GET /orders/{id} (200). */
public record OrderResponse(
        UUID id,
        String customerId,
        BigDecimal total,
        OrderStatus status,
        OffsetDateTime createdAt) {
}

/**
 * What they do: Jackson 3 maps JSON to records using the record’s own constructor, so you don’t need setters or annotations.
 * The models are deliberately copies of the SUT’s records, not imports.
 * If the SUT renames a field, the tests should fail. If they shared the SUT’s class, they would quietly change along with it.
 */
