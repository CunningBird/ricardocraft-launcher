package ru.ricardocraft.backend.socket;

import com.google.gson.Gson;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthProviders;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.auth.protect.AdvancedProtectHandler;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.base.Launcher;
import ru.ricardocraft.backend.base.events.RequestEvent;
import ru.ricardocraft.backend.base.events.request.ErrorRequestEvent;
import ru.ricardocraft.backend.base.events.request.ExitRequestEvent;
import ru.ricardocraft.backend.base.events.request.LauncherRequestEvent;
import ru.ricardocraft.backend.base.request.WebSocketEvent;
import ru.ricardocraft.backend.binary.EXELauncherBinary;
import ru.ricardocraft.backend.binary.JARLauncherBinary;
import ru.ricardocraft.backend.core.managers.GsonManager;
import ru.ricardocraft.backend.helper.IOHelper;
import ru.ricardocraft.backend.manangers.*;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.socket.handlers.WebSocketFrameHandler;
import ru.ricardocraft.backend.socket.response.SimpleResponse;
import ru.ricardocraft.backend.socket.response.WebSocketServerResponse;
import ru.ricardocraft.backend.socket.response.auth.*;
import ru.ricardocraft.backend.socket.response.cabinet.AssetUploadInfoResponse;
import ru.ricardocraft.backend.socket.response.cabinet.GetAssetUploadInfoResponse;
import ru.ricardocraft.backend.socket.response.management.FeaturesResponse;
import ru.ricardocraft.backend.socket.response.management.GetConnectUUIDResponse;
import ru.ricardocraft.backend.socket.response.management.GetPublicKeyResponse;
import ru.ricardocraft.backend.socket.response.profile.BatchProfileByUsername;
import ru.ricardocraft.backend.socket.response.profile.ProfileByUUIDResponse;
import ru.ricardocraft.backend.socket.response.profile.ProfileByUsername;
import ru.ricardocraft.backend.socket.response.secure.GetSecureLevelInfoResponse;
import ru.ricardocraft.backend.socket.response.secure.HardwareReportResponse;
import ru.ricardocraft.backend.socket.response.secure.SecurityReportResponse;
import ru.ricardocraft.backend.socket.response.secure.VerifySecureLevelKeyResponse;
import ru.ricardocraft.backend.socket.response.update.LauncherResponse;
import ru.ricardocraft.backend.socket.response.update.UpdateResponse;
import ru.ricardocraft.backend.utils.BiHookSet;
import ru.ricardocraft.backend.utils.HookSet;
import ru.ricardocraft.backend.utils.ProviderMap;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

@Component
public class WebSocketService {
    public static final ProviderMap<WebSocketServerResponse> providers = new ProviderMap<>();
    public static final Map<String, RestoreResponse.ExtendedTokenProvider> restoreProviders = new HashMap<>();
    private static boolean registeredProviders = false;

    public final ChannelGroup channels;
    public final HookSet<WebSocketRequestContext> hookBeforeParsing = new HookSet<>();
    public final HookSet<WebSocketRequestContext> hookBeforeExecute = new HookSet<>();
    public final HookSet<WebSocketRequestContext> hookComplete = new HookSet<>();
    public final BiHookSet<Channel, Object> hookSend = new BiHookSet<>();

    public transient final LaunchServerConfig config;
    public transient final AuthProviders authProviders;
    public transient final AuthManager authManager;

    public transient final AuthHookManager authHookManager;
    public transient final UpdatesManager updatesManager;
    public transient final KeyAgreementManager keyAgreementManager;
    public transient final JARLauncherBinary launcherBinary;

    public transient final EXELauncherBinary exeLauncherBinary;
    public transient final FeaturesManager featuresManager;
    public transient final ProtectHandler protectHandler;
    public transient final ProfileProvider profileProvider;

    private final Gson gson;
    private transient final Logger logger = LogManager.getLogger();

    private final ExecutorService executors;

    @Autowired
    public WebSocketService(LaunchServerConfig config,

                            AuthProviders authProviders,
                            AuthManager authManager,

                            AuthHookManager authHookManager,
                            UpdatesManager updatesManager,
                            KeyAgreementManager keyAgreementManager,
                            JARLauncherBinary launcherBinary,

                            EXELauncherBinary exeLauncherBinary,
                            FeaturesManager featuresManager,
                            ProtectHandler protectHandler,
                            ProfileProvider profileProvider,

                            GsonManager gsonManager) {
        this.channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

        this.config = config;

        this.authProviders = authProviders;
        this.authManager = authManager;

        this.authHookManager = authHookManager;
        this.updatesManager = updatesManager;
        this.keyAgreementManager = keyAgreementManager;
        this.launcherBinary = launcherBinary;

        this.exeLauncherBinary = exeLauncherBinary;
        this.featuresManager = featuresManager;
        this.protectHandler = protectHandler;
        this.profileProvider = profileProvider;

        this.gson = gsonManager.gson;
        executors = switch (config.netty.performance.executorType) {
            case NONE -> null;
            case DEFAULT -> Executors.newCachedThreadPool();
            case WORK_STEAL -> Executors.newWorkStealingPool();
            case VIRTUAL_THREADS -> Executors.newVirtualThreadPerTaskExecutor();
        };
    }

    public static void registerResponses() {
        // Auth
        providers.register("additionalData", AdditionalDataResponse.class);
        providers.register("auth", AuthResponse.class);
        providers.register("checkServer", CheckServerResponse.class);
        providers.register("currentUser", CurrentUserResponse.class);
        providers.register("exit", ExitResponse.class);
        providers.register("clientProfileKey", FetchClientProfileKeyResponse.class);
        providers.register("getAvailabilityAuth", GetAvailabilityAuthResponse.class);
        providers.register("joinServer", JoinServerResponse.class);
        providers.register("profiles", ProfilesResponse.class);
        providers.register("refreshToken", RefreshTokenResponse.class);
        providers.register("restore", RestoreResponse.class);
        providers.register("setProfile", SetProfileResponse.class);

        // Update
        providers.register("launcher", LauncherResponse.class);
        providers.register("update", UpdateResponse.class);

        // Profile
        providers.register("batchProfileByUsername", BatchProfileByUsername.class);
        providers.register("profileByUsername", ProfileByUsername.class);
        providers.register("profileByUUID", ProfileByUUIDResponse.class);

        // Secure
        providers.register("getSecureLevelInfo", GetSecureLevelInfoResponse.class);
        providers.register("hardwareReport", HardwareReportResponse.class);
        providers.register("securityReport", SecurityReportResponse.class);
        providers.register("verifySecureLevelKey", VerifySecureLevelKeyResponse.class);

        // Management
        providers.register("features", FeaturesResponse.class);
        providers.register("getConnectUUID", GetConnectUUIDResponse.class);
        providers.register("getPublicKey", GetPublicKeyResponse.class);

        // Cabinet
        providers.register("assetUploadInfo", AssetUploadInfoResponse.class);
        providers.register("getAssetUploadUrl", GetAssetUploadInfoResponse.class);
    }

    public static void registerProviders(AuthProviders authProviders,
                                         AuthManager authManager,
                                         KeyAgreementManager keyAgreementManager) {
        if (!registeredProviders) {
            restoreProviders.put(LauncherRequestEvent.LAUNCHER_EXTENDED_TOKEN_NAME, new LauncherResponse.LauncherTokenVerifier(keyAgreementManager));
            restoreProviders.put("publicKey", new AdvancedProtectHandler.PublicKeyTokenVerifier(keyAgreementManager));
            restoreProviders.put("hardware", new AdvancedProtectHandler.HardwareInfoTokenVerifier(keyAgreementManager));
            restoreProviders.put("checkServer", new AuthManager.CheckServerVerifier(authManager, authProviders));
            registeredProviders = true;
        }
    }

    public static String getIPFromContext(ChannelHandlerContext ctx) {
        var handler = ctx.pipeline().get(WebSocketFrameHandler.class);
        if (handler == null || handler.context == null || handler.context.ip == null) {
            return IOHelper.getIP(ctx.channel().remoteAddress());
        }
        return handler.context.ip;
    }

    public static String getIPFromChannel(Channel channel) {
        var handler = channel.pipeline().get(WebSocketFrameHandler.class);
        if (handler == null || handler.context == null || handler.context.ip == null) {
            return IOHelper.getIP(channel.remoteAddress());
        }
        return handler.context.ip;
    }

    public void forEachActiveChannels(BiConsumer<Channel, WebSocketFrameHandler> callback) {
        for (Channel channel : channels) {
            if (channel == null || channel.pipeline() == null) continue;
            WebSocketFrameHandler wsHandler = channel.pipeline().get(WebSocketFrameHandler.class);
            if (wsHandler == null) continue;
            callback.accept(channel, wsHandler);
        }
    }

    public void process(ChannelHandlerContext ctx, TextWebSocketFrame frame, Client client, String ip, UUID connectUUID) {
        String request = frame.text();
        WebSocketRequestContext context = new WebSocketRequestContext(ctx, request, client, ip, connectUUID);
        if (hookBeforeParsing.hook(context)) {
            return;
        }
        WebSocketServerResponse response = gson.fromJson(request, WebSocketServerResponse.class);
        context.response = response;
        if (response == null) {
            RequestEvent event = new ErrorRequestEvent("This type of request is not supported");
            hookComplete.hook(context);
            sendObject(ctx.channel(), event, WebSocketEvent.class);
            return;
        }
        var safeStatus = config.netty.performance.disableThreadSafeClientObject ?
                WebSocketServerResponse.ThreadSafeStatus.NONE : response.getThreadSafeStatus();
        if (executors == null) {
            process(safeStatus, client, ip, context, response);
        } else {
            executors.submit(() -> process(safeStatus, client, ip, context, response));
        }
    }

    private void process(WebSocketServerResponse.ThreadSafeStatus safeStatus, Client client, String ip, WebSocketRequestContext context, WebSocketServerResponse response) {
        switch (safeStatus) {
            case NONE -> {
                process(context, response, client, ip);
            }
            case READ -> {
                var lock = client.lock.readLock();
                lock.lock();
                try {
                    process(context, response, client, ip);
                } finally {
                    lock.unlock();
                }
            }
            case READ_WRITE -> {
                var lock = client.lock.writeLock();
                lock.lock();
                try {
                    process(context, response, client, ip);
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    void process(WebSocketRequestContext context, WebSocketServerResponse response, Client client, String ip) {
        if (hookBeforeExecute.hook(context)) {
            return;
        }
        ChannelHandlerContext ctx = context.context;
        if (response instanceof SimpleResponse simpleResponse) {

            simpleResponse.config = config;
            simpleResponse.authProviders = authProviders;
            simpleResponse.authManager = authManager;

            simpleResponse.authHookManager = authHookManager;
            simpleResponse.updatesManager = updatesManager;
            simpleResponse.keyAgreementManager = keyAgreementManager;
            simpleResponse.launcherBinary = launcherBinary;

            simpleResponse.exeLauncherBinary = exeLauncherBinary;
            simpleResponse.featuresManager = featuresManager;
            simpleResponse.protectHandler = protectHandler;
            simpleResponse.profileProvider = profileProvider;

            simpleResponse.service = this;
            simpleResponse.ctx = ctx;
            if (ip != null) simpleResponse.ip = ip;
            else simpleResponse.ip = IOHelper.getIP(ctx.channel().remoteAddress());
            simpleResponse.connectUUID = context.connectUUID;
        }
        try {
            response.execute(ctx, client);
        } catch (Throwable e) {
            context.exception = e;
            logger.error("WebSocket request processing failed", e);
            RequestEvent event;
            event = new ErrorRequestEvent("Fatal server error. Contact administrator");
            if (response instanceof SimpleResponse simpleResponse) event.requestUUID = simpleResponse.requestUUID;
            sendObject(ctx.channel(), event);
        }
        hookComplete.hook(context);
    }

    public void registerClient(Channel channel) {
        channels.add(channel);
    }

    public void sendObject(Channel channel, Object obj) {
        if (hookSend.hook(channel, obj)) {
            return;
        }
        String msg = gson.toJson(obj, WebSocketEvent.class);
        if (logger.isTraceEnabled()) {
            logger.trace("Send to channel {}: {}", getIPFromChannel(channel), msg);
        }
        channel.writeAndFlush(new TextWebSocketFrame(msg), channel.voidPromise());
    }

    public void sendObject(Channel channel, Object obj, Type type) {
        if (hookSend.hook(channel, obj)) {
            return;
        }
        String msg = gson.toJson(obj, type);
        if (logger.isTraceEnabled()) {
            logger.trace("Send to channel {}: {}", getIPFromChannel(channel), msg);
        }
        channel.writeAndFlush(new TextWebSocketFrame(msg), channel.voidPromise());
    }

    public void sendObjectAll(Object obj, Type type) {
        for (Channel ch : channels) {
            sendObject(ch, obj, type);
        }
    }

    public void sendObjectToUUID(UUID userUuid, Object obj, Type type) {
        for (Channel ch : channels) {
            if (ch == null || ch.pipeline() == null) continue;
            WebSocketFrameHandler wsHandler = ch.pipeline().get(WebSocketFrameHandler.class);
            if (wsHandler == null) continue;
            Client client = wsHandler.getClient();
            if (client == null || !userUuid.equals(client.uuid)) continue;
            if (hookSend.hook(ch, obj)) {
                continue;
            }
            String msg = gson.toJson(obj, type);
            if (logger.isTraceEnabled()) {
                logger.trace("Send to {}({}): {}", getIPFromChannel(ch), userUuid, msg);
            }
            ch.writeAndFlush(new TextWebSocketFrame(msg), ch.voidPromise());
        }
    }

    public Channel getChannelFromConnectUUID(UUID connectUuid) {
        for (Channel ch : channels) {
            if (ch == null || ch.pipeline() == null) continue;
            WebSocketFrameHandler wsHandler = ch.pipeline().get(WebSocketFrameHandler.class);
            if (wsHandler == null) continue;
            if (connectUuid.equals(wsHandler.getConnectUUID())) {
                return ch;
            }
        }
        return null;
    }

    public boolean kickByUserUUID(UUID userUuid, boolean isClose) {
        boolean result = false;
        for (Channel ch : channels) {
            if (ch == null || ch.pipeline() == null) continue;
            WebSocketFrameHandler wsHandler = ch.pipeline().get(WebSocketFrameHandler.class);
            if (wsHandler == null) continue;
            Client client = wsHandler.getClient();
            if (client == null || !userUuid.equals(client.uuid)) continue;
            ExitResponse.exit(wsHandler, ch, ExitRequestEvent.ExitReason.SERVER);
            if (isClose) ch.close();
            result = true;
        }
        return result;
    }

    public boolean kickByConnectUUID(UUID connectUuid, boolean isClose) {
        for (Channel ch : channels) {
            if (ch == null || ch.pipeline() == null) continue;
            WebSocketFrameHandler wsHandler = ch.pipeline().get(WebSocketFrameHandler.class);
            if (wsHandler == null) continue;
            if (connectUuid.equals(wsHandler.getConnectUUID())) {
                ExitResponse.exit(wsHandler, ch, ExitRequestEvent.ExitReason.SERVER);
                if (isClose) ch.close();
                return true;
            }
        }
        return false;
    }

    public boolean kickByIP(String ip, boolean isClose) {
        boolean result = false;
        for (Channel ch : channels) {
            if (ch == null || ch.pipeline() == null) continue;
            WebSocketFrameHandler wsHandler = ch.pipeline().get(WebSocketFrameHandler.class);
            if (wsHandler == null) continue;
            String clientIp;
            if (wsHandler.context != null && wsHandler.context.ip != null) clientIp = wsHandler.context.ip;
            else clientIp = IOHelper.getIP(ch.remoteAddress());
            if (ip.equals(clientIp)) {
                ExitResponse.exit(wsHandler, ch, ExitRequestEvent.ExitReason.SERVER);
                if (isClose) ch.close();
                result = true;
            }
        }
        return result;
    }

    public void sendObjectAndClose(ChannelHandlerContext ctx, Object obj) {
        if (hookSend.hook(ctx.channel(), obj)) {
            return;
        }
        String msg = gson.toJson(obj, WebSocketEvent.class);
        if (logger.isTraceEnabled()) {
            logger.trace("Send and close {}: {}", getIPFromContext(ctx), msg);
        }
        ctx.writeAndFlush(new TextWebSocketFrame(msg)).addListener(ChannelFutureListener.CLOSE);
    }

    public void sendObjectAndClose(ChannelHandlerContext ctx, Object obj, Type type) {
        if (hookSend.hook(ctx.channel(), obj)) {
            return;
        }
        String msg = gson.toJson(obj, type);
        if (logger.isTraceEnabled()) {
            logger.trace("Send and close {}: {}", getIPFromContext(ctx), msg);
        }
        ctx.writeAndFlush(new TextWebSocketFrame(msg)).addListener(ChannelFutureListener.CLOSE);
    }

    public static class WebSocketRequestContext {
        public final ChannelHandlerContext context;
        public final String text;
        public final Client client;
        public final String ip;
        public final UUID connectUUID;
        public WebSocketServerResponse response;
        public Throwable exception;

        public WebSocketRequestContext(ChannelHandlerContext context, String text, Client client, String ip, UUID connectUUID) {
            this.context = context;
            this.text = text;
            this.client = client;
            this.ip = ip;
            this.connectUUID = connectUUID;
        }
    }

    public static class EventResult implements WebSocketEvent {
        public EventResult() {

        }

        @Override
        public String getType() {
            return "event";
        }
    }

}
