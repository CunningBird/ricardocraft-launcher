package ru.ricardocraft.backend.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.ricardocraft.backend.service.command.service.*;

@Slf4j
@ShellComponent
@ShellCommandGroup("service")
@RequiredArgsConstructor
public class ServiceCommands {

    private final ClientsService clientsService;
    private final ConfigService configService;
    private final NotifyService notifyService;
    private final SecurityCheckService securityCheckService;
    private final ServerStatusService serverStatusService;
    private final TokenInfoService tokenInfoService;
    private final TokenServerService tokenServerService;

    @ShellMethod("[] Show all connected clients")
    public void clients() {
        clientsService.clients();
    }

    @ShellMethod("[] Clear authLimiter map")
    public void configAuthLimiterClear() {
        configService.configAuthLimiterClear();
    }

    @ShellMethod("[exclusion] Add exclusion to authLimiter")
    public void configAuthLimiterAddExclude(@ShellOption String exclusion) {
        configService.configAuthLimiterAddExclude(exclusion);
    }

    @ShellMethod("[] Clear exclusions in authLimiter")
    public void configAuthLimiterClearExclude() {
        configService.configAuthLimiterClearExclude();
    }

    @ShellMethod("[] invoke GC for authLimiter")
    public void configAuthLimiterGc() {
        configService.configAuthLimiterGc();
    }

    @ShellMethod("[] Remove exclusion from authLimiter")
    public void configAuthLimiterRmExclude(@ShellOption String exclude) {
        configService.configAuthLimiterRmExclude(exclude);
    }

    @ShellMethod("[login] (json/plain password data) Test auth")
    public void configAuthProviderAuth(@ShellOption String login, @ShellOption(defaultValue = ShellOption.NULL) String passwordData) throws Exception {
        configService.configAuthProviderAuth(login, passwordData);
    }

    @ShellMethod("[username] get user by username")
    public void configAuthProviderGetUserByUsername(@ShellOption String username) {
        configService.configAuthProviderGetUserByUsername(username);
    }

    @ShellMethod("[uuid] get user by uuid")
    public void configAuthProviderGetUserByUuid(@ShellOption String uuid) {
        configService.configAuthProviderGetUserByUuid(uuid);
    }

    @ShellMethod("[] reset proguard config")
    public void configProGuardClean() throws Exception {
        configService.configProGuardClean();
    }

    @ShellMethod("[] regenerate proguard dictionary")
    public void configProGuardRegen() throws Exception {
        configService.configProGuardRegen();
    }

    @ShellMethod("[] reset proguard config")
    public void configProGuardReset() throws Exception {
        configService.configProGuardReset();
    }

    @ShellMethod("[head] [message] (icon) send notification to all connected client")
    public void notify(@ShellOption String head,
                       @ShellOption String message,
                       @ShellOption(defaultValue = ShellOption.NULL) String icon) throws Exception {
        notifyService.notify(head, message, icon);
    }

    @ShellMethod("[] multiModCheck configuration")
    public void securityCheck() {
        securityCheckService.securityCheck();
    }

    @ShellMethod("[] Check server status")
    public void serverStatus() {
        serverStatusService.serverStatus();
    }

    @ShellMethod("[token] print token info")
    public void tokenInfo(@ShellOption String token) {
        tokenInfoService.tokenInfo(token);
    }

    @ShellMethod("[profileName] (authId) (public only) generate new server token")
    public void tokenServer(@ShellOption String profileName,
                            @ShellOption(defaultValue = ShellOption.NULL) String authId,
                            @ShellOption(defaultValue = "false") Boolean publicOnly) {
        tokenServerService.tokenServer(profileName, authId, publicOnly);
    }
}
