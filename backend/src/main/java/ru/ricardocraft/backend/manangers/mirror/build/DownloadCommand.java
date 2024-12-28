package ru.ricardocraft.backend.manangers.mirror.build;

import lombok.extern.slf4j.Slf4j;
import ru.ricardocraft.backend.socket.Downloader;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class DownloadCommand implements BuildInCommand {

    @Override
    public void run(List<String> args, BuildContext context, Path workdir) throws Exception {
        URI uri = new URI(args.get(0));
        Path target = Path.of(args.get(1));
        log.info("Download {} to {}", uri, target);
        Downloader.downloadFile(uri, target, null).getFuture().get();
    }
}