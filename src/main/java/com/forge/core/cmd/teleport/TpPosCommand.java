package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Locs;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /tppos <x> <y> <z> [world] — teleport to coordinates (~ for relative). */
public final class TpPosCommand extends TeleportCommand {
    public TpPosCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "tppos";
    }

    @Override
    public String description() {
        return "Teleport to coordinates (use ~ for relative).";
    }

    @Override
    public String usage() {
        return "/tppos <x> <y> <z> [world]";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = requirePlayer(sender);
        if (args.length < 3) {
            Text.usage(sender, usage());
            return;
        }
        World world = player.getWorld();
        if (args.length >= 4) {
            World named = Bukkit.getWorld(args[3]);
            if (named == null) {
                throw fail("World <white>" + Text.escape(args[3]) + "</white> does not exist.");
            }
            world = named;
        }
        Location base = player.getLocation();
        double x;
        double y;
        double z;
        try {
            x = parseCoord(args[0], base.getX());
            y = parseCoord(args[1], base.getY());
            z = parseCoord(args[2], base.getZ());
        } catch (NumberFormatException exception) {
            throw fail("Coordinates must be numbers (prefix with ~ for relative).");
        }
        Location target = new Location(world, x, y, z, base.getYaw(), base.getPitch());
        teleport(player, target, "<gray>Teleported to <white>" + Text.escape(Locs.pretty(target)) + "</white>.");
    }

    private static double parseCoord(String arg, double base) {
        String text = arg.toLowerCase(Locale.ROOT);
        if (text.startsWith("~")) {
            return text.length() == 1 ? base : base + Double.parseDouble(text.substring(1));
        }
        return Double.parseDouble(text);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
