package ru.ricardocraft.backend.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import ru.ricardocraft.backend.socket.ServerWebSocketHandler;

@Configuration
@RequiredArgsConstructor
public class WebSocketConfiguration implements WebSocketConfigurer {

    private final ServerWebSocketHandler serverWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(serverWebSocketHandler, "/api");
    }
}
