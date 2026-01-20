package com.bantunes82.ledger.gatling;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import java.time.Duration;
import java.util.UUID;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TransactionSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8081")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Gatling/Performance Test");

    // Seeded accounts
    String debitAccountId = "c3b3b3f0-9b6b-4b1f-8b3f-7b1b3b1f0b3c"; // Bruno
    String creditAccountId = "a7b7b3f0-9b6b-4b1f-8b3f-7b1b3b1f0b3a"; // Jose

    Iterator<Map<String, Object>> feeder = Stream.generate((Supplier<Map<String, Object>>) () ->
         Map.of("idempotencyKey", UUID.randomUUID().toString())
    ).iterator();

    ScenarioBuilder scn = scenario("Transaction Load Test")
            .feed(feeder)
            .exec(http("Create Transaction")
                    .post("/ledger-service/api/v1/transactions")
                    .body(StringBody(
                            "{ \"debitAccountId\": \"" + debitAccountId + "\", " +
                                    "\"creditAccountId\": \"" + creditAccountId + "\", " +
                                    "\"amount\": 1.00, " +
                                    "\"idempotencyKey\": \"#{idempotencyKey}\", " +
                                    "\"description\": \"Load Test\" }"))
                    .asJson()
                    .check(status().is(201))
                    .check(header("Location").saveAs("location")));

    {
        setUp(
                scn.injectOpen(
                        // Warmup
                        rampUsersPerSec(0).to(100).during(Duration.ofSeconds(10)),
                        // Load to peak
                        rampUsersPerSec(100).to(1000).during(Duration.ofSeconds(30)),
                        // Hold peak
                        constantUsersPerSec(1000).during(Duration.ofSeconds(30))))
                .protocols(httpProtocol)
                .assertions(
                        //maximum response time based on the setup should be less than or equal to 10 seconds
                        global().responseTime().max().lte(10000),
                        //percentage of successful requests should be greater than 90
                        global().successfulRequests().percent().gt(90d));
    }
}
