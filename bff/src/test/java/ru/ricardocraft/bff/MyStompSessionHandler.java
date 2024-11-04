package ru.ricardocraft.bff;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import ru.ricardocraft.bff.dto.ChatMessage;

import java.lang.reflect.Type;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class MyStompSessionHandler implements StompSessionHandler {

    private static final Logger logger = LoggerFactory.getLogger(MyStompSessionHandler.class);

    private final CountDownLatch latch;

    private final AtomicReference<Throwable> failure;

    public MyStompSessionHandler(CountDownLatch latch, AtomicReference<Throwable> failure) {
        this.latch = latch;
        this.failure = failure;
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        System.out.println("payloadType");
        return null;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        System.out.println("handleFrame");
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        logger.info("Connected to the WebSocket ...");

        ChatMessage message = new ChatMessage();
        message.setId(UUID.randomUUID().toString());
        message.setContent("Some flex chat message");
        session.send("/app/ticks", message);

        session.subscribe("/topic/ticks", new MyStompFrameHandler(latch, failure, session));
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {

    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {

    }
}
