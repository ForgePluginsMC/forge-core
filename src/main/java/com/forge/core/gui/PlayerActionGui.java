package com.forge.core.gui;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Quick actions for a single player: teleport, moderate, economy.
 */
@NullMarked
public final class PlayerActionGui extends ForgeGui {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final ForgeCore plugin;
    private final Player target;
    private final ForgeGui parent;

    public PlayerActionGui(ForgeCore plugin, Player target, ForgeGui parent) {
        this.plugin = plugin;
        this.target = target;
        this.parent = parent;
    }

    @Override
    protected Component title() {
        return MM.deserialize("<aqua><bold>" + Text.escape(target.getName()));
    }

    @Override
    protected int size() {
        return 27;
    }

    @Override
    protected @Nullable ForgeGui parent() {
        return parent;
    }

    @Override
    protected void build(Player viewer) {
        String name = target.getName();
        boolean self = viewer.getUniqueId().equals(target.getUniqueId());

        set(10, GuiItem.of(Material.ENDER_PEARL)
                .name("<aqua>Teleport to " + Text.escape(name))
                .lore("<gray>Teleport to this player.", "", "<green>Click to teleport")
                .action(p -> runCommand(p, "tpa " + name)));
        set(11, GuiItem.of(Material.ENDER_EYE)
                .name("<aqua>Bring " + Text.escape(name))
                .lore("<gray>Teleport them to you.", "", "<green>Click to teleport")
                .action(p -> runCommand(p, "tpahere " + name)));
        if (!self) {
            set(12, GuiItem.of(Material.PAPER)
                    .name("<yellow>Message " + Text.escape(name))
                    .lore("<gray>Send a private message.", "", "<yellow>Click to type message")
                    .action(p -> runCommandWithInput(p,
                            "Type your message to " + name + ":", "msg " + name)));
            set(13, GuiItem.of(Material.GOLD_INGOT)
                    .name("<gold>Pay " + Text.escape(name))
                    .lore("<gray>Send money to this player.", "", "<yellow>Click to enter amount")
                    .action(p -> runCommandWithInput(p,
                            "Type the amount to pay " + name + ":", "money pay " + name)));
        }
        set(14, GuiItem.of(Material.BARRIER)
                .name("<red>Kick " + Text.escape(name))
                .lore("<gray>Kick with a reason.", "", "<yellow>Click to enter reason")
                .action(p -> {
                    if (!canModerate(viewer)) {
                        Text.error(p, "<red>You don't have permission.");
                        return;
                    }
                    runCommandWithInput(p, "Type kick reason:", "kick " + name);
                }));
        set(15, GuiItem.of(Material.IRON_BARS)
                .name("<dark_red>Ban " + Text.escape(name))
                .lore("<gray>Ban with a reason.", "", "<yellow>Click to enter reason")
                .action(p -> {
                    if (!canModerate(viewer)) {
                        Text.error(p, "<red>You don't have permission.");
                        return;
                    }
                    runCommandWithInput(p, "Type ban reason:", "ban " + name);
                }));
        set(16, GuiItem.of(Material.OAK_SIGN)
                .name("<gray>Mute " + Text.escape(name))
                .lore("<gray>Mute with a reason.", "", "<yellow>Click to enter reason")
                .action(p -> {
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
