package ru.ricardocraft.backend.manangers.mirror;

import lombok.Getter;
import lombok.Setter;
import ru.ricardocraft.backend.base.helper.IOHelper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

@Getter
@Setter
public class Mirror {

    private final String baseUrl;
    private Boolean enabled;

    public Mirror(String url) {
        baseUrl = url;
    }

    private URL formatArgs(String mask, Object... args) throws MalformedURLException {
        Object[] data = Arrays.stream(args).map(e -> IOHelper.urlEncode(e.toString())).toArray();
        return new URL(baseUrl.concat(mask.formatted(data)));
    }

    public URL getURL(String mask, Object... args) throws MalformedURLException {
        return formatArgs(mask, args);
    }
}