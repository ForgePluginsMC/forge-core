package com.forge.core.gui;

import com.forge.core.ForgeCore;
import com.forge.core.data.KitManager;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Kit browser: preview contents, click to claim.
 */
@NullMarked
public final class KitGui extends WebGui {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final ForgeCore plugin;
    private final WebGui parent;

    public KitGui(ForgeCore plugin, WebGui parent) {
        this.plugin = plugin;
        this.parent = parent;
    }

    @Override
    protected List<String> breadcrumb() {
        return List.of("Menu", "Kits");
    }

    @Override
    protected @Nullable WebGui parent() {
        return parent;
    }

    @Override
    protected void buildContent(Player viewer) {
        List<String> names = new ArrayList<>(plugin.kits().names());
        names.sort(String.CASE_INSENSITIVE_ORDER);

        int[] slots = CONTENT_SLOTS;
        for (int i = 0; i < names.size() && i < slots.length; i++) {
            String name = names.get(i);
            KitManager.Kit kit = plugin.kits().get(name);
            if (kit == null) {
                continue;
            }
            long remaining = plugin.kits().cooldownRemaining(viewer, kit);
            boolean ready = remaining <= 0;

            Material icon = Material.CHEST;
            List<ItemStack> items = kit.items();
            if (!items.isEmpty() && items.get(0) != null) {
                icon = items.get(0).getType();
            }

            List<String> lore = new ArrayList<>();
            lore.add("<gray>" + items.size() + " items");
            if (kit.cost() > 0) {
                lore.add("<gold>Cost: <white>$" + String.format("%.2f", kit.cost()));
            }
            if (kit.cooldownSeconds() > 0) {
                lore.add("<gray>Cooldown: <white>" + formatCooldown(kit.cooldownSeconds()));
            }
            lore.add("");
            if (ready) {
                lore.add("<green>Click to claim");
            } else {
                lore.add("<red>Available in " + formatCooldown(remaining));
            }
            // Preview first few items.
            lore.add("<dark_gray>Contains:");
            for (int j = 0; j < Math.min(3, items.size()); j++) {
                ItemStack item = items.get(j);
                if (item != null) {
                    lore.add("<dark_gray>- " + Text.escape(prettyName(item.getType())));
                }
            }

            GuiItem guiItem = GuiItem.of(ready ? icon : Material.GRAY_DYE)
                    .name((ready ? "<gold>" : "<gray>") + Text.escape(name))
                    .lore(lore);
            if (ready) {
                guiItem.action(p -> runCommand(p, "kit " + name));
            }
            set(slots[i], guiItem);
        }
    }

    private static String formatCooldown(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        }
        if (seconds < 3600) {
            return (seconds / 60) + "m";
        }
        return (seconds / 3600) + "h";
    }

    private static String prettyName(Material material) {
        String[] parts = material.name().toLowerCase(java.util.Locale.ROOT).split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }

}
