package ru.ricardocraft.client.launch;

import ru.ricardocraft.client.utils.helper.JavaHelper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientLauncherWrapperContext {
    public JavaHelper.JavaVersion javaVersion;
    public Path executePath;
    public ProcessBuilder processBuilder;
    public List<String> args = new ArrayList<>(8);
    public Map<String, String> jvmProperties = new HashMap<>();
    public List<String> clientArgs = new ArrayList<>();
}