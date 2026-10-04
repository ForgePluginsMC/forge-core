package com.forge.core.gui;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Routes inventory clicks to open ForgeGuis and tracks them per player.
 */
@NullMarked
public final class GuiManager implements Listener {
    private static final GuiManager INSTANCE = new GuiManager();

    private final Map<UUID, ForgeGui> open = new ConcurrentHashMap<>();

    private GuiManager() {
    }

    public static GuiManager get() {
        return INSTANCE;
    }

    void track(Player player, ForgeGui gui) {
        open.put(player.getUniqueId(), gui);
    }

    public @Nullable ForgeGui openGui(Player player) {
        return open.get(player.getUniqueId());
    }

    public void forget(Player player) {
        open.remove(player.getUniqueId());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof ForgeGui.Tag tag)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        // Only handle clicks in the top (GUI) inventory, not the player's own.
        if (event.getClickedInventory() == null
                || !event.getClickedInventory().equals(top)) {
            return;
        }
        tag.gui.click(player, event.getSlot());
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof ForgeGui.Tag) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof ForgeGui.Tag
                && event.getPlayer() instanceof Player player) {
            open.remove(player.getUniqueId());
        }
    }
}
