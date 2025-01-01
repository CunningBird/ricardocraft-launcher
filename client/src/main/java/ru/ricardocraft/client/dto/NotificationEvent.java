package ru.ricardocraft.client.dto;

import ru.ricardocraft.client.dto.response.WebSocketEvent;

public class NotificationEvent implements WebSocketEvent {

    public final String head;
    public final String message;
    public final NotificationType icon;

    public NotificationEvent(String head, String message) {
        this.head = head;
        this.message = message;
        this.icon = NotificationType.INFO;
    }

    public NotificationEvent(String head, String message, NotificationType icon) {
        this.head = head;
        this.message = message;
        this.icon = icon;
    }

    @Override
    public String getType() {
        return "notification";
    }

    public enum NotificationType {
        INFO, WARN, ERROR, OTHER
    }
}
