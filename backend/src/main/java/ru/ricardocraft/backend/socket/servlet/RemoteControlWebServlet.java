package ru.ricardocraft.backend.socket.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ricardocraft.backend.command.CommandHandler;
import ru.ricardocraft.backend.manangers.JacksonManager;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.properties.config.RemoteControlTokenProperties;
import ru.ricardocraft.backend.socket.NettyConnectContext;
import ru.ricardocraft.backend.socket.handlers.NettyWebAPIHandler;

import java.util.Map;

public class RemoteControlWebServlet implements NettyWebAPIHandler.SimpleServletHandler {

    private transient final Logger logger = LogManager.getLogger(RemoteControlWebServlet.class);

    private final LaunchServerProperties config;
    private final CommandHandler commandHandler;
    private final JacksonManager jacksonManager;

    public RemoteControlWebServlet(LaunchServerProperties config,
                                   CommandHandler commandHandler,
                                   JacksonManager jacksonManager) {
        this.config = config;
        this.commandHandler = commandHandler;
        this.jacksonManager = jacksonManager;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest msg, NettyConnectContext context) throws JsonProcessingException {
        if (!config.getRemoteControl().getEnabled()) {
            sendHttpResponse(ctx, simpleJsonResponse(HttpResponseStatus.FORBIDDEN, new RemoteControlResponse<Void>("RemoteControl disabled"), jacksonManager));
            return;
        }
        if (msg.method() != HttpMethod.GET && msg.method() != HttpMethod.POST) {
            sendHttpResponse(ctx, simpleJsonResponse(HttpResponseStatus.METHOD_NOT_ALLOWED, new RemoteControlResponse<Void>("You can used only GET and POST requests"), jacksonManager));
            return;
        }
        Map<String, String> params = getParamsFromUri(msg.uri());
        String accessToken = params.get("token");
        if (accessToken == null || accessToken.isEmpty()) {
            sendHttpResponse(ctx, simpleJsonResponse(HttpResponseStatus.BAD_REQUEST, new RemoteControlResponse<Void>("Missing required parameter: token"), jacksonManager));
            return;
        }
        RemoteControlTokenProperties token = find(accessToken);
        if (token == null) {
            sendHttpResponse(ctx, simpleJsonResponse(HttpResponseStatus.FORBIDDEN, new RemoteControlResponse<Void>("Token not valid"), jacksonManager));
            return;
        }
        String command;
        if (token.getAllowAll()) {
            command = params.get("command");
            if (command == null) {
                sendHttpResponse(ctx, simpleJsonResponse(HttpResponseStatus.BAD_REQUEST, new RemoteControlResponse<Void>("Missing required parameter: command"), jacksonManager));
                return;
            }
        } else {
            command = params.get("command");
            if (command == null) {
                if (token.getCommands().size() != 1) {
                    sendHttpResponse(ctx, simpleJsonResponse(HttpResponseStatus.BAD_REQUEST, new RemoteControlResponse<Void>("Missing required parameter: command"), jacksonManager));
                    return;
                }
                command = token.getCommands().getFirst();
            }
            String finalCommand = command;
            if (token.getStartWithMode() ? token.getCommands().stream().noneMatch(finalCommand::startsWith) : !token.getCommands().contains(command)) {
                sendHttpResponse(ctx, simpleJsonResponse(HttpResponseStatus.FORBIDDEN, new RemoteControlResponse<Void>("You cannot execute this command"), jacksonManager));
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
        sendHttpResponse(ctx, simpleJsonResponse(HttpResponseStatus.OK, new RemoteControlResponse<>(response), jacksonManager));
    }

    public RemoteControlTokenProperties find(String token) {
        for (RemoteControlTokenProperties r : config.getRemoteControl().getList()) {
            if (token.equals(r.getToken())) {
                return r;
            }
        }
        return null;
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
