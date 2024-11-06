package ru.ricardocraft.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(BackendApplication.class, args);
        LaunchServer server = context.getBean(LaunchServer.class);

        server.run(); // TODO move to spring context
    }
}
