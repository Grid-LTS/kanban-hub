package com.github.gridlts.kanbanhub.gtasks;

import com.github.gridlts.kanbanhub.sources.api.ITaskResourceConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ComponentScan(basePackages = {"com.github.gridlts.kanbanhub.gtasks"})
@RequiredArgsConstructor
public class GTasksConfiguration implements ITaskResourceConfiguration {

    private final GTasksProperties gTasksProperties;

    public Map<String, String> getProperties() {
        Map<String, String> gTasksConfig = new HashMap<>();
        gTasksConfig.put("apiKey", gTasksProperties.getApiKey());
        gTasksConfig.put("scope", gTasksProperties.getScope());
        gTasksConfig.put("clientId", gTasksProperties.getClientId());
        return gTasksConfig;
    }

}
