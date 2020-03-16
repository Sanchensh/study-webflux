package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class DemoWebFluxApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoWebFluxApplication.class, args);
    }

}
