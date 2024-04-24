package com.sda.rentacar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class RentCarApp {
    public static void main(String[] args) {
        SpringApplication.run(RentCarApp.class, args);
    }
}
