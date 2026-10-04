package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Locs;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /blockinfo — inspect the block you are looking at. */
public final class BlockInfoCommand extends ForgeCommand {
    public BlockInfoCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "blockinfo";
    }

    @Override
    public String description() {
        return "Show info about the block you are looking at.";
    }

    @Override
    public String usage() {
        return "/blockinfo";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        Block block = player.getTargetBlockExact(64);
        if (block == null) {
            Text.error(sender, "Look at a block first.");
            return;
        }
        Text.send(sender, "<gold>Block info:</gold>");
        Text.send(sender, "  Type: <white>" + block.getType().name().toLowerCase(Locale.ROOT) + "</white>");
        Text.send(sender, "  Location: <white>" + Text.escape(Locs.pretty(block.getLocation())) + "</white>");
        Text.send(sender, "  Light: <white>" + block.getLightLevel() + "</white>"
                + " <gray>(sky " + block.getLightFromSky() + ", blocks " + block.getLightFromBlocks() + ")</gray>");
        Text.send(sender, "  Biome: <white>" + block.getBiome().getKey().getKey() + "</white>");
        Text.send(sender, "  State: <white>" + Text.escape(block.getState().getClass().getSimpleName()) + "</white>");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
