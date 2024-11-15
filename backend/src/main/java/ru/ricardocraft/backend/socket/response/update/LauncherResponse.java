package ru.ricardocraft.backend.socket.response.update;

import ru.ricardocraft.backend.socket.response.SimpleResponse;
import ru.ricardocraft.backend.utils.Version;

public class LauncherResponse extends SimpleResponse {
    public Version version;
    public String hash;
    public byte[] digest;
    public int launcher_type;

    public String secureHash;
    public String secureSalt;

    @Override
    public String getType() {
        return "launcher";
    }

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }

}
