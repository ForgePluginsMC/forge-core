package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Enchant the held item. Levels above the enchantment's max need
 * {@code forgecore.enchant.unsafe} (also bypasses item-type restrictions).
 */
public final class EnchantCommand extends PlayerACommand {
    public EnchantCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "enchant";
    }

    @Override
    public String description() {
        return "Enchant the held item.";
    }

    @Override
    public String usage() {
        return "/enchant <enchantment> [level]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        Enchantment enchantment = Enchants.find(args[0]);
        if (enchantment == null) {
            Text.error(sender, "Unknown enchantment <white>" + Text.escape(args[0]) + "</white>.");
            return;
        }
        int level = 1;
        if (args.length >= 2) {
            try {
                level = Integer.parseInt(args[1]);
            } catch (NumberFormatException exception) {
                Text.error(sender, "Level must be a number.");
                return;
            }
        }
        if (level <= 0) {
            Text.error(sender, "Level must be positive.");
            return;
        }
        boolean unsafe = sender.hasPermission("forgecore.enchant.unsafe");
        if (level > enchantment.getMaxLevel() && !unsafe) {
            Text.error(sender, "Max level for <white>" + Text.escape(enchantment.getKey().getKey())
                    + "</white> is " + enchantment.getMaxLevel() + ".");
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            Text.error(sender, "Hold an item to enchant.");
            return;
        }
        if (!enchantment.canEnchantItem(item) && !unsafe) {
            Text.error(sender, "That enchantment cannot be applied to <white>"
                    + Text.escape(item.getType().name().toLowerCase(java.util.Locale.ROOT)) + "</white>.");
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (!meta.addEnchant(enchantment, level, unsafe)) {
            Text.error(sender, "Could not apply that enchantment.");
            return;
        }
        item.setItemMeta(meta);
        Text.ok(sender, "Enchanted with <white>" + Text.escape(enchantment.getKey().getKey())
                + " " + level + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Enchants.keys(), args);
        }
        return List.of();
    }
}
