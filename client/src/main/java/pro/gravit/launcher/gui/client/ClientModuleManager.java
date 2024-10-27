package pro.gravit.launcher.gui.client;

import pro.gravit.launcher.gui.base.Launcher;
import pro.gravit.launcher.gui.base.modules.LauncherModule;
import pro.gravit.launcher.gui.base.modules.impl.SimpleModuleManager;
import pro.gravit.launcher.gui.core.LauncherTrustManager;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public final class ClientModuleManager extends SimpleModuleManager {
    public ClientModuleManager() {
        super(null, null, Launcher.getConfig().trustManager);
    }

    @Override
    public void autoload() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void autoload(Path dir) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LauncherModule loadModule(LauncherModule module) {
        return super.loadModule(module);
    }

    public List<LauncherModule> getModules() {
        return Collections.unmodifiableList(modules);
    }

    @Override
    protected ModulesClassLoader createClassLoader() {
        return null;
    }

    @Override
    public boolean verifyClassCheckResult(LauncherTrustManager.CheckClassResult result) {
        return result.type == LauncherTrustManager.CheckClassResultType.SUCCESS;
    }
}
