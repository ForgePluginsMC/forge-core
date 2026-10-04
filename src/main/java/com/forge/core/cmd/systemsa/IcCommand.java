package com.forge.core.cmd.systemsa;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.systemsa.ic.InteractiveManager;
import com.forge.core.cmd.systemsa.ic.InteractiveManager.Interactive;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Locs;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * Interactive commands: bind command lists to blocks or entities;
 * right-clicking them runs the commands.
 *
 * <p>Usage: /ic &lt;create|delete|list|addcmd|delcmd|info&gt; &lt;name&gt; [args...]
 */
public final class IcCommand extends ForgeCommand {
    private static final List<String> SUBS = List.of(
            "create", "delete", "list", "addcmd", "delcmd", "info");

    public IcCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "ic";
    }

    @Override
    public String description() {
        return "Bind clickable commands to blocks or entities.";
    }

    @Override
    public String usage() {
        return "/ic <create|delete|list|addcmd|delcmd|info> <name> [args...]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            throw new CommandRegistry.CommandFailure("Missing arguments.");
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        InteractiveManager manager = InteractiveManager.get();
        switch (sub) {
            case "list" -> {
                List<String> names = manager.names();
                if (names.isEmpty()) {
                    Text.send(sender, "No interactive commands defined.");
                } else {
                    Text.send(sender, "Interactive: <white>" + Text.escape(String.join(", ", names)) + "</white>");
                }
            }
            case "create" -> {
                Player player = requirePlayer(sender);
                String name = arg(args, 1, "binding name");
                Block target = player.getTargetBlockExact(5);
                if (target != null && !target.getType().isAir()) {
                    if (!manager.createBlock(name, target.getLocation())) {
                        throw new CommandRegistry.CommandFailure(
                                "A binding named '" + Text.escape(name) + "' already exists.");
                    }
                    Text.ok(sender, "Interactive <white>" + Text.escape(name)
                            + "</white> bound to the targeted block.");
                } else {
                    Entity nearest = nearestEntity(player);
                    if (nearest == null) {
                        throw new CommandRegistry.CommandFailure(
                                "Look at a block or stand near an entity (within 5 blocks).");
                    }
                    if (!manager.createEntity(name, nearest.getUniqueId())) {
                        throw new CommandRegistry.CommandFailure(
                                "A binding named '" + Text.escape(name) + "' already exists.");
                    }
                    Text.ok(sender, "Interactive <white>" + Text.escape(name)
                            + "</white> bound to <white>"
                            + Text.escape(nearest.getType().name().toLowerCase(Locale.ROOT).replace('_', ' '))
                            + "</white>.");
                }
            }
            case "delete" -> {
                String name = arg(args, 1, "binding name");
                if (!manager.delete(name)) {
                    throw new CommandRegistry.CommandFailure("No binding named '" + Text.escape(name) + "'.");
                }
                Text.ok(sender, "Interactive <white>" + Text.escape(name) + "</white> deleted.");
            }
            case "info" -> info(sender, existing(manager, arg(args, 1, "binding name")));
            case "addcmd" -> {
                Interactive interactive = existing(manager, arg(args, 1, "binding name"));
                if (args.length < 3) {
                    throw new CommandRegistry.CommandFailure("Missing arguments.", "/ic addcmd <name> <command...>");
                }
                String command = join(args, 2);
                interactive.commands.add(command);
                manager.save();
                Text.ok(sender, "Command added to <white>" + Text.escape(interactive.name)
                        + "</white> (<white>" + interactive.commands.size() + "</white> total).");
            }
            case "delcmd" -> {
                Interactive interactive = existing(manager, arg(args, 1, "binding name"));
                int index = parseIndex(args, 2, interactive.commands.size());
                String removed = interactive.commands.remove(index);
                manager.save();
                Text.ok(sender, "Removed <white>" + Text.escape(removed) + "</white>.");
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

    private static Interactive existing(InteractiveManager manager, String name) {
        Interactive interactive = manager.get(name);
        if (interactive == null) {
            throw new CommandRegistry.CommandFailure("No binding named '" + Text.escape(name) + "'.");
        }
        return interactive;
    }

    private static int parseIndex(String[] args, int index, int size) {
        if (args.length <= index) {
            throw new CommandRegistry.CommandFailure("Missing command number.");
        }
        int number;
        try {
            number = Integer.parseInt(args[index]) - 1;
        } catch (NumberFormatException exception) {
            throw new CommandRegistry.CommandFailure("Not a number: '" + Text.escape(args[index]) + "'.");
        }
        if (number < 0 || number >= size) {
            throw new CommandRegistry.CommandFailure("Number out of range (1-" + size + ").");
        }
        return number;
    }

    private static void info(CommandSender sender, Interactive interactive) {
        Text.send(sender, "<gold>Interactive <white>" + Text.escape(interactive.name) + "</white>:");
        if (interactive.block && interactive.blockLocation != null) {
            Text.send(sender, "  Bound to block at <white>" + Text.escape(Locs.pretty(interactive.blockLocation))
                    + "</white>");
        } else {
            Text.send(sender, "  Bound to entity <white>" + interactive.entityId + "</white>");
        }
        if (interactive.commands.isEmpty()) {
            Text.send(sender, "  Commands: <gray>none</gray>");
        } else {
            for (int i = 0; i < interactive.commands.size(); i++) {
                Text.send(sender, "  <white>" + (i + 1) + ".</white> <gray>"
                        + Text.escape(interactive.commands.get(i)) + "</gray>");
            }
        }
    }

    private static @Nullable Entity nearestEntity(Player player) {
        Entity best = null;
        double bestDistance = 25.0;
        for (Entity entity : player.getNearbyEntities(5.0, 5.0, 5.0)) {
            if (entity.equals(player)) {
                continue;
            }
            double distance = entity.getLocation().distanceSquared(player.getLocation());
            if (distance < bestDistance) {
                bestDistance = distance;
                best = entity;
            }
        }
        return best;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(new ArrayList<>(SUBS), args);
        }
        if (args.length == 2 && !"list".equalsIgnoreCase(args[0]) && !"create".equalsIgnoreCase(args[0])) {
            return Players.filter(InteractiveManager.get().names(), args);
        }
        return List.of();
    }
}
