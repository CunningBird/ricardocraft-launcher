package ru.ricardocraft.client.configuration;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VisualComponentScopeConfigurer implements Scope {

    public static final String SCOPE_NAME = "visualComponent";

    public static final Map<String, Object> components = new ConcurrentHashMap<>();

    @Override
    @NonNull
    public Object get(@NonNull String name, @NonNull ObjectFactory<?> objectFactory) {
        if (!components.containsKey(name)) {
            components.put(name, objectFactory.getObject());
        }
        return components.get(name);
    }

    @Override
    public Object remove(@NonNull String name) {
        return null;
    }

    @Override
    public void registerDestructionCallback(@NonNull String name, @NonNull Runnable callback) {

    }

    @Override
    public Object resolveContextualObject(@NonNull String key) {
        return null;
    }

    @Override
    public String getConversationId() {
        return "";
    }
}
