package com.amarakeerthi.userservice;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserServiceApplication {

    public static void main(String[] args) {
        try {
            // Try to load .env file if it exists (for local development)
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
        } catch (Exception e) {
            // If .env file doesn't exist, continue without it (Docker will provide env vars)
            System.out.println("No .env file found, using system environment variables");
        }
        SpringApplication.run(UserServiceApplication.class, args);
    }

}
