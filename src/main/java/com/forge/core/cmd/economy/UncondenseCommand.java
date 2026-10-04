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

/** Convert blocks back into 9 items each. */
public final class UncondenseCommand extends ForgeCommand {
    /** Block -> raw item, always 1:9. */
    private static final Map<Material, Material> RECIPES = new HashMap<>();

    static {
        RECIPES.put(Material.IRON_BLOCK, Material.IRON_INGOT);
        RECIPES.put(Material.GOLD_BLOCK, Material.GOLD_INGOT);
        RECIPES.put(Material.COPPER_BLOCK, Material.COPPER_INGOT);
        RECIPES.put(Material.DIAMOND_BLOCK, Material.DIAMOND);
        RECIPES.put(Material.EMERALD_BLOCK, Material.EMERALD);
        RECIPES.put(Material.LAPIS_BLOCK, Material.LAPIS_LAZULI);
        RECIPES.put(Material.REDSTONE_BLOCK, Material.REDSTONE);
        RECIPES.put(Material.COAL_BLOCK, Material.COAL);
        RECIPES.put(Material.NETHERITE_BLOCK, Material.NETHERITE_INGOT);
        RECIPES.put(Material.QUARTZ_BLOCK, Material.QUARTZ);
        RECIPES.put(Material.AMETHYST_BLOCK, Material.AMETHYST_SHARD);
        RECIPES.put(Material.RAW_IRON_BLOCK, Material.RAW_IRON);
        RECIPES.put(Material.RAW_GOLD_BLOCK, Material.RAW_GOLD);
        RECIPES.put(Material.RAW_COPPER_BLOCK, Material.RAW_COPPER);
        RECIPES.put(Material.HAY_BLOCK, Material.WHEAT);
        RECIPES.put(Material.SNOW_BLOCK, Material.SNOWBALL);
        RECIPES.put(Material.MELON, Material.MELON_SLICE);
        RECIPES.put(Material.DRIED_KELP_BLOCK, Material.DRIED_KELP);
        RECIPES.put(Material.HONEYCOMB_BLOCK, Material.HONEYCOMB);
        RECIPES.put(Material.SLIME_BLOCK, Material.SLIME_BALL);
        RECIPES.put(Material.CLAY, Material.CLAY_BALL);
        RECIPES.put(Material.BONE_BLOCK, Material.BONE_MEAL);
    }

    public UncondenseCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "uncondense";
    }

    @Override
    public String description() {
        return "Convert blocks back into their 9 raw items.";
    }

    @Override
    public String usage() {
        return "/uncondense";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        ItemStack[] contents = player.getInventory().getStorageContents();
        int itemsMade = 0;
        for (Map.Entry<Material, Material> recipe : RECIPES.entrySet()) {
            Material block = recipe.getKey();
            int total = 0;
            for (ItemStack item : contents) {
                if (item != null && item.getType() == block) {
                    total += item.getAmount();
                }
            }
            if (total == 0) {
                continue;
            }
            int toRemove = total;
            for (int slot = 0; slot < contents.length && toRemove > 0; slot++) {
                ItemStack item = contents[slot];
                if (item != null && item.getType() == block) {
                    int take = Math.min(item.getAmount(), toRemove);
                    toRemove -= take;
                    int left = item.getAmount() - take;
                    contents[slot] = left > 0 ? item.asQuantity(left) : null;
                }
            }
            ItemStack raws = new ItemStack(recipe.getValue(), total * 9);
            Map<Integer, ItemStack> overflow = player.getInventory().addItem(raws);
            for (ItemStack item : overflow.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
            itemsMade += total * 9;
        }
        if (itemsMade == 0) {
            Text.send(player, "<gray>Nothing to uncondense.");
            return;
        }
        player.getInventory().setStorageContents(contents);
        Text.ok(player, "Uncondensed into <white>" + itemsMade + "</white> item(s).");
    }
}
