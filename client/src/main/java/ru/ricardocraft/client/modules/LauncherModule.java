package ru.ricardocraft.client.modules;

import ru.ricardocraft.client.core.LauncherTrustManager;

import java.util.ArrayList;
import java.util.List;

public abstract class LauncherModule {
    protected final LauncherModuleInfo moduleInfo;
    private final List<EventEntity<? extends Event>> eventList = new ArrayList<>(4);
    protected LauncherModulesManager modulesManager;
    protected ModulesConfigManager modulesConfigManager;
    protected InitStatus initStatus = InitStatus.CREATED;
    private LauncherModulesContext context;
    private LauncherTrustManager.CheckClassResult checkResult;

    protected LauncherModule() {
        moduleInfo = new LauncherModuleInfo("UnknownModule");
    }

    protected LauncherModule(LauncherModuleInfo info) {
        moduleInfo = info;
    }

    public final LauncherModuleInfo getModuleInfo() {
        return moduleInfo;
    }

    public InitStatus getInitStatus() {
        return initStatus;
    }

    public void setInitStatus(InitStatus initStatus) {
        this.initStatus = initStatus;
    }

    /**
     * The internal method used by the ModuleManager
     * DO NOT TOUCH
     *
     * @param context Private context
     */
    public final void setContext(LauncherModulesContext context) {
        if (this.context != null) throw new IllegalStateException("Module already set context");
        this.context = context;
        this.modulesManager = context.getModulesManager();
        this.modulesConfigManager = context.getModulesConfigManager();
        this.setInitStatus(InitStatus.PRE_INIT_WAIT);
    }

    public final LauncherTrustManager.CheckClassResultType getCheckStatus() {
        if (this.checkResult == null) return null;
        return this.checkResult.type;
    }

    public final LauncherTrustManager.CheckClassResult getCheckResult() {
        if (this.checkResult == null) return null;
        return new LauncherTrustManager.CheckClassResult(this.checkResult);
    }

    /**
     * The internal method used by the ModuleManager
     * DO NOT TOUCH
     *
     * @param result Check result
     */
    public final void setCheckResult(LauncherTrustManager.CheckClassResult result) {
        if (this.checkResult != null) throw new IllegalStateException("Module already set check result");
        this.checkResult = result;
    }

    /**
     * This method is called before initializing all modules and resolving dependencies.
     * <b>You can</b>:
     * - Use to Module Manager
     * - Add custom modules not described in the manifest
     * - Change information about your module or modules you control
     * <b>You can not</b>:
     * - Use your dependencies
     * - Use Launcher, LaunchServer, ServerWrapper API
     * - Change the names of any modules
     */
    public void preInitAction() {
        //NOP
    }

    public final void preInit() {
        if (!initStatus.equals(InitStatus.PRE_INIT_WAIT))
            throw new IllegalStateException("PreInit not allowed in current state");
        initStatus = InitStatus.PRE_INIT;
        preInitAction();
        initStatus = InitStatus.INIT_WAIT;
    }

    /**
     * Basic module initialization method
     * <b>You can</b>:
     * - Subscribe to events
     * - Use your dependencies
     * - Use provided initContext
     * - Receive modules and access the module’s internal methods
     * <b>You can not</b>:
     * - Modify module description, dependencies
     * - Add modules
     * - Read configuration
     *
     * @param initContext <b>null</b> on module initialization during boot or startup
     *                    Not <b>null</b> during module initialization while running
     */
    public abstract void init(LauncherInitContext initContext);

    /**
     * Registers an event handler for the current module
     *
     * @param handle your event handler
     * @param tClass event class
     * @param <T>    event type
     */
    protected <T extends Event> void registerEvent(EventHandler<T> handle, Class<T> tClass) {
        EventEntity<T> eventEntity = new EventEntity<>(handle, tClass);
        eventList.add(eventEntity);
    }

    /**
     * Call the handler of the current module
     *
     * @param event event handled
     * @param <T>   event type
     */
    @SuppressWarnings("unchecked")
    public final <T extends Event> void callEvent(T event) {
        Class<? extends Event> tClass = event.getClass();
        for (EventEntity<? extends Event> entity : eventList) {

            if (entity.clazz.isAssignableFrom(tClass)) {
                //noinspection RedundantCast
                ((EventEntity<T>) entity).handler.event(event);
                if (event.isCancel()) return;
            }
        }
    }

    /**
     * Module initialization status at the current time
     */
    public enum InitStatus {
        /**
         * When creating an object
         */
        CREATED(false),
        /**
         * After setting the context
         */
        PRE_INIT_WAIT(true),
        /**
         * During the pre-initialization phase
         */
        PRE_INIT(false),
        /**
         * Awaiting initialization phase
         */
        INIT_WAIT(true),
        /**
         * During the initialization phase
         */
        INIT(false),
        FINISH(true);

        private final boolean isAvailable;

        InitStatus(boolean b) {
            isAvailable = b;
        }

        public boolean isAvailable() {
            return isAvailable;
        }
    }

    @FunctionalInterface
    public interface EventHandler<T extends Event> {
        void event(T e);
    }

    private final static class EventEntity<T extends Event> {
        final Class<T> clazz;
        final EventHandler<T> handler;

        private EventEntity(EventHandler<T> handler, Class<T> clazz) {
            this.clazz = clazz;
            this.handler = handler;
        }
    }

    public static class Event {
        protected boolean cancel = false;

        public boolean isCancel() {
            return cancel;
        }

    }
}
