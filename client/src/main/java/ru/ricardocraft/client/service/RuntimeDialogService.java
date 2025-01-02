package ru.ricardocraft.client.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.client.dto.NotificationEvent;
import ru.ricardocraft.client.service.client.DialogService;

@Component
public class RuntimeDialogService implements DialogService.DialogServiceNotificationImplementation, DialogService.DialogServiceImplementation {

    private final LaunchService launchService;

    @Autowired
    public RuntimeDialogService(LaunchService launchService) {
        this.launchService = launchService;
    }

    @Override
    public void createNotification(NotificationEvent.NotificationType type, String head, String message) {
        launchService.createNotification(head, message);
    }
}
