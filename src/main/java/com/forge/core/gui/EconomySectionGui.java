package com.forge.core.gui;

import com.forge.core.ForgeCore;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Economy section: personal finance, shops, admin.
 *
 * <p>Glyph-rendered: button art is baked into the title glyph; slots hold
 * invisible click targets with tooltips.
 */
@NullMarked
public final class EconomySectionGui extends GlyphGui {
    /** PUA glyphs for economy pages 1-2. */
    private static final String[] GLYPHS = {"\uE108", "\uE109"};

    private final ForgeCore plugin;
    private final WebGui parent;
    private final int page;

    public EconomySectionGui(ForgeCore plugin, WebGui parent) {
        this(plugin, parent, 0);
    }

    public EconomySectionGui(ForgeCore plugin, WebGui parent, int page) {
        this.plugin = plugin;
        this.parent = parent;
        this.page = Math.max(0, page);
    }

    @Override
    protected String glyphChar() {
        return GLYPHS[Math.min(page, GLYPHS.length - 1)];
    }

    @Override
    protected List<String> breadcrumb() {
        return List.of("Menu", "Economy");
    }

    @Override
    protected @Nullable WebGui parent() {
        return parent;
    }

    /** A section button: short baked label, tooltip description, click action. */
    private record SecButton(String label, String desc, Consumer<Player> action) {
    }

    private List<SecButton> buttons() {
        List<SecButton> out = new ArrayList<>();
        out.add(new SecButton("BALANCE", "Check your balance",
                p -> runCmd(p, "balance")));
        out.add(new SecButton("PAY", "Send money to someone",
                p -> inputCmd(p, "Player and amount (e.g. Steve 100):", "pay")));
        out.add(new SecButton("BALTOP", "Richest players",
                p -> runCmd(p, "baltop")));
        out.add(new SecButton("SELL", "Sell held item",
                p -> runCmd(p, "sell hand")));
        out.add(new SecButton("WORTH", "Check item value",
                p -> runCmd(p, "worth")));
        out.add(new SecButton("PRICES", "Browse item prices",
                p -> runCmd(p, "worthlist")));
        out.add(new SecButton("CHEQUE", "Create a money cheque",
                p -> inputCmd(p, "Amount:", "cheque")));
        out.add(new SecButton("KITS", "Claim free kits",
                p -> new KitGui(plugin, this).open(p)));
        out.add(new SecButton("ECO GIVE", "Admin: give money",
                p -> inputCmd(p, "Player and amount:", "eco give")));
        out.add(new SecButton("ECO TAKE", "Admin: take money",
                p -> inputCmd(p, "Player and amount:", "eco take")));
        out.add(new SecButton("SET WORTH", "Admin: set item price",
                p -> inputCmd(p, "Price:", "setworth")));
        return out;
    }

    @Override
    protected void buildContent(Player viewer) {
        List<SecButton> all = buttons();
        int start = page * 6;
        for (int i = 0; i < 6 && start + i < all.size(); i++) {
            SecButton b = all.get(start + i);
            GuiItem item = ghostTip("<gold><bold>" + b.label(),
                    List.of("<gray>" + b.desc()), b.action());
            for (int slot : CARD_SLOTS[i]) {
                set(slot, item);
            }
        }
    }

    @Override
    protected void buildFooter(Player viewer) {
        super.buildFooter(viewer);
        int totalPages = (buttons().size() + 5) / 6;
        if (page > 0) {
            set(SLOT_PREV, ghost(p -> new EconomySectionGui(plugin, parent, page - 1).open(p)));
        }
        if (page < totalPages - 1) {
            set(SLOT_NEXT, ghost(p -> new EconomySectionGui(plugin, parent, page + 1).open(p)));
        }
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
