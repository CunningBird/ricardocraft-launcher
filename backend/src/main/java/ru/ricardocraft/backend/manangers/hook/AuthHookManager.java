package ru.ricardocraft.backend.manangers.hook;

import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.response.auth.AuthResponse;
import ru.ricardocraft.backend.socket.response.auth.CheckServerResponse;
import ru.ricardocraft.backend.socket.response.auth.JoinServerResponse;
import ru.ricardocraft.backend.socket.response.auth.SetProfileResponse;
import ru.ricardocraft.backend.utils.BiHookSet;

public class AuthHookManager {
    public final BiHookSet<AuthResponse.AuthContext, Client> preHook = new BiHookSet<>();
    public final BiHookSet<AuthResponse.AuthContext, Client> postHook = new BiHookSet<>();
    public final BiHookSet<CheckServerResponse, Client> checkServerHook = new BiHookSet<>();
    public final BiHookSet<AuthManager.CheckServerReport, Client> postCheckServerHook = new BiHookSet<>();
    public final BiHookSet<JoinServerResponse, Client> joinServerHook = new BiHookSet<>();
    public final BiHookSet<SetProfileResponse, Client> setProfileHook = new BiHookSet<>();
}
