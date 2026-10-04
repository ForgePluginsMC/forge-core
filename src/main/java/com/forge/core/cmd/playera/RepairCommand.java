package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Repair the held item (or every item with {@code /repair all}). Costs
 * {@code repair-cost-levels} experience levels (default 0).
 */
public final class RepairCommand extends PlayerACommand {
    public RepairCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "repair";
    }

    @Override
    public String description() {
        return "Repair the held item (or all items).";
    }

    @Override
    public String usage() {
        return "/repair [all]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        boolean all = args.length > 0 && args[0].equalsIgnoreCase("all");
        int cost = plugin.getConfig().getInt("repair-cost-levels", 0);
        if (cost > 0 && player.getLevel() < cost) {
            Text.error(sender, "Repairing costs <white>" + cost + "</white> levels; you have <white>"
                    + player.getLevel() + "</white>.");
            return;
        }
        List<ItemStack> items = all
                ? new ArrayList<>(Arrays.asList(player.getInventory().getContents()))
                : List.of(player.getInventory().getItemInMainHand());
        int repaired = 0;
        for (ItemStack item : items) {
            if (item == null || item.getType().isAir()) {
                continue;
            }
            ItemMeta meta = item.getItemMeta();
            if (meta instanceof Damageable damageable && damageable.hasDamage()) {
                damageable.setDamage(0);
                item.setItemMeta(meta);
                repaired++;
            }
        }
        if (repaired == 0) {
            Text.error(sender, "Nothing to repair.");
            return;
        }
        if (cost > 0) {
            player.giveExpLevels(-cost);
        }
        Text.ok(sender, "Repaired <white>" + repaired + "</white> item(s).");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(List.of("all"), args);
        }
        return List.of();
    }
}
