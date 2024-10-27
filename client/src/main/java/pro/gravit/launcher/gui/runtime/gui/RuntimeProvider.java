package pro.gravit.launcher.gui.runtime.gui;

public interface RuntimeProvider {
    void run(String[] args);

    void preLoad();

    void init(boolean clientInstance);
}
