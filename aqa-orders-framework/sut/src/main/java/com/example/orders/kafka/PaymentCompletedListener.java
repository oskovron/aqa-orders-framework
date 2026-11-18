package com.example.orders.kafka;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Consumes payment.completed and sets the order to PAID.
 * Idempotent: the same event twice leaves the order PAID (at-least-once delivery is safe).
 * Unknown orderId or malformed JSON: logged and skipped, so the next valid event still works.
 */
@Component
public class PaymentCompletedListener {

    public static final String TOPIC = "payment.completed";
    private static final String CORRELATION_HEADER = "X-Correlation-Id";
    private static final String MDC_KEY = "correlationId";

    private static final Logger log = LoggerFactory.getLogger(PaymentCompletedListener.class);

    private final JdbcClient jdbc;
    private final ObjectMapper mapper;

    public PaymentCompletedListener(JdbcClient jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @KafkaListener(
            topics = TOPIC,
            groupId = "orders-service",
            properties = "auto.offset.reset=earliest")
    public void onPaymentCompleted(ConsumerRecord<String, String> record) {
        MDC.put(MDC_KEY, correlationIdOf(record));
        try {
            PaymentCompletedEvent event = mapper.readValue(record.value(), PaymentCompletedEvent.class);
            if (event.orderId() == null) {
                log.warn("payment.completed without orderId skipped (partition {}, offset {})",
                        record.partition(), record.offset());
                return;
            }

            int updated = jdbc.sql("UPDATE orders SET status = 'PAID' WHERE id = :id")
                    .param("id", event.orderId())
                    .update();

            if (updated == 0) {
                log.warn("payment.completed for unknown order {} skipped (partition {}, offset {})",
                        event.orderId(), record.partition(), record.offset());
            } else {
                log.info("Order {} set to PAID (partition {}, offset {})",
                        event.orderId(), record.partition(), record.offset());
            }
        } catch (JacksonException e) {
            log.warn("Malformed payment.completed skipped (partition {}, offset {}): {}",
                    record.partition(), record.offset(), e.getOriginalMessage());
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    private static String correlationIdOf(ConsumerRecord<String, String> record) {
        Header header = record.headers().lastHeader(CORRELATION_HEADER);
        return header != null
                ? new String(header.value(), StandardCharsets.UTF_8)
                : UUID.randomUUID().toString();
    }
}
