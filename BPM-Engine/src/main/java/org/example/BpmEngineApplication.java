package org.example;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableProcessApplication
@SpringBootApplication(scanBasePackages = "org.example")
@EnableJpaRepositories(basePackages = "org.example.historial")
@EntityScan(basePackages = "org.example.historial")
public class BpmEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(BpmEngineApplication.class, args);
    }
}

