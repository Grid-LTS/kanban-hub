package com.github.gridlts.kanbanhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.github.gridlts.kanbanhub"})
public class KhapiApplication {

    public static void main(String[] args) {
        SpringApplication.run(KhapiApplication.class, args);
    }

}

