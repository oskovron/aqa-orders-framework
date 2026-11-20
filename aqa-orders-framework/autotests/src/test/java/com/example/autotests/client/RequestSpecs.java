package com.example.autotests.client;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import com.example.autotests.config.TestConfig;
import com.example.autotests.env.TestEnvironment;

/**
 * One shared, read-only base request spec for every client.
 * No RestAssured.baseURI or other static globals: those are mutable state shared by all threads.
 */
public final class RequestSpecs {

    private RequestSpecs() {
    }

    public static RequestSpecification base() {
        return Holder.BASE;
    }

    // Built on first use, so the stack starts only when a test actually calls the API
    private static final class Holder {
        private static final RequestSpecification BASE = build();
    }

    private static RequestSpecification build() {
        TestConfig config = TestConfig.get();

        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(TestEnvironment.sutBaseUri())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON);

        if (config.logAllRequests()) {
            // local: print every request and response
            builder.addFilter(new RequestLoggingFilter())
                    .addFilter(new ResponseLoggingFilter());
        } else {
            // ci: print request + response only when a then() check fails
            builder.setConfig(RestAssuredConfig.config()
                    .logConfig(LogConfig.logConfig().enableLoggingOfRequestAndResponseIfValidationFails()));
        }
        return builder.build();
    }
}