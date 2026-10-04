package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Locs;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** /search — find containers holding a material within a radius. */
public final class SearchCommand extends ForgeCommand {
    public SearchCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "search";
    }

    @Override
    public String description() {
        return "Find containers holding a material nearby.";
    }

    @Override
    public String usage() {
        return "/search <material> [radius]";
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
        List<Location> hits = new ArrayList<>();
        int checked = 0;
        outer:
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int y = Math.max(cy - radius, world.getMinHeight());
                    y <= Math.min(cy + radius, world.getMaxHeight() - 1); y++) {
                for (int z = cz - radius; z <= cz + radius; z++) {
                    if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                        continue;
                    }
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getState() instanceof Container container) {
                        checked++;
                        if (holds(container, material)) {
                            hits.add(block.getLocation());
                            if (hits.size() >= 10) {
                                break outer;
                            }
                        }
                    }
                }
            }
        }
        String pretty = material.name().toLowerCase(Locale.ROOT);
        if (hits.isEmpty()) {
            Text.send(sender, "Checked <white>" + checked + "</white> containers — none hold <white>"
                    + pretty + "</white> within <white>" + radius + "</white> blocks.");
            return;
        }
        Text.ok(sender, "<white>" + hits.size() + (hits.size() >= 10 ? "+" : "")
                + "</white> containers hold " + pretty + ":");
        for (Location location : hits) {
            Text.send(sender, "  <gray>•</gray> <white>" + Text.escape(Locs.pretty(location)) + "</white>");
        }
    }

    private static boolean holds(Container container, Material material) {
        for (ItemStack item : container.getInventory().getStorageContents()) {
            if (item != null && item.getType() == material) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return Players.filter(AdminUtil.materialNames(), args);
        }
        return List.of();
    }
}
