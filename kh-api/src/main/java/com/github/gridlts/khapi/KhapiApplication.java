package com.github.gridlts.khapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@ComponentScan(basePackages = {"com.github.gridlts.khapi.service","com.github.gridlts.khapi.gtasks"})
@SpringBootApplication
public class KhapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(KhapiApplication.class, args);
	}

}

