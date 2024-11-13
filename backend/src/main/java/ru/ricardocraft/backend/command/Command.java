package ru.ricardocraft.backend.command;

import lombok.NoArgsConstructor;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import ru.ricardocraft.backend.base.Downloader;
import ru.ricardocraft.backend.base.Launcher;
import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.command.utls.CommandException;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

@NoArgsConstructor
public abstract class Command extends ru.ricardocraft.backend.command.utls.Command {

    public Command(Map<String, ru.ricardocraft.backend.command.utls.Command> childCommands) {
        super(childCommands);
    }

    protected ClientProfile.Version parseClientVersion(String arg) throws CommandException {
        if(arg.isEmpty()) {
            throw new CommandException("ClientVersion can't be empty");
        }
        return Launcher.gsonManager.gson.fromJson(arg, ClientProfile.Version.class);
    }

    protected Downloader downloadWithProgressBar(String taskName, List<Downloader.SizedFile> list, String baseUrl, Path targetDir) throws Exception {
        long total = 0;
        for (Downloader.SizedFile file : list) {
            if(file.size < 0) {
                continue;
            }
            total += file.size;
        }
        long totalFiles = list.size();
        AtomicLong current = new AtomicLong(0);
        AtomicLong currentFiles = new AtomicLong(0);
        ProgressBar bar = (new ProgressBarBuilder()).setTaskName(taskName)
                .setInitialMax(total)
                .showSpeed()
                .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BLOCK)
                .setUnit("MB", 1024 * 1024)
                .build();
        bar.setExtraMessage(" [0/%d]".formatted(totalFiles));
        Downloader downloader = Downloader.downloadList(list, baseUrl, targetDir, new Downloader.DownloadCallback() {
            @Override
            public void apply(long fullDiff) {
                current.addAndGet(fullDiff);
                bar.stepBy(fullDiff);
            }

            @Override
            public void onComplete(Path path) {
                bar.setExtraMessage(" [%d/%d]".formatted(currentFiles.incrementAndGet(), totalFiles));
            }
        }, null, 4);
        downloader.getFuture().handle((v, e) -> {
            CompletableFuture<Void> future = new CompletableFuture<>();
            bar.close();
            if (e != null) {
                future.completeExceptionally(e);
            } else {
                future.complete(null);
            }
            return future;
        });
        return downloader;
    }
}
