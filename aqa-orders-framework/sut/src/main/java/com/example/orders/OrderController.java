package com.example.orders;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.*;

record CreateOrderRequest(@NotBlank String customerId, @NotNull @Positive BigDecimal total) {}

record Order(UUID id, String customerId, BigDecimal total, String status, OffsetDateTime createdAt) {}

@RestController
@RequestMapping("/orders")
class OrderController {
	private final JdbcClient jdbc;

	OrderController(JdbcClient jdbc) { this.jdbc = jdbc; }

	@PostMapping
	ResponseEntity<Order> create(@Valid @RequestBody CreateOrderRequest req) {
		Order order = jdbc.sql("""
                INSERT INTO orders (id, customer_id, total, status)
                VALUES (:id, :customerId, :total, 'NEW')
                RETURNING id, customer_id, total, status, created_at""")
				.param("id", UUID.randomUUID())
				.param("customerId", req.customerId())
				.param("total", req.total())
				.query(Order.class).single();
		return ResponseEntity.created(URI.create("/orders/" + order.id())).body(order);
	}

	@GetMapping("/{id}")
	ResponseEntity<Order> get(@PathVariable UUID id) {
		return jdbc.sql("SELECT id, customer_id, total, status, created_at FROM orders WHERE id = :id")
				.param("id", id)
				.query(Order.class).optional()
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
}