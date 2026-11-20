package com.example.autotests.client;

import static io.restassured.RestAssured.given;

import java.util.UUID;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import com.example.autotests.model.CreateOrderRequest;
import com.example.autotests.model.OrderResponse;

/**
 * Knows the URLs and HTTP verbs of the orders API. Tests never build HTTP calls themselves.
 * Raw methods return Response (for negative tests); typed methods check the success status and return a record.
 */
public final class OrdersClient {

    private static final String ORDERS = "/orders";
    private static final String ORDER_BY_ID = "/orders/{id}";

    private final RequestSpecification spec;

    public OrdersClient() {
        this(RequestSpecs.base());
    }

    public OrdersClient(RequestSpecification spec) {
        this.spec = spec;
    }

    public Response post(Object body) {
        return given().spec(spec).body(body).when().post(ORDERS);
    }

    public Response get(Object id) {
        return given().spec(spec).pathParam("id", id).when().get(ORDER_BY_ID);
    }

    public OrderResponse createOrder(CreateOrderRequest request) {
        return post(request).then().statusCode(201).extract().as(OrderResponse.class);
    }

    public OrderResponse getOrder(UUID id) {
        return get(id).then().statusCode(200).extract().as(OrderResponse.class);
    }
}