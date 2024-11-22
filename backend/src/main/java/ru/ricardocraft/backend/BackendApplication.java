package ru.ricardocraft.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.ricardocraft.backend.properties.DirectoriesProperties;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.properties.NettyProperties;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties({LaunchServerProperties.class, NettyProperties.class, DirectoriesProperties.class})
public class BackendApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(BackendApplication.class, args);
        LaunchServer server = context.getBean(LaunchServer.class);

        server.run(); // TODO move to spring context
    }
}
