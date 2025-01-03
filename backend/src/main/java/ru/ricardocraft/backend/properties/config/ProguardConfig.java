package ru.ricardocraft.backend.properties.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.base.helper.SecurityHelper;
import ru.ricardocraft.backend.base.helper.UnpackHelper;
import ru.ricardocraft.backend.service.DirectoriesService;
import ru.ricardocraft.backend.properties.LaunchServerProperties;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.security.SecureRandom;

@Slf4j
@Component
public class ProguardConfig {

    public final char[] chars = "1aAbBcC2dDeEfF3gGhHiI4jJkKlL5mMnNoO6pPqQrR7sStT8uUvV9wWxX0yYzZ".toCharArray();

    private transient final LaunchServerProperties launchServerConfig;
    private transient final DirectoriesService directoriesService;

    @Autowired
    public ProguardConfig(LaunchServerProperties launchServerConfig, DirectoriesService directoriesService) {
        this.launchServerConfig = launchServerConfig;
        this.directoriesService = directoriesService;
    }

    public void prepare(boolean force) {
        try {
            IOHelper.createParentDirs(directoriesService.getProguardConfigFile());
            genWords(force);
            genConfig(force);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void genConfig(boolean force) throws IOException {
        if (IOHelper.exists(directoriesService.getProguardConfigFile()) && !force) return;
        Files.deleteIfExists(directoriesService.getProguardConfigFile());
        UnpackHelper.unpack(ResourceUtils.getFile("classpath:defaults/proguard.cfg").toURL(), directoriesService.getProguardConfigFile());
    }

    public void genWords(boolean force) throws IOException {
        if (IOHelper.exists(directoriesService.getProguardWordsFile()) && !force) return;
        Files.deleteIfExists(directoriesService.getProguardWordsFile());
        SecureRandom rand = SecurityHelper.newRandom();
        rand.setSeed(SecureRandom.getSeed(32));
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(IOHelper.newOutput(directoriesService.getProguardWordsFile()), IOHelper.UNICODE_CHARSET))) {
            String projectName = launchServerConfig.getProjectName().replaceAll("\\W", "");
            String lowName = projectName.toLowerCase();
            String upName = projectName.toUpperCase();
            for (int i = 0; i < Short.MAX_VALUE; i++) out.println(generateString(rand, lowName, upName, 14));
        }
    }

    private String generateString(SecureRandom rand, String lowString, String upString, int il) {
        StringBuilder sb = new StringBuilder(Math.max(il, lowString.length()));
        for (int i = 0; i < lowString.length(); ++i) {
            sb.append(rand.nextBoolean() ? lowString.charAt(i) : upString.charAt(i));
        }
        int toI = il - lowString.length();
        for (int i = 0; i < toI; i++) sb.append(chars[rand.nextInt(chars.length)]);
        return sb.toString();
    }
}
