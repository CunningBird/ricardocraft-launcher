package pro.gravit.launcher.gui.base.modules;

import pro.gravit.launcher.gui.base.modules.LauncherModulesManager;
import pro.gravit.launcher.gui.base.modules.ModulesConfigManager;

import java.net.URL;

public interface LauncherModulesContext {
    LauncherModulesManager getModulesManager();

    ModulesConfigManager getModulesConfigManager();

    void addURL(URL url);
}
