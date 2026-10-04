package com.forge.core.gui;

import com.forge.core.util.Text;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Base class for all ForgeCore GUIs.
 *
 * <p>Subclasses implement {@link #build(Player)} to populate slots. Clicks are
 * routed to the item's action. Every GUI may declare a parent for the Back button.
 */
@NullMarked
public abstract class ForgeGui implements InventoryHolder {
    /** Tag holder so GuiManager can route clicks. */
    static final class Tag implements InventoryHolder {
        final ForgeGui gui;

        Tag(ForgeGui gui) {
            this.gui = gui;
        }

        @Override
        public @Nullable Inventory getInventory() {
            return null;
        }
    }

    private final Map<Integer, GuiItem> items = new HashMap<>();
    private @Nullable Inventory inventory;

    /** GUI title. */
    protected abstract Component title();

    /** Inventory size: 9, 18, 27, 36, 45 or 54. */
    protected abstract int size();

    /** Populate slots. Called on open and on refresh. */
    protected abstract void build(Player viewer);

    /** Parent GUI for the Back button, or null for top-level. */
    protected @Nullable ForgeGui parent() {
        return null;
    }

    /** Set the item at a slot. */
    protected final void set(int slot, GuiItem item) {
        items.put(slot, item);
    }

    /** Set the item at a slot only if no item is already there. */
    protected final void setIfAbsent(int slot, GuiItem item) {
        items.putIfAbsent(slot, item);
    }

    /** Fill empty slots with a decorative pane. */
    protected void fillEmpty() {
        GuiItem filler = GuiItem.of(Material.BLACK_STAINED_GLASS_PANE).name(" ");
        for (int i = 0; i < size(); i++) {
            items.putIfAbsent(i, filler);
        }
    }

    /** Add a Back button at the last slot if a parent exists. */
    protected void addBackButton() {
        ForgeGui parent = parent();
        if (parent != null) {
            set(size() - 1, GuiItem.of(Material.ARROW)
                    .name("<red><bold>Back")
                    .lore("<gray>Return to the previous menu.")
                    .action(p -> parent.open(p)));
        }
    }

    /** Open this GUI for a player. */
    public final void open(Player player) {
        GuiManager.get().stashInventory(player);
        items.clear();
        build(player);
        fillEmpty();
        addBackButton();
        Tag tag = new Tag(this);
        Inventory inv = Bukkit.createInventory(tag, size(), title());
        for (Map.Entry<Integer, GuiItem> entry : items.entrySet()) {
            int slot = entry.getKey();
            if (slot < 54) {
                inv.setItem(slot, entry.getValue().stack());
            } else {
                // Raw slots 54-89 map to the player's own inventory view slots.
                player.getInventory().setItem(slot - 54, entry.getValue().stack());
            }
        }
        this.inventory = inv;
        GuiManager.get().track(player, this);
        player.openInventory(inv);
    }

    /** Rebuild and reopen for the same viewer. */
    public final void refresh(Player player) {
        open(player);
    }

    /** Handle a click on a slot. */
    final void click(Player player, int slot) {
        GuiItem item = items.get(slot);
        if (item != null) {
            item.click(player);
        }
    }

    @Override
    public @Nullable Inventory getInventory() {
        return inventory;
    }

    /** Convenience: run a command as the clicking player (with permission checks). */
    protected final void runCommand(Player player, String command) {
        player.closeInventory();
        player.performCommand(command);
    }

    /** Convenience: ask for chat input, then run a command with the result. */
    protected final void runCommandWithInput(
            Player player, String prompt, String commandPrefix) {
        player.closeInventory();
        Text.send(player, "<yellow>" + prompt + " <gray>(type in chat, or 'cancel')");
        ChatInput.request(player, input -> {
            if (input.equalsIgnoreCase("cancel")) {
                Text.send(player, "<gray>Cancelled.");
                return;
            }
            player.performCommand(commandPrefix + " " + input);
        });
    }
}
