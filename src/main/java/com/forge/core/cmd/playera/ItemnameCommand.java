package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** Rename the held item (MiniMessage; colors need forgecore.itemname.color). */
public final class ItemnameCommand extends PlayerACommand {
    public ItemnameCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "itemname";
    }

    @Override
    public String description() {
        return "Rename the held item.";
    }

    @Override
    public String usage() {
        return "/itemname <name...>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            Text.error(sender, "Hold an item to rename.");
            return;
        }
        String input = String.join(" ", args);
        String mini = sender.hasPermission("forgecore.itemname.color") ? input : Text.strip(input);
        if (Text.strip(mini).isBlank()) {
            Text.error(sender, "Name cannot be blank.");
            return;
        }
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Text.of(mini));
        item.setItemMeta(meta);
        Text.ok(sender, "Item renamed to " + mini + "<green>.");
    }
}
