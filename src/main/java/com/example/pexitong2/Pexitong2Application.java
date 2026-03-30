package com.example.pexitong2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class Pexitong2Application {

    public static void main(String[] args) {
        SpringApplication.run(Pexitong2Application.class, args);
    }

}
