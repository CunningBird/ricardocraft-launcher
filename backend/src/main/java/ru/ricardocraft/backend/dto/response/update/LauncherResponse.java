package ru.ricardocraft.backend.dto.response.update;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ru.ricardocraft.backend.base.Version;
import ru.ricardocraft.backend.dto.response.SimpleResponse;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LauncherResponse extends SimpleResponse {
    public Version version;
    public String hash;
//    public byte[] digest; TODO return this shit
    public int launcher_type;

    public String secureHash;
    public String secureSalt;

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }

}
