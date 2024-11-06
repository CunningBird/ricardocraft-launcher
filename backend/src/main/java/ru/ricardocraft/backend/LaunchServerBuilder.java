package ru.ricardocraft.backend;

import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.properties.LaunchServerEnv;
import ru.ricardocraft.backend.properties.LaunchServerRuntimeConfig;
import ru.ricardocraft.backend.config.LaunchServerConfigManager;
import ru.ricardocraft.backend.configuration.LaunchServerDirectories;
import ru.ricardocraft.backend.manangers.CertificateManager;
import ru.ricardocraft.backend.manangers.KeyAgreementManager;
import ru.ricardocraft.backend.command.utls.CommandHandler;

import java.nio.file.Path;

public class LaunchServerBuilder {
    private LaunchServerConfig config;
    private LaunchServerRuntimeConfig runtimeConfig;
    private CommandHandler commandHandler;
    private LaunchServerEnv env;
    private LaunchServerDirectories directories = new LaunchServerDirectories();
    private KeyAgreementManager keyAgreementManager;
    private CertificateManager certificateManager;
    private LaunchServerConfigManager launchServerConfigManager;
    private Integer shardId;

    public LaunchServerBuilder setConfig(LaunchServerConfig config) {
        this.config = config;
        return this;
    }

    public LaunchServerBuilder setEnv(LaunchServerEnv env) {
        this.env = env;
        return this;
    }

    public LaunchServerBuilder setRuntimeConfig(LaunchServerRuntimeConfig runtimeConfig) {
        this.runtimeConfig = runtimeConfig;
        return this;
    }

    public LaunchServerBuilder setCommandHandler(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
        return this;
    }

    public LaunchServerBuilder setDirectories(LaunchServerDirectories directories) {
        this.directories = directories;
        return this;
    }

    public LaunchServerBuilder setDir(Path dir) {
        this.directories.dir = dir;
        return this;
    }

    public LaunchServerBuilder setLaunchServerConfigManager(LaunchServerConfigManager launchServerConfigManager) {
        this.launchServerConfigManager = launchServerConfigManager;
        return this;
    }

    public LaunchServer build() throws Exception {
        directories.collect();
        if (launchServerConfigManager == null) throw new Exception("launchServerConfigManager is null");
        if (keyAgreementManager == null) {
            keyAgreementManager = new KeyAgreementManager(directories.keyDirectory);
        }
        if(shardId == null) {
            shardId = Integer.parseInt(System.getProperty("launchserver.shardId", "0"));
        }
        return new LaunchServer(directories, env, config, runtimeConfig, launchServerConfigManager, keyAgreementManager, commandHandler, certificateManager, shardId);
    }

    public LaunchServerBuilder setCertificateManager(CertificateManager certificateManager) {
        this.certificateManager = certificateManager;
        return this;
    }

    public void setKeyAgreementManager(KeyAgreementManager keyAgreementManager) {
        this.keyAgreementManager = keyAgreementManager;
    }
}
