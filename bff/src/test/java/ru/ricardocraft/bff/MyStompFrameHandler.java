package ru.ricardocraft.bff;

import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class MyStompFrameHandler implements StompFrameHandler {

    private static final Logger logger = LoggerFactory.getLogger(MyStompSessionHandler.class);

    private final CountDownLatch latch;

    private final AtomicReference<Throwable> failure;

    private final StompSession session;

    public MyStompFrameHandler(CountDownLatch latch, AtomicReference<Throwable> failure, StompSession session) {
        this.latch = latch;
        this.failure = failure;
        this.session = session;
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return Map.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        try {
            Assertions.assertThat(payload).isNotNull();
            Assertions.assertThat(payload).isInstanceOf(Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Integer> map = (Map<String, Integer>) payload;

            Assertions.assertThat(map).containsKey("HPE");
            Assertions.assertThat(map.get("HPE")).isInstanceOf(Integer.class);

            logger.info("Handled WebSocket message: {}", map.get("HPE").toString());
        } catch (Throwable t) {
            failure.set(t);
            logger.error("There is an exception ", t);
        } finally {
            session.disconnect();
            latch.countDown();
        }

    }
}
