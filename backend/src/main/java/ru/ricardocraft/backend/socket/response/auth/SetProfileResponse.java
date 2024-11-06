package ru.ricardocraft.backend.socket.response.auth;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.backend.base.events.request.SetProfileRequestEvent;
import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.auth.protect.interfaces.ProfilesProtectHandler;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.response.SimpleResponse;
import ru.ricardocraft.backend.utils.HookException;

import java.util.Collection;

public class SetProfileResponse extends SimpleResponse {
    public String client;

    @Override
    public String getType() {
        return "setProfile";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) {
        try {
            server.authHookManager.setProfileHook.hook(this, client);
        } catch (HookException e) {
            sendError(e.getMessage());
        }
        Collection<ClientProfile> profiles = server.getProfiles();
        for (ClientProfile p : profiles) {
            if (p.getTitle().equals(this.client)) {
                if (server.config.protectHandler instanceof ProfilesProtectHandler profilesProtectHandler &&
                        !profilesProtectHandler.canChangeProfile(p, client)) {
                    sendError("Access denied");
                    return;
                }
                client.profile = p;
                sendResult(new SetProfileRequestEvent(p));
                return;
            }
        }
        sendError("Profile not found");
    }

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }
}
