package ru.ricardocraft.backend.dto;

import ru.ricardocraft.backend.dto.response.TypeSerializeInterface;

public class NotificationResponse implements TypeSerializeInterface {
    public final String head;
    public final String message;
    public final NotificationType icon;

    public NotificationResponse(String head, String message) {
        this.head = head;
        this.message = message;
        this.icon = NotificationType.INFO;
    }

    public NotificationResponse(String head, String message, NotificationType icon) {
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
