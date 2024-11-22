package ru.ricardocraft.backend.dto.updates;

import java.net.InetSocketAddress;

public class ServerProfile {
    public String name;
    public String serverAddress;
    public int serverPort;
    public boolean isDefault = true;
    public int protocol = -1;
    public boolean socketPing = true;

    public ServerProfile() {
    }

    public ServerProfile(String name, String serverAddress, int serverPort) {
        this.name = name;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public ServerProfile(String name, String serverAddress, int serverPort, boolean isDefault) {
        this.name = name;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.isDefault = isDefault;
    }

    public InetSocketAddress toSocketAddress() {
        return InetSocketAddress.createUnresolved(serverAddress, serverPort);
    }
}