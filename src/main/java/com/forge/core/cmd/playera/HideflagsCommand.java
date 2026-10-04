package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** Toggle hiding all item flags (enchants, attributes, …) on the held item. */
public final class HideflagsCommand extends PlayerACommand {
    public HideflagsCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "hideflags";
    }

    @Override
    public String description() {
        return "Toggle hidden item flags on the held item.";
    }

    @Override
    public String usage() {
        return "/hideflags";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            Text.error(sender, "Hold an item.");
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta.getItemFlags().isEmpty()) {
            meta.addItemFlags(ItemFlag.values());
            Text.ok(sender, "Item flags <green>hidden</green>.");
        } else {
            meta.removeItemFlags(ItemFlag.values());
            Text.ok(sender, "Item flags <green>shown</green>.");
        }
        item.setItemMeta(meta);
    }
}
