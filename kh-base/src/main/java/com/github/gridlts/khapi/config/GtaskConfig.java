package com.github.gridlts.khapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value="${spring.config.additional-location}/custom.properties")
@ConfigurationProperties(prefix = "gtasks")
public class GtaskConfig {

    private String apiKey;
    private String clientId;
    private String clientKey;
    private String Scope;


}
