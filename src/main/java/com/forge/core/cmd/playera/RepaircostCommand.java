package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

/** Show the experience-level cost of repairing the held item. */
public final class RepaircostCommand extends PlayerACommand {
    public RepaircostCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "repaircost";
    }

    @Override
    public String description() {
        return "Show the cost of repairing the held item.";
    }

    @Override
    public String usage() {
        return "/repaircost";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        int cost = plugin.getConfig().getInt("repair-cost-levels", 0);
        ItemStack item = player.getInventory().getItemInMainHand();
        String condition = "not damaged";
        if (!item.getType().isAir() && item.getItemMeta() instanceof Damageable damageable) {
            condition = damageable.hasDamage() ? "damaged (" + damageable.getDamage() + ")" : "undamaged";
        }
        Text.send(sender, "<gray>Repair cost: <green>" + cost + " levels</green> "
                + "<gray>(you have <white>" + player.getLevel() + "</white>). "
                + "Held item: <white>" + Text.escape(condition) + "</white>.");
    }
}
