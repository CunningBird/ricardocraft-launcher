package ru.ricardocraft.backend.dto.socket.update;

import ru.ricardocraft.backend.base.Version;
import ru.ricardocraft.backend.dto.socket.SimpleResponse;

public class LauncherResponse extends SimpleResponse {
    public Version version;
    public String hash;
    public byte[] digest;
    public int launcher_type;

    public String secureHash;
    public String secureSalt;

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }

}
