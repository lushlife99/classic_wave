package com.chosun.classicwave;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableFeignClients
public class ClassicWaveApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClassicWaveApplication.class, args);
    }

}
