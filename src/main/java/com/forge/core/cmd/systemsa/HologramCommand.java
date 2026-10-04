package com.forge.core.cmd.systemsa;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.systemsa.hologram.HologramManager;
import com.forge.core.cmd.systemsa.hologram.HologramManager.Hologram;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Floating text holograms with MiniMessage and placeholder support.
 *
 * <p>Usage: /hologram &lt;create|delete|addline|setline|removeline|list|movehere&gt; &lt;name&gt; [text...]
 */
public final class HologramCommand extends ForgeCommand {
    private static final List<String> SUBS = List.of(
            "create", "delete", "addline", "setline", "removeline", "list", "movehere");

    public HologramCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "hologram";
    }

    @Override
    public String description() {
        return "Create and manage floating text holograms.";
    }

    @Override
    public String usage() {
        return "/hologram <create|delete|addline|setline|removeline|list|movehere> <name> [text...]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            throw new CommandRegistry.CommandFailure("Missing arguments.");
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        HologramManager manager = HologramManager.get();
        switch (sub) {
            case "list" -> {
                List<String> names = manager.names();
                if (names.isEmpty()) {
                    Text.send(sender, "No holograms defined.");
                } else {
                    Text.send(sender, "Holograms: <white>" + Text.escape(String.join(", ", names)) + "</white>");
                }
            }
            case "create" -> {
                Player player = requirePlayer(sender);
                String name = arg(args, 1, "hologram name");
                String firstLine = args.length > 2 ? join(args, 2) : null;
                if (!manager.create(name, player.getLocation().clone().add(0.0, 2.0, 0.0), firstLine)) {
                    throw new CommandRegistry.CommandFailure(
                            "A hologram named '" + Text.escape(name) + "' already exists.");
                }
                Text.ok(sender, "Hologram <white>" + Text.escape(name) + "</white> created.");
            }
            case "delete" -> {
                String name = arg(args, 1, "hologram name");
                if (!manager.delete(name)) {
                    throw new CommandRegistry.CommandFailure("No hologram named '" + Text.escape(name) + "'.");
                }
                Text.ok(sender, "Hologram <white>" + Text.escape(name) + "</white> deleted.");
            }
            case "addline" -> {
                Hologram hologram = existing(manager, arg(args, 1, "hologram name"));
                if (args.length < 3) {
                    throw new CommandRegistry.CommandFailure("Missing arguments.", "/hologram addline <name> <text...>");
                }
                manager.addLine(hologram, join(args, 2));
                Text.ok(sender, "Line added to <white>" + Text.escape(hologram.name) + "</white>.");
            }
            case "setline" -> {
                Hologram hologram = existing(manager, arg(args, 1, "hologram name"));
                int index = parseIndex(args, 2, hologram.lines.size());
                if (args.length < 4) {
                    throw new CommandRegistry.CommandFailure("Missing arguments.", "/hologram setline <name> <line#> <text...>");
                }
                manager.setLine(hologram, index, join(args, 3));
                Text.ok(sender, "Line <white>" + (index + 1) + "</white> updated.");
            }
            case "removeline" -> {
                Hologram hologram = existing(manager, arg(args, 1, "hologram name"));
                int index = parseIndex(args, 2, hologram.lines.size());
                manager.removeLine(hologram, index);
                Text.ok(sender, "Line <white>" + (index + 1) + "</white> removed.");
            }
            case "movehere" -> {
                Player player = requirePlayer(sender);
                Hologram hologram = existing(manager, arg(args, 1, "hologram name"));
                manager.moveHere(hologram, player.getLocation().clone().add(0.0, 2.0, 0.0));
                Text.ok(sender, "Hologram <white>" + Text.escape(hologram.name) + "</white> moved to you.");
            }
            default -> throw new CommandRegistry.CommandFailure("Unknown subcommand. Usage: " + usage());
        }
    }

    private static Player requirePlayer(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            throw new CommandRegistry.CommandFailure("Only players can use that.");
        }
        return player;
    }

    private static String arg(String[] args, int index, String what) {
        if (args.length <= index) {
            throw new CommandRegistry.CommandFailure("Missing " + what + ".");
        }
        return args[index];
    }

    private static String join(String[] args, int from) {
        return String.join(" ", Arrays.copyOfRange(args, from, args.length));
    }

    private static Hologram existing(HologramManager manager, String name) {
        Hologram hologram = manager.get(name);
        if (hologram == null) {
            throw new CommandRegistry.CommandFailure("No hologram named '" + Text.escape(name) + "'.");
        }
        return hologram;
    }

    private static int parseIndex(String[] args, int index, int size) {
        if (args.length <= index) {
            throw new CommandRegistry.CommandFailure("Missing line number.");
        }
        int number;
        try {
            number = Integer.parseInt(args[index]) - 1;
        } catch (NumberFormatException exception) {
            throw new CommandRegistry.CommandFailure("Not a number: '" + Text.escape(args[index]) + "'.");
        }
        if (number < 0 || number >= size) {
            throw new CommandRegistry.CommandFailure("Line number out of range (1-" + size + ").");
        }
        return number;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(new ArrayList<>(SUBS), args);
        }
        if (args.length == 2 && !"list".equalsIgnoreCase(args[0]) && !"create".equalsIgnoreCase(args[0])) {
            return Players.filter(HologramManager.get().names(), args);
        }
        if (args.length == 3 && ("setline".equalsIgnoreCase(args[0]) || "removeline".equalsIgnoreCase(args[0]))) {
            Hologram hologram = HologramManager.get().get(args[1]);
            if (hologram != null) {
                List<String> numbers = new ArrayList<>();
                for (int i = 1; i <= hologram.lines.size(); i++) {
                    numbers.add(String.valueOf(i));
                }
                return Players.filter(numbers, args);
            }
        }
        return List.of();
    }
}
