package ru.ricardocraft.client.launch;

import java.security.ProtectionDomain;

public interface ClassLoaderControl {

    interface ClassTransformer {
        boolean filter(String moduleName, String name);
        byte[] transform(String moduleName, String name, ProtectionDomain protectionDomain, byte[] data);
    }
}
