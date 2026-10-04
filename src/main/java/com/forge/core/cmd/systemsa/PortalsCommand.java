package com.forge.core.cmd.systemsa;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.systemsa.portal.PortalManager;
import com.forge.core.cmd.systemsa.portal.PortalManager.Portal;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Locs;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Portal pads with teleport destinations, console commands, BungeeCord
 * server hops and custom particles.
 *
 * <p>Usage: /portals &lt;create|delete|list|info|setdest|addcmd|delcmd|setparticle|setserver&gt; &lt;name&gt; [args...]
 */
public final class PortalsCommand extends ForgeCommand {
    private static final List<String> SUBS = List.of(
            "create", "delete", "list", "info", "setdest",
            "addcmd", "delcmd", "setparticle", "setserver");

    public PortalsCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "portals";
    }

    @Override
    public String description() {
        return "Create and manage portal pads with destinations, commands and particles.";
    }

    @Override
    public String usage() {
        return "/portals <create|delete|list|info|setdest|addcmd|delcmd|setparticle|setserver> <name> [args...]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            throw new CommandRegistry.CommandFailure("Usage: " + usage());
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        PortalManager manager = PortalManager.get();
        switch (sub) {
            case "list" -> {
                List<String> names = manager.names();
                if (names.isEmpty()) {
                    Text.send(sender, "No portals defined.");
                } else {
                    Text.send(sender, "Portals: <white>" + Text.escape(String.join(", ", names)) + "</white>");
                }
            }
            case "create" -> {
                Player player = requirePlayer(sender);
                String name = arg(args, 1, "portal name");
                if (!manager.create(name, player.getLocation().getBlock().getLocation())) {
                    throw new CommandRegistry.CommandFailure("A portal named '" + Text.escape(name) + "' already exists.");
                }
                Text.ok(sender, "Portal <white>" + Text.escape(name) + "</white> created at your feet. "
                        + "Set a destination with <white>/portals setdest " + Text.escape(name) + "</white>.");
            }
            case "delete" -> {
                String name = arg(args, 1, "portal name");
                if (!manager.delete(name)) {
                    throw new CommandRegistry.CommandFailure("No portal named '" + Text.escape(name) + "'.");
                }
                Text.ok(sender, "Portal <white>" + Text.escape(name) + "</white> deleted.");
            }
            case "info" -> {
                Portal portal = existing(manager, arg(args, 1, "portal name"));
                info(sender, portal);
            }
            case "setdest" -> {
                Player player = requirePlayer(sender);
                Portal portal = existing(manager, arg(args, 1, "portal name"));
                portal.destination = player.getLocation().clone();
                manager.save();
                Text.ok(sender, "Destination of <white>" + Text.escape(portal.name)
                        + "</white> set to your location.");
            }
            case "addcmd" -> {
                Portal portal = existing(manager, arg(args, 1, "portal name"));
                if (args.length < 3) {
                    throw new CommandRegistry.CommandFailure("Usage: /portals addcmd <name> <command...>");
                }
                String command = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                portal.commands.add(command);
                manager.save();
                Text.ok(sender, "Command added to <white>" + Text.escape(portal.name)
                        + "</white> (<white>" + portal.commands.size() + "</white> total).");
            }
            case "delcmd" -> {
                Portal portal = existing(manager, arg(args, 1, "portal name"));
                int index = parseIndex(args, 2, portal.commands.size());
                String removed = portal.commands.remove(index);
                manager.save();
                Text.ok(sender, "Removed command <white>" + Text.escape(removed)
                        + "</white> from <white>" + Text.escape(portal.name) + "</white>.");
            }
            case "setparticle" -> {
                Portal portal = existing(manager, arg(args, 1, "portal name"));
                String particleName = arg(args, 2, "particle name").toUpperCase(Locale.ROOT);
                try {
                    Particle.valueOf(particleName);
                } catch (IllegalArgumentException exception) {
                    throw new CommandRegistry.CommandFailure("Unknown particle '" + Text.escape(particleName) + "'.");
                }
                portal.particleName = particleName;
                portal.resolveParticle();
                manager.save();
                Text.ok(sender, "Particle for <white>" + Text.escape(portal.name)
                        + "</white> set to <white>" + Text.escape(particleName) + "</white>.");
            }
            case "setserver" -> {
                Portal portal = existing(manager, arg(args, 1, "portal name"));
                String server = arg(args, 2, "server name");
                portal.server = server;
                manager.save();
                Text.ok(sender, "BungeeCord server for <white>" + Text.escape(portal.name)
                        + "</white> set to <white>" + Text.escape(server) + "</white>.");
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

    private static Portal existing(PortalManager manager, String name) {
        Portal portal = manager.get(name);
        if (portal == null) {
            throw new CommandRegistry.CommandFailure("No portal named '" + Text.escape(name) + "'.");
        }
        return portal;
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

    private static void info(CommandSender sender, Portal portal) {
        Text.send(sender, "<gold>Portal <white>" + Text.escape(portal.name) + "</white>:");
        Text.send(sender, "  Center: <white>" + Text.escape(Locs.pretty(portal.center)) + "</white>");
        Text.send(sender, "  Destination: <white>"
                + (portal.destination == null ? "none" : Text.escape(Locs.pretty(portal.destination))) + "</white>");
        Text.send(sender, "  BungeeCord server: <white>"
                + Text.escape(portal.server == null ? "none" : portal.server) + "</white>");
        Text.send(sender, "  Particle: <white>" + Text.escape(portal.particleName) + "</white>");
        if (portal.commands.isEmpty()) {
            Text.send(sender, "  Commands: <gray>none</gray>");
        } else {
            for (int i = 0; i < portal.commands.size(); i++) {
                Text.send(sender, "  <white>" + (i + 1) + ".</white> <gray>"
                        + Text.escape(portal.commands.get(i)) + "</gray>");
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(new ArrayList<>(SUBS), args);
        }
        if (args.length == 2 && !"list".equalsIgnoreCase(args[0]) && !"create".equalsIgnoreCase(args[0])) {
            return Players.filter(PortalManager.get().names(), args);
        }
        return List.of();
    }
}
