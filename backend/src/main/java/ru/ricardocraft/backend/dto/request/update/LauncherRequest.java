package ru.ricardocraft.backend.dto.request.update;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ru.ricardocraft.backend.base.Version;
import ru.ricardocraft.backend.dto.request.AbstractRequest;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LauncherRequest extends AbstractRequest {
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
