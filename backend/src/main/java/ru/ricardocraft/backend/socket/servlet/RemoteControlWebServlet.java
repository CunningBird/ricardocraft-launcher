package ru.ricardocraft.backend.socket.servlet;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ricardocraft.backend.command.CommandHandler;
import ru.ricardocraft.backend.manangers.GsonManager;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.socket.NettyConnectContext;
import ru.ricardocraft.backend.socket.handlers.NettyWebAPIHandler;

import java.util.Map;

public class RemoteControlWebServlet implements NettyWebAPIHandler.SimpleServletHandler {

    private transient final Logger logger = LogManager.getLogger(RemoteControlWebServlet.class);

    private final LaunchServerConfig config;
    private final CommandHandler commandHandler;
    private final GsonManager gsonManager;

    public RemoteControlWebServlet(LaunchServerConfig config,
                                   CommandHandler commandHandler,
                                   GsonManager gsonManager) {
        this.config = config;
        this.commandHandler = commandHandler;
        this.gsonManager = gsonManager;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest msg, NettyConnectContext context) {
        if (!config.remoteControlConfig.enabled) {
            sendHttpResponse(ctx, simpleJsonResponse(HttpResponseStatus.FORBIDDEN, new RemoteControlResponse<Void>("RemoteControl disabled"), gsonManager));
            return;
        }
        if (msg.method() != HttpMethod.GET && msg.method() != HttpMethod.POST) {
            sendHttpResponse(ctx, simpleJsonResponse(HttpResponseStatus.METHOD_NOT_ALLOWED, new RemoteControlResponse<Void>("You can used only GET and POST requests"), gsonManager));
            return;
        }
        Map<String, String> params = getParamsFromUri(msg.uri());
        String accessToken = params.get("token");
        if (accessToken == null || accessToken.isEmpty()) {
            sendHttpResponse(ctx, simpleJsonResponse(HttpResponseStatus.BAD_REQUEST, new RemoteControlResponse<Void>("Missing required parameter: token"), gsonManager));
            return;
        }
        LaunchServerConfig.RemoteControlConfig.RemoteControlToken token = config.remoteControlConfig.find(accessToken);
        if (token == null) {
            sendHttpResponse(ctx, simpleJsonResponse(HttpResponseStatus.FORBIDDEN, new RemoteControlResponse<Void>("Token not valid"), gsonManager));
            return;
        }
        String command;
        if (token.allowAll) {
            command = params.get("command");
            if (command == null) {
                sendHttpResponse(ctx, simpleJsonResponse(HttpResponseStatus.BAD_REQUEST, new RemoteControlResponse<Void>("Missing required parameter: command"), gsonManager));
                return;
            }
        } else {
            command = params.get("command");
            if (command == null) {
                if (token.commands.size() != 1) {
                    sendHttpResponse(ctx, simpleJsonResponse(HttpResponseStatus.BAD_REQUEST, new RemoteControlResponse<Void>("Missing required parameter: command"), gsonManager));
                    return;
                }
                command = token.commands.getFirst();
            }
            String finalCommand = command;
            if (token.startWithMode ? token.commands.stream().noneMatch(finalCommand::startsWith) : !token.commands.contains(command)) {
                sendHttpResponse(ctx, simpleJsonResponse(HttpResponseStatus.FORBIDDEN, new RemoteControlResponse<Void>("You cannot execute this command"), gsonManager));
                return;
            }
        }
        logger.info("[RemoteControl][Web] IP {} execute command '{}' with token {}...", context.ip, command, accessToken.substring(0, 5));
        String exception = null;
        try {
            commandHandler.evalNative(command, false);
        } catch (Throwable e) {
            logger.error(e);
            exception = e.toString();
        }
        SuccessCommandResponse response = new SuccessCommandResponse();
        response.exception = exception;
        response.success = exception == null;
        sendHttpResponse(ctx, simpleJsonResponse(HttpResponseStatus.OK, new RemoteControlResponse<>(response), gsonManager));
    }

    public static class RemoteControlResponse<T> {
        public String error;
        public T data;

        public RemoteControlResponse(String error) {
            this.error = error;
        }

        public RemoteControlResponse(T data) {
            this.data = data;
        }
    }

    public static class SuccessCommandResponse {
        public String exception;
        public boolean success;
    }
}
