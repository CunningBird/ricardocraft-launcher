package ru.ricardocraft.client.utils.command;

import org.springframework.stereotype.Component;
import ru.ricardocraft.client.utils.helper.CommonHelper;
import ru.ricardocraft.client.utils.helper.IOHelper;
import ru.ricardocraft.client.utils.helper.JVMHelper;

import java.io.BufferedReader;
import java.io.IOException;

@Component
public class StdCommandHandler extends CommandHandler {

    private final BufferedReader reader = IOHelper.newReader(System.in);

    public StdCommandHandler() {
        Thread thread = CommonHelper.newThread("Launcher Console", true, this);
        thread.start();
    }

    @Override
    public void bell() {
    }

    @Override
    public void clear() throws IOException {
        System.out.flush();
        if (JVMHelper.OS_TYPE == JVMHelper.OS.WINDOWS) {
            try {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } catch (InterruptedException ex) {
                throw new IOException(ex);
            }
        } else {
            System.out.print("\033[H\033[2J");
            System.out.flush();
        }
    }

    @Override
    public String readLine() throws IOException {
        return reader.readLine();
    }
}
