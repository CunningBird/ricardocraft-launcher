package ru.ricardocraft.client.service.modules.impl;

import ru.ricardocraft.client.service.LauncherTrustManager;
import ru.ricardocraft.client.base.helper.JVMHelper;
import ru.ricardocraft.client.base.helper.LogHelper;
import ru.ricardocraft.client.service.modules.*;

import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleModuleManager implements LauncherModulesManager {

    protected final List<LauncherModule> modules = new ArrayList<>();
    protected final List<String> moduleNames = new ArrayList<>();
    protected final SimpleModuleContext context;
    protected final ModulesConfigManager modulesConfigManager;
    protected final Path modulesDir;
    protected final LauncherTrustManager trustManager;
    protected LauncherInitContext initContext;

    @Override
    public LauncherModule loadModule(LauncherModule module) {
        if (modules.contains(module)) return module;
        if (module.getCheckStatus() == null) {
            LauncherTrustManager.CheckClassResult result = checkModuleClass(module.getClass());
            module.setCheckResult(result);
        }
        modules.add(module);
        LauncherModuleInfo info = module.getModuleInfo();
        moduleNames.add(info.name);
        module.setContext(context);
        module.preInit();
        if (initContext != null) {
            module.setInitStatus(LauncherModule.InitStatus.INIT);
            module.init(initContext);
            module.setInitStatus(LauncherModule.InitStatus.FINISH);
        }
        return module;
    }

    @Override
    public LauncherModule getModule(String name) {
        for (LauncherModule module : modules) {
            LauncherModuleInfo info = module.getModuleInfo();
            if (info.name.equals(name) || (info.providers.length > 0 && Arrays.asList(info.providers).contains(name)))
                return module;
        }
        return null;
    }

    @Override
    public <T extends LauncherModule.Event> void invokeEvent(T event) {
        for (LauncherModule module : modules) {
            module.callEvent(event);
            if (event.isCancel()) return;
        }
    }

    public SimpleModuleManager(Path modulesDir, Path configDir, LauncherTrustManager trustManager) {
        modulesConfigManager = new SimpleModulesConfigManager(configDir);
        context = new SimpleModuleContext(this, modulesConfigManager);
        this.modulesDir = modulesDir;
        this.trustManager = trustManager;
    }

    public void initModules(LauncherInitContext initContext) {
        boolean isAnyModuleLoad = true;
        modules.sort((m1, m2) -> {
            int priority1 = m1.getModuleInfo().priority;
            int priority2 = m2.getModuleInfo().priority;
            return Integer.compare(priority1, priority2);
        });
        while (isAnyModuleLoad) {
            isAnyModuleLoad = false;
            for (LauncherModule module : modules) {
                if (!module.getInitStatus().equals(LauncherModule.InitStatus.INIT_WAIT)) continue;
                if (checkDepend(module)) {
                    isAnyModuleLoad = true;
                    module.setInitStatus(LauncherModule.InitStatus.INIT);
                    module.init(initContext);
                    module.setInitStatus(LauncherModule.InitStatus.FINISH);
                }
            }
        }
        for (LauncherModule module : modules) {
            if (module.getInitStatus().equals(LauncherModule.InitStatus.INIT_WAIT)) {
                LauncherModuleInfo info = module.getModuleInfo();
                LogHelper.warning("Module %s required %s. Cyclic dependencies?", info.name, Arrays.toString(info.dependencies));
                module.setInitStatus(LauncherModule.InitStatus.INIT);
                module.init(initContext);
                module.setInitStatus(LauncherModule.InitStatus.FINISH);
            } else if (module.getInitStatus().equals(LauncherModule.InitStatus.PRE_INIT_WAIT)) {
                LauncherModuleInfo info = module.getModuleInfo();
                LogHelper.error("Module %s skip pre-init phase. This module NOT finish loading", info.name, Arrays.toString(info.dependencies));
            }
        }
    }

    public LauncherTrustManager.CheckClassResult checkModuleClass(Class<? extends LauncherModule> clazz) {
        if (trustManager == null) return null;
        X509Certificate[] certificates = getCertificates(clazz);
        return trustManager.checkCertificates(certificates, trustManager::stdCertificateChecker);
    }

    private boolean checkDepend(LauncherModule module) {
        LauncherModuleInfo info = module.getModuleInfo();
        for (String dep : info.dependencies) {
            LauncherModule depModule = getModule(dep);
            if (depModule == null)
                throw new RuntimeException(String.format("Module %s required %s. %s not found", info.name, dep, dep));
            if (!depModule.getInitStatus().equals(LauncherModule.InitStatus.FINISH)) return false;
        }
        return true;
    }

    private static X509Certificate[] getCertificates(Class<?> clazz) {
        return JVMHelper.getCertificates(clazz);
    }

}
