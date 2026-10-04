package com.forge.core.gui;

import com.forge.core.ForgeCore;
import com.forge.core.guild.Guild;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Guild dashboard: info, members, bank, claims, war status.
 *
 * <p>Glyph-rendered background with bright button tiles.
 */
@NullMarked
public final class GuildGui extends GlyphGui {
    private final ForgeCore plugin;
    private final WebGui parent;

    public GuildGui(ForgeCore plugin, WebGui parent) {
        this.plugin = plugin;
        this.parent = parent;
    }

    @Override
    protected String glyphChar() {
        return "\uE116";
    }

    @Override
    protected List<String> breadcrumb() {
        return List.of("Menu", "Guilds");
    }

    @Override
    protected @Nullable WebGui parent() {
        return parent;
    }

    @Override
    protected void buildContent(Player viewer) {
        Guild guild = plugin.guilds().guildOf(viewer);
        if (guild == null) {
            set(11, tile(ForgeIcons.TILE_YELLOW, "<yellow><bold>Create a Guild",
                    List.of("<gray>Start your own guild.",
                            "", "<yellow>Click to enter name & tag"),
                    p -> {
                        p.closeInventory();
                        Text.send(p, "<yellow>Type the guild name (or 'cancel'):");
                        ChatInput.request(p, name -> {
                            if (name.equalsIgnoreCase("cancel")) {
                                Text.send(p, "<gray>Cancelled.");
                                return;
                            }
                            final String guildName = name;
                            Text.send(p, "<yellow>Type the guild tag (or 'cancel'):");
                            ChatInput.request(p, tag -> {
                                if (tag.equalsIgnoreCase("cancel")) {
                                    Text.send(p, "<gray>Cancelled.");
                                    return;
                                }
                                p.performCommand("guild create " + guildName + " " + tag);
                            });
                        });
                    }));
            set(15, tile(ForgeIcons.TILE_BASE, "<aqua><bold>Guild List",
                    List.of("<gray>Browse all guilds.",
                            "", "<green>Click to view"),
                    p -> runCommand(p, "guild list")));
            return;
        }

        int claims = plugin.guilds().claimsOf(guild).size();
        List<String> memberNames = new ArrayList<>();
        for (var uuid : guild.members()) {
            var offline = Bukkit.getOfflinePlayer(uuid);
            String name = offline.getName() == null ? "?" : offline.getName();
            memberNames.add(guild.roleOf(uuid) + ": " + name);
        }

        set(10, tile(ForgeIcons.TILE_YELLOW,
                "<yellow><bold>" + Text.escape(guild.name())
                        + " <gray>[" + Text.escape(guild.tag()) + "]",
                List.of("<gray>Members: <white>" + guild.size(),
                        "<gray>Claims: <white>" + claims,
                        "<gray>Bank: <green>$" + String.format("%.2f", guild.bank()),
                        "", "<green>Click for details"),
                p -> runCommand(p, "guild info")));
        set(12, tile(ForgeIcons.TILE_BASE,
                "<aqua><bold>Members (" + guild.size() + ")",
                memberNames.isEmpty() ? List.of("<gray>No members.")
                        : memberNames.stream().limit(8)
                                .map(m -> "<gray>" + Text.escape(m)).toList(),
                p -> runCommand(p, "guild info")));
        set(14, tile(ForgeIcons.TILE_GOLD,
                "<gold><bold>Guild Bank",
                List.of("<gray>Balance: <green>$" + String.format("%.2f", guild.bank()),
                        "", "<yellow>Click to deposit",
                        "<gray>Type amount in chat"),
                p -> runCommandWithInput(p,
                        "Type amount to deposit:", "guild bank deposit")));
        set(16, tile(ForgeIcons.TILE_GREEN,
                "<green><bold>Territory (" + claims + " claims)",
                List.of("<gray>View your claim map.",
                        "", "<green>Click to view"),
                p -> runCommand(p, "claimmap")));
        set(22, tile(ForgeIcons.TILE_RED,
                "<red><bold>Guild War",
                List.of("<gray>Challenge another guild.",
                        "", "<yellow>Click to enter guild name"),
                p -> runCommandWithInput(p,
                        "Type the guild to challenge:", "guild war")));
    }
}
