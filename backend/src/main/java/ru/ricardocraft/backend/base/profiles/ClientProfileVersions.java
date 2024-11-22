package ru.ricardocraft.backend.base.profiles;

import ru.ricardocraft.backend.dto.updates.Version;

public class ClientProfileVersions {
    private ClientProfileVersions() {
        throw new UnsupportedOperationException();
    }
    public static final Version MINECRAFT_1_6_4 = Version.of("1.6.4");
    public static final Version MINECRAFT_1_7_2 = Version.of("1.7.2");
    public static final Version MINECRAFT_1_7_10 = Version.of("1.7.10");
    public static final Version MINECRAFT_1_9_4 = Version.of("1.9.4");
    public static final Version MINECRAFT_1_12_2 = Version.of("1.12.2");

    public static final Version MINECRAFT_1_13 = Version.of("1.13");
    public static final Version MINECRAFT_1_16_5 = Version.of("1.16.5");
    public static final Version MINECRAFT_1_17 = Version.of("1.17");
    public static final Version MINECRAFT_1_18 = Version.of("1.18");
    public static final Version MINECRAFT_1_19 = Version.of("1.19");
    public static final Version MINECRAFT_1_20 = Version.of("1.20");
    public static final Version MINECRAFT_1_20_2 = Version.of("1.20.2");
    public static final Version MINECRAFT_1_20_3 = Version.of("1.20.3");
    public static final Version MINECRAFT_1_20_5 = Version.of("1.20.5");
}
