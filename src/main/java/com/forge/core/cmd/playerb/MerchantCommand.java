package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

/**
 * /merchant — open a villager trading GUI (empty trade list).
 */
public final class MerchantCommand extends ForgeCommand {
    public MerchantCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "merchant";
    }

    @Override
    public String description() {
        return "Open a villager merchant trading GUI.";
    }

    @Override
    public String usage() {
        return "/merchant";
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
        player.openInventory(Bukkit.createInventory(null, InventoryType.MERCHANT,
                Text.of("<gold>Merchant</gold>")));
        Text.ok(sender, "Merchant opened.");
    }
}
