package com.application.wa9ti;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Wa9TiApplication {

    public static void main(String[] args) {
        SpringApplication.run(Wa9TiApplication.class, args);
    }

}
