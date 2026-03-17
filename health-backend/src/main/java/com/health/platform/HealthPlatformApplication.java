package com.health.platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.health.platform.mapper")
public class HealthPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(HealthPlatformApplication.class, args);
        System.out.println("Health Platform Backend Started Successfully!");
    }
}
