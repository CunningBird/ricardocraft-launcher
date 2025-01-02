package ru.ricardocraft.client.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import ru.ricardocraft.client.commands.CommandHandler;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.config.LauncherConfig;
import ru.ricardocraft.client.dto.request.RequestService;
import ru.ricardocraft.client.ui.impl.AbstractVisualComponent;
import ru.ricardocraft.client.ui.impl.BackgroundComponent;
import ru.ricardocraft.client.service.launch.RuntimeModuleManager;
import ru.ricardocraft.client.service.launch.RuntimeSecurityService;
import ru.ricardocraft.client.service.launch.SkinManager;
import ru.ricardocraft.client.ui.overlays.ProcessingOverlay;
import ru.ricardocraft.client.ui.overlays.UploadAssetOverlay;
import ru.ricardocraft.client.ui.overlays.WelcomeOverlay;
import ru.ricardocraft.client.service.SettingsManager;
import ru.ricardocraft.client.ui.scenes.console.ConsoleScene;
import ru.ricardocraft.client.ui.scenes.debug.DebugScene;
import ru.ricardocraft.client.ui.scenes.internal.BrowserScene;
import ru.ricardocraft.client.ui.scenes.login.LoginScene;
import ru.ricardocraft.client.ui.scenes.login.WebAuthOverlay;
import ru.ricardocraft.client.ui.scenes.options.OptionsScene;
import ru.ricardocraft.client.ui.scenes.serverinfo.ServerInfoScene;
import ru.ricardocraft.client.ui.scenes.servermenu.ServerMenuScene;
import ru.ricardocraft.client.ui.scenes.settings.GlobalSettingsScene;
import ru.ricardocraft.client.ui.scenes.settings.SettingsScene;
import ru.ricardocraft.client.ui.scenes.update.UpdateScene;
import ru.ricardocraft.client.service.*;

import java.io.IOException;
import java.util.Collection;

@Configuration
public class GuiConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public GuiObjectsContainer guiObjectsContainer(LauncherConfig config, SettingsManager settingsManager) {
        return new GuiObjectsContainer(config, settingsManager) {
            @Override
            protected BackgroundComponent getBackgroundComponent() {
                return applicationContext.getBean(BackgroundComponent.class);
            }

            @Override
            protected LoginScene getLoginScene() {
                return applicationContext.getBean(LoginScene.class);
            }

            @Override
            protected AbstractVisualComponent getByName(String name) {
                return applicationContext.getBean(name, AbstractVisualComponent.class);
            }
        };
    }

    @Bean("webView")
    @Scope(VisualComponentScopeConfigurer.SCOPE_NAME)
    public WebAuthOverlay webAuthOverlay(GuiModuleConfig guiModuleConfig, LaunchService launchService) {
        return new WebAuthOverlay(guiModuleConfig, launchService);
    }

    @Bean("background")
    @Scope(VisualComponentScopeConfigurer.SCOPE_NAME)
    public BackgroundComponent backgroundComponent(GuiModuleConfig guiModuleConfig, LaunchService launchService) {
        return new BackgroundComponent(guiModuleConfig, launchService);
    }

    @Bean("login")
    @Scope(VisualComponentScopeConfigurer.SCOPE_NAME)
    public LoginScene loginScene(LauncherConfig config,
                                 GuiObjectsContainer guiObjectsContainer,
                                 GuiModuleConfig guiModuleConfig,
                                 SettingsManager settingsManager,
                                 AuthService authService,
                                 SkinManager skinManager,
                                 LaunchService launchService,
                                 RuntimeSecurityService securityService) {
        return new LoginScene(config, guiObjectsContainer, guiModuleConfig, settingsManager, authService, skinManager, launchService, securityService) {
            @Override
            protected ProcessingOverlay getProcessingOverlay() {
                return applicationContext.getBean(ProcessingOverlay.class);
            }

            @Override
            protected LoginScene getLoginScene() {
                return applicationContext.getBean(LoginScene.class);
            }

            @Override
            protected GlobalSettingsScene getGlobalSettingsScene() {
                return applicationContext.getBean(GlobalSettingsScene.class);
            }

            @Override
            protected WelcomeOverlay geWelcomeOverlay() {
                return applicationContext.getBean(WelcomeOverlay.class);
            }

            @Override
            protected OptionsScene getOptionsScene() {
                return applicationContext.getBean(OptionsScene.class);
            }

            @Override
            protected ServerMenuScene getServerMenuScene() {
                return applicationContext.getBean(ServerMenuScene.class);
            }

            @Override
            protected WebAuthOverlay getWebAuthOverlay() {
                return applicationContext.getBean(WebAuthOverlay.class);
            }

        };
    }

    @Bean("processing")
    @Scope(VisualComponentScopeConfigurer.SCOPE_NAME)
    public ProcessingOverlay processingOverlay(GuiModuleConfig guiModuleConfig,
                                               RequestService service,
                                               LaunchService launchService) {
        return new ProcessingOverlay(guiModuleConfig, service, launchService);
    }

    @Bean("welcome")
    @Scope(VisualComponentScopeConfigurer.SCOPE_NAME)
    public WelcomeOverlay welcomeOverlay(GuiModuleConfig guiModuleConfig,
                                         AuthService authService,
                                         SkinManager skinManager,
                                         LaunchService launchService) {
        return new WelcomeOverlay(guiModuleConfig, authService, skinManager, launchService);
    }

    @Bean("uploadasset")
    @Scope(VisualComponentScopeConfigurer.SCOPE_NAME)
    public UploadAssetOverlay getUploadAssetOverlay(GuiModuleConfig guiModuleConfig,
                                                    AuthService authService,
                                                    SkinManager skinManager,
                                                    LaunchService launchService) {
        return new UploadAssetOverlay(guiModuleConfig, authService, skinManager, launchService) {
            @Override
            protected ProcessingOverlay getProcessingOverlay() {
                return applicationContext.getBean(ProcessingOverlay.class);
            }

            @Override
            protected Collection<AbstractVisualComponent> getComponents() {
                return applicationContext.getBeansOfType(AbstractVisualComponent.class).values();
            }
        };
    }

    @Bean("serverMenu")
    @Scope(VisualComponentScopeConfigurer.SCOPE_NAME)
    public ServerMenuScene serverMenuScene(LauncherConfig config,
                                           GuiModuleConfig guiModuleConfig,
                                           SettingsManager settingsManager,
                                           AuthService authService,
                                           SkinManager skinManager,
                                           LaunchService launchService,
                                           PingService pingService) {
        return new ServerMenuScene(config, guiModuleConfig, settingsManager, authService, skinManager, launchService, pingService) {
            @Override
            protected ProcessingOverlay getProcessingOverlay() {
                return applicationContext.getBean(ProcessingOverlay.class);
            }

            @Override
            protected LoginScene getLoginScene() {
                return applicationContext.getBean(LoginScene.class);
            }

            @Override
            protected GlobalSettingsScene getGlobalSettingsScene() {
                return applicationContext.getBean(GlobalSettingsScene.class);
            }

            @Override
            protected ServerInfoScene getServerInfoScene() {
                return applicationContext.getBean(ServerInfoScene.class);
            }

            @Override
            protected UploadAssetOverlay getUploadAsset() {
                return applicationContext.getBean(UploadAssetOverlay.class);
            }
        };
    }

    @Bean("serverinfo")
    @Scope(VisualComponentScopeConfigurer.SCOPE_NAME)
    public ServerInfoScene serverInfoScene(LauncherConfig config,
                                           GuiModuleConfig guiModuleConfig,
                                           SettingsManager settingsManager,
                                           AuthService authService,
                                           SkinManager skinManager,
                                           LaunchService launchService,
                                           PingService pingService) {
        return new ServerInfoScene(config, guiModuleConfig, settingsManager, authService, skinManager, launchService, pingService) {
            @Override
            protected ProcessingOverlay getProcessingOverlay() {
                return applicationContext.getBean(ProcessingOverlay.class);
            }

            @Override
            protected LoginScene getLoginScene() {
                return applicationContext.getBean(LoginScene.class);
            }

            @Override
            protected OptionsScene getOptionsScene() {
                return applicationContext.getBean(OptionsScene.class);
            }

            @Override
            protected SettingsScene getSettingsScene() {
                return applicationContext.getBean(SettingsScene.class);
            }

            @Override
            protected DebugScene getDebugScene() {
                return applicationContext.getBean(DebugScene.class);
            }

            @Override
            protected UploadAssetOverlay getUploadAsset() {
                return applicationContext.getBean(UploadAssetOverlay.class);
            }
        };
    }

    @Bean("options")
    @Scope(VisualComponentScopeConfigurer.SCOPE_NAME)
    public OptionsScene optionsScene(LauncherConfig config,
                                     GuiModuleConfig guiModuleConfig,
                                     AuthService authService,
                                     SkinManager skinManager,
                                     LaunchService launchService,
                                     PingService pingService,
                                     SettingsManager settingsManager) {
        return new OptionsScene(config, guiModuleConfig, authService, skinManager, launchService, pingService, settingsManager) {
            @Override
            protected ProcessingOverlay getProcessingOverlay() {
                return applicationContext.getBean(ProcessingOverlay.class);
            }

            @Override
            protected LoginScene getLoginScene() {
                return applicationContext.getBean(LoginScene.class);
            }

            @Override
            protected ServerInfoScene getServerInfoScene() {
                return applicationContext.getBean(ServerInfoScene.class);
            }

            @Override
            protected UploadAssetOverlay getUploadAsset() {
                return applicationContext.getBean(UploadAssetOverlay.class);
            }
        };
    }

    @Bean("settings")
    @Scope(VisualComponentScopeConfigurer.SCOPE_NAME)
    public SettingsScene settingsScene(LauncherConfig config,
                                       GuiModuleConfig guiModuleConfig,
                                       SettingsManager settingsManager,
                                       AuthService authService,
                                       SkinManager skinManager,
                                       LaunchService launchService,
                                       JavaService javaService,
                                       PingService pingService) {
        return new SettingsScene(config, guiModuleConfig, settingsManager, authService, skinManager, launchService, javaService, pingService) {
            @Override
            protected ProcessingOverlay getProcessingOverlay() {
                return applicationContext.getBean(ProcessingOverlay.class);
            }

            @Override
            protected LoginScene getLoginScene() {
                return applicationContext.getBean(LoginScene.class);
            }

            @Override
            protected UploadAssetOverlay getUploadAsset() {
                return applicationContext.getBean(UploadAssetOverlay.class);
            }
        };
    }

    @Bean("globalsettings")
    @Scope(VisualComponentScopeConfigurer.SCOPE_NAME)
    public GlobalSettingsScene globalSettingsScene(LauncherConfig config,
                                                   GuiObjectsContainer guiObjectsContainer,
                                                   GuiModuleConfig guiModuleConfig,
                                                   SettingsManager settingsManager,
                                                   AuthService authService,
                                                   LaunchService launchService,
                                                   JavaService javaService) {
        return new GlobalSettingsScene(config, guiObjectsContainer, guiModuleConfig, settingsManager, authService, launchService, javaService) {
            @Override
            protected ProcessingOverlay getProcessingOverlay() {
                return applicationContext.getBean(ProcessingOverlay.class);
            }

            @Override
            protected LoginScene getLoginScene() {
                return applicationContext.getBean(LoginScene.class);
            }

            @Override
            protected ConsoleScene getConsoleScene() {
                return applicationContext.getBean(ConsoleScene.class);
            }

            @Override
            protected AbstractVisualComponent getByName(String name) {
                return applicationContext.getBean(name, AbstractVisualComponent.class);
            }
        };
    }

    @Bean("console")
    @Scope(VisualComponentScopeConfigurer.SCOPE_NAME)
    public ConsoleScene consoleScene(LauncherConfig config,
                                     GuiModuleConfig guiModuleConfig,
                                     AuthService authService,
                                     LaunchService launchService,
                                     CommandHandler commandHandler,
                                     SettingsManager settingsManager) {
        return new ConsoleScene(config, guiModuleConfig, authService, launchService, commandHandler, settingsManager) {
            @Override
            protected ProcessingOverlay getProcessingOverlay() {
                return applicationContext.getBean(ProcessingOverlay.class);
            }

            @Override
            protected LoginScene getLoginScene() {
                return applicationContext.getBean(LoginScene.class);
            }
        };
    }

    @Bean("update")
    @Scope(VisualComponentScopeConfigurer.SCOPE_NAME)
    public UpdateScene updateScene(LauncherConfig config,
                                   GuiModuleConfig guiModuleConfig,
                                   RequestService service,
                                   AuthService authService,
                                   LaunchService launchService,
                                   SettingsManager settingsManager) {
        return new UpdateScene(config, guiModuleConfig, service, authService, launchService, settingsManager) {
            @Override
            protected ProcessingOverlay getProcessingOverlay() {
                return applicationContext.getBean(ProcessingOverlay.class);
            }

            @Override
            protected LoginScene getLoginScene() {
                return applicationContext.getBean(LoginScene.class);
            }
        };
    }

    @Bean("debug")
    @Scope(VisualComponentScopeConfigurer.SCOPE_NAME)
    public DebugScene debugScene(LauncherConfig config,
                                 GuiModuleConfig guiModuleConfig,
                                 AuthService authService,
                                 LaunchService launchService,
                                 SettingsManager settingsManager) {
        return new DebugScene(config, guiModuleConfig, authService, launchService, settingsManager) {
            @Override
            protected ProcessingOverlay getProcessingOverlay() {
                return applicationContext.getBean(ProcessingOverlay.class);
            }

            @Override
            protected LoginScene getLoginScene() {
                return applicationContext.getBean(LoginScene.class);
            }
        };
    }

    @Bean("browser")
    @Scope(VisualComponentScopeConfigurer.SCOPE_NAME)
    public BrowserScene browserScene(LauncherConfig config,
                                     GuiModuleConfig guiModuleConfig,
                                     AuthService authService,
                                     LaunchService launchService,
                                     SettingsManager settingsManager) {
        return new BrowserScene(config, guiModuleConfig, authService, launchService, settingsManager) {
            @Override
            protected ProcessingOverlay getProcessingOverlay() {
                return applicationContext.getBean(ProcessingOverlay.class);
            }

            @Override
            protected LoginScene getLoginScene() {
                return applicationContext.getBean(LoginScene.class);
            }
        };
    }

    @Bean
    public LaunchService launchService(SettingsManager settingsManager,
                                       GuiObjectsContainer guiObjectsContainer,
                                       GuiModuleConfig guiModuleConfig,
                                       RuntimeModuleManager modulesManager,
                                       OfflineService offlineService,
                                       AuthService authService,
                                       JavaService javaService) throws IOException {
        return new LaunchService(guiObjectsContainer, settingsManager, guiModuleConfig, modulesManager, offlineService, authService, javaService) {
            @Override
            protected UpdateScene getUpdateScene() {
                return applicationContext.getBean(UpdateScene.class);
            }

            @Override
            protected ProcessingOverlay getProcessingOverlay() {
                return applicationContext.getBean(ProcessingOverlay.class);
            }

            @Override
            protected AbstractVisualComponent getByName(String name) {
                return applicationContext.getBean(name, AbstractVisualComponent.class);
            }
        };
    }
}
