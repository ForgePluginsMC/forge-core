package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

/**
 * Guards read-only inventory views opened by /invcheck: every click and
 * drag inside a watched inventory is cancelled.
 */
final class InvViewGuard implements Listener {
    private static final Set<Inventory> READ_ONLY = ConcurrentHashMap.newKeySet();

    InvViewGuard(ForgeCore plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /** Mark an inventory as read-only until it is closed. */
    static void watch(Inventory inventory) {
        READ_ONLY.add(inventory);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (READ_ONLY.contains(event.getView().getTopInventory())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        if (READ_ONLY.contains(event.getView().getTopInventory())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent event) {
        READ_ONLY.remove(event.getView().getTopInventory());
    }
}
