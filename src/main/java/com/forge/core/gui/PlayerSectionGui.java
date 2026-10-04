package com.forge.core.gui;

import com.forge.core.ForgeCore;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Player Tools section: movement, appearance, utility, fun.
 */
@NullMarked
public final class PlayerSectionGui extends WebGui {
    private final ForgeCore plugin;
    private final WebGui parent;

    public PlayerSectionGui(ForgeCore plugin, WebGui parent) {
        this.plugin = plugin;
        this.parent = parent;
    }

    @Override
    protected List<String> breadcrumb() {
        return List.of("Menu", "Player Tools");
    }

    @Override
    protected @Nullable WebGui parent() {
        return parent;
    }

    @Override
    protected void buildContent(Player viewer) {
        // Movement
        set(10, toggleCard("Fly", ForgeIcons.ICON_TELEPORT,
                "Toggle flight mode", "fly"));
        set(11, cmdCard("Fly Speed", ForgeIcons.ICON_TELEPORT,
                "Adjust fly speed",
                p -> inputCmd(p, "Speed (1-10):", "flyspeed")));
        set(12, toggleCard("God Mode", ForgeIcons.BUTTON_SUCCESS,
                "Toggle invincibility", "god"));
        set(13, cmdCard("Walk Speed", ForgeIcons.ICON_TELEPORT,
                "Adjust walk speed",
                p -> inputCmd(p, "Speed (1-10):", "walkspeed")));
        set(14, cmdCard("Heal", ForgeIcons.BUTTON_SUCCESS,
                "Restore health",
                p -> runCmd(p, "heal")));
        set(15, cmdCard("Feed", ForgeIcons.BUTTON_SUCCESS,
                "Restore hunger",
                p -> runCmd(p, "feed")));

        // Appearance
        set(19, cmdCard("Nickname", ForgeIcons.ICON_NPC,
                "Set your nickname",
                p -> inputCmd(p, "Nickname:", "nick")));
        set(20, cmdCard("Skin", ForgeIcons.ICON_NPC,
                "Change your skin",
                p -> inputCmd(p, "Player name:", "skin")));
        set(21, cmdCard("Hat", ForgeIcons.ICON_TOOLS,
                "Wear held item as hat",
                p -> runCmd(p, "hat")));
        set(22, cmdCard("Dye Armor", ForgeIcons.ICON_TOOLS,
                "Color leather armor",
                p -> inputCmd(p, "Color name or hex:", "dye")));

        // Utility
        set(24, cmdCard("Crafting", ForgeIcons.ICON_TOOLS,
                "Open crafting table",
                p -> runCmd(p, "workbench")));
        set(25, cmdCard("Ender Chest", ForgeIcons.ICON_TOOLS,
                "Open ender chest",
                p -> runCmd(p, "enderchest")));
        set(28, cmdCard("Anvil", ForgeIcons.ICON_TOOLS,
                "Open anvil",
                p -> runCmd(p, "anvil")));
        set(29, cmdCard("Repair", ForgeIcons.BUTTON_SUCCESS,
                "Repair held item",
                p -> runCmd(p, "repair")));

        // Fun
        set(31, cmdCard("Lightning", ForgeIcons.BUTTON_DANGER,
                "Strike lightning",
                p -> runCmd(p, "lightning")));
        set(32, cmdCard("Fireball", ForgeIcons.BUTTON_DANGER,
                "Launch a fireball",
                p -> runCmd(p, "fireball")));
        set(33, toggleCard("Unlimited", ForgeIcons.ICON_TOOLS,
                "Infinite block placement", "unlimited"));
    }

    private GuiItem toggleCard(String name, String icon, String desc, String cmd) {
        return GuiItem.card(icon, "<aqua><bold>" + name, desc, "Toggle on/off")
                .action(p -> runCmd(p, cmd));
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
