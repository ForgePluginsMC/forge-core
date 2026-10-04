package com.forge.core.gui;

import com.forge.core.ForgeCore;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Economy section: personal finance, shops, admin.
 */
@NullMarked
public final class EconomySectionGui extends WebGui {
    private final ForgeCore plugin;
    private final WebGui parent;

    public EconomySectionGui(ForgeCore plugin, WebGui parent) {
        this.plugin = plugin;
        this.parent = parent;
    }

    @Override
    protected List<String> breadcrumb() {
        return List.of("Menu", "Economy");
    }

    @Override
    protected @Nullable WebGui parent() {
        return parent;
    }

    @Override
    protected void buildContent(Player viewer) {
        // Personal
        set(10, cmdCard("Balance", ForgeIcons.ICON_ECONOMY,
                "Check your balance",
                p -> runCmd(p, "balance")));
        set(11, cmdCard("Pay Player", ForgeIcons.BUTTON_SUCCESS,
                "Send money to someone",
                p -> inputCmd(p, "Player and amount (e.g. Steve 100):", "pay")));
        set(12, cmdCard("Top Balances", ForgeIcons.ICON_ECONOMY,
                "Richest players",
                p -> runCmd(p, "baltop")));

        // Shop
        set(14, cmdCard("Sell Item", ForgeIcons.BUTTON_SUCCESS,
                "Sell held item",
                p -> runCmd(p, "sell hand")));
        set(15, cmdCard("Item Worth", ForgeIcons.ICON_ECONOMY,
                "Check item value",
                p -> runCmd(p, "worth")));
        set(16, cmdCard("Price List", ForgeIcons.ICON_ECONOMY,
                "Browse item prices",
                p -> runCmd(p, "worthlist")));

        // Cheques
        set(19, cmdCard("Write Cheque", ForgeIcons.ICON_MAIL,
                "Create a money cheque",
                p -> inputCmd(p, "Amount:", "cheque")));

        // Kits (economy-adjacent)
        set(21, card("Kits", ForgeIcons.ICON_KIT,
                "Claim free kits",
                p -> new KitGui(plugin, this).open(p)));

        // Admin
        set(23, cmdCard("Give Money", ForgeIcons.BUTTON_SUCCESS,
                "Admin: give money",
                p -> inputCmd(p, "Player and amount:", "eco give")));
        set(24, cmdCard("Take Money", ForgeIcons.BUTTON_DANGER,
                "Admin: take money",
                p -> inputCmd(p, "Player and amount:", "eco take")));
        set(25, cmdCard("Set Worth", ForgeIcons.BUTTON_SECONDARY,
                "Admin: set item price",
                p -> inputCmd(p, "Price:", "setworth")));
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
