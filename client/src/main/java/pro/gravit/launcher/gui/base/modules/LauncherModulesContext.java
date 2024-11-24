package pro.gravit.launcher.gui.base.modules;

import java.net.URL;

public interface LauncherModulesContext {
    LauncherModulesManager getModulesManager();

    ModulesConfigManager getModulesConfigManager();

    void addURL(URL url);
}
