package com.bantunes82.ledger.gatling;

import io.gatling.app.Gatling;
import io.gatling.core.config.GatlingPropertiesBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TransactionLoadTest {

    @LocalServerPort
    private int port;

    @Test
    void runLoadTest() {
       // System.setProperty("baseUrl", "http://localhost:" + port);

        GatlingPropertiesBuilder props = new GatlingPropertiesBuilder()
                .simulationClass("com.bantunes82.ledger.gatling.TransactionSimulation")
                .resultsDirectory("target/gatling-results")
                .binariesDirectory("target/test-classes");

        int exitCode = Gatling.fromMap(props.build());

        assertThat(exitCode).isZero();
    }
}
