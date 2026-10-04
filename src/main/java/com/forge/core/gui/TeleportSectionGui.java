package com.forge.core.gui;

import com.forge.core.ForgeCore;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Teleport section: homes, warps, requests, admin teleports.
 */
@NullMarked
public final class TeleportSectionGui extends WebGui {
    private final ForgeCore plugin;
    private final WebGui parent;

    public TeleportSectionGui(ForgeCore plugin, WebGui parent) {
        this.plugin = plugin;
        this.parent = parent;
    }

    @Override
    protected List<String> breadcrumb() {
        return List.of("Menu", "Teleport");
    }

    @Override
    protected @Nullable WebGui parent() {
        return parent;
    }

    @Override
    protected void buildContent(Player viewer) {
        // My Homes
        set(10, card("My Homes", ForgeIcons.ICON_HOME,
                "Teleport to your saved homes",
                p -> new HomeGui(plugin, this).open(p)));
        set(11, cmdCard("Set Home", ForgeIcons.BUTTON_SUCCESS,
                "Save your current location",
                p -> inputCmd(p, "Name your home:", "sethome")));
        set(12, cmdCard("Delete Home", ForgeIcons.BUTTON_DANGER,
                "Remove a saved home",
                p -> inputCmd(p, "Home to delete:", "removehome")));

        // Warps
        set(14, card("Warps", ForgeIcons.ICON_WARP,
                "Browse server warp points",
                p -> new WarpGui(plugin, this).open(p)));
        set(15, cmdCard("Set Warp", ForgeIcons.BUTTON_SUCCESS,
                "Create a warp (admin)",
                p -> inputCmd(p, "Warp name:", "setwarp")));

        // Requests
        set(19, cmdCard("Request Teleport", ForgeIcons.ICON_TELEPORT,
                "Ask to teleport to a player",
                p -> inputCmd(p, "Player name:", "tpa")));
        set(20, cmdCard("Teleport Here Request", ForgeIcons.ICON_TELEPORT,
                "Ask a player to teleport to you",
                p -> inputCmd(p, "Player name:", "tpahere")));
        set(21, cmdCard("Accept Request", ForgeIcons.BUTTON_SUCCESS,
                "Accept pending teleport",
                p -> runCmd(p, "tpaccept")));
        set(22, cmdCard("Deny Request", ForgeIcons.BUTTON_DANGER,
                "Deny pending teleport",
                p -> runCmd(p, "tpdeny")));
        set(23, cmdCard("Toggle Requests", ForgeIcons.BUTTON_SECONDARY,
                "Block/unblock teleport requests",
                p -> runCmd(p, "tptoggle")));

        // Admin
        set(28, cmdCard("Teleport", ForgeIcons.ICON_TELEPORT,
                "Teleport to player/location",
                p -> inputCmd(p, "Player or coordinates:", "teleport")));
        set(29, cmdCard("Teleport Here", ForgeIcons.ICON_TELEPORT,
                "Bring player to you",
                p -> inputCmd(p, "Player name:", "tphere")));
        set(30, cmdCard("Back", ForgeIcons.ARROW_LEFT,
                "Return to last location",
                p -> runCmd(p, "back")));
        set(31, cmdCard("Spawn", ForgeIcons.ICON_HOME,
                "Teleport to spawn",
                p -> runCmd(p, "spawn")));
        set(32, cmdCard("Random Teleport", ForgeIcons.ICON_WARP,
                "Teleport to random location",
                p -> runCmd(p, "rtp")));
    }

    private GuiItem card(String name, String icon, String desc, Consumer<Player> action) {
        return GuiItem.card(icon, "<gold><bold>" + name, desc).action(action);
    }

    private GuiItem cmdCard(String name, String icon, String desc, Consumer<Player> action) {
        return GuiItem.card(icon, "<yellow><bold>" + name, desc).action(action);
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
