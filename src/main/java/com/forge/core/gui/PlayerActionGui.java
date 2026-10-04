package com.forge.core.gui;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Quick actions for a single player: teleport, moderate, economy.
 *
 * <p>Glyph-rendered background with bright button tiles.
 */
@NullMarked
public final class PlayerActionGui extends GlyphGui {
    private final ForgeCore plugin;
    private final Player target;
    private final WebGui parent;

    public PlayerActionGui(ForgeCore plugin, Player target, WebGui parent) {
        this.plugin = plugin;
        this.target = target;
        this.parent = parent;
    }

    @Override
    protected String glyphChar() {
        return "\uE110";
    }

    @Override
    protected List<String> breadcrumb() {
        return List.of("Menu", "Players", "Actions");
    }

    @Override
    protected @Nullable WebGui parent() {
        return parent;
    }

    @Override
    protected void buildContent(Player viewer) {
        String name = target.getName();
        boolean self = viewer.getUniqueId().equals(target.getUniqueId());
        int slot = 10;

        set(slot++, tile(ForgeIcons.TILE_BASE, "<aqua><bold>Teleport",
                List.of("<gray>Teleport to " + Text.escape(name) + ".",
                        "", "<green>Click to teleport"),
                p -> runCommand(p, "tpa " + name)));
        set(slot++, tile(ForgeIcons.TILE_BASE, "<aqua><bold>Bring Here",
                List.of("<gray>Teleport them to you.",
                        "", "<green>Click to teleport"),
                p -> runCommand(p, "tpahere " + name)));
        if (!self) {
            set(slot++, tile(ForgeIcons.TILE_YELLOW, "<yellow><bold>Message",
                    List.of("<gray>Send a private message.",
                            "", "<yellow>Click to type message"),
                    p -> runCommandWithInput(p,
                            "Type your message to " + name + ":", "msg " + name)));
            set(slot++, tile(ForgeIcons.TILE_GOLD, "<gold><bold>Pay",
                    List.of("<gray>Send money to this player.",
                            "", "<yellow>Click to enter amount"),
                    p -> runCommandWithInput(p,
                            "Type the amount to pay " + name + ":", "money pay " + name)));
        }
        set(slot++, tile(ForgeIcons.TILE_RED, "<red><bold>Kick",
                List.of("<gray>Kick with a reason.",
                        "", "<yellow>Click to enter reason"),
                p -> {
                    if (!canModerate(viewer)) {
                        Text.error(p, "<red>You don't have permission.");
                        return;
                    }
                    runCommandWithInput(p, "Type kick reason:", "kick " + name);
                }));
        set(slot++, tile(ForgeIcons.TILE_RED, "<dark_red><bold>Ban",
                List.of("<gray>Ban with a reason.",
                        "", "<yellow>Click to enter reason"),
                p -> {
                    if (!canModerate(viewer)) {
                        Text.error(p, "<red>You don't have permission.");
                        return;
                    }
                    runCommandWithInput(p, "Type ban reason:", "ban " + name);
                }));
        set(slot++, tile(ForgeIcons.TILE_GRAY, "<gray><bold>Mute",
                List.of("<gray>Mute with a reason.",
                        "", "<yellow>Click to enter reason"),
                p -> {
                    if (!canModerate(viewer)) {
                        Text.error(p, "<red>You don't have permission.");
                        return;
                    }
                    runCommandWithInput(p, "Type mute reason:", "mute " + name);
                }));
    }

    private boolean canModerate(Player viewer) {
        return plugin.permissions().hasPermission(viewer, "forgecore.kick")
                || plugin.permissions().hasPermission(viewer, "forgecore.ban")
                || viewer.isOp();
    }
}
