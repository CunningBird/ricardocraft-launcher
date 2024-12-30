package ru.ricardocraft.backend.service.mirror.build;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class DownloadCommand implements BuildInCommand {

    private final RestTemplate restTemplate;

    @Override
    public void run(List<String> args, BuildContext context, Path workdir) throws Exception {
        URI uri = new URI(args.get(0));
        Path target = Path.of(args.get(1));
        log.info("Download {} to {}", uri, target);

        File ret = new File(String.valueOf(target));
        restTemplate.execute(uri, HttpMethod.GET, null, clientHttpResponse -> {
            StreamUtils.copy(clientHttpResponse.getBody(), new FileOutputStream(ret));
            return ret;
        });
    }
}