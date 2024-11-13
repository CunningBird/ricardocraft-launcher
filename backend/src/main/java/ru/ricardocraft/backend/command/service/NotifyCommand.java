package ru.ricardocraft.backend.command.service;

import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.events.NotificationEvent;
import ru.ricardocraft.backend.base.request.WebSocketEvent;
import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.socket.WebSocketService;
import ru.ricardocraft.backend.socket.handlers.NettyServerSocketHandler;

@Component
public class NotifyCommand extends Command {

    private transient final NettyServerSocketHandler nettyServerSocketHandler;

    public NotifyCommand(NettyServerSocketHandler nettyServerSocketHandler) {
        super();
        this.nettyServerSocketHandler = nettyServerSocketHandler;
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
        WebSocketService service = nettyServerSocketHandler.nettyServer.service;
        service.sendObjectAll(event, WebSocketEvent.class);
    }
}
