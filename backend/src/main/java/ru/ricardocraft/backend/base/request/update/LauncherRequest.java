package ru.ricardocraft.backend.base.request.update;

import ru.ricardocraft.backend.base.Launcher;
import ru.ricardocraft.backend.base.events.request.LauncherRequestEvent;
import ru.ricardocraft.backend.base.request.Request;
import ru.ricardocraft.backend.base.request.RequestService;
import ru.ricardocraft.backend.base.request.websockets.WebSocketRequest;
import ru.ricardocraft.backend.core.LauncherNetworkAPI;
import ru.ricardocraft.backend.helper.IOHelper;
import ru.ricardocraft.backend.helper.LogHelper;
import ru.ricardocraft.backend.helper.SecurityHelper;

import java.io.IOException;
import java.nio.file.Path;

public final class LauncherRequest extends Request<LauncherRequestEvent> implements WebSocketRequest {
    public static final Path BINARY_PATH = IOHelper.getCodeSource(Launcher.class);
    public static final boolean EXE_BINARY = IOHelper.hasExtension(BINARY_PATH, "exe");
    @LauncherNetworkAPI
    public final String secureHash;
    @LauncherNetworkAPI
    public final String secureSalt;
    @LauncherNetworkAPI
    public byte[] digest;
    @LauncherNetworkAPI
    public int launcher_type = EXE_BINARY ? 2 : 1;


    public LauncherRequest() {
        Path launcherPath = IOHelper.getCodeSource(LauncherRequest.class);
        try {
            digest = SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA512, launcherPath);
        } catch (IOException e) {
            LogHelper.error(e);
        }
        secureHash = Launcher.getConfig().secureCheckHash;
        secureSalt = Launcher.getConfig().secureCheckSalt;
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
