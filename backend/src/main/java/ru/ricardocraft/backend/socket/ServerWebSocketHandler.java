//package ru.ricardocraft.backend.socket;
//
//import lombok.extern.slf4j.Slf4j;
//import org.jetbrains.annotations.NotNull;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.SubProtocolCapable;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//import org.springframework.web.util.HtmlUtils;
//import ru.ricardocraft.backend.dto.response.SimpleResponse;
//import ru.ricardocraft.backend.service.AbstractResponseService;
//
//import java.io.IOException;
//import java.time.LocalTime;
//import java.util.*;
//import java.util.concurrent.CopyOnWriteArraySet;
//
//@Slf4j
//@Component
//public class ServerWebSocketHandler extends TextWebSocketHandler implements SubProtocolCapable {
//
//    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
//
//    private final Map<Class<? extends SimpleResponse>, AbstractResponseService> services = new HashMap<>();
//
//    @Override
//    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {
//        log.info("Server connection opened");
//        sessions.add(session);
//
//        TextMessage message = new TextMessage("one-time message from server");
//        log.info("Server sends: {}", message);
//        session.sendMessage(message);
//    }
//
//    @Override
//    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus status) {
//        log.info("Server connection closed: {}", status);
//        sessions.remove(session);
//    }
//
////    @Scheduled(fixedRate = 10000)
////    void sendPeriodicMessages() throws IOException {
////        for (WebSocketSession session : sessions) {
////            if (session.isOpen()) {
////                String broadcast = "server periodic message " + LocalTime.now();
////                log.info("Server sends: {}", broadcast);
////                session.sendMessage(new TextMessage(broadcast));
////            }
////        }
////    }
//
//    @Override
//    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        String request = message.getPayload();
//        log.info("Server received: {}", request);
//
//        String response = String.format("response from server to '%s'", HtmlUtils.htmlEscape(request));
//        log.info("Server sends: {}", response);
//        session.sendMessage(new TextMessage(response));
//    }
//
//    @Override
//    public void handleTransportError(@NotNull WebSocketSession session, Throwable exception) {
//        log.info("Server transport error: {}", exception.getMessage());
//    }
//
//    @NotNull
//    @Override
//    public List<String> getSubProtocols() {
//        return Collections.singletonList("subprotocol.demo.websocket");
//    }
//}