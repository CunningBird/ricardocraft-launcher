package ru.ricardocraft.client.dto.request.update;

import ru.ricardocraft.client.config.LauncherConfig;
import ru.ricardocraft.client.core.Launcher;
import ru.ricardocraft.client.dto.request.Request;
import ru.ricardocraft.client.dto.request.RequestService;
import ru.ricardocraft.client.websockets.WebSocketRequest;
import ru.ricardocraft.client.dto.response.LauncherRequestEvent;
import ru.ricardocraft.client.helper.IOHelper;
import ru.ricardocraft.client.helper.LogHelper;
import ru.ricardocraft.client.helper.SecurityHelper;

import java.io.IOException;
import java.nio.file.Path;

public final class LauncherRequest extends Request<LauncherRequestEvent> implements WebSocketRequest {
    public static final Path BINARY_PATH = IOHelper.getCodeSource(Launcher.class);
    public static final boolean EXE_BINARY = IOHelper.hasExtension(BINARY_PATH, "exe");

    public final String secureHash;
    public final String secureSalt;
    public byte[] digest;
    public int launcher_type = EXE_BINARY ? 2 : 1;


    public LauncherRequest(LauncherConfig config) {
        Path launcherPath = IOHelper.getCodeSource(LauncherRequest.class);
        try {
            digest = SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA512, launcherPath);
        } catch (IOException e) {
            LogHelper.error(e);
        }
        secureHash = config.secureCheckHash;
        secureSalt = config.secureCheckSalt;
    }

    @Override
    public LauncherRequestEvent requestDo(RequestService service) throws Exception {
        return super.request(service);
    }

    @Override
    public String getType() {
        return "launcher";
    }
}
