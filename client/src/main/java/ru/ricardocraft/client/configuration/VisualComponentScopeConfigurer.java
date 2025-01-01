//package ru.ricardocraft.client.configuration;
//
//import org.springframework.beans.factory.ObjectFactory;
//import org.springframework.beans.factory.config.Scope;
//import org.springframework.stereotype.Component;
//import ru.ricardocraft.client.JavaFXApplication;
//import ru.ricardocraft.client.config.LauncherConfig;
//import ru.ricardocraft.client.impl.AbstractVisualComponent;
//import ru.ricardocraft.client.impl.BackgroundComponent;
//import ru.ricardocraft.client.impl.ContextHelper;
//import ru.ricardocraft.client.runtime.managers.SettingsManager;
//
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Map;
//
//import static ru.ricardocraft.client.helper.EnFSHelper.resetDirectory;
//
//@Component
//public class VisualComponentScopeConfigurer implements Scope {
//
//    private final JavaFXApplication application = JavaFXApplication.getInstance();
//    private final Map<String, Object> components = new HashMap<>();
//
//    private final LauncherConfig config;
//    private final SettingsManager settingsManager;
//
//    public VisualComponentScopeConfigurer(LauncherConfig config, SettingsManager settingsManager) {
//        this.config = config;
//        this.settingsManager = settingsManager;
//    }
//
//    @Override
//    public Object get(String name, ObjectFactory<?> objectFactory) {
//        return components.get(name);
//    }
//
//    @Override
//    public Object remove(String name) {
//        return null;
//    }
//
//    @Override
//    public void registerDestructionCallback(String name, Runnable callback) {
//
//    }
//
//    @Override
//    public Object resolveContextualObject(String key) {
//        return null;
//    }
//
//    @Override
//    public String getConversationId() {
//        return "";
//    }
//
//    public void registerComponent(AbstractVisualComponent component) {
//        components.put(component.getName(), component);
//    }
//
//
//    public Collection<Object> getComponents() {
//        return components.values();
//    }
//
//    public void reload() throws Exception {
//        String sceneName = application.gui.getCurrentScene().getName();
//        ContextHelper.runInFxThreadStatic(() -> {
//            application.getMainStage().setScene(null, false);
//            BackgroundComponent backgroundComponent = (BackgroundComponent) getByName("background");
//            application.getMainStage().pullBackground(backgroundComponent);
//            resetDirectory(config, settingsManager.getRuntimeSettings());
//            components.clear();
//            application.getMainStage().resetStyles();
//            init();
//            application.getMainStage().pushBackground(backgroundComponent);
//            for (AbstractVisualComponent s : components.values()) {
//                if (sceneName.equals(s.getName())) {
//                    application.getMainStage().setScene(s, false);
//                }
//            }
//        }).get();
//    }
//}
