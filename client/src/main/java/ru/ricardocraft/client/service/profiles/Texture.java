package ru.ricardocraft.client.service.profiles;

import ru.ricardocraft.client.base.serialize.HOutput;
import ru.ricardocraft.client.base.serialize.stream.StreamObject;
import ru.ricardocraft.client.base.helper.IOHelper;
import ru.ricardocraft.client.base.helper.SecurityHelper;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public final class Texture extends StreamObject {
    private static final SecurityHelper.DigestAlgorithm DIGEST_ALGO = SecurityHelper.DigestAlgorithm.SHA256;

    // Instance

    public final String url;

    public final byte[] digest;

    public final Map<String, String> metadata;


    @Deprecated
    public Texture(String url, byte[] digest) {
        this.url = IOHelper.verifyURL(url);
        this.digest = digest == null ? new byte[0] : digest;
        this.metadata = null;
    }

    public Texture(String url, byte[] digest, Map<String, String> metadata) {
        this.url = url;
        this.digest = digest == null ? new byte[0] : digest;
        this.metadata = metadata;
    }

    @Override
    public void write(HOutput output) throws IOException {
        output.writeASCII(url, 2048);
        output.writeByteArray(digest, -DIGEST_ALGO.bytes);
    }

    @Override
    public String toString() {
        return "Texture{" +
                "url='" + url + '\'' +
                ", digest=" + Arrays.toString(digest) +
                ", metadata=" + metadata +
                '}';
    }
}
