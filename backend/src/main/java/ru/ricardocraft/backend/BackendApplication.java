package ru.ricardocraft.backend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import ru.ricardocraft.backend.properties.DirectoriesProperties;
import ru.ricardocraft.backend.properties.HttpServerProperties;
import ru.ricardocraft.backend.properties.LaunchServerProperties;

@Slf4j
@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties({LaunchServerProperties.class, HttpServerProperties.class, DirectoriesProperties.class})
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
