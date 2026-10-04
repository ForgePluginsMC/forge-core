package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /blocknbt — dump a block's tile-entity data and block-data string. */
public final class BlockNbtCommand extends ForgeCommand {
    public BlockNbtCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "blocknbt";
    }

    @Override
    public String description() {
        return "Dump the looked-at block's tile-entity data.";
    }

    @Override
    public String usage() {
        return "/blocknbt";
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
        Text.send(sender, "<gold>Block data:</gold> <white>"
                + Text.escape(block.getBlockData().getAsString()) + "</white>");
        if (block.getState() instanceof TileState tile) {
            List<String> keys = new ArrayList<>();
            for (NamespacedKey key : tile.getPersistentDataContainer().getKeys()) {
                keys.add(key.toString());
            }
            if (keys.isEmpty()) {
                Text.send(sender, "<gray>No persistent data keys on this tile entity.</gray>");
            } else {
                Text.send(sender, "<gold>Persistent data keys (" + keys.size() + "):</gold>");
                for (String key : keys.subList(0, Math.min(keys.size(), 30))) {
                    Text.send(sender, "  <gray>•</gray> <white>" + Text.escape(key) + "</white>");
                }
            }
        } else {
            Text.send(sender, "<gray>This block has no tile entity.</gray>");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
