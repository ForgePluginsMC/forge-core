package com.forge.core.gui;

import com.forge.core.ForgeCore;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Player Tools section: movement, appearance, utility, fun.
 *
 * <p>Glyph-rendered: button art is baked into the title glyph; slots hold
 * invisible click targets with tooltips.
 */
@NullMarked
public final class PlayerSectionGui extends GlyphGui {
    /** PUA glyphs for player-tools pages 1-3. */
    private static final String[] GLYPHS = {"\uE105", "\uE106", "\uE107"};

    private final ForgeCore plugin;
    private final WebGui parent;
    private final int page;

    public PlayerSectionGui(ForgeCore plugin, WebGui parent) {
        this(plugin, parent, 0);
    }

    public PlayerSectionGui(ForgeCore plugin, WebGui parent, int page) {
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
        return List.of("Menu", "Player Tools");
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
        out.add(new SecButton("FLY", "Toggle flight mode",
                p -> runCmd(p, "fly")));
        out.add(new SecButton("FLY SPEED", "Adjust fly speed",
                p -> inputCmd(p, "Speed (1-10):", "flyspeed")));
        out.add(new SecButton("GOD", "Toggle invincibility",
                p -> runCmd(p, "god")));
        out.add(new SecButton("WALK SPEED", "Adjust walk speed",
                p -> inputCmd(p, "Speed (1-10):", "walkspeed")));
        out.add(new SecButton("HEAL", "Restore health",
                p -> runCmd(p, "heal")));
        out.add(new SecButton("FEED", "Restore hunger",
                p -> runCmd(p, "feed")));
        out.add(new SecButton("NICK", "Set your nickname",
                p -> inputCmd(p, "Nickname:", "nick")));
        out.add(new SecButton("SKIN", "Change your skin",
                p -> inputCmd(p, "Player name:", "skin")));
        out.add(new SecButton("HAT", "Wear held item as hat",
                p -> runCmd(p, "hat")));
        out.add(new SecButton("DYE", "Color leather armor",
                p -> inputCmd(p, "Color name or hex:", "dye")));
        out.add(new SecButton("CRAFT", "Open crafting table",
                p -> runCmd(p, "workbench")));
        out.add(new SecButton("E-CHEST", "Open ender chest",
                p -> runCmd(p, "enderchest")));
        out.add(new SecButton("ANVIL", "Open anvil",
                p -> runCmd(p, "anvil")));
        out.add(new SecButton("REPAIR", "Repair held item",
                p -> runCmd(p, "repair")));
        out.add(new SecButton("BOLT", "Strike lightning",
                p -> runCmd(p, "lightning")));
        out.add(new SecButton("FIREBALL", "Launch a fireball",
                p -> runCmd(p, "fireball")));
        out.add(new SecButton("UNLIM", "Infinite block placement",
                p -> runCmd(p, "unlimited")));
        return out;
    }

    @Override
    protected void buildContent(Player viewer) {
        List<SecButton> all = buttons();
        int start = page * 6;
        for (int i = 0; i < 6 && start + i < all.size(); i++) {
            SecButton b = all.get(start + i);
            GuiItem item = ghostTip("<aqua><bold>" + b.label(),
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
            set(SLOT_PREV, ghost(p -> new PlayerSectionGui(plugin, parent, page - 1).open(p)));
        }
        if (page < totalPages - 1) {
            set(SLOT_NEXT, ghost(p -> new PlayerSectionGui(plugin, parent, page + 1).open(p)));
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
