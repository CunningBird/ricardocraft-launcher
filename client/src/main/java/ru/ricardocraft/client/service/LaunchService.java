package ru.ricardocraft.client.service;

import javafx.scene.layout.Pane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.base.Launcher;
import ru.ricardocraft.client.base.profiles.ClientProfile;
import ru.ricardocraft.client.base.profiles.ClientProfileBuilder;
import ru.ricardocraft.client.base.profiles.ClientProfileVersions;
import ru.ricardocraft.client.base.profiles.optional.OptionalView;
import ru.ricardocraft.client.base.request.auth.SetProfileRequest;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.config.RuntimeSettings;
import ru.ricardocraft.client.core.hasher.HashedDir;
import ru.ricardocraft.client.dialogs.AbstractDialog;
import ru.ricardocraft.client.dialogs.ApplyDialog;
import ru.ricardocraft.client.dialogs.InfoDialog;
import ru.ricardocraft.client.dialogs.NotificationDialog;
import ru.ricardocraft.client.helper.EnFSHelper;
import ru.ricardocraft.client.helper.PositionHelper;
import ru.ricardocraft.client.impl.ContextHelper;
import ru.ricardocraft.client.impl.FXMLFactory;
import ru.ricardocraft.client.launch.RuntimeModuleManager;
import ru.ricardocraft.client.overlays.ProcessingOverlay;
import ru.ricardocraft.client.runtime.client.ClientLauncherProcess;
import ru.ricardocraft.client.runtime.client.DirBridge;
import ru.ricardocraft.client.runtime.managers.SettingsManager;
import ru.ricardocraft.client.scenes.AbstractScene;
import ru.ricardocraft.client.scenes.update.UpdateScene;
import ru.ricardocraft.client.stage.AbstractStage;
import ru.ricardocraft.client.stage.DialogStage;
import ru.ricardocraft.client.utils.helper.*;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Component
public class LaunchService {

    private final JavaFXApplication application = JavaFXApplication.getInstance();

    private final SettingsManager settingsManager;
    private final GuiModuleConfig guiModuleConfig;
    private final RuntimeModuleManager modulesManager;
    private final OfflineService offlineService;
    private final AuthService authService;
    private final JavaService javaService;

    public FXMLFactory fxmlFactory;
    private ResourceBundle resources;

    public final ExecutorService workers = Executors.newWorkStealingPool(4);

    @Autowired
    public LaunchService(SettingsManager settingsManager,
                         GuiModuleConfig guiModuleConfig,
                         RuntimeModuleManager modulesManager,
                         OfflineService offlineService,
                         AuthService authService,
                         JavaService javaService) throws IOException {
        this.settingsManager = settingsManager;
        this.modulesManager = modulesManager;
        this.guiModuleConfig = guiModuleConfig;
        this.offlineService = offlineService;
        this.authService = authService;
        this.javaService = javaService;
        updateLocaleResources(settingsManager.getRuntimeSettings().locale.name);
    }

    public void createNotification(String head, String message) {
        NotificationDialog dialog = new NotificationDialog(application, head, message, guiModuleConfig, this);
        if (application.getCurrentScene() != null) {
            AbstractStage stage = application.getMainStage();
            if (stage == null)
                throw new NullPointerException("Try show launcher notification in application.getMainStage() == null");
            ContextHelper.runInFxThreadStatic(() -> {
                dialog.init();
                stage.pushNotification(dialog.getFxmlRootPrivate());
                dialog.setOnClose(() -> stage.pullNotification(dialog.getFxmlRootPrivate()));
            });
        } else {
            AtomicReference<DialogStage> stage = new AtomicReference<>(null);
            ContextHelper.runInFxThreadStatic(() -> {
                NotificationDialog.NotificationSlot slot = new NotificationDialog.NotificationSlot(
                        (scrollTo) -> stage.get().stage.setY(stage.get().stage.getY() + scrollTo),
                        ((Pane) dialog.getFxmlRootPrivate()).getPrefHeight() + 20);
                dialog.setPosition(PositionHelper.PositionInfo.BOTTOM_RIGHT, slot);
                dialog.setOnClose(() -> {
                    stage.get().close();
                    stage.get().stage.setScene(null);
                });
                stage.set(new DialogStage(application, head, dialog));
                stage.get().show();
            });
        }
    }

    public void showDialog(String header, String text, Runnable onApplyCallback, Runnable onCloseCallback, boolean isLauncher) {
        InfoDialog dialog = new InfoDialog(application, header, text, onApplyCallback, onCloseCallback, guiModuleConfig, this);
        showAbstractDialog(dialog, header, isLauncher);
    }

    public void showApplyDialog(String header, String text, Runnable onApplyCallback, Runnable onDenyCallback, boolean isLauncher) {
        ApplyDialog dialog = new ApplyDialog(application, header, text, onApplyCallback, onDenyCallback, onDenyCallback, guiModuleConfig, this);
        showAbstractDialog(dialog, header, isLauncher);
    }

    public void showAbstractDialog(AbstractDialog dialog, String header, boolean isLauncher) {
        if (isLauncher) {
            AbstractScene scene = application.getCurrentScene();
            if (scene == null)
                throw new NullPointerException("Try show launcher dialog in application.getCurrentScene() == null");
            ContextHelper.runInFxThreadStatic(() -> initDialogInScene(scene, dialog));
        } else {
            AtomicReference<DialogStage> stage = new AtomicReference<>(null);
            ContextHelper.runInFxThreadStatic(() -> {
                stage.set(new DialogStage(application, header, dialog));
                stage.get().show();
            });
            dialog.setOnClose(() -> {
                stage.get().close();
                stage.get().stage.setScene(null);
            });
        }
    }

    public void initDialogInScene(AbstractScene scene, AbstractDialog dialog) {
        Pane dialogRoot = (Pane) dialog.getFxmlRootPrivate();
        if (!dialog.isInit()) {
            try {
                dialog.currentStage = scene.currentStage;
                dialog.init();
            } catch (Exception e) {
                scene.errorHandle(e);
            }
        }
        dialog.setOnClose(() -> {
            scene.currentStage.pull(dialogRoot);
            scene.currentStage.enable();
        });
        scene.disable();
        scene.currentStage.push(dialogRoot);
    }

    public void updateLocaleResources(String locale) throws IOException {
        try (InputStream input = IOHelper.newInput(EnFSHelper.getResourceURL("runtime_%s.properties".formatted(locale)))) {
            resources = new PropertyResourceBundle(input);
        }
        fxmlFactory = new FXMLFactory(resources, workers);
    }

    public final String getTranslation(String name) {
        return getTranslation(name, "'%s'".formatted(name));
    }

    public final String getTranslation(String key, String defaultValue) {
        try {
            return resources.getString(key);
        } catch (Throwable e) {
            return defaultValue;
        }
    }

    public boolean isTestUpdate(RuntimeSettings.ProfileSettings settings) {
        return offlineService.isOfflineMode() || (authService.checkDebugPermission("skipupdate") && settings.debugSkipUpdate);
    }

    private void downloadClients(CompletableFuture<ClientInstance> future,
                                 ClientProfile profile,
                                 RuntimeSettings.ProfileSettings settings,
                                 JavaHelper.JavaVersion javaVersion,
                                 HashedDir jvmHDir) {
        Path target = DirBridge.dirUpdates.resolve(profile.getAssetDir());
        LogHelper.info("Start update to %s", target.toString());
        boolean testUpdate = isTestUpdate(settings);

        Consumer<HashedDir> next = (assetHDir) -> {
            Path targetClient = DirBridge.dirUpdates.resolve(profile.getDir());
            LogHelper.info("Start update to %s", targetClient.toString());
            ((UpdateScene) application.gui.getByName("update")).sendUpdateRequest(profile.getDir(), targetClient,
                    profile.getClientUpdateMatcher(), true,
                    settingsManager.getOptionalView(), true, testUpdate,
                    (clientHDir) -> {
                        LogHelper.info("Success update");
                        try {
                            ClientInstance instance = doLaunchClient(
                                    target,
                                    assetHDir,
                                    targetClient,
                                    clientHDir,
                                    profile,
                                    settingsManager.getOptionalView(),
                                    javaVersion,
                                    jvmHDir
                            );
                            future.complete(instance);
                        } catch (Throwable e) {
                            future.completeExceptionally(e);
                        }
                    });
        };
        UpdateScene updateScene = (UpdateScene) application.gui.getByName("update");
        if (profile.getVersion().compareTo(ClientProfileVersions.MINECRAFT_1_6_4) <= 0) {
            updateScene.sendUpdateRequest(profile.getAssetDir(), target,
                    profile.getAssetUpdateMatcher(), true, null, false, testUpdate, next);
        } else {
            updateScene.sendUpdateAssetRequest(profile.getAssetDir(), target,
                    profile.getAssetUpdateMatcher(), true,
                    profile.getAssetIndex(), testUpdate, next);
        }
    }

    private ClientInstance doLaunchClient(Path assetDir,
                                          HashedDir assetHDir,
                                          Path clientDir,
                                          HashedDir clientHDir,
                                          ClientProfile profile,
                                          OptionalView view,
                                          JavaHelper.JavaVersion javaVersion,
                                          HashedDir jvmHDir) {
        RuntimeSettings.ProfileSettings profileSettings = settingsManager.getProfileSettings();
        if (javaVersion == null) {
            javaVersion = javaService.getRecommendJavaVersion(profile);
        }
        if (javaVersion == null) {
            javaVersion = JavaHelper.JavaVersion.getCurrentJavaVersion();
        }
        if (authService.checkDebugPermission("skipfilemonitor") && profileSettings.debugSkipFileMonitor) {
            var builder = new ClientProfileBuilder(profile);
            builder.setUpdate(new ArrayList<>());
            builder.setUpdateVerify(new ArrayList<>());
            builder.setUpdateExclusions(new ArrayList<>());
            profile = builder.createClientProfile();
        }
        ClientLauncherProcess clientLauncherProcess =
                new ClientLauncherProcess(settingsManager, modulesManager, clientDir, assetDir, javaVersion, clientDir.resolve("resourcepacks"), profile,
                        authService.getPlayerProfile(), view,
                        authService.getAccessToken(), clientHDir, assetHDir, jvmHDir);
        clientLauncherProcess.params.ram = profileSettings.ram;
        clientLauncherProcess.params.offlineMode = offlineService.isOfflineMode();
        if (clientLauncherProcess.params.ram > 0) {
            clientLauncherProcess.jvmArgs.add("-Xms" + clientLauncherProcess.params.ram + 'M');
            clientLauncherProcess.jvmArgs.add("-Xmx" + clientLauncherProcess.params.ram + 'M');
        }
        clientLauncherProcess.params.fullScreen = profileSettings.fullScreen;
        clientLauncherProcess.params.autoEnter = profileSettings.autoEnter;
        if (JVMHelper.OS_TYPE == JVMHelper.OS.LINUX) {
            clientLauncherProcess.params.lwjglGlfwWayland = profileSettings.waylandSupport;
        }
        return new ClientInstance(clientLauncherProcess, profileSettings);
    }

    private String getJavaDirName(Path javaPath) {
        String prefix = DirBridge.dirUpdates.toAbsolutePath().toString();
        if (javaPath == null || !javaPath.startsWith(prefix)) {
            return null;
        }
        Path result = DirBridge.dirUpdates.relativize(javaPath);
        return result.toString();
    }

    private void showJavaAlert(ClientProfile profile) {
        if ((JVMHelper.ARCH_TYPE == JVMHelper.ARCH.ARM32 || JVMHelper.ARCH_TYPE == JVMHelper.ARCH.ARM64)
                && profile.getVersion().compareTo(ClientProfileVersions.MINECRAFT_1_12_2) <= 0) {
            showDialog(
                    getTranslation("runtime.scenes.serverinfo.javaalert.lwjgl2.header"),
                    getTranslation("runtime.scenes.serverinfo.javaalert.lwjgl2.description")
                            .formatted(profile.getRecommendJavaVersion()), () -> {
                    }, () -> {
                    }, true);
        } else {
            showDialog(
                    getTranslation("runtime.scenes.serverinfo.javaalert.header"),
                    getTranslation("runtime.scenes.serverinfo.javaalert.description")
                            .formatted(profile.getRecommendJavaVersion()), () -> {
                    }, () -> {
                    }, true);
        }
    }

    public CompletableFuture<ClientInstance> launchClient() {
        AbstractStage stage = application.getMainStage();

        ClientProfile profile = settingsManager.getProfile();
        if (profile == null) throw new NullPointerException("profilesService.getProfile() is null");
        CompletableFuture<ClientInstance> future = new CompletableFuture<>();
        ((ProcessingOverlay) application.gui.getByName("processing")).processRequest(stage, getTranslation("runtime.overlay.processing.text.setprofile"),
                new SetProfileRequest(profile), (result) -> ContextHelper.runInFxThreadStatic(() -> {
                    UpdateScene updateScene = (UpdateScene) application.gui.getByName("update");
                    RuntimeSettings.ProfileSettings profileSettings = settingsManager.getProfileSettings();
                    JavaHelper.JavaVersion javaVersion = null;
                    for (JavaHelper.JavaVersion v : javaService.javaVersions) {
                        if (v.jvmDir.toAbsolutePath().toString().equals(profileSettings.javaPath)) {
                            javaVersion = v;
                        }
                    }
                    if (javaVersion == null
                            && profileSettings.javaPath != null
                            && !guiModuleConfig.forceDownloadJava) {
                        try {
                            javaVersion = JavaHelper.JavaVersion.getByPath(Paths.get(profileSettings.javaPath));
                        } catch (Throwable e) {
                            if (LogHelper.isDevEnabled()) {
                                LogHelper.error(e);
                            }
                            LogHelper.warning("Incorrect java path %s", profileSettings.javaPath);
                        }
                    }
                    if (javaVersion == null || javaService.isIncompatibleJava(javaVersion, profile)) {
                        javaVersion = javaService.getRecommendJavaVersion(profile);
                    }
                    if (javaVersion == null) {
                        showJavaAlert(profile);
                        return;
                    }
                    String jvmDirName = getJavaDirName(javaVersion.jvmDir);
                    if (jvmDirName != null) {
                        final JavaHelper.JavaVersion finalJavaVersion = javaVersion;
                        try {
                            stage.setScene(updateScene, true);
                            updateScene.reset();
                        } catch (Exception e) {
                            future.completeExceptionally(e);
                        }
                        updateScene.
                                sendUpdateRequest(jvmDirName, javaVersion.jvmDir, null, true,
                                        settingsManager.getOptionalView(), false, isTestUpdate(profileSettings),
                                        (jvmHDir) -> {
                                            if (JVMHelper.OS_TYPE == JVMHelper.OS.LINUX
                                                    || JVMHelper.OS_TYPE == JVMHelper.OS.MACOSX) {
                                                Path javaFile = finalJavaVersion.jvmDir.resolve("bin")
                                                        .resolve("java");
                                                if (Files.exists(javaFile)) {
                                                    if (!javaFile.toFile().setExecutable(true)) {
                                                        LogHelper.warning(
                                                                "Set permission for %s unsuccessful",
                                                                javaFile.toString());
                                                    }
                                                }
                                            }
                                            downloadClients(future, profile, profileSettings, finalJavaVersion, jvmHDir);
                                        });
                    } else {
                        try {
                            stage.setScene(updateScene, true);
                            updateScene.reset();
                        } catch (Exception e) {
                            future.completeExceptionally(e);
                        }
                        downloadClients(future, profile, profileSettings, javaVersion, null);
                    }
                }), future::completeExceptionally, null);
        return future;
    }

    public static class ClientInstance {
        private final ClientLauncherProcess process;
        private final RuntimeSettings.ProfileSettings settings;
        private final Thread writeParamsThread;
        private Thread runThread;
        private final CompletableFuture<Void> onWriteParams = new CompletableFuture<>();
        private final CompletableFuture<Integer> runFuture = new CompletableFuture<>();
        private final Set<ProcessListener> listeners = ConcurrentHashMap.newKeySet();

        public ClientInstance(ClientLauncherProcess process, RuntimeSettings.ProfileSettings settings) {
            this.process = process;
            this.settings = settings;
            this.writeParamsThread = CommonHelper.newThread("Client Params Writer Thread", true, () -> {
                try {
                    process.runWriteParams(
                            new InetSocketAddress("127.0.0.1", Launcher.getConfig().clientPort));
                    onWriteParams.complete(null);
                } catch (Throwable e) {
                    LogHelper.error(e);
                    onWriteParams.completeExceptionally(e);
                }
            });
        }

        private void run() {
            try {
                process.start(true);
                Process proc = process.getProcess();
                InputStream stream = proc.getInputStream();
                byte[] buf = IOHelper.newBuffer();
                try {
                    for (int length = stream.read(buf); length >= 0; length = stream.read(buf)) {
                        //append(new String(buf, 0, length));
                        handleListeners(buf, 0, length);
                    }
                } catch (Exception e) {
                    System.out.println("Flex");
                }
                if (proc.isAlive()) proc.waitFor();
                if (writeParamsThread != null && writeParamsThread.isAlive()) {
                    writeParamsThread.interrupt();
                }
                runFuture.complete(proc.exitValue());
            } catch (Throwable e) {
                if (writeParamsThread != null && writeParamsThread.isAlive()) {
                    writeParamsThread.interrupt();
                }
                runFuture.completeExceptionally(e);
            }
        }

        public void kill() {
            process.getProcess().destroyForcibly();
        }

        private void handleListeners(byte[] buf, int offset, int length) {
            for (var l : listeners) {
                l.onNext(buf, offset, length);
            }
        }

        public synchronized CompletableFuture<Integer> start() {
            if (runThread == null) {
                runThread = CommonHelper.newThread("Run Thread", true, this::run);
                writeParamsThread.start();
                runThread.start();
            }
            return runFuture;
        }

        public RuntimeSettings.ProfileSettings getSettings() {
            return settings;
        }

        public CompletableFuture<Void> getOnWriteParamsFuture() {
            return onWriteParams;
        }

        public void registerListener(ProcessListener listener) {
            listeners.add(listener);
        }

        public void unregisterListener(ProcessListener listener) {
            listeners.remove(listener);
        }

        @FunctionalInterface
        public interface ProcessListener {
            void onNext(byte[] buf, int offset, int length);
        }
    }
}
