package ru.ricardocraft.backend.manangers;

import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.service.auth.AuthResponseService;
import ru.ricardocraft.backend.service.auth.CheckServerResponseService;
import ru.ricardocraft.backend.service.auth.JoinServerResponseService;
import ru.ricardocraft.backend.service.auth.SetProfileResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.utils.BiHookSet;

@Component
public class AuthHookManager {
    public final BiHookSet<AuthResponseService.AuthContext, Client> preHook = new BiHookSet<>();
    public final BiHookSet<AuthResponseService.AuthContext, Client> postHook = new BiHookSet<>();
    public final BiHookSet<CheckServerResponseService, Client> checkServerHook = new BiHookSet<>();
    public final BiHookSet<AuthManager.CheckServerReport, Client> postCheckServerHook = new BiHookSet<>();
    public final BiHookSet<JoinServerResponseService, Client> joinServerHook = new BiHookSet<>();
    public final BiHookSet<SetProfileResponseService, Client> setProfileHook = new BiHookSet<>();
}
