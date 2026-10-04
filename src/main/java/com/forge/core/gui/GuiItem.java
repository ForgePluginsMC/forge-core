package com.forge.core.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Builder for a clickable GUI item.
 */
@NullMarked
public final class GuiItem {
    private final ItemStack stack;
    private @Nullable Consumer<Player> action;

    private GuiItem(Material material) {
        this.stack = new ItemStack(material);
    }

    public static GuiItem of(Material material) {
        return new GuiItem(material);
    }

    public GuiItem name(String miniMessage) {
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                .deserialize(miniMessage));
        stack.setItemMeta(meta);
        return this;
    }

    public GuiItem lore(String... lines) {
        ItemMeta meta = stack.getItemMeta();
        List<Component> lore = new ArrayList<>();
        var mm = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage();
        for (String line : lines) {
            lore.add(mm.deserialize(line));
        }
        meta.lore(lore);
        stack.setItemMeta(meta);
        return this;
    }

    public GuiItem lore(List<String> lines) {
        return lore(lines.toArray(new String[0]));
    }

    public GuiItem action(Consumer<Player> action) {
        this.action = action;
        return this;
    }

    /**
     * Set a custom model data key for resource pack textures.
     *
     * @param key the string key matching a case in the pack's item definition
     */
    public GuiItem model(String key) {
        ItemMeta meta = stack.getItemMeta();
        var cmd = meta.getCustomModelDataComponent();
        cmd.setStrings(List.of(key));
        meta.setCustomModelDataComponent(cmd);
        stack.setItemMeta(meta);
        return this;
    }

    /**
     * Create a web-style card item: icon with title and description.
     *
     * @param icon the ForgeIcons key for the item texture
     * @param title MiniMessage title
     * @param description MiniMessage description lines
     */
    public static GuiItem card(String icon, String title, String... description) {
        List<String> lore = new ArrayList<>();
        for (String line : description) {
            lore.add("<gray>" + line);
        }
        lore.add("");
        lore.add("<green>Click to open");
        return GuiItem.of(Material.PAPER)
                .model(icon)
                .name(title)
                .lore(lore);
    }

    /** Copy an existing ItemStack (e.g. player head) into a GuiItem. */
    public static GuiItem from(ItemStack stack) {
        GuiItem item = new GuiItem(stack.getType());
        item.stack.setItemMeta(stack.getItemMeta());
        item.stack.setAmount(stack.getAmount());
        return item;
    }

    ItemStack stack() {
        return stack.clone();
    }

    void click(Player player) {
        if (action != null) {
            action.accept(player);
        }
    }
}
