package com.github.gridlts.kanbanhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages={"com.github.gridlts.kanbanhub.controller",
        "com.github.gridlts.kanbanhub.config",
        "com.github.gridlts.kanbanhub.service"})
public class KhapiApplication {

    public static void main(String[] args) {
        SpringApplication.run(KhapiApplication.class, args);
    }

}

