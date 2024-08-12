package com.qworks.workflow.config;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CosmosDBConfig extends AbstractCosmosConfiguration {

    @Value("${spring.data.cosmos.uri}")
    private String cosmosUri;

    @Value("${spring.data.cosmos.key}")
    private String cosmosKey;

    @Value("${spring.data.cosmos.populate-query-metrics}")
    private String queryMetricsEnabled;

    @Value("${spring.data.cosmos.database}")
    private String cosmosDatabase;

    @Bean
    public CosmosClientBuilder cosmosClientBuilder() {
        DirectConnectionConfig directConnectionConfig = DirectConnectionConfig.getDefaultConfig();
        return new CosmosClientBuilder()
                .endpoint(this.cosmosUri)
                .key(this.cosmosKey)
                .directMode(directConnectionConfig);
    }

    @Bean
    public CosmosConfig cosmosConfig() {
        return CosmosConfig.builder()
                .enableQueryMetrics(true)
                .build();
    }

    @Override
    protected String getDatabaseName() {
        return this.cosmosDatabase;
    }
}