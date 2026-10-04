package com.forge.core.gui;

import com.forge.core.ForgeCore;
import com.forge.core.punish.BanManager;
import com.forge.core.util.Text;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Ban list: click a ban to unban.
 *
 * <p>Glyph-rendered background with bright button tiles.
 */
@NullMarked
public final class BanListGui extends GlyphGui {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());
    private final ForgeCore plugin;
    private final WebGui parent;

    public BanListGui(ForgeCore plugin, WebGui parent) {
        this.plugin = plugin;
        this.parent = parent;
    }

    @Override
    protected String glyphChar() {
        return "\uE115";
    }

    @Override
    protected List<String> breadcrumb() {
        return List.of("Menu", "Moderation", "Bans");
    }

    @Override
    protected @Nullable WebGui parent() {
        return parent;
    }

    @Override
    protected void buildContent(Player viewer) {
        List<BanManager.BanInfo> bans = new ArrayList<>(plugin.bans().all());
        bans.removeIf(BanManager.BanInfo::expired);

        int[] slots = CONTENT_SLOTS;
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
            set(slots[i], tile(ForgeIcons.TILE_RED,
                    "<red><bold>" + Text.escape(ban.name()), lore,
                    p -> runCommand(p, "unban " + ban.name())));
        }

        if (bans.isEmpty()) {
            set(22, tile(ForgeIcons.TILE_GREEN, "<green><bold>No active bans",
                    List.of("<gray>Everyone is behaving."), p -> {}));
        }
    }
}
