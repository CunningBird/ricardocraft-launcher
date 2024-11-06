package ru.ricardocraft.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Fail.fail;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BackendApplicationTests {

    private static final Logger logger = LoggerFactory.getLogger(BackendApplicationTests.class);

    WebSocketClient client;

    WebSocketStompClient stompClient;

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setup() {
        logger.info("Setting up the tests ...");

        client = new StandardWebSocketClient();
        stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    void givenWebSocket_whenMessage_thenVerifyMessage() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Throwable> failure = new AtomicReference<>();

        StompSessionHandler sessionHandler = new MyStompSessionHandler(latch, failure);

        stompClient.connectAsync("ws://localhost:{port}/stock-ticks/websocket", sessionHandler, this.port);

        if (latch.await(20, TimeUnit.SECONDS)) {
            if (failure.get() != null) {
                fail("Assertion Failed", failure.get());
            }
        } else {
            fail("Could not receive the message on time");
        }
    }
}