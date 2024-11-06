//package ru.ricardocraft.bff.controller;
//
//import lombok.RequiredArgsConstructor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Controller;
//import ru.ricardocraft.bff.dto.ChatMessage;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.ThreadLocalRandom;
//
//@Controller
//@RequiredArgsConstructor
//public class StockTicksController {
//
//    private static final Logger logger = LoggerFactory.getLogger(StockTicksController.class);
//
//    private final SimpMessagingTemplate simpMessagingTemplate;
//
//    @Scheduled(fixedRate = 3000)
//    public void sendTicks() {
//        simpMessagingTemplate.convertAndSend("/topic/ticks", getStockTicks());
//    }
//
//    @MessageMapping("/ticks")
//    public void processMessage(@Payload ChatMessage chatMessage) {
//        logger.info("Server recieved message: {}", chatMessage);
//        System.out.println(chatMessage.getChatId() + "-" + chatMessage.getContent());
//    }
//
//    private Map<String, Integer> getStockTicks() {
//        Map<String, Integer> ticks = new HashMap<>();
//        ticks.put("AAPL", getRandomTick());
//        ticks.put("GOOGL", getRandomTick());
//        ticks.put("MSFT", getRandomTick());
//        ticks.put("TSLA", getRandomTick());
//        ticks.put("AMZN", getRandomTick());
//        ticks.put("HPE", getRandomTick());
//
//        return ticks;
//    }
//
//    private int getRandomTick() {
//        return ThreadLocalRandom.current().nextInt(-100, 100 + 1);
//    }
//}