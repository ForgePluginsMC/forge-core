package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry.CommandFailure;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /replaceblock — replace one material with another in a radius around you. */
public final class ReplaceBlockCommand extends ForgeCommand {
    public ReplaceBlockCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "replaceblock";
    }

    @Override
    public String description() {
        return "Replace blocks of one material with another around you.";
    }

    @Override
    public String usage() {
        return "/replaceblock <from-material> <to-material> [radius]";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            Text.usage(sender, usage());
            return;
        }
        Material from = AdminUtil.material(args[0]);
        Material to = AdminUtil.material(args[1]);
        if (from == to) {
            throw new CommandFailure("Both materials are the same.");
        }
        if (!to.isBlock()) {
            throw new CommandFailure(to.name().toLowerCase(Locale.ROOT) + " is not a block.");
        }
        int radius = args.length >= 3 ? AdminUtil.intInRange(args[2], 1, 25, "Radius") : 10;

        Player player = (Player) sender;
        World world = player.getWorld();
        int cx = player.getLocation().getBlockX();
        int cy = player.getLocation().getBlockY();
        int cz = player.getLocation().getBlockZ();
        int replaced = 0;
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int y = Math.max(cy - radius, world.getMinHeight());
                    y <= Math.min(cy + radius, world.getMaxHeight() - 1); y++) {
                for (int z = cz - radius; z <= cz + radius; z++) {
                    if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                        continue;
                    }
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() == from) {
                        block.setType(to, false);
                        replaced++;
                    }
                }
            }
        }
        Text.ok(sender, "Replaced <white>" + replaced + "</white> blocks of <white>"
                + from.name().toLowerCase(Locale.ROOT) + "</white> with <white>"
                + to.name().toLowerCase(Locale.ROOT) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 2) {
            return Players.filter(AdminUtil.materialNames(), args);
        }
        return List.of();
    }
}
