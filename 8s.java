package com.company.badgemate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BadgeMateApplication {

    public static void main(String[] args) {
        SpringApplication.run(BadgeMateApplication.class, args);
    }
}
