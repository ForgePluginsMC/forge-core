package com.forge.core.gui;

import com.forge.core.ForgeCore;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * NPCs & Dialogs section.
 *
 * <p>Glyph-rendered: button art is baked into the title glyph; slots hold
 * invisible click targets with tooltips.
 */
@NullMarked
public final class NpcSectionGui extends GlyphGui {
    /** PUA glyphs for NPC pages 1-2. */
    private static final String[] GLYPHS = {"\uE10A", "\uE10B"};

    private final ForgeCore plugin;
    private final WebGui parent;
    private final int page;

    public NpcSectionGui(ForgeCore plugin, WebGui parent) {
        this(plugin, parent, 0);
    }

    public NpcSectionGui(ForgeCore plugin, WebGui parent, int page) {
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
        return List.of("Menu", "NPCs");
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
        out.add(new SecButton("CREATE", "Spawn an NPC at your location",
                p -> inputCmd(p, "NPC id and name (e.g. shopkeeper Bob):", "npc create")));
        out.add(new SecButton("LIST", "View all NPCs",
                p -> runCmd(p, "npc list")));
        out.add(new SecButton("MOVE", "Teleport NPC to you",
                p -> inputCmd(p, "NPC id:", "npc move")));
        out.add(new SecButton("REMOVE", "Delete an NPC",
                p -> inputCmd(p, "NPC id:", "npc remove")));
        out.add(new SecButton("ATTACH", "Link a dialog to an NPC",
                p -> inputCmd(p, "NPC id and dialog id:", "npc dialog")));
        out.add(new SecButton("NEW DIALOG", "Build a new dialog",
                p -> inputCmd(p, "Dialog id and title:", "dialog create")));
        out.add(new SecButton("DIALOGS", "View all dialogs",
                p -> runCmd(p, "dialog list")));
        out.add(new SecButton("SHOW", "Display a dialog",
                p -> inputCmd(p, "Dialog id:", "dialog show")));
        return out;
    }

    @Override
    protected void buildContent(Player viewer) {
        List<SecButton> all = buttons();
        int start = page * 6;
        for (int i = 0; i < 6 && start + i < all.size(); i++) {
            SecButton b = all.get(start + i);
            GuiItem item = ghostTip("<yellow><bold>" + b.label(),
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
            set(SLOT_PREV, ghost(p -> new NpcSectionGui(plugin, parent, page - 1).open(p)));
        }
        if (page < totalPages - 1) {
            set(SLOT_NEXT, ghost(p -> new NpcSectionGui(plugin, parent, page + 1).open(p)));
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
