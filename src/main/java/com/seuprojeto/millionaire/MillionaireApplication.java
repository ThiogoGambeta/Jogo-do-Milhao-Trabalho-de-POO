package com.seuprojeto.millionaire;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MillionaireApplication {

    public static void main(String[] args) {
        SpringApplication.run(MillionaireApplication.class, args);
    }
}

