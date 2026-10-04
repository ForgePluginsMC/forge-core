package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Locs;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /scan — count blocks of a material in a radius; report the nearest. */
public final class ScanCommand extends ForgeCommand {
    public ScanCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "scan";
    }

    @Override
    public String description() {
        return "Count blocks of a material nearby; report the nearest.";
    }

    @Override
    public String usage() {
        return "/scan <material> [radius]";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        Material material = AdminUtil.material(args[0]);
        int radius = args.length >= 2 ? AdminUtil.intInRange(args[1], 1, 100, "Radius") : 50;

        Player player = (Player) sender;
        World world = player.getWorld();
        Location origin = player.getLocation();
        int cx = origin.getBlockX();
        int cy = origin.getBlockY();
        int cz = origin.getBlockZ();
        int count = 0;
        Location nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int y = Math.max(cy - radius, world.getMinHeight());
                    y <= Math.min(cy + radius, world.getMaxHeight() - 1); y++) {
                for (int z = cz - radius; z <= cz + radius; z++) {
                    if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                        continue;
                    }
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() == material) {
                        count++;
                        double dist = origin.distanceSquared(block.getLocation());
                        if (dist < nearestDist) {
                            nearestDist = dist;
                            nearest = block.getLocation();
                        }
                    }
                }
            }
        }
        String pretty = material.name().toLowerCase(Locale.ROOT);
        if (count == 0) {
            Text.send(sender, "No <white>" + pretty + "</white> within <white>" + radius + "</white> blocks.");
            return;
        }
        Text.ok(sender, "Found <white>" + count + "</white> " + pretty
                + " within <white>" + radius + "</white> blocks.");
        if (nearest != null) {
            Text.send(sender, "Nearest: <white>" + Text.escape(Locs.pretty(nearest)) + "</white>.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return Players.filter(AdminUtil.materialNames(), args);
        }
        return List.of();
    }
}
