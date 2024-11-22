package ru.ricardocraft.backend.manangers.mirror;

import java.util.ArrayList;
import java.util.List;

public class ClientInfo {
    public Downloadable server, client;
    public List<Artifact> libraries = new ArrayList<>();
    public List<Artifact> natives = new ArrayList<>();
}