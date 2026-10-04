package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Locs;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/** /rtp [world] — random safe teleport around the world's RTP center. */
public final class RtpCommand extends TeleportCommand {
    public RtpCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "rtp";
    }

    @Override
    public String description() {
        return "Teleport to a random safe location.";
    }

    @Override
    public String usage() {
        return "/rtp [world]";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = requirePlayer(sender);
        World world = player.getWorld();
        if (args.length > 0) {
            World named = Bukkit.getWorld(args[0]);
            if (named == null) {
                throw fail("World <white>" + Text.escape(args[0]) + "</white> does not exist.");
            }
            world = named;
        }
        Location center = center(world);
        long radius = plugin.getConfig().getLong("rtp." + world.getName() + ".radius", 5000);
        if (radius < 16) {
            radius = 16;
        }
        Location safe = findSafe(world, center, radius);
        if (safe == null) {
            throw fail("Could not find a safe spot — try again.");
        }
        teleport(player, safe, "<gray>Teleported to a random location: <white>"
                + Text.escape(Locs.pretty(safe)) + "</white>.");
    }

    /** RTP center from config, falling back to the world spawn. */
    private Location center(World world) {
        Location configured = Locs.deserialize(plugin.getConfig().get("rtp." + world.getName() + ".center"));
        return configured != null ? configured : world.getSpawnLocation();
    }

    /** Try random offsets for a spot whose footing is not lava. */
    private @Nullable Location findSafe(World world, Location center, long radius) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int attempt = 0; attempt < 12; attempt++) {
            int x = center.getBlockX() + (int) (random.nextDouble() * 2 * radius - radius);
            int z = center.getBlockZ() + (int) (random.nextDouble() * 2 * radius - radius);
            x = Math.max(-29999984, Math.min(29999984, x));
            z = Math.max(-29999984, Math.min(29999984, z));
            int y = world.getHighestBlockYAt(x, z);
            if (y <= world.getMinHeight()) {
                continue;
            }
            if (world.getBlockAt(x, y, z).getType() == Material.LAVA) {
                continue;
            }
            Location candidate = new Location(world, x + 0.5, y + 1, z + 0.5);
            if (candidate.getBlock().getType().isAir() || candidate.getBlock().isPassable()) {
                return candidate;
            }
        }
        return null;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> worlds = new ArrayList<>();
            for (World world : Bukkit.getWorlds()) {
                worlds.add(world.getName());
            }
            return Players.filter(worlds, args);
        }
        return List.of();
    }
}
