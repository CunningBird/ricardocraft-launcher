package ru.ricardocraft.client.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.client.runtime.utils.HWIDProvider;
import ru.ricardocraft.client.utils.command.Command;
import ru.ricardocraft.client.utils.command.CommandHandler;
import ru.ricardocraft.client.utils.helper.LogHelper;

@Component
public class PrintHardwareInfoCommand extends Command {

    @Autowired
    public PrintHardwareInfoCommand(CommandHandler commandHandler) {
        commandHandler.registerCommand("printhardware", this);
    }

    @Override
    public String getArgsDescription() {
        return "[]";
    }

    @Override
    public String getUsageDescription() {
        return "print your hardware info and timings";
    }

    @Override
    public void invoke(String... args) {
        LogHelper.info("Your Hardware ID:");
        long startTime = System.currentTimeMillis();
        long currentTime;
        HWIDProvider provider = new HWIDProvider();
        currentTime = System.currentTimeMillis();
        LogHelper.info("Create HWIDProvider instance: %d ms", currentTime - startTime);
        startTime = System.currentTimeMillis();
        int bitness = provider.getBitness();
        long totalMemory = provider.getTotalMemory();
        boolean isBattery = provider.isBattery();
        currentTime = System.currentTimeMillis();
        LogHelper.info("Bitness: %d, totalMemory: %d(%.3f GB), battery %s, TIME: %d ms", bitness, totalMemory, (double) totalMemory / (1024.0 * 1024.0 * 1024.0), Boolean.toString(isBattery), currentTime - startTime);
        startTime = System.currentTimeMillis();
        int logicalProcessors = provider.getProcessorLogicalCount();
        int physicalProcessors = provider.getProcessorPhysicalCount();
        long processorMaxFreq = provider.getProcessorMaxFreq();
        currentTime = System.currentTimeMillis();
        LogHelper.info("Processors || logical: %d physical %d freq %d, TIME: %d ms", logicalProcessors, physicalProcessors, processorMaxFreq, currentTime - startTime);
        startTime = System.currentTimeMillis();
        String hwDiskID = provider.getHWDiskID();
        currentTime = System.currentTimeMillis();
        LogHelper.info("HWDiskID %s, TIME: %d ms", hwDiskID, currentTime - startTime);
        startTime = System.currentTimeMillis();
        String baseboardSerial = provider.getBaseboardSerialNumber();
        currentTime = System.currentTimeMillis();
        LogHelper.info("BaseboardSerial %s, TIME: %d ms", baseboardSerial, currentTime - startTime);
        startTime = System.currentTimeMillis();
        String graphicCardName = provider.getGraphicCardName();
        long graphicCardVRam = provider.getGraphicCardMemory();
        currentTime = System.currentTimeMillis();
        LogHelper.info("GraphicCard %s (%.3f vram), TIME: %d ms", graphicCardName, (double) graphicCardVRam, currentTime - startTime);
        LogHelper.info("Hardware ID end");
    }
}
