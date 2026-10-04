package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/** Wear the held item as a hat (swaps with the current helmet). */
public final class HatCommand extends PlayerACommand {
    public HatCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "hat";
    }

    @Override
    public String description() {
        return "Wear the held item as a hat.";
    }

    @Override
    public String usage() {
        return "/hat";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        PlayerInventory inventory = player.getInventory();
        ItemStack hand = inventory.getItemInMainHand();
        if (hand.getType().isAir()) {
            Text.error(sender, "Hold an item to wear as a hat.");
            return;
        }
        ItemStack helmet = inventory.getHelmet();
        inventory.setHelmet(hand);
        inventory.setItemInMainHand(helmet);
        Text.ok(sender, "Nice hat.");
    }
}
