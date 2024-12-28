package ru.ricardocraft.backend.command.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.ricardocraft.backend.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.events.NotificationEvent;

@Slf4j
@ShellComponent
@ShellCommandGroup("service")
@RequiredArgsConstructor
public class NotifyCommand {

    private final ServerWebSocketHandler handler;

    @ShellMethod("[head] [message] (icon) send notification to all connected client")
    public void notify(@ShellOption String head,
                       @ShellOption String message,
                       @ShellOption(defaultValue = ShellOption.NULL) String icon) throws Exception {
        NotificationEvent event;
        if (icon == null) {
            event = new NotificationEvent(head, message);
        } else {
            event = new NotificationEvent(head, message, Enum.valueOf(NotificationEvent.NotificationType.class, icon));
        }
        handler.sendMessageToAll(event);
    }
}
