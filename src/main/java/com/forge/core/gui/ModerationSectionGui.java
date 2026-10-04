package com.forge.core.gui;

import com.forge.core.ForgeCore;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Moderation section: punishments, player info, management.
 */
@NullMarked
public final class ModerationSectionGui extends WebGui {
    private final ForgeCore plugin;
    private final WebGui parent;

    public ModerationSectionGui(ForgeCore plugin, WebGui parent) {
        this.plugin = plugin;
        this.parent = parent;
    }

    @Override
    protected List<String> breadcrumb() {
        return List.of("Menu", "Moderation");
    }

    @Override
    protected @Nullable WebGui parent() {
        return parent;
    }

    @Override
    protected void buildContent(Player viewer) {
        // Punishments
        set(10, cmdCard("Ban Player", ForgeIcons.BUTTON_DANGER,
                "Ban a player from the server",
                p -> inputCmd(p, "Player to ban:", "ban")));
        set(11, cmdCard("Temp Ban", ForgeIcons.BUTTON_DANGER,
                "Temporarily ban a player",
                p -> inputCmd(p, "Player and duration (e.g. Steve 1d):", "tempban")));
        set(12, cmdCard("Kick Player", ForgeIcons.BUTTON_DANGER,
                "Kick a player",
                p -> inputCmd(p, "Player to kick:", "kick")));
        set(13, cmdCard("Mute Player", ForgeIcons.BUTTON_DANGER,
                "Mute a player's chat",
                p -> inputCmd(p, "Player to mute:", "mute")));
        set(14, cmdCard("Warn Player", ForgeIcons.BUTTON_DANGER,
                "Issue a warning",
                p -> inputCmd(p, "Player and reason:", "warn")));
        set(15, cmdCard("Ban IP", ForgeIcons.BUTTON_DANGER,
                "Ban an IP address",
                p -> inputCmd(p, "IP or player:", "banip")));
        set(16, cmdCard("Jail Player", ForgeIcons.BUTTON_DANGER,
                "Jail a player",
                p -> inputCmd(p, "Player to jail:", "jail")));

        // Player Info
        set(19, card("Online Players", ForgeIcons.STATUS_ONLINE,
                "View and manage players",
                p -> new PlayerListGui(plugin, this).open(p)));
        set(20, cmdCard("Player Info", ForgeIcons.ICON_TOOLS,
                "Check player details",
                p -> inputCmd(p, "Player name:", "seen")));
        set(21, cmdCard("Check Ban", ForgeIcons.ICON_TOOLS,
                "Check ban status",
                p -> inputCmd(p, "Player name:", "checkban")));

        // Management
        set(23, card("Ban List", ForgeIcons.BUTTON_DANGER,
                "View and manage bans",
                p -> new BanListGui(plugin, this).open(p)));
        set(24, cmdCard("Unban", ForgeIcons.BUTTON_SUCCESS,
                "Unban a player",
                p -> inputCmd(p, "Player to unban:", "unban")));
        set(25, cmdCard("Unmute", ForgeIcons.BUTTON_SUCCESS,
                "Unmute a player",
                p -> inputCmd(p, "Player to unmute:", "unmute")));

        // Tools
        set(28, cmdCard("Vanish", ForgeIcons.BUTTON_SECONDARY,
                "Toggle invisibility",
                p -> runCmd(p, "vanish")));
        set(29, cmdCard("Social Spy", ForgeIcons.BUTTON_SECONDARY,
                "Monitor private messages",
                p -> runCmd(p, "socialspy")));
        set(30, cmdCard("Clear Chat", ForgeIcons.BUTTON_SECONDARY,
                "Clear public chat",
                p -> runCmd(p, "clearchat")));
    }

    private GuiItem card(String name, String icon, String desc, Consumer<Player> action) {
        return GuiItem.card(icon, "<gold><bold>" + name, desc).action(action);
    }

    private GuiItem cmdCard(String name, String icon, String desc, Consumer<Player> action) {
        return GuiItem.card(icon, "<red><bold>" + name, desc).action(action);
    }

    private void runCmd(Player p, String cmd) {
        p.closeInventory();
        p.performCommand(cmd);
    }

    private void inputCmd(Player p, String prompt, String cmd) {
        p.closeInventory();
        com.forge.core.util.Text.send(p, "<yellow>" + prompt + " <gray>(type in chat, or 'cancel')");
        ChatInput.request(p, input -> {
            if (input.equalsIgnoreCase("cancel")) {
                com.forge.core.util.Text.send(p, "<gray>Cancelled.");
                return;
            }
            p.performCommand(cmd + " " + input);
        });
    }
}
