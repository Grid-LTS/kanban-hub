package com.github.gridlts.khapi.gtasks;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="gtasks")
public class GTasksProperties {

    private String clientId;
    private String apiKey;
    private String scope;

    public String getClientId() {
        return clientId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
