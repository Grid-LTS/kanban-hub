package com.github.gridlts.kanbanhub.taskw;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value="${spring.config.additional-location}/custom.properties")
public class TaskWarriorConfig {

    @Value("${store.path}")
    private String storeDirectoryPath;

    public String getStoreDirectoryPath() {
        return storeDirectoryPath;
    }

    public void setStoreDirectoryPath(String storeDirectoryPath) {
        this.storeDirectoryPath = storeDirectoryPath;
    }
}
