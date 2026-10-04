package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/** /dispose — trash GUI; anything left inside is deleted on close. */
public final class DisposeCommand extends ForgeCommand {
    public DisposeCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "dispose";
    }

    @Override
    public String description() {
        return "Open a trash GUI; items left inside are deleted.";
    }

    @Override
    public String usage() {
        return "/dispose";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return;
        }
        Inventory trash = Bukkit.createInventory(null, 54, Text.of("<red>Dispose — items are deleted on close</red>"));
        PlayerBState.disposeOpen.add(player.getUniqueId());
        player.openInventory(trash);
    }
}
