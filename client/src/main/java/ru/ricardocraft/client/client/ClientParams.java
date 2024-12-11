package ru.ricardocraft.client.client;

import ru.ricardocraft.client.base.events.request.AuthRequestEvent;
import ru.ricardocraft.client.base.profiles.ClientProfile;
import ru.ricardocraft.client.base.profiles.PlayerProfile;
import ru.ricardocraft.client.base.profiles.optional.actions.OptionalAction;
import ru.ricardocraft.client.base.request.Request;
import ru.ricardocraft.client.core.hasher.HashedDir;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ClientParams {
    public long timestamp;
    public String assetDir;

    public String clientDir;

    public String resourcePackDir;

    public String nativesDir;

    // Client params

    public PlayerProfile playerProfile;

    public ClientProfile profile;

    public String accessToken;

    //==Minecraft params==

    public boolean autoEnter;

    public boolean fullScreen;
    public boolean lwjglGlfwWayland;

    public int ram;

    public int width;

    public int height;

    public Set<OptionalAction> actions = new HashSet<>();

    //========

    public UUID session;

    public AuthRequestEvent.OAuthRequestEvent oauth;

    public String authId;

    public long oauthExpiredTime;

    public Map<String, Request.ExtendedToken> extendedTokens;

    public boolean offlineMode;

    public transient HashedDir assetHDir;

    public transient HashedDir clientHDir;

    public transient HashedDir javaHDir;

}
