package ru.ricardocraft.client.service;

import ru.ricardocraft.client.base.events.NotificationEvent;
import ru.ricardocraft.client.client.DialogService;
import ru.ricardocraft.client.impl.MessageManager;

public class RuntimeDialogService implements DialogService.DialogServiceNotificationImplementation, DialogService.DialogServiceImplementation {
    private final MessageManager messageManager;

    public RuntimeDialogService(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    @Override
    public void createNotification(NotificationEvent.NotificationType type, String head, String message) {
        messageManager.createNotification(head, message);
    }
}
