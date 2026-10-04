package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jspecify.annotations.Nullable;

/**
 * /recipe — show the crafting recipe for an item in a workbench GUI
 * (shaped and shapeless recipes only).
 */
public final class RecipeCommand extends ForgeCommand {
    private static final List<String> MATERIALS = Arrays.stream(Material.values())
            .map(material -> material.name().toLowerCase(Locale.ROOT))
            .sorted()
            .collect(Collectors.toList());

    public RecipeCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "recipe";
    }

    @Override
    public String description() {
        return "Show the crafting recipe for an item.";
    }

    @Override
    public String usage() {
        return "/recipe <item>";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return;
        }
        if (args.length != 1) {
            Text.usage(sender, usage());
            return;
        }
        Material material = Material.matchMaterial(args[0]);
        if (material == null) {
            Text.error(sender, "Unknown item <white>" + Text.escape(args[0]) + "</white>.");
            return;
        }
        Recipe recipe = findRecipe(material);
        if (recipe == null) {
            Text.error(sender, "No shaped/shapeless crafting recipe for <white>"
                    + Text.escape(material.name().toLowerCase(Locale.ROOT)) + "</white>.");
            return;
        }
        Inventory view = Bukkit.createInventory(null, InventoryType.WORKBENCH,
                Text.of("<gold>Recipe: " + Text.escape(pretty(material)) + "</gold>"));
        if (recipe instanceof ShapedRecipe shaped) {
            String[] shape = shaped.getShape();
            Map<Character, RecipeChoice> ingredients = shaped.getChoiceMap();
            for (int row = 0; row < shape.length && row < 3; row++) {
                String line = shape[row];
                for (int col = 0; col < line.length() && col < 3; col++) {
                    ItemStack icon = choiceIcon(ingredients.get(line.charAt(col)));
                    if (icon != null) {
                        view.setItem(1 + row * 3 + col, icon);
                    }
                }
            }
        } else if (recipe instanceof ShapelessRecipe shapeless) {
            int slot = 1;
            for (RecipeChoice choice : shapeless.getChoiceList()) {
                if (slot > 9) {
                    break;
                }
                ItemStack icon = choiceIcon(choice);
                if (icon != null) {
                    view.setItem(slot++, icon);
                }
            }
        }
        view.setItem(0, recipe.getResult().clone());
        player.openInventory(view);
    }

    private static @Nullable Recipe findRecipe(Material material) {
        Iterator<Recipe> iterator = Bukkit.recipeIterator();
        while (iterator.hasNext()) {
            Recipe recipe = iterator.next();
            if ((recipe instanceof ShapedRecipe || recipe instanceof ShapelessRecipe)
                    && recipe.getResult().getType() == material) {
                return recipe;
            }
        }
        return null;
    }

    private static @Nullable ItemStack choiceIcon(@Nullable RecipeChoice choice) {
        if (choice instanceof RecipeChoice.MaterialChoice materialChoice) {
            List<Material> options = materialChoice.getChoices();
            if (!options.isEmpty()) {
                return new ItemStack(options.get(0));
            }
        } else if (choice instanceof RecipeChoice.ExactChoice exactChoice) {
            List<ItemStack> options = exactChoice.getChoices();
            if (!options.isEmpty()) {
                return options.get(0).clone();
            }
        }
        return null;
    }

    private static String pretty(Material material) {
        return material.name().toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(MATERIALS, args);
        }
        return List.of();
    }
}
