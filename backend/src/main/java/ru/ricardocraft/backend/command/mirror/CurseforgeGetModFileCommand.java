package ru.ricardocraft.backend.command.mirror;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.ricardocraft.backend.manangers.mirror.modapi.CurseforgeAPI;

@Slf4j
@ShellComponent
@ShellCommandGroup("mirror")
@RequiredArgsConstructor
public class CurseforgeGetModFileCommand {

    private final CurseforgeAPI api;
    private final ObjectMapper objectMapper;

    @ShellMethod(value = "[modId] [fileId] Get mod file info by id")
    public void curseForgeGetModFile(@ShellOption Long modId, @ShellOption Long fileId) throws Exception {
        var e = api.fetchModFileById(modId, fileId);
        log.info("Response: {}", objectMapper.writeValueAsString(e));
    }
}
