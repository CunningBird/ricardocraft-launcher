package ru.ricardocraft.backend.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.ricardocraft.backend.service.command.unsafe.CipherListService;
import ru.ricardocraft.backend.service.command.unsafe.LoadJarService;
import ru.ricardocraft.backend.service.command.unsafe.PatcherService;
import ru.ricardocraft.backend.service.command.unsafe.SendAuthService;

@Slf4j
@ShellComponent
@ShellCommandGroup("unsafe")
@RequiredArgsConstructor
public class UnsafeCommands {

    private final CipherListService cipherListService;
    private final LoadJarService loadJarService;
    private final PatcherService patcherService;
    private final SendAuthService sendAuthService;

    @ShellMethod("[] list all available ciphers.")
    public void loadJar() {
        cipherListService.loadJar();
    }

    @ShellMethod("[jarfile] Load jar file")
    public void sendAuth(@ShellOption String jarFile) throws Exception {
        loadJarService.sendAuth(jarFile);
    }

    @ShellMethod("[patcher name or class] [path] [test mode(true/false)] (other args)")
    public void patcher(@ShellOption String name,
                        @ShellOption String path,
                        @ShellOption Boolean testMode,
                        @ShellOption(defaultValue = ShellOption.NULL) String[] realArgs) throws Exception {
        patcherService.patcher(name, path, testMode, realArgs);
    }

    @ShellMethod("[connectUUID] [username] [auth_id] [client type] (permissions) manual send auth request")
    public void cipherList(@ShellOption String connectUUID,
                           @ShellOption String username,
                           @ShellOption String auth_id,
                           @ShellOption String clientType,
                           @ShellOption(defaultValue = ShellOption.NULL) String[] cipherPermissions) throws Exception {
        sendAuthService.cipherList(connectUUID, username, auth_id, clientType, cipherPermissions);
    }
}
