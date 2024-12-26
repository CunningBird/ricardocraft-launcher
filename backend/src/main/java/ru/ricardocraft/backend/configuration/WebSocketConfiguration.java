//package ru.ricardocraft.backend.configuration;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.socket.WebSocketHandler;
//import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
//import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
//import ru.ricardocraft.backend.socket.ServerWebSocketHandler;
//
//@Configuration
//public class WebSocketConfiguration implements WebSocketConfigurer {
//
//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        registry.addHandler(webSocketHandler(), "/api");
//    }
//
//    @Bean
//    public WebSocketHandler webSocketHandler() {
//        return new ServerWebSocketHandler();
//    }
//}
