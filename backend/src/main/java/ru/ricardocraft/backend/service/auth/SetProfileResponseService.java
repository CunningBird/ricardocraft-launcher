package ru.ricardocraft.backend.service.auth;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.auth.protect.interfaces.ProfilesProtectHandler;
import ru.ricardocraft.backend.base.events.request.SetProfileRequestEvent;
import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.manangers.AuthHookManager;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;
import ru.ricardocraft.backend.socket.response.WebSocketServerResponse;
import ru.ricardocraft.backend.socket.response.auth.SetProfileResponse;
import ru.ricardocraft.backend.utils.HookException;

import java.util.Collection;

@Component
public class SetProfileResponseService extends AbstractResponseService {

    private final AuthHookManager authHookManager;
    private final ProfileProvider profileProvider;
    private final ProtectHandler protectHandler;

    @Autowired
    public SetProfileResponseService(WebSocketService service,
                                     AuthHookManager authHookManager,
                                     ProfileProvider profileProvider,
                                     ProtectHandler protectHandler) {
        super(SetProfileResponse.class, service);
        this.authHookManager = authHookManager;
        this.profileProvider = profileProvider;
        this.protectHandler = protectHandler;
    }

    @Override
    public void execute(WebSocketServerResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        SetProfileResponse response = (SetProfileResponse) rawResponse;

        try {
            authHookManager.setProfileHook.hook(this, client);
        } catch (HookException e) {
            sendError(ctx, e.getMessage(), response.requestUUID);
        }
        Collection<ClientProfile> profiles = profileProvider.getProfiles();
        for (ClientProfile p : profiles) {
            if (p.getTitle().equals(response.client)) {
                if (protectHandler instanceof ProfilesProtectHandler profilesProtectHandler &&
                        !profilesProtectHandler.canChangeProfile(p, client)) {
                    sendError(ctx,"Access denied", response.requestUUID);
                    return;
                }
                client.profile = p;
                sendResult(ctx, new SetProfileRequestEvent(p), response.requestUUID);
                return;
            }
        }
        sendError(ctx, "Profile not found", response.requestUUID);
    }
}