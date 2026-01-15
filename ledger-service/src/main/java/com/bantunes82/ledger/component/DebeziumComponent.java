package com.bantunes82.ledger.component;

import com.bantunes82.ledger.service.TransactionOutboxService;
import io.debezium.config.Configuration;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Component
public class DebeziumComponent {

    private final Executor executor;
    private final DebeziumEngine<ChangeEvent<String, String>> debeziumEngine;
    private final TransactionOutboxService outboxService;
    private final JsonMapper jsonMapper;
    
    private final Logger logger = LoggerFactory.getLogger(DebeziumComponent.class);

    @PostConstruct
    private void start() {
        this.executor.execute(debeziumEngine);
    }

    @PreDestroy
    private void stop() throws IOException {
        if (this.debeziumEngine != null) {
            this.debeziumEngine.close();
        }
    }

    public DebeziumComponent(Configuration postgresConnector, TransactionOutboxService outboxService, JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
        this.executor = Executors.newVirtualThreadPerTaskExecutor();

        this.debeziumEngine = DebeziumEngine.create(Json.class)
                .using(postgresConnector.asProperties())
                .notifying(this::handleEvent)
                .build();
        this.outboxService = outboxService;
    }

    private void handleEvent(ChangeEvent<String, String> event) {
        logger.info("Received event: {}", event);

        JsonNode eventNode = jsonMapper.readTree(event.value());

        JsonNode afterNode = eventNode.path("payload").path("after");

        if (!afterNode.isMissingNode()) {
            Map<String, Object> payload = jsonMapper.convertValue(afterNode, new TypeReference<>() {});

            outboxService.processDebeziumEvent(payload);
        }
    }

}