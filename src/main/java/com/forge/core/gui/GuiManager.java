package com.forge.core.gui;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Routes inventory clicks to open ForgeGuis and tracks them per player.
 *
 * <p>Also owns the inventory save/restore cycle: when a GUI opens, the
 * player's real inventory (36 slots) is stashed so the entire screen —
 * chest + player inventory + hotbar — can be used as GUI space. Contents
 * are restored on close, quit, or plugin disable. If restoration fails,
 * items are dropped at the player's feet so nothing is ever lost.
 */
@NullMarked
public final class GuiManager implements Listener {
    private static final GuiManager INSTANCE = new GuiManager();

    private final Map<UUID, ForgeGui> open = new ConcurrentHashMap<>();
    private final Map<UUID, ItemStack[]> stashed = new ConcurrentHashMap<>();

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

    /**
     * Stash the player's inventory contents before a GUI takes over the
     * full screen. Idempotent — nested GUI opens don't double-save.
     */
    void stashInventory(Player player) {
        stashed.computeIfAbsent(player.getUniqueId(), uuid -> {
            ItemStack[] contents = player.getInventory().getContents();
            ItemStack[] copy = new ItemStack[contents.length];
            for (int i = 0; i < contents.length; i++) {
                copy[i] = contents[i] == null ? null : contents[i].clone();
            }
            player.getInventory().clear();
            return copy;
        });
    }

    /**
     * Restore a stashed inventory. Falls back to dropping items at the
     * player's location if anything goes wrong — items are never lost.
     */
    void restoreInventory(Player player) {
        ItemStack[] contents = stashed.remove(player.getUniqueId());
        if (contents == null) {
            return;
        }
        try {
            player.getInventory().setContents(contents);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING,
                    "Failed to restore inventory for " + player.getName()
                            + " — dropping items at feet instead.", e);
            Location loc = player.getLocation();
            for (ItemStack item : contents) {
                if (item != null) {
                    player.getWorld().dropItemNaturally(loc, item);
                }
            }
        }
    }

    /** Restore every stashed inventory (called on plugin disable). */
    public void restoreAll() {
        for (UUID uuid : stashed.keySet().toArray(new UUID[0])) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                restoreInventory(player);
            } else {
                stashed.remove(uuid);
            }
        }
    }

    /** True if this player currently has a stashed inventory. */
    public boolean hasStash(UUID uuid) {
        return stashed.containsKey(uuid);
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
        // The GUI owns the whole screen now (raw slots 0-89):
        // 0-53 chest, 54-80 player storage, 81-89 hotbar.
        int raw = event.getRawSlot();
        if (raw < 0 || raw > 89) {
            return;
        }
        tag.gui.click(player, raw);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof ForgeGui.Tag) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof ForgeGui.Tag tag
                && event.getPlayer() instanceof Player player) {
            ForgeGui closed = tag.gui;
            // Only handle close if this is the currently tracked GUI.
            // Opening a new GUI fires close for the old one synchronously —
            // don't untrack the new GUI.
            if (open.get(player.getUniqueId()) != closed) {
                return;
            }
            open.remove(player.getUniqueId());
            // Delay restore by a tick: opening another GUI fires close first,
            // and we don't want to restore between chained opens.
            Bukkit.getScheduler().runTaskLater(
                    JavaPlugin.getProvidingPlugin(GuiManager.class),
                    () -> {
                        if (open.get(player.getUniqueId()) == null
                                && player.isOnline()) {
                            restoreInventory(player);
                        }
                    }, 1L);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        open.remove(player.getUniqueId());
        restoreInventory(player);
    }
}
