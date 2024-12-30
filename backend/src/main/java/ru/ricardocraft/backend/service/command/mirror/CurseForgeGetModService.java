package ru.ricardocraft.backend.service.command.mirror;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.service.mirror.modapi.CurseforgeAPI;

@Slf4j
@Component
@RequiredArgsConstructor
public class CurseForgeGetModService {

    private final CurseforgeAPI api;
    private final ObjectMapper objectMapper;

    public void curseForgeGetMod(Long modId) throws Exception {
        var e = api.fetchModById(modId);
        log.info("Response: {}", objectMapper.writeValueAsString(e));
    }
}
