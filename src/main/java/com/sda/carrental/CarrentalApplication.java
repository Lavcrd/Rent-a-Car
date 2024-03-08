package com.sda.carrental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class CarrentalApplication {
    public static void main(String[] args) {
        SpringApplication.run(CarrentalApplication.class, args);
    }
}
