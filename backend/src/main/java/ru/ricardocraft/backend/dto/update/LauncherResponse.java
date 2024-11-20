package ru.ricardocraft.backend.dto.update;

import ru.ricardocraft.backend.base.utils.Version;
import ru.ricardocraft.backend.dto.SimpleResponse;

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
