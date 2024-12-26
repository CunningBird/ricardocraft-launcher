package ru.ricardocraft.client.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.client.dto.NotificationEvent;
import ru.ricardocraft.client.service.RuntimeDialogService;

@Component
public class DialogService {
    private static DialogServiceNotificationImplementation notificationImpl;

    @Autowired
    private DialogService(RuntimeDialogService dialogService) {
        DialogService.setNotificationImpl(dialogService);
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
