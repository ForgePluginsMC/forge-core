package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Fill the held stack to its max stack size. */
public final class MoreCommand extends PlayerACommand {
    public MoreCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "more";
    }

    @Override
    public String description() {
        return "Fill the held stack to max size.";
    }

    @Override
    public String usage() {
        return "/more";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            Text.error(sender, "Hold an item to fill.");
            return;
        }
        item.setAmount(item.getMaxStackSize());
        Text.ok(sender, "Stack filled to <white>" + item.getMaxStackSize() + "</white>.");
    }
}
