package com.forge.core.gui;

import com.forge.core.ForgeCore;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * NPCs & Dialogs section.
 */
@NullMarked
public final class NpcSectionGui extends WebGui {
    private final ForgeCore plugin;
    private final WebGui parent;

    public NpcSectionGui(ForgeCore plugin, WebGui parent) {
        this.plugin = plugin;
        this.parent = parent;
    }

    @Override
    protected List<String> breadcrumb() {
        return List.of("Menu", "NPCs");
    }

    @Override
    protected @Nullable WebGui parent() {
        return parent;
    }

    @Override
    protected void buildContent(Player viewer) {
        set(10, cmdCard("Create NPC", ForgeIcons.BUTTON_SUCCESS,
                "Spawn an NPC at your location",
                p -> inputCmd(p, "NPC id and name (e.g. shopkeeper Bob):", "npc create")));
        set(11, cmdCard("List NPCs", ForgeIcons.ICON_NPC,
                "View all NPCs",
                p -> runCmd(p, "npc list")));
        set(12, cmdCard("Move NPC", ForgeIcons.ICON_NPC,
                "Teleport NPC to you",
                p -> inputCmd(p, "NPC id:", "npc move")));
        set(13, cmdCard("Remove NPC", ForgeIcons.BUTTON_DANGER,
                "Delete an NPC",
                p -> inputCmd(p, "NPC id:", "npc remove")));

        set(15, cmdCard("Attach Dialog", ForgeIcons.ICON_MAIL,
                "Link a dialog to an NPC",
                p -> inputCmd(p, "NPC id and dialog id:", "npc dialog")));

        set(19, cmdCard("Create Dialog", ForgeIcons.BUTTON_SUCCESS,
                "Build a new dialog",
                p -> inputCmd(p, "Dialog id and title:", "dialog create")));
        set(20, cmdCard("List Dialogs", ForgeIcons.ICON_MAIL,
                "View all dialogs",
                p -> runCmd(p, "dialog list")));
        set(21, cmdCard("Show Dialog", ForgeIcons.ICON_NPC,
                "Display a dialog",
                p -> inputCmd(p, "Dialog id:", "dialog show")));
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
