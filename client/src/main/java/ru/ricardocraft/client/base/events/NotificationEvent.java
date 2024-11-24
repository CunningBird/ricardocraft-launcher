package ru.ricardocraft.client.base.events;

import ru.ricardocraft.client.base.request.WebSocketEvent;
import ru.ricardocraft.client.core.LauncherNetworkAPI;

public class NotificationEvent implements WebSocketEvent {
    @LauncherNetworkAPI
    public final String head;
    @LauncherNetworkAPI
    public final String message;
    @LauncherNetworkAPI
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
