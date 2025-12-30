package com.bantunes82.ledger.config.debezium;

import io.debezium.config.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class DebeziumConnectorConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${database.dbname}")
    private String databaseDbname;

    @Value("${database.hostname}")
    private String databaseHostname;

    @Value("${database.port}")
    private Integer databasePort;

    @Value("${spring.datasource.username}")
    private String databaseUsername;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    @Value("${debezium.table.include.list}")
    private String tableList;


    @Bean
    public Configuration configuration() {
        return Configuration.create()
                .with("name", "outbox-postgres")
                .with("database.server.name", applicationName)
                .with("database.hostname", databaseHostname)
                .with("database.port", databasePort)
                .with("database.user", databaseUsername)
                .with("database.password", databasePassword)
                .with("database.dbname", databaseDbname)
                .with("connector.class", "io.debezium.connector.postgresql.PostgresConnector")
                .with("skipped.operations", "t,d")
                .with("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore")
                .with("offset.storage.file.filename", "offset.dat")
                .with("offset.flush.interval.ms", 60000)
                .with("schema.history.internal.file.filename", "schistory.dat")
                .with("topic.prefix", "test")
                .with("decimal.handling.mode", "string")
                .with("wal_level", "logical")
                .with("plugin.name", "pgoutput")
                .with("table.include.list", tableList)
                .with("tasks.max", "1")
                .with("tombstones.on.delete", "false")
                .with("route.topic.regex", "")
                .with("snapshot.mode", "initial")
                .build();
    }
}
