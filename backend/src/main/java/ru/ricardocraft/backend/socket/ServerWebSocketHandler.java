package ru.ricardocraft.backend.socket;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.SubProtocolCapable;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.ricardocraft.backend.dto.events.RequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.manangers.JacksonManager;
import ru.ricardocraft.backend.service.AbstractResponseService;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

@Slf4j
@Component
public class ServerWebSocketHandler extends TextWebSocketHandler implements SubProtocolCapable {

    private final Map<WebSocketSession, Client> sessions = new ConcurrentHashMap<>();
    private final Map<Class<? extends SimpleResponse>, AbstractResponseService> services = new HashMap<>();

    private final JacksonManager jacksonManager;

    public void registerService(Class<? extends SimpleResponse> responseClass, AbstractResponseService service) {
        services.put(responseClass, service);
    }

    public Map<WebSocketSession, Client> getSessions() {
        return new ConcurrentHashMap<>(sessions);
    }

    public void updateSessionClient(WebSocketSession session, Client newClient) {
        sessions.put(session, newClient);
    }

    @Autowired
    public ServerWebSocketHandler(JacksonManager jacksonManager) {
        this.jacksonManager = jacksonManager;
    }

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) {
        log.info("Server connection opened");
        sessions.put(session, new Client());
    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus status) {
        log.info("Server connection closed: {}", status);
        sessions.remove(session);
    }

    @Override
    public void handleTextMessage(@NotNull WebSocketSession session, TextMessage message) throws Exception {
        String request = message.getPayload();
        log.info("Server received: {}", request);

        Client client = sessions.get(session);

        SimpleResponse requestDeserialized = jacksonManager.getMapper().readValue(request, SimpleResponse.class);
        RequestEvent requestEvent = services.get(requestDeserialized.getClass()).execute(requestDeserialized, session, client);
        requestEvent.requestUUID = requestDeserialized.requestUUID;

        sendMessage(session, requestEvent, requestEvent.closeChannel);
    }

    @Override
    public void handleTransportError(@NotNull WebSocketSession session, Throwable exception) {
        log.info("Server transport error: {}", exception.getMessage());
    }

    @NotNull
    @Override
    public List<String> getSubProtocols() {
        return Collections.singletonList("subprotocol.demo.websocket");
    }

    public void forEachActiveChannels(BiConsumer<WebSocketSession, Client> callback) {
        for (Map.Entry<WebSocketSession, Client> session : sessions.entrySet()) {
            if (session.getKey().isOpen()) {
                callback.accept(session.getKey(), session.getValue());
            }
        }
    }

    public void sendMessageToAll(@NotNull Object object) throws IOException {
        for (WebSocketSession session : sessions.keySet()) {
            if (session.isOpen()) {
                sendMessage(session, object, false);
            }
        }
    }

    public void sendMessage(WebSocketSession session, Object object, Boolean closeChannel) throws IOException {
        String response = jacksonManager.getMapper().writeValueAsString(object);
        log.info("Server sends: {}", response);
        session.sendMessage(new TextMessage(response));
        if (closeChannel) {
            session.close();
        }
    }
}