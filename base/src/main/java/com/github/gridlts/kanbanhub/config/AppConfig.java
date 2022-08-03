package com.github.gridlts.kanbanhub.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.sqlite.SQLiteConfig;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.Properties;
import java.util.TimeZone;

@Configuration
@PropertySource(value="${spring.config.additional-location}/custom.properties")
@Getter
@Setter
@EnableJpaRepositories(basePackages = "com.github.gridlts.kanbanhub.repository")
public class AppConfig {

    @Value("${store.path}")
    private String storeDirectoryPath;


    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Bean
    @Profile("!test")
    DataSource getDataSource() {
        DataSourceBuilder dataSourceBuilder =  DataSourceBuilder.create();
        String url = String.format("jdbc:sqlite:file:%s/kanban_hub.sqlite", this.storeDirectoryPath);
        dataSourceBuilder.url(url);
        HikariDataSource datasource = (HikariDataSource) dataSourceBuilder.build();
        SQLiteConfig sqLiteConfig = new SQLiteConfig();
        Properties properties = sqLiteConfig.toProperties();
        properties.setProperty(SQLiteConfig.Pragma.DATE_CLASS.pragmaName, "text");
        datasource.getDataSourceProperties().putAll(properties);
        return datasource;
    }

}
