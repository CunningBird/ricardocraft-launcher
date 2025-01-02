package ru.ricardocraft.client.service.client;

import ru.ricardocraft.client.dto.response.AuthRequestEvent;
import ru.ricardocraft.client.service.profiles.ClientProfile;
import ru.ricardocraft.client.service.profiles.ClientProfileVersions;
import ru.ricardocraft.client.service.profiles.PlayerProfile;
import ru.ricardocraft.client.service.profiles.optional.actions.OptionalAction;
import ru.ricardocraft.client.service.profiles.optional.actions.OptionalActionClientArgs;
import ru.ricardocraft.client.dto.request.Request;
import ru.ricardocraft.client.base.hasher.HashedDir;
import ru.ricardocraft.client.base.utils.Version;

import java.util.*;
import java.util.regex.Pattern;

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


    private static final Pattern UUID_PATTERN = Pattern.compile("-", Pattern.LITERAL);

    private String getAccessToken() {
        return (accessToken == null) ? "empty_value" : accessToken;
    }

    public void addClientArgs(Collection<String> args) {
        if (profile.getVersion().compareTo(ClientProfileVersions.MINECRAFT_1_6_4) >= 0)
            addModernClientArgs(args);
        else
            addClientLegacyArgs(args);
    }

    public void addClientLegacyArgs(Collection<String> args) {
        args.add(playerProfile.username);
        args.add(getAccessToken());

        // Add args for tweaker
        Collections.addAll(args, "--version", profile.getVersion().toString());
        Collections.addAll(args, "--gameDir", clientDir);
        Collections.addAll(args, "--assetsDir", assetDir);
    }

    private void addModernClientArgs(Collection<String> args) {

        // Add version-dependent args
        ClientProfile.Version version = profile.getVersion();
        Collections.addAll(args, "--username", playerProfile.username);
        if (version.compareTo(ClientProfileVersions.MINECRAFT_1_7_2) >= 0) {
            Collections.addAll(args, "--uuid", toHash(playerProfile.uuid));
            Collections.addAll(args, "--accessToken", getAccessToken());

            // Add 1.7.10+ args (user properties, asset index)
            if (version.compareTo(ClientProfileVersions.MINECRAFT_1_7_10) >= 0) {
                // Add user properties
                Collections.addAll(args, "--userType", "mojang");
                Collections.addAll(args, "--userProperties", "{}");

                // Add asset index
                Collections.addAll(args, "--assetIndex", profile.getAssetIndex());
            }
        } else
            Collections.addAll(args, "--session", getAccessToken());

        // Add version and dirs args
        Collections.addAll(args, "--version", profile.getVersion().toString());
        Collections.addAll(args, "--gameDir", clientDir);
        Collections.addAll(args, "--assetsDir", assetDir);
        Collections.addAll(args, "--resourcePackDir", resourcePackDir);
        if (version.compareTo(ClientProfileVersions.MINECRAFT_1_9_4) >= 0)
            Collections.addAll(args, "--versionType", "Launcher v" + Version.getVersion().getVersionString());

        // Add server args
        if (autoEnter) {
            if (version.compareTo(ClientProfileVersions.MINECRAFT_1_20) <= 0) {
                Collections.addAll(args, "--server", profile.getServerAddress());
                Collections.addAll(args, "--port", Integer.toString(profile.getServerPort()));
            } else {
                Collections.addAll(args, "--quickPlayMultiplayer", String.format("%s:%d", profile.getServerAddress(), profile.getServerPort()));
            }
        }
        for (OptionalAction a : actions) {
            if (a instanceof OptionalActionClientArgs) {
                args.addAll(((OptionalActionClientArgs) a).args);
            }
        }
        // Add window size args
        if (fullScreen)
            Collections.addAll(args, "--fullscreen", Boolean.toString(true));
        if (width > 0 && height > 0) {
            Collections.addAll(args, "--width", Integer.toString(width));
            Collections.addAll(args, "--height", Integer.toString(height));
        }
    }

    public static String toHash(UUID uuid) {
        return UUID_PATTERN.matcher(uuid.toString()).replaceAll("");
    }
}
