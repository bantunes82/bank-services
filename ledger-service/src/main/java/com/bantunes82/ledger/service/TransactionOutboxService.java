package com.bantunes82.ledger.service;

import com.bantunes82.ledger.enums.EventType;
import com.bantunes82.ledger.component.KafkaPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;
import java.util.Map;


@Service
public class TransactionOutboxService {

    private final KafkaPublisher kafkaPublisher;
    private final JsonMapper jsonMapper;

    @Value("${kafka.ledger.topic}")
    private String topicName;

    private final Logger logger = LoggerFactory.getLogger(TransactionOutboxService.class);

    public TransactionOutboxService(KafkaPublisher kafkaPublisher,JsonMapper jsonMapper) {
        this.kafkaPublisher = kafkaPublisher;
        this.jsonMapper = jsonMapper;
    }

    public void processDebeziumEvent(Map<String, Object> eventPayload) {
        logger.info("Received Debezium event payload: {}", eventPayload);

        if (EventType.ACCOUNT_TRANSACTION_CREATED.name().equals(eventPayload.get("event_type"))) {
            String payloadAsString = jsonMapper.writeValueAsString(eventPayload);
            kafkaPublisher.publish(topicName, payloadAsString);

            logger.info("Serialized Debezium event payload: {}", payloadAsString);
        }
    }
}
