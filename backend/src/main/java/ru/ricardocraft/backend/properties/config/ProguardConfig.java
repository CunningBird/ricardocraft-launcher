package ru.ricardocraft.backend.properties.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.base.helper.SecurityHelper;
import ru.ricardocraft.backend.base.helper.UnpackHelper;
import ru.ricardocraft.backend.manangers.DirectoriesManager;
import ru.ricardocraft.backend.properties.LaunchServerProperties;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

@Component
public class ProguardConfig {

    private transient final Logger logger = LogManager.getLogger(ProguardConfig.class);

    public final char[] chars = "1aAbBcC2dDeEfF3gGhHiI4jJkKlL5mMnNoO6pPqQrR7sStT8uUvV9wWxX0yYzZ".toCharArray();

    public final Path proguard;
    public final Path config;
    public final Path mappings;
    public final Path words;

    private transient final LaunchServerProperties launchServerConfig;
    @Autowired
    public ProguardConfig(LaunchServerProperties launchServerConfig, DirectoriesManager directoriesManager) {
        this.proguard = directoriesManager.getProguard();
        config = proguard.resolve("proguard.config");
        mappings = proguard.resolve("mappings.pro");
        words = proguard.resolve("random.pro");
        this.launchServerConfig = launchServerConfig;
    }

    public void prepare(boolean force) {
        try {
            IOHelper.createParentDirs(config);
            genWords(force);
            genConfig(force);
        } catch (IOException e) {
            logger.error(e);
        }
    }

    private void genConfig(boolean force) throws IOException {
        if (IOHelper.exists(config) && !force) return;
        Files.deleteIfExists(config);
        UnpackHelper.unpack(IOHelper.getResourceURL("pro/gravit/launchserver/defaults/proguard.cfg"), config);
    }

    public void genWords(boolean force) throws IOException {
        if (IOHelper.exists(words) && !force) return;
        Files.deleteIfExists(words);
        SecureRandom rand = SecurityHelper.newRandom();
        rand.setSeed(SecureRandom.getSeed(32));
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(IOHelper.newOutput(words), IOHelper.UNICODE_CHARSET))) {
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
