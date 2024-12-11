package ru.ricardocraft.client.base.modules;

import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.base.ClientPermissions;
import ru.ricardocraft.client.base.events.request.AuthRequestEvent;
import ru.ricardocraft.client.base.events.request.ProfilesRequestEvent;
import ru.ricardocraft.client.base.modules.events.OfflineModeEvent;
import ru.ricardocraft.client.base.profiles.ClientProfile;
import ru.ricardocraft.client.base.profiles.PlayerProfile;
import ru.ricardocraft.client.base.request.auth.AuthRequest;
import ru.ricardocraft.client.base.request.auth.password.AuthOAuthPassword;
import ru.ricardocraft.client.base.request.update.ProfilesRequest;
import ru.ricardocraft.client.base.request.websockets.OfflineRequestService;
import ru.ricardocraft.client.client.events.ClientExitPhase;
import ru.ricardocraft.client.client.events.ClientUnlockConsoleEvent;
import ru.ricardocraft.client.runtime.client.DirBridge;
import ru.ricardocraft.client.utils.Version;
import ru.ricardocraft.client.utils.helper.JVMHelper;
import ru.ricardocraft.client.utils.helper.LogHelper;
import ru.ricardocraft.client.utils.helper.SecurityHelper;

import javax.swing.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class JavaRuntimeModule extends LauncherModule {

    public final static String RUNTIME_NAME = "stdruntime";

    public JavaRuntimeModule() {
        super(new LauncherModuleInfo("StdJavaRuntime",
                new Version(4, 0, 5, 1, Version.Type.STABLE),
                0, new String[]{}, new String[]{"runtime"}));
    }

    public static void noLocaleAlert(String file) {
        String message = """
                Не найден файл языка '%s' при инициализации GUI. Дальнейшая работа невозможна.
                Убедитесь что все файлы дизайна лаунчера присутствуют в папке runtime при сборке лаунчера
                """.formatted(file);
        JOptionPane.showMessageDialog(null, message, "GravitLauncher", JOptionPane.ERROR_MESSAGE);
    }

    public static void noEnFSAlert() {
        String message = """
                Запуск лаунчера невозможен из-за ошибки расшифровки рантайма.
                Администраторам: установите библиотеку EnFS для исправления этой проблемы
                """;
        JOptionPane.showMessageDialog(null, message, "GravitLauncher", JOptionPane.ERROR_MESSAGE);
    }

    public static String getLauncherInfo() {
        return "Launcher %s | Java %d(%s %s) x%d | %s x%d"
                .formatted(Version.getVersion().toString(), JVMHelper.JVM_VERSION, JVMHelper.RUNTIME_MXBEAN.getVmName(),
                        System.getProperty("java.version"), JVMHelper.JVM_BITS, JVMHelper.OS_TYPE.name(),
                        JVMHelper.OS_BITS);
    }

    public static String getMiniLauncherInfo() {
        return "Launcher %s | Java %d(%s) x%d | %s x%d"
                .formatted(Version.getVersion().toString(), JVMHelper.JVM_VERSION, System.getProperty("java.version"),
                        JVMHelper.JVM_BITS, JVMHelper.OS_TYPE.name(), JVMHelper.OS_BITS);
    }

    @Override
    public void init(LauncherInitContext initContext) {
        registerEvent(this::exitPhase, ClientExitPhase.class);
        registerEvent(this::consoleUnlock, ClientUnlockConsoleEvent.class);
        registerEvent(this::offlineMode, OfflineModeEvent.class);
    }

    private void consoleUnlock(ClientUnlockConsoleEvent event) {
        JavaFXApplication application = JavaFXApplication.getInstance();
        if (application != null) {
            application.registerPrivateCommands();
        }
    }

    private void offlineMode(OfflineModeEvent event) {
        OfflineRequestService service = (OfflineRequestService) event.service;
        service.registerRequestProcessor(
                AuthRequest.class, (r) -> {
                    var permissions = new ClientPermissions();
                    String login = r.login;
                    if (login == null && r.password instanceof AuthOAuthPassword oAuthPassword) {
                        login = oAuthPassword.accessToken;
                    }
                    if (login == null) {
                        login = "Player";
                    }
                    return new AuthRequestEvent(
                            permissions,
                            new PlayerProfile(
                                    UUID.nameUUIDFromBytes(login.getBytes(StandardCharsets.UTF_8)), login,
                                    new HashMap<>(), new HashMap<>()), SecurityHelper.randomStringToken(), "", null,
                            new AuthRequestEvent.OAuthRequestEvent(
                                    login, null, 0));
                });
        service.registerRequestProcessor(
                ProfilesRequest.class, (r) -> {
                    JavaFXApplication application = JavaFXApplication.getInstance();
                    List<ClientProfile> profileList =
                            application.runtimeSettings.profiles.stream()
                                    .filter(profile -> Files.exists(
                                            DirBridge.dirUpdates.resolve(profile.getDir()))
                                            && Files.exists(DirBridge.dirUpdates.resolve(
                                            profile.getAssetDir())))
                                    .collect(Collectors.toList());
                    return new ProfilesRequestEvent(profileList);
                });
    }

    private void exitPhase(ClientExitPhase exitPhase) {
        try {
            JavaFXApplication.getInstance().saveSettings();
        } catch (Throwable e) {
            LogHelper.error(e);
        }
    }
}
