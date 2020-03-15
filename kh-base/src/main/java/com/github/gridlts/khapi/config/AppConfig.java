package com.github.gridlts.khapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value="${spring.config.additional-location}/custom.properties")
@Getter
@Setter
public class AppConfig {

    @Value("${store.path}")
    private String storeDirectoryPath;
}
