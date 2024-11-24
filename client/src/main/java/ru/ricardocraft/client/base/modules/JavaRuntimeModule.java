package ru.ricardocraft.client.base.modules;

import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.base.modules.events.OfflineModeEvent;
import ru.ricardocraft.client.base.request.websockets.OfflineRequestService;
import ru.ricardocraft.client.client.events.ClientExitPhase;
import ru.ricardocraft.client.client.events.ClientUnlockConsoleEvent;
import ru.ricardocraft.client.service.OfflineService;
import ru.ricardocraft.client.utils.Version;
import ru.ricardocraft.client.utils.helper.JVMHelper;
import ru.ricardocraft.client.utils.helper.LogHelper;

import javax.swing.*;

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

    public static void errorHandleAlert(Throwable e) {
        String message = """
                Произошла серьезная ошибка при инициализации интерфейса лаунчера.
                Для пользователей:
                Обратитесь к администрации своего проекта с скриншотом этого окна
                Java %d (x%d) Ошибка %s
                Описание: %s
                Более подробную информацию можно получить из лога
                """.formatted(JVMHelper.JVM_VERSION, JVMHelper.JVM_BITS, e.getClass().getName(),
                e.getMessage() == null ? "null" : e.getMessage());
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
        OfflineService.applyRuntimeProcessors((OfflineRequestService) event.service);
    }

    private void exitPhase(ClientExitPhase exitPhase) {
        try {
            JavaFXApplication.getInstance().saveSettings();
        } catch (Throwable e) {
            LogHelper.error(e);
        }
    }
}
