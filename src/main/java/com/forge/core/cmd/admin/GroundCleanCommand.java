package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.Trident;

/** /groundclean — remove dropped items (and stray arrows) in a radius. */
public final class GroundCleanCommand extends ForgeCommand {
    public GroundCleanCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "groundclean";
    }

    @Override
    public List<String> aliases() {
        return List.of("gc");
    }

    @Override
    public String description() {
        return "Remove dropped items and arrows in a radius.";
    }

    @Override
    public String usage() {
        return "/groundclean [radius]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        int radius = args.length >= 1 ? AdminUtil.intInRange(args[0], 1, 500, "Radius") : 100;
        Location center;
        if (sender instanceof Player player) {
            center = player.getLocation();
        } else {
            World world = Bukkit.getWorlds().get(0);
            center = world.getSpawnLocation();
        }
        World world = center.getWorld();
        int removed = 0;
        for (var entity : world.getNearbyEntities(center, radius, radius, radius,
                e -> e instanceof Item || e instanceof Arrow
                        || e instanceof SpectralArrow || e instanceof Trident)) {
            entity.remove();
            removed++;
        }
        Text.ok(sender, "Removed <white>" + removed + "</white> dropped items/arrows within <white>"
                + radius + "</white> blocks.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
