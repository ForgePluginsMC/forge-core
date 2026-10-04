package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

/** /workbench — open a portable crafting table. */
public final class WorkbenchCommand extends ForgeCommand {
    public WorkbenchCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "workbench";
    }

    @Override
    public String description() {
        return "Open a portable crafting table.";
    }

    @Override
    public String usage() {
        return "/workbench";
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
        player.openInventory(Bukkit.createInventory(null, InventoryType.WORKBENCH,
                Text.of("<gold>Crafting</gold>")));
        Text.ok(sender, "Portable workbench opened.");
    }
}
