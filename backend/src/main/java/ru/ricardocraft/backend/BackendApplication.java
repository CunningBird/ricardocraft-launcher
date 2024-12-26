package ru.ricardocraft.backend;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.ricardocraft.backend.properties.DirectoriesProperties;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.properties.NettyProperties;

import java.security.Security;

@Slf4j
@EnableScheduling
//@EnableWebSocket
@SpringBootApplication
@EnableConfigurationProperties({LaunchServerProperties.class, NettyProperties.class, DirectoriesProperties.class})
public class BackendApplication {

    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());

        ConfigurableApplicationContext context = SpringApplication.run(BackendApplication.class, args);
        LaunchServer server = context.getBean(LaunchServer.class);

        server.run(); // TODO move to spring context
    }
}
