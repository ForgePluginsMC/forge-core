package com.forge.core.gui;

import com.forge.core.ForgeCore;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Teleport section: homes, warps, requests, admin teleports.
 *
 * <p>Glyph-rendered: button art is baked into the title glyph; slots hold
 * invisible click targets with tooltips.
 */
@NullMarked
public final class TeleportSectionGui extends GlyphGui {
    /** PUA glyphs for teleport pages 1-3. */
    private static final String[] GLYPHS = {"\uE102", "\uE103", "\uE104"};

    private final ForgeCore plugin;
    private final WebGui parent;
    private final int page;

    public TeleportSectionGui(ForgeCore plugin, WebGui parent) {
        this(plugin, parent, 0);
    }

    public TeleportSectionGui(ForgeCore plugin, WebGui parent, int page) {
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
        return List.of("Menu", "Teleport");
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
        out.add(new SecButton("HOMES", "Teleport to your saved homes",
                p -> new HomeGui(plugin, this).open(p)));
        out.add(new SecButton("SET HOME", "Save your current location",
                p -> inputCmd(p, "Name your home:", "sethome")));
        out.add(new SecButton("DEL HOME", "Remove a saved home",
                p -> inputCmd(p, "Home to delete:", "removehome")));
        out.add(new SecButton("WARPS", "Browse server warp points",
                p -> new WarpGui(plugin, this).open(p)));
        out.add(new SecButton("SET WARP", "Create a warp (admin)",
                p -> inputCmd(p, "Warp name:", "setwarp")));
        out.add(new SecButton("TPA", "Ask to teleport to a player",
                p -> inputCmd(p, "Player name:", "tpa")));
        out.add(new SecButton("TPA HERE", "Ask a player to teleport to you",
                p -> inputCmd(p, "Player name:", "tpahere")));
        out.add(new SecButton("ACCEPT", "Accept pending teleport",
                p -> runCmd(p, "tpaccept")));
        out.add(new SecButton("DENY", "Deny pending teleport",
                p -> runCmd(p, "tpdeny")));
        out.add(new SecButton("TPA TOGGLE", "Block/unblock teleport requests",
                p -> runCmd(p, "tptoggle")));
        out.add(new SecButton("TELEPORT", "Teleport to player/location",
                p -> inputCmd(p, "Player or coordinates:", "teleport")));
        out.add(new SecButton("TP HERE", "Bring player to you",
                p -> inputCmd(p, "Player name:", "tphere")));
        out.add(new SecButton("BACK", "Return to last location",
                p -> runCmd(p, "back")));
        out.add(new SecButton("SPAWN", "Teleport to spawn",
                p -> runCmd(p, "spawn")));
        out.add(new SecButton("RTP", "Teleport to random location",
                p -> runCmd(p, "rtp")));
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
            set(HOTBAR_PREV, GuiItem.of(org.bukkit.Material.PAPER).model(ForgeIcons.ARROW_LEFT).name("<yellow><bold>Previous Page").lore("<gray>Go back one page").action(p -> new TeleportSectionGui(plugin, parent, page - 1).open(p)));
        }
        if (page < totalPages - 1) {
            set(HOTBAR_NEXT, GuiItem.of(org.bukkit.Material.PAPER).model(ForgeIcons.ARROW_RIGHT).name("<yellow><bold>Next Page").lore("<gray>Go forward one page").action(p -> new TeleportSectionGui(plugin, parent, page + 1).open(p)));
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
