package com.example.autotests.env;

import java.io.File;
import java.time.Duration;

import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * Starts docker-compose.yml once per test JVM (singleton) and gives tests the SUT address.
 * No stop() call: Ryuk removes everything when the JVM exits.
 */
public final class TestEnvironment {

    // Compose v2 container name for a service: <service>-<instance>
    private static final String SUT_SERVICE = "sut-1";
    private static final int SUT_PORT = 8080;

    private TestEnvironment() {
    }

    // Holder idiom: the JVM loads Holder (and starts compose) on first use only,
    // and class loading is thread-safe, so parallel tests can't start it twice.
    private static final class Holder {
        private static final ComposeContainer COMPOSE = start();
    }

    // kept open for the whole run on purpose; Ryuk cleans up
    private static ComposeContainer start() {
        File composeFile = new File(System.getProperty("compose.file", "../docker-compose.yml"));
        if (!composeFile.isFile()) {
            throw new IllegalStateException("Compose file not found: " + composeFile.getAbsolutePath());
        }

        // docker:25.0.5 = helper container that runs `docker compose` against your Mac's Docker
        ComposeContainer compose = new ComposeContainer(DockerImageName.parse("docker:25.0.5"), composeFile)
                .withBuild(true)          // rebuild the sut image from the fresh jar
                .withExposedService(SUT_SERVICE, SUT_PORT,
                        Wait.forHttp("/actuator/health")
                                .forStatusCode(200)
                                .withStartupTimeout(Duration.ofMinutes(3)));

        compose.start();
        return compose;
    }

    public static String sutBaseUri() {
        ComposeContainer compose = Holder.COMPOSE;
        return "http://" + compose.getServiceHost(SUT_SERVICE, SUT_PORT)
                + ":" + compose.getServicePort(SUT_SERVICE, SUT_PORT);
    }
}