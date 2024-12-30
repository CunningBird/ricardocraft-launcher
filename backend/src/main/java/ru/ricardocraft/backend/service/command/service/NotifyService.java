package ru.ricardocraft.backend.service.command.service;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.NotificationResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotifyService {

    private final ServerWebSocketHandler handler;

    public void notify(String head, String message, @Nullable String icon) throws Exception {
        NotificationResponse event;
        if (icon == null) {
            event = new NotificationResponse(head, message);
        } else {
            event = new NotificationResponse(head, message, Enum.valueOf(NotificationResponse.NotificationType.class, icon));
        }
        handler.sendMessageToAll(event);
    }
}
