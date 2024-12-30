package ru.ricardocraft.client.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.ricardocraft.client.core.Launcher;
import ru.ricardocraft.client.config.LauncherConfig;

@Configuration
public class SettingsConfiguration {

    @Bean
    public LauncherConfig config() {
        return Launcher.getConfig();
    }
}
