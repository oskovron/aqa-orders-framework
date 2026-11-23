package com.example.autotests.interview;

import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderTotalsTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    record Order(String id, String status, BigDecimal total) {
    }

    static final String JSON = """
            [
              {"id": "o1", "status": "NEW",       "total": 10.50},
              {"id": "o2", "status": "PAID",      "total": 20.00},
              {"id": "o3", "status": "NEW",       "total": 4.50},
              {"id": "o4", "status": "CANCELLED", "total": 7.25},
              {"id": "o5", "status": "PAID",      "total": 0.99}
            ]
            """;

    @Test
    void numOfOrdersByStatus() {
        List<Order> orders = parse(JSON);

        Map<String, Long> ordersByStatus = orders.stream()
                .collect(Collectors.groupingBy(Order::status, Collectors.counting()));
        System.out.println("ordersByStatus " + ordersByStatus);

        Map<String, List<String>> idsByStatus = orders.stream().collect(
                Collectors.groupingBy(Order::status, Collectors.mapping(Order::id, Collectors.toList())));
        System.out.println("idsByStatus " + idsByStatus);

        BigDecimal total = new BigDecimal("5.00");
        Map<Boolean, List<Order>> partitionBy = orders.stream()
                .collect(Collectors.partitioningBy(o -> o.total.compareTo(total) > 0));
        System.out.println("partitionBy" + partitionBy);

        Map<String, Optional<Order>> maxOrderByStatus = orders.stream()
                .collect(Collectors.groupingBy(Order::status, Collectors.maxBy(Comparator.comparing(Order::total))));
        System.out.println("maxOrderByStatus " + maxOrderByStatus);

        Map<String, BigDecimal> IdTotalMap = orders.stream()
                .collect(Collectors.toMap(Order::id, Order::total));
        System.out.println("IdTotalMap " + IdTotalMap);

        List<Order> newOrders = orders.stream()
                .filter(order -> order.status().equals("NEW"))
                .collect(Collectors.toList());

        System.out.println("newOrders " + newOrders);

        String writeValueAsString = MAPPER.writeValueAsString(newOrders);
        System.out.println("writeValueAsString " + writeValueAsString);

        String statusTree = MAPPER.readTree(JSON).get(0).get("status").asString();
        System.out.println("readTree "+ statusTree);

    }

    static List<Order> parse(String json) {
        return MAPPER.readValue(json, new TypeReference<>() {
        });
    }

    static Map<String, BigDecimal> totalsByStatus(List<Order> orders) {
        return orders.stream()
                .collect(Collectors.groupingBy(Order::status, Collectors.reducing(BigDecimal.ZERO, Order::total, BigDecimal::add)));
    }

    @Test
    void sumsTotalsPerStatus() {
        Map<String, BigDecimal> result = totalsByStatus(parse(JSON));

        assertThat(result).hasSize(3);
        assertThat(result.get("NEW")).isEqualByComparingTo("15.00");
        assertThat(result.get("PAID")).isEqualByComparingTo("20.99");
        assertThat(result.get("CANCELLED")).isEqualByComparingTo("7.25");
    }
}
