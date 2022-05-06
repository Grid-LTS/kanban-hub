package com.github.gridlts.kanbanhub.todotxt;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value="${spring.config.additional-location}/custom.properties")
@Data
public class TodoTxtConfig {

    @Value("${todotxt.done.filename:done.txt}")
    private String doneFileName;

    @Value("${todotxt.pending.filename:todo.txt}")
    private String pendingFileName;

    @Value("${store.path}")
    private String storeDirectoryPath;
}
