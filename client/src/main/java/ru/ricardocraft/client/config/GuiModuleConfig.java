package ru.ricardocraft.client.config;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GuiModuleConfig {
    public String createAccountURL;
    public String forgotPassURL;
    public String hastebinServer;
    public boolean forceDownloadJava;
    public Map<String, String> javaList;
    public boolean lazy;
    public boolean disableOfflineMode;
    public boolean disableDebugPermissions;
    public boolean autoAuth;
    public String locale;
    public int downloadThreads = 4;

    public static Object getDefault() {
        GuiModuleConfig config = new GuiModuleConfig();
        config.createAccountURL = "https://gravit.pro/createAccount.php";
        config.forgotPassURL = "https://gravit.pro/fogotPass.php";
        config.hastebinServer = "https://hastebin.com";
        config.lazy = false;
        config.javaList = new HashMap<>();
        config.disableOfflineMode = false;
        config.autoAuth = false;
        config.locale = "RUSSIAN";
        config.downloadThreads = 4;
        return config;
    }
}
