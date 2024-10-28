package pro.gravit.launchserver.base.modules;

import java.net.URL;

public interface LauncherModulesContext {
    LauncherModulesManager getModulesManager();

    ModulesConfigManager getModulesConfigManager();

    void addURL(URL url);
}
