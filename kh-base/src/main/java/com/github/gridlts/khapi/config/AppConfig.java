package com.github.gridlts.khapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@Configuration
@PropertySource(value="${spring.config.additional-location}/custom.properties")
@Getter
@Setter
@EnableJpaRepositories(basePackages = "com.github.gridlts.khapi.repository")
public class AppConfig {

    @Value("${store.path}")
    private String storeDirectoryPath;


    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

}
