package ru.ricardocraft.backend.service.auth;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.auth.protect.interfaces.ProfilesProtectHandler;
import ru.ricardocraft.backend.dto.events.request.auth.SetProfileRequestEvent;
import ru.ricardocraft.backend.profiles.ClientProfile;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.auth.SetProfileResponse;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;

import java.util.Collection;

@Component
public class SetProfileResponseService extends AbstractResponseService {

    private final ProfileProvider profileProvider;
    private final ProtectHandler protectHandler;

    @Autowired
    public SetProfileResponseService(WebSocketService service,
                                     ProfileProvider profileProvider,
                                     ProtectHandler protectHandler) {
        super(SetProfileResponse.class, service);
        this.profileProvider = profileProvider;
        this.protectHandler = protectHandler;
    }

    @Override
    public void execute(SimpleResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        SetProfileResponse response = (SetProfileResponse) rawResponse;
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
