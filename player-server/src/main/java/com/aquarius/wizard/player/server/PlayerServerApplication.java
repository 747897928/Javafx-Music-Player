package com.aquarius.wizard.player.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Spring Boot entry point for the rebuilt music backend.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class PlayerServerApplication {

    public static void main(final String[] args) {
        SpringApplication.run(PlayerServerApplication.class, args);
    }
}

