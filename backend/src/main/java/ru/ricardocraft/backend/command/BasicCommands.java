package ru.ricardocraft.backend.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.ricardocraft.backend.dto.Version;
import ru.ricardocraft.backend.base.helper.JVMHelper;
import ru.ricardocraft.backend.service.command.BasicService;
import ru.ricardocraft.backend.service.command.GenerateCertificateService;

import java.lang.management.RuntimeMXBean;

import static ru.ricardocraft.backend.base.helper.JVMHelper.RUNTIME;

@Slf4j
@ShellComponent("basic")
@RequiredArgsConstructor
public class BasicCommands {

    private final BasicService basicService;
    private final GenerateCertificateService generateCertificateService;

    @ShellMethod("[] Invoke Garbage Collector")
    public void gc() {
        basicService.gc();
    }

    @ShellMethod("[] Print LaunchServer version")
    public void launcherVersion() {
        basicService.launcherVersion();
    }

    @ShellMethod("[] Generate self-signed certificate")
    public void generateCertificate() throws Exception {
        generateCertificateService.generateCertificate();
    }
}
