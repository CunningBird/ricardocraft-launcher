package pro.gravit.launcher.gui.start;

import pro.gravit.launcher.gui.base.Launcher;
import pro.gravit.launcher.gui.base.modules.LauncherModule;
import pro.gravit.launcher.gui.base.modules.impl.SimpleModuleManager;
import pro.gravit.launcher.gui.core.LauncherTrustManager;
import pro.gravit.launcher.gui.start.ClientLauncherWrapper;
import pro.gravit.launcher.gui.start.ClientWrapperModule;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public final class RuntimeModuleManager extends SimpleModuleManager {
    public RuntimeModuleManager() {
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
    public boolean verifyClassCheckResult(LauncherTrustManager.CheckClassResult result) {
        return result.type == LauncherTrustManager.CheckClassResultType.SUCCESS;
    }

    @Override
    protected ModulesClassLoader createClassLoader() {
        return null;
    }

    public void callWrapper(ClientLauncherWrapper.ClientLauncherWrapperContext context) {
        for (LauncherModule module : modules) {
            if (module instanceof ClientWrapperModule) {
                ((ClientWrapperModule) module).wrapperPhase(context);
            }
        }
    }
}
