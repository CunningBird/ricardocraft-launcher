package ru.ricardocraft.backend.command.service;

import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.dto.events.NotificationEvent;
import ru.ricardocraft.backend.ServerWebSocketHandler;

@Component
public class NotifyCommand extends Command {

    private transient final ServerWebSocketHandler handler;

    public NotifyCommand(ServerWebSocketHandler handler) {
        super();
        this.handler = handler;
    }

    @Override
    public String getArgsDescription() {
        return "[head] [message] (icon)";
    }

    @Override
    public String getUsageDescription() {
        return "send notification to all connected client";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 2);
        NotificationEvent event;
        if (args.length < 3) {
            event = new NotificationEvent(args[0], args[1]);
        } else {
            event = new NotificationEvent(args[0], args[1], Enum.valueOf(NotificationEvent.NotificationType.class, args[2]));
        }
        handler.sendMessageToAll(event);
    }
}
