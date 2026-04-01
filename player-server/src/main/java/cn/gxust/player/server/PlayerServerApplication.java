package cn.gxust.player.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Spring Boot entry point for the rebuilt music backend.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@MapperScan("cn.gxust.player.server.mapper")
public class PlayerServerApplication {

    public static void main(final String[] args) {
        SpringApplication.run(PlayerServerApplication.class, args);
    }
}
