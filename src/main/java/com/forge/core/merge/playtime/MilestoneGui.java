package com.forge.core.merge.playtime;

import com.forge.core.util.Text;
import com.forge.core.util.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

/** The milestone GUI: view progress across 54 slots and click to claim rewards. */
final class MilestoneGui implements Listener {
    private static final int[] ROW_STARTS = {10, 19, 28, 37};
    private static final int INFO_SLOT = 49;

    private final MilestoneManager manager;

    MilestoneGui(MilestoneManager manager) {
        this.manager = manager;
    }

    /** Tags inventories built by this GUI so clicks can be identified safely. */
    static final class Holder implements InventoryHolder {
        private final UUID viewer;
        private Inventory inventory;

        Holder(UUID viewer) {
            this.viewer = viewer;
        }

        void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        UUID viewer() {
            return viewer;
        }

        @Override
        public @Nullable Inventory getInventory() {
            // Nullable until setInventory() runs: the holder is created first
            // and the inventory is attached immediately after
            // Bukkit.createInventory() returns. (The Bukkit interface declares
            // @NotNull; this deferred population is the one known exception.)
            return inventory;
        }
    }

    void open(Player player) {
        List<Milestone> milestones = manager.milestones();
        Holder holder = new Holder(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(holder, 54, Text.of("<dark_aqua><bold>Playtime Milestones"));
        holder.setInventory(inv);

        ItemStack filler = named(new ItemStack(Material.BLACK_STAINED_GLASS_PANE), Component.empty());
        for (int slot = 0; slot < 54; slot++) {
            inv.setItem(slot, filler);
        }

        long seconds = manager.plugin().users().get(player).playtimeSeconds();
        for (int i = 0; i < milestones.size() && i < ROW_STARTS.length * 7; i++) {
            inv.setItem(slotFor(i), milestoneItem(player, milestones.get(i), seconds));
        }
        inv.setItem(INFO_SLOT, infoItem(player));

        player.openInventory(inv);
    }

    private static int slotFor(int milestoneIndex) {
        return ROW_STARTS[milestoneIndex / ROW_STARTS.length] + (milestoneIndex % 7);
    }

    private ItemStack milestoneItem(Player player, Milestone milestone, long seconds) {
        boolean claimed = manager.isClaimed(player, milestone.seconds());
        boolean unlocked = seconds >= milestone.seconds();

        String prefix = claimed ? "<green>\u2714 " : unlocked ? "<gold>\u25b6 " : "<gray>\ud83d\udd12 ";
        Component name = Text.of(prefix + milestone.guiName());

        List<Component> lore = new ArrayList<>();
        for (String line : milestone.guiLore()) {
            lore.add(Text.of(line));
        }
        lore.add(Component.empty());
        if (claimed) {
            lore.add(Text.of("<green>Already claimed!"));
        } else if (unlocked) {
            lore.add(Text.of("<gold>Click to claim your rewards!"));
        } else {
            lore.add(Text.of("<gray>Unlocks in <white>" + Time.format(milestone.seconds() - seconds)));
        }
        return named(new ItemStack(milestone.guiItem()), name, lore);
    }

    private ItemStack infoItem(Player player) {
        long seconds = manager.plugin().users().get(player).playtimeSeconds();
        List<Component> lore = List.of(
                Text.of("<gray>Total: <white>" + Time.format(seconds)),
                Text.of("<gray>Claimed: <white>" + manager.claimedCount(player)
                        + "<gray> / <white>" + manager.milestones().size()));
        return named(new ItemStack(Material.PAPER), Text.of("<aqua><bold>Your Playtime"), lore);
    }

    private static ItemStack named(ItemStack item, Component name) {
        return named(item, name, List.of());
    }

    private static ItemStack named(ItemStack item, Component name, List<Component> lore) {
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof Holder holder)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!player.getUniqueId().equals(holder.viewer())) {
            return;
        }
        // Only the top (GUI) inventory is interactive; the player's own is locked too.
        if (event.getClickedInventory() == null || !(event.getClickedInventory().getHolder() instanceof Holder)) {
            return;
        }

        int slot = event.getSlot();
        if (slot == INFO_SLOT) {
            return;
        }
        List<Milestone> milestones = manager.milestones();
        for (int i = 0; i < milestones.size() && i < ROW_STARTS.length * 7; i++) {
            if (slotFor(i) == slot) {
                tryClaim(player, milestones.get(i));
                return;
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof Holder) {
            event.setCancelled(true);
        }
    }

    private void tryClaim(Player player, Milestone milestone) {
        if (manager.isClaimed(player, milestone.seconds())) {
            return;
        }
        long seconds = manager.plugin().users().get(player).playtimeSeconds();
        if (seconds < milestone.seconds()) {
            Text.error(player, "That milestone is not unlocked yet!");
            return;
        }
        manager.claim(player, milestone);
        Text.ok(player, "<bold>Milestone claimed!</bold> <gray>Enjoy your rewards.");
        open(player); // refresh the GUI to show the new state
    }
}
