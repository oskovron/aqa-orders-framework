package com.example.autotests.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Properties;

/**
 * Immutable test configuration, loaded once per test JVM.
 * Pick the environment with -Denv=local|ci (default: local).
 */
public record TestConfig(
        String env,
        Duration sutStartupTimeout,
        Duration awaitTimeout,
        Duration pollInterval,
        boolean logAllRequests) {

    /** Compact constructor: reject a broken config before any test runs. */
    public TestConfig {
        if (pollInterval.compareTo(awaitTimeout) >= 0) {
            throw new IllegalStateException(
                    "await.pollInterval (" + pollInterval + ") must be shorter than await.timeout (" + awaitTimeout + ")");
        }
    }

    public static TestConfig get() {
        return Holder.INSTANCE;
    }

    /** Lazy, thread-safe singleton: the JVM initialises this class once, on first use. */
    private static final class Holder {
        private static final TestConfig INSTANCE = load(System.getProperty("env", "local"));
    }

    static TestConfig load(String env) {
        String file = "config-" + env + ".properties";
        Properties p = new Properties();
        try (InputStream in = TestConfig.class.getClassLoader().getResourceAsStream(file)) {
            if (in == null) {
                throw new IllegalStateException("No " + file + " on the test classpath (env=" + env + ")");
            }
            p.load(in);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + file, e);
        }
        return new TestConfig(
                env,
                Duration.parse(required(p, "sut.startupTimeout")),
                Duration.parse(required(p, "await.timeout")),
                Duration.parse(required(p, "await.pollInterval")),
                Boolean.parseBoolean(required(p, "log.allRequests")));
    }

    private static String required(Properties p, String key) {
        String value = p.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing '" + key + "' in config");
        }
        return value.trim();
    }
}