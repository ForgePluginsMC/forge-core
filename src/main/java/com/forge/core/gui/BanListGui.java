package com.forge.core.gui;

import com.forge.core.ForgeCore;
import com.forge.core.punish.BanManager;
import com.forge.core.util.Text;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Ban list: click a ban to unban.
 */
@NullMarked
public final class BanListGui extends ForgeGui {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());
    private final ForgeCore plugin;
    private final ForgeGui parent;

    public BanListGui(ForgeCore plugin, ForgeGui parent) {
        this.plugin = plugin;
        this.parent = parent;
    }

    @Override
    protected Component title() {
        return MM.deserialize("<red><bold>Ban List");
    }

    @Override
    protected int size() {
        return 54;
    }

    @Override
    protected @Nullable ForgeGui parent() {
        return parent;
    }

    @Override
    protected void build(Player viewer) {
        List<BanManager.BanInfo> bans = new ArrayList<>(plugin.bans().all());
        // Filter out expired temp bans.
        bans.removeIf(BanManager.BanInfo::expired);

        int[] slots = gridSlots();
        for (int i = 0; i < bans.size() && i < slots.length; i++) {
            BanManager.BanInfo ban = bans.get(i);
            List<String> lore = new ArrayList<>();
            lore.add("<gray>Reason: <white>" + Text.escape(ban.reason()));
            lore.add("<gray>By: <white>" + Text.escape(ban.by()));
            lore.add("<gray>Since: <white>" + FMT.format(Instant.ofEpochMilli(ban.createdAt())));
            if (ban.permanent()) {
                lore.add("<red>Permanent");
            } else {
                lore.add("<yellow>Expires: <white>" + FMT.format(Instant.ofEpochMilli(ban.until())));
            }
            lore.add("");
            lore.add("<green>Click to unban");
            set(slots[i], GuiItem.of(Material.BARRIER)
                    .name("<red>" + Text.escape(ban.name()))
                    .lore(lore)
                    .action(p -> runCommand(p, "unban " + ban.name())));
        }

        if (bans.isEmpty()) {
            set(22, GuiItem.of(Material.LIME_DYE)
                    .name("<green>No active bans")
                    .lore("<gray>Everyone is behaving."));
        }
    }

    private static int[] gridSlots() {
        List<Integer> slots = new ArrayList<>();
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 9; col++) {
                slots.add(row * 9 + col);
            }
        }
        return slots.stream().mapToInt(Integer::intValue).toArray();
    }
}
