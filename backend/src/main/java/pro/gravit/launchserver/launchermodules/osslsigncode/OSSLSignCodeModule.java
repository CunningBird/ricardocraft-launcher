package pro.gravit.launchserver.launchermodules.osslsigncode;

import pro.gravit.launchserver.LaunchServer;
import pro.gravit.launchserver.base.config.JsonConfigurable;
import pro.gravit.launchserver.base.modules.LauncherInitContext;
import pro.gravit.launchserver.base.modules.LauncherModule;
import pro.gravit.launchserver.base.modules.LauncherModuleInfo;
import pro.gravit.launchserver.binary.tasks.exe.BuildExeMainTask;
import pro.gravit.launchserver.modules.events.LaunchServerPostInitPhase;
import pro.gravit.launchserver.modules.impl.LaunchServerInitContext;
import pro.gravit.launchserver.utils.Version;
import pro.gravit.launchserver.utils.helper.LogHelper;

import java.io.IOException;
import java.nio.file.Path;

public class OSSLSignCodeModule extends LauncherModule {
    public OSSLSignCodeConfig config;

    public OSSLSignCodeModule() {
        super(new LauncherModuleInfo("OSSLSignCode", new Version(1, 0, 0), new String[]{"LaunchServerCore"}));
    }

    @Override
    public void init(LauncherInitContext initContext) {
        if (initContext instanceof LaunchServerInitContext launchServerInitContext) {
            initOSSLSignCode(launchServerInitContext.server);
        } else {
            registerEvent(this::registerTask, LaunchServerPostInitPhase.class);
        }
    }

    public void registerTask(LaunchServerPostInitPhase event) {
        initOSSLSignCode(event.server);
    }

    public void initOSSLSignCode(LaunchServer server) {
        Path configPath = modulesConfigManager.getModuleConfig("OSSLSignCode");
        OSSLSignCodeModule module = this;
        JsonConfigurable<OSSLSignCodeConfig> configurable = new JsonConfigurable<>(OSSLSignCodeConfig.class, configPath) {
            @Override
            public OSSLSignCodeConfig getConfig() {
                return config;
            }

            @Override
            public void setConfig(OSSLSignCodeConfig config) {
                module.config = config;
            }

            @Override
            public OSSLSignCodeConfig getDefaultConfig() {
                return OSSLSignCodeConfig.getDefault();
            }
        };
        try {
            configurable.loadConfig();
        } catch (IOException e) {
            LogHelper.error(e);
            return;
        }
        config = configurable.getConfig();
        server.commandHandler.registerCommand("osslsignexe", new OSSLSignEXECommand(server, config));
        server.launcherEXEBinary.addAfter((t) -> t instanceof BuildExeMainTask, new OSSLSignTask(server, config));
    }
}
