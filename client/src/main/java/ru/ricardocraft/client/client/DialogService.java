package ru.ricardocraft.client.client;

import ru.ricardocraft.client.base.events.NotificationEvent;

public class DialogService {
    private static DialogServiceNotificationImplementation notificationImpl;

    private DialogService() {
        throw new UnsupportedOperationException();
    }

    public static void setNotificationImpl(DialogServiceNotificationImplementation impl) {
        DialogService.notificationImpl = impl;
    }

    public static boolean isNotificationsAvailable() {
        return notificationImpl != null;
    }

    public static void createNotification(NotificationEvent.NotificationType type, String head, String message) {
        if (!isNotificationsAvailable()) {
            throw new UnsupportedOperationException("DialogService notifications implementation not available");
        }
        notificationImpl.createNotification(type, head, message);
    }

    public interface DialogServiceImplementation {

    }

    public interface DialogServiceNotificationImplementation {
        void createNotification(NotificationEvent.NotificationType type, String head, String message);
    }
}
