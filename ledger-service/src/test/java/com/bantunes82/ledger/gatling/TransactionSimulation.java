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

    static Iterator<Map<String, Object>> accountFeeder;

    static {
        try {
            java.util.List<String> lines = java.nio.file.Files
                    .readAllLines(java.nio.file.Paths.get(System.getProperty("user.dir"),
                            "src", "test", "resources", "accounts.csv"));

            java.util.List<String> accountIds = new java.util.ArrayList<>();
            // Skip header if present (account_id)
            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("account_id")) {
                    accountIds.add(line);
                }
            }

            if (accountIds.isEmpty()) {
                throw new RuntimeException("No accounts found in accounts.csv");
            }

            accountFeeder = Stream.generate(() -> {
                String debit = accountIds
                        .get(java.util.concurrent.ThreadLocalRandom.current().nextInt(accountIds.size()));
                String credit = accountIds
                        .get(java.util.concurrent.ThreadLocalRandom.current().nextInt(accountIds.size()));
                // Ensure they are different
                while (debit.equals(credit) && accountIds.size() > 1) {
                    credit = accountIds
                            .get(java.util.concurrent.ThreadLocalRandom.current().nextInt(accountIds.size()));
                }
                return Map.<String, Object>of("debitAccountId", debit, "creditAccountId", credit);
            }).iterator();

        } catch (Exception e) {
            throw new RuntimeException("Failed to read accounts.csv", e);
        }
    }

    HttpProtocolBuilder httpProtocol = http.baseUrl("http://localhost:8081")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Gatling/Performance Test");

    ScenarioBuilder scn = scenario("Transaction Load Test")
            .feed(accountFeeder)
            .feed(Stream.generate(
                    (Supplier<Map<String, Object>>) () -> Map.of("idempotencyKey", UUID.randomUUID().toString()))
                    .iterator())
            .exec(http("Create Transaction")
                    .post("/ledger-service/api/v1/transactions")
                    .body(StringBody("{ \"debitAccountId\": \"#{debitAccountId}\", " +
                            "\"creditAccountId\": \"#{creditAccountId}\", " +
                            "\"amount\": 1.00, " +
                            "\"idempotencyKey\": \"#{idempotencyKey}\", " +
                            "\"description\": \"Load Test\" }"))
                    .asJson()
                    .check(status().is(201))
                    .check(header("Location").saveAs("location")));

    {
        setUp(scn.injectOpen(
                // Warmup
                rampUsersPerSec(0).to(100).during(Duration.ofSeconds(10)),
                // Load to peak
                rampUsersPerSec(100).to(1000).during(Duration.ofSeconds(30)),
                // Hold peak
                constantUsersPerSec(1000).during(Duration.ofSeconds(60)))).protocols(httpProtocol).assertions(
                        // maximum response time based on the setup should be less than or equal
                        // to 10 seconds
                        // global().responseTime().max().lte(10000),
                        // percentage of successful requests should be greater than 90
                        global().successfulRequests().percent().gt(90d));
    }
}
