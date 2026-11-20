package com.example.autotests.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class TestConfigTest {

    @Test
    void ciConfigIsLoadedFromItsFile() {
        TestConfig ci = TestConfig.load("ci");

        assertThat(ci.env()).isEqualTo("ci");
        assertThat(ci.sutStartupTimeout()).isEqualTo(Duration.ofMinutes(5));
        assertThat(ci.awaitTimeout()).isEqualTo(Duration.ofSeconds(20));
        assertThat(ci.pollInterval()).isEqualTo(Duration.ofMillis(500));
        assertThat(ci.logAllRequests()).isFalse();
    }

    @Test
    void unknownEnvFailsFast() {
        assertThatThrownBy(() -> TestConfig.load("nope"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("config-nope.properties");
    }

    @Test
    void pollIntervalLongerThanTimeoutIsRejected() {
        assertThatThrownBy(() -> new TestConfig("x", Duration.ofMinutes(1),
                Duration.ofSeconds(1), Duration.ofSeconds(2), true))
                .isInstanceOf(IllegalStateException.class);
    }
}