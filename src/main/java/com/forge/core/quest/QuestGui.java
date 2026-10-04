package com.forge.core.quest;

import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.key.Key;
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
 *
 * <p>Glyph-rendered background (forge_cards font); quest entries use bright
 * button tiles color-coded by status.
 */
@NullMarked
public final class QuestGui implements Listener {
    /** Glyph font for menu art. */
    private static final Key GLYPH_FONT = Key.key("minecraft:forge_cards");
    /** Negative-space prefix aligning the glyph to the container edge. */
    private static final String ALIGN_LEFT = "\uF803";
    /** Quests list background glyph. */
    private static final String GLYPH = ALIGN_LEFT + "\uE118";

    /** Tile model keys (custom_model_data) for quest status. */
    private static final String TILE_GREEN = "forge_tile_green";
    private static final String TILE_GOLD = "forge_tile_gold";
    private static final String TILE_YELLOW = "forge_tile_yellow";
    private static final String TILE_GRAY = "forge_tile_gray";
    private static final String TILE_INVISIBLE = "forge_invisible";

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

    /** Build an invisible filler item (no tooltip). */
    private static ItemStack invisibleFiller() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(" "));
        meta.setHideTooltip(true);
        var cmd = meta.getCustomModelDataComponent();
        cmd.setStrings(List.of(TILE_INVISIBLE));
        meta.setCustomModelDataComponent(cmd);
        item.setItemMeta(meta);
        return item;
    }

    /** Build a tile item with the given model key, name, and lore. */
    private static ItemStack tileItem(String modelKey, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        meta.lore(lore);
        var cmd = meta.getCustomModelDataComponent();
        cmd.setStrings(List.of(modelKey));
        meta.setCustomModelDataComponent(cmd);
        item.setItemMeta(meta);
        return item;
    }

    /** Open the quest browser for a player. */
    public void open(Player player) {
        List<Quest> quests = manager.all();
        int size = 54;
        Holder holder = new Holder(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(holder, size,
                Component.text(GLYPH).font(GLYPH_FONT));
        holder.setInventory(inv);

        ItemStack filler = invisibleFiller();
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

        String tile;
        String prefix;
        if (complete) {
            tile = TILE_GREEN;
            prefix = "<green>\u2714 ";
        } else if (started) {
            tile = TILE_GOLD;
            prefix = "<gold>\u25b6 ";
        } else if (canStart) {
            tile = TILE_YELLOW;
            prefix = "<yellow>\u25cb ";
        } else {
            tile = TILE_GRAY;
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

        return tileItem(tile, Text.of(prefix + quest.name()), lore);
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
        if (clicked == null || clicked.getType() != Material.PAPER) {
            return;
        }
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || meta.isHideTooltip()) {
            return; // filler
        }

        // Find which quest was clicked by matching the name
        String displayName = Text.strip(meta.displayName().toString());
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
        if (event.getView().getTopInventory().getHolder() instanceof Holder) {
            event.setCancelled(true);
        }
    }
}
