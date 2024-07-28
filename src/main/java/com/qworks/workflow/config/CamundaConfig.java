package com.qworks.workflow.config;

import org.camunda.community.rest.client.api.DeploymentApi;
import org.camunda.community.rest.client.api.ProcessDefinitionApi;
import org.camunda.community.rest.client.invoker.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamundaConfig {

    @Value("${camunda.bpm.client.base-url}")
    private String camundaBaseUrl;

    @Bean
    public ApiClient camundaApiClient() {
        ApiClient client = new ApiClient();
        client.setBasePath(camundaBaseUrl);
        return client;
    }

    @Bean
    public DeploymentApi deploymentApi(ApiClient camundaApiClient) {
        return new DeploymentApi(camundaApiClient);
    }

    @Bean
    public ProcessDefinitionApi processDefinitionApi(ApiClient camundaApiClient) {
        return new ProcessDefinitionApi(camundaApiClient);
    }

}
