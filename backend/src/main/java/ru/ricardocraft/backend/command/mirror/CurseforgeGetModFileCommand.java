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
public class CurseforgeGetModFileCommand {

    private final CurseforgeAPI api;
    private final JacksonManager jacksonManager;

    @ShellMethod(value = "[modId] [fileId] Get mod file info by id")
    public void curseForgeGetModFile(@ShellOption Long modId, @ShellOption Long fileId) throws Exception {
        var e = api.fetchModFileById(modId, fileId);
        log.info("Response: {}", jacksonManager.getMapper().writeValueAsString(e));
    }
}
