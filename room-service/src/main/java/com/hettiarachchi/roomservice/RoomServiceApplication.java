package com.hettiarachchi.roomservice;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling
public class RoomServiceApplication {

    public static void main(String[] args) {
        try {
            // Try to load .env file if it exists (for local development)
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
        } catch (Exception e) {
            // If .env file doesn't exist, continue without it (Docker will provide env vars)
            System.out.println("No .env file found, using system environment variables");
        }
        SpringApplication.run(RoomServiceApplication.class, args);
    }

}
