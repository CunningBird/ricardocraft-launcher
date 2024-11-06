package ru.ricardocraft.bff.manangers.hook;

import ru.ricardocraft.bff.manangers.AuthManager;
import ru.ricardocraft.bff.socket.Client;
import ru.ricardocraft.bff.socket.response.auth.AuthResponse;
import ru.ricardocraft.bff.socket.response.auth.CheckServerResponse;
import ru.ricardocraft.bff.socket.response.auth.JoinServerResponse;
import ru.ricardocraft.bff.socket.response.auth.SetProfileResponse;
import ru.ricardocraft.bff.utils.BiHookSet;

public class AuthHookManager {
    public final BiHookSet<AuthResponse.AuthContext, Client> preHook = new BiHookSet<>();
    public final BiHookSet<AuthResponse.AuthContext, Client> postHook = new BiHookSet<>();
    public final BiHookSet<CheckServerResponse, Client> checkServerHook = new BiHookSet<>();
    public final BiHookSet<AuthManager.CheckServerReport, Client> postCheckServerHook = new BiHookSet<>();
    public final BiHookSet<JoinServerResponse, Client> joinServerHook = new BiHookSet<>();
    public final BiHookSet<SetProfileResponse, Client> setProfileHook = new BiHookSet<>();
}
