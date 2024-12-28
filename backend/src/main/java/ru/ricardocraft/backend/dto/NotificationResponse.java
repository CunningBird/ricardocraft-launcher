package ru.ricardocraft.backend.dto;

import ru.ricardocraft.backend.base.core.LauncherNetworkAPI;
import ru.ricardocraft.backend.base.request.TypeSerializeInterface;

public class NotificationResponse implements TypeSerializeInterface {
    @LauncherNetworkAPI
    public final String head;
    @LauncherNetworkAPI
    public final String message;
    @LauncherNetworkAPI
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
