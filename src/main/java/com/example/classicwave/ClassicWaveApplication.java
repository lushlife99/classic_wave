package com.example.classicwave;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ClassicWaveApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClassicWaveApplication.class, args);
    }

}
