package com.forge.core.quest;

import com.forge.core.util.Text;
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
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Quest GUI: browse available quests, view progress, and start/abandon them.
 */
@NullMarked
public final class QuestGui implements Listener {
    private final QuestManager manager;

    public QuestGui(QuestManager manager) {
        this.manager = manager;
    }

    /** Tags inventories built by this GUI. */
    static final class Holder implements InventoryHolder {
        private final UUID viewer;
        private @Nullable Inventory inventory;

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
            return inventory;
        }
    }

    /** Open the quest browser for a player. */
    public void open(Player player) {
        List<Quest> quests = manager.all();
        int size = 54;
        Holder holder = new Holder(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(holder, size, Text.of("<gold><bold>Quests"));
        holder.setInventory(inv);

        ItemStack filler = named(new ItemStack(Material.BLACK_STAINED_GLASS_PANE), Component.empty());
        for (int slot = 0; slot < size; slot++) {
            inv.setItem(slot, filler);
        }

        int slot = 10;
        for (Quest quest : quests) {
            if (slot >= 44) {
                break;
            }
            if ((slot + 1) % 9 == 0) {
                slot += 2;
            }
            inv.setItem(slot, questItem(player, quest));
            slot++;
        }

        player.openInventory(inv);
    }

    private ItemStack questItem(Player player, Quest quest) {
        boolean complete = manager.isComplete(player, quest.id());
        boolean started = manager.isStarted(player, quest.id());
        boolean canStart = manager.canStart(player, quest);

        Material icon;
        String prefix;
        if (complete) {
            icon = Material.EMERALD;
            prefix = "<green>\u2714 ";
        } else if (started) {
            icon = Material.GOLD_INGOT;
            prefix = "<gold>\u25b6 ";
        } else if (canStart) {
            icon = Material.BOOK;
            prefix = "<yellow>\u25cb ";
        } else {
            icon = Material.BARRIER;
            prefix = "<gray>\ud83d\udd12 ";
        }

        List<Component> lore = new ArrayList<>();
        for (String line : quest.description().split("\n")) {
            lore.add(Text.of("<gray>" + line));
        }
        lore.add(Component.empty());
        lore.add(Text.of("<gray>Type: <white>" + quest.type().name().toLowerCase()));

        if (!quest.prerequisites().isEmpty()) {
            List<String> names = new ArrayList<>();
            for (String prereq : quest.prerequisites()) {
                Quest q = manager.get(prereq);
                names.add(q == null ? prereq : q.name());
            }
            lore.add(Text.of("<gray>Requires: <white>" + String.join(", ", names)));
        }

        lore.add(Component.empty());
        if (complete) {
            lore.add(Text.of("<green>Completed!"));
        } else if (started) {
            int stageIndex = manager.currentStage(player, quest.id());
            if (stageIndex >= 0 && stageIndex < quest.stages().size()) {
                QuestStage stage = quest.stages().get(stageIndex);
                lore.add(Text.of("<gold>Stage " + (stageIndex + 1) + "/" + quest.stages().size()
                        + ": <white>" + stage.name()));
                for (QuestObjective objective : stage.objectives()) {
                    int prog = manager.progress(player, quest, stageIndex, objective);
                    int pct = objective.target() > 0
                            ? Math.min(100, prog * 100 / objective.target()) : 100;
                    lore.add(Text.of("<gray>- " + objective.description() + ": <white>"
                            + prog + "<gray>/<white>" + objective.target()
                            + " <gray>(" + pct + "%)"));
                }
            }
            lore.add(Component.empty());
            lore.add(Text.of("<red>Shift-click to abandon"));
        } else if (canStart) {
            lore.add(Text.of("<yellow>Click to start this quest!"));
        } else {
            lore.add(Text.of("<gray>Complete prerequisites first."));
        }

        return named(new ItemStack(icon), Text.of(prefix + quest.name()), lore);
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
        if (event.getClickedInventory() == null
                || !(event.getClickedInventory().getHolder() instanceof Holder)) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.BLACK_STAINED_GLASS_PANE) {
            return;
        }

        // Find which quest was clicked by matching the name
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }
        String displayName = Text.strip(meta.displayName().toString());
        // Extract quest name by stripping prefix symbols
        for (Quest quest : manager.all()) {
            if (displayName.contains(quest.name())) {
                handleQuestClick(player, quest, event.isShiftClick());
                break;
            }
        }
    }

    private void handleQuestClick(Player player, Quest quest, boolean shiftClick) {
        if (shiftClick && manager.isStarted(player, quest.id())) {
            manager.abandon(player, quest.id());
            Text.send(player, "<yellow>Quest abandoned: <white>" + Text.escape(quest.name()));
            open(player);
            return;
        }
        if (manager.canStart(player, quest)) {
            manager.start(player, quest);
            Text.send(player, "<green>Quest started: <white>" + Text.escape(quest.name()));
            open(player);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof Holder) {
            event.setCancelled(true);
        }
    }
}
