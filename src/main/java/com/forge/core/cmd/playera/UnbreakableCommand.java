package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** Toggle the unbreakable flag on the held item. */
public final class UnbreakableCommand extends PlayerACommand {
    public UnbreakableCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "unbreakable";
    }

    @Override
    public String description() {
        return "Toggle unbreakable on the held item.";
    }

    @Override
    public String usage() {
        return "/unbreakable";
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
        boolean now = !meta.isUnbreakable();
        meta.setUnbreakable(now);
        item.setItemMeta(meta);
        Text.ok(sender, "Unbreakable " + (now ? "<green>on</green>." : "<red>off</red>."));
    }
}
