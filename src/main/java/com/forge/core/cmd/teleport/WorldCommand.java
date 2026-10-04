package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Teleport to another world, keeping your current coordinates. */
public final class WorldCommand extends TeleportCommand {
    public WorldCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "world";
    }

    @Override
    public String description() {
        return "Teleport to another world keeping coordinates.";
    }

    @Override
    public String usage() {
        return "/world <world>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        Player player = requirePlayer(sender);
        World world = Bukkit.getWorld(args[0]);
        if (world == null) {
            // Try case-insensitive match
            for (World w : Bukkit.getWorlds()) {
                if (w.getName().equalsIgnoreCase(args[0])) {
                    world = w;
                    break;
                }
            }
        }
        if (world == null) {
            Text.error(sender, "World <white>" + Text.escape(args[0]) + "</white> not found.");
            return;
        }
        if (world.equals(player.getWorld())) {
            Text.error(sender, "You are already in that world.");
            return;
        }
        Location from = player.getLocation();
        Location to = new Location(world, from.getX(), from.getY(), from.getZ(),
                from.getYaw(), from.getPitch());
        // Clamp Y to world bounds
        int maxY = world.getMaxHeight() - 1;
        int minY = world.getMinHeight();
        if (to.getY() > maxY) {
            to.setY(maxY);
        } else if (to.getY() < minY) {
            to.setY(minY);
        }
        teleport(player, to, "<gray>Teleported to world <white>" + Text.escape(world.getName())
                + "</white> at your coordinates.</gray>");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            String last = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
            return Bukkit.getWorlds().stream()
                    .map(World::getName)
                    .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(last))
                    .toList();
        }
        return List.of();
    }
}
