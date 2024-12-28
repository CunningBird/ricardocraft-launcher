package ru.ricardocraft.backend.command.mirror;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.ricardocraft.backend.manangers.JacksonManager;
import ru.ricardocraft.backend.manangers.mirror.modapi.CurseforgeAPI;

@Slf4j
@ShellComponent
@ShellCommandGroup("mirror")
@RequiredArgsConstructor
public class CurseforgeGetModCommand {

    private final CurseforgeAPI api;
    private final JacksonManager jacksonManager;

    @ShellMethod(value = "[modId] Get mod info by id")
    public void curseForgeGetMod(@ShellOption Long modId) throws Exception {
        var e = api.fetchModById(modId);
        log.info("Response: {}", jacksonManager.getMapper().writeValueAsString(e));
    }
}
