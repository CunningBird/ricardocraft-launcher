package ru.ricardocraft.backend.service.mirror.build;

import ru.ricardocraft.backend.dto.updates.Version;

import java.nio.file.Path;
import java.util.List;

public class If implements BuildInCommand {

    @Override
    public void run(List<String> args, BuildContext context, Path workdir) {
        int ArgOffset = 1;
        boolean ifValue;
        if (args.get(0).equals("version")) {
            var first = Version.of(args.get(1));
            var op = args.get(2);
            var second = Version.of(args.get(3));
            ArgOffset += 3;
            ifValue = switch (op) {
                case ">" -> first.compareTo(second) > 0;
                case ">=" -> first.compareTo(second) >= 0;
                case "<" -> first.compareTo(second) < 0;
                case "<=" -> first.compareTo(second) <= 0;
                default -> throw new IllegalStateException("Unexpected value: " + op);
            };
        } else {
            throw new UnsupportedOperationException(args.get(0));
        }
        if (ifValue) {
            context.variables.put(args.get(ArgOffset), args.get(ArgOffset + 1));
        } else if (args.size() > ArgOffset + 1) {
            context.variables.put(args.get(ArgOffset), args.get(ArgOffset + 2));
        }
    }
}
