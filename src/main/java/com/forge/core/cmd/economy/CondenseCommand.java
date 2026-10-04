package com.forge.core.cmd.economy;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Convert full sets of 9 items into their block form. */
public final class CondenseCommand extends ForgeCommand {
    /** Raw item -> block, always 9:1. */
    private static final Map<Material, Material> RECIPES = new HashMap<>();

    static {
        RECIPES.put(Material.IRON_INGOT, Material.IRON_BLOCK);
        RECIPES.put(Material.GOLD_INGOT, Material.GOLD_BLOCK);
        RECIPES.put(Material.COPPER_INGOT, Material.COPPER_BLOCK);
        RECIPES.put(Material.DIAMOND, Material.DIAMOND_BLOCK);
        RECIPES.put(Material.EMERALD, Material.EMERALD_BLOCK);
        RECIPES.put(Material.LAPIS_LAZULI, Material.LAPIS_BLOCK);
        RECIPES.put(Material.REDSTONE, Material.REDSTONE_BLOCK);
        RECIPES.put(Material.COAL, Material.COAL_BLOCK);
        RECIPES.put(Material.NETHERITE_INGOT, Material.NETHERITE_BLOCK);
        RECIPES.put(Material.QUARTZ, Material.QUARTZ_BLOCK);
        RECIPES.put(Material.AMETHYST_SHARD, Material.AMETHYST_BLOCK);
        RECIPES.put(Material.RAW_IRON, Material.RAW_IRON_BLOCK);
        RECIPES.put(Material.RAW_GOLD, Material.RAW_GOLD_BLOCK);
        RECIPES.put(Material.RAW_COPPER, Material.RAW_COPPER_BLOCK);
        RECIPES.put(Material.WHEAT, Material.HAY_BLOCK);
        RECIPES.put(Material.SNOWBALL, Material.SNOW_BLOCK);
        RECIPES.put(Material.MELON_SLICE, Material.MELON);
        RECIPES.put(Material.DRIED_KELP, Material.DRIED_KELP_BLOCK);
        RECIPES.put(Material.HONEYCOMB, Material.HONEYCOMB_BLOCK);
        RECIPES.put(Material.SLIME_BALL, Material.SLIME_BLOCK);
        RECIPES.put(Material.CLAY_BALL, Material.CLAY);
        RECIPES.put(Material.BONE_MEAL, Material.BONE_BLOCK);
    }

    public CondenseCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "condense";
    }

    @Override
    public String description() {
        return "Convert sets of 9 items into blocks.";
    }

    @Override
    public String usage() {
        return "/condense";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        ItemStack[] contents = player.getInventory().getStorageContents();
        int blocksMade = 0;
        for (Map.Entry<Material, Material> recipe : RECIPES.entrySet()) {
            Material raw = recipe.getKey();
            int total = 0;
            for (ItemStack item : contents) {
                if (item != null && item.getType() == raw) {
                    total += item.getAmount();
                }
            }
            int sets = total / 9;
            if (sets == 0) {
                continue;
            }
            int toRemove = sets * 9;
            for (int slot = 0; slot < contents.length && toRemove > 0; slot++) {
                ItemStack item = contents[slot];
                if (item != null && item.getType() == raw) {
                    int take = Math.min(item.getAmount(), toRemove);
                    toRemove -= take;
                    int left = item.getAmount() - take;
                    contents[slot] = left > 0 ? item.asQuantity(left) : null;
                }
            }
            ItemStack blocks = new ItemStack(recipe.getValue(), sets);
            Map<Integer, ItemStack> overflow = player.getInventory().addItem(blocks);
            for (ItemStack item : overflow.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
            blocksMade += sets;
        }
        if (blocksMade == 0) {
            Text.send(player, "<gray>Nothing to condense — you need at least 9 of an item.");
            return;
        }
        player.getInventory().setStorageContents(contents);
        Text.ok(player, "Condensed into <white>" + blocksMade + "</white> block(s).");
    }
}
