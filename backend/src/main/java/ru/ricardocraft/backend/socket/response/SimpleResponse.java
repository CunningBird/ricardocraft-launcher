package ru.ricardocraft.backend.socket.response;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.backend.base.events.RequestEvent;
import ru.ricardocraft.backend.base.events.request.ErrorRequestEvent;
import ru.ricardocraft.backend.binary.EXELauncherBinary;
import ru.ricardocraft.backend.binary.JARLauncherBinary;
import ru.ricardocraft.backend.manangers.*;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.properties.LaunchServerRuntimeConfig;
import ru.ricardocraft.backend.socket.WebSocketService;

import java.util.UUID;

public abstract class SimpleResponse implements WebSocketServerResponse {
    public UUID requestUUID;

    public transient LaunchServerRuntimeConfig runtimeConfig;
    public transient LaunchServerConfig config;
    public transient AuthManager authManager;
    public transient AuthHookManager authHookManager;
    public transient UpdatesManager updatesManager;
    public transient KeyAgreementManager keyAgreementManager;
    public transient JARLauncherBinary launcherBinary;
    public transient EXELauncherBinary exeLauncherBinary;
    public transient FeaturesManager featuresManager;
    public transient int shardId;

    public transient WebSocketService service;
    public transient ChannelHandlerContext ctx;
    public transient UUID connectUUID;
    public transient String ip;

    public void sendResult(RequestEvent result) {
        result.requestUUID = requestUUID;
        service.sendObject(ctx.channel(), result);
    }

    public void sendResultAndClose(RequestEvent result) {
        result.requestUUID = requestUUID;
        service.sendObjectAndClose(ctx, result);
    }

    public void sendError(String errorMessage) {
        ErrorRequestEvent event = new ErrorRequestEvent(errorMessage);
        event.requestUUID = requestUUID;
        service.sendObject(ctx.channel(), event);
    }
}
