package com.forge.core.gui;

import java.util.List;
import java.util.function.Consumer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

/**
 * Base class for glyph-rendered GUIs.
 *
 * <p>The entire GUI visual (header, buttons, footer, player inventory
 * styling) is drawn by a single wide bitmap glyph in the inventory title
 * ({@code minecraft:forge_cards} font). Slots hold invisible items that
 * provide click targets.
 *
 * <p>Layout (54 slots, 176x222 glyph):
 * <ul>
 *   <li>Rows 0-1 (slots 0-35): content — 6 card buttons (3x2 slots each)
 *   <li>Row 5 (slots 45-53): footer — back, pagination, close (baked art)
 * </ul>
 */
@NullMarked
public abstract class GlyphGui extends WebGui {
    /** Glyph font for menu art. */
    protected static final Key GLYPH_FONT = Key.key("minecraft:forge_cards");

    /**
     * Negative-space prefix: the inventory title renders at x=8, so shift
     * left 8px to align the glyph image with the container edge (x=0).
     */
    protected static final String ALIGN_LEFT = "\uF803";

    /** Card slot groups: 6 slots each (3 wide x 2 tall). */
    protected static final int[][] CARD_SLOTS = {
        {0, 1, 2, 9, 10, 11},
        {3, 4, 5, 12, 13, 14},
        {6, 7, 8, 15, 16, 17},
        {18, 19, 20, 27, 28, 29},
        {21, 22, 23, 30, 31, 32},
        {24, 25, 26, 33, 34, 35},
    };

    /** PUA char of this GUI's background glyph. */
    protected abstract String glyphChar();

    @Override
    protected Component title() {
        return Component.text(ALIGN_LEFT + glyphChar()).font(GLYPH_FONT);
    }

    @Override
    protected void buildHeader(Player viewer) {
        // Header is baked into the title glyph.
    }

    @Override
    protected void buildFooter(Player viewer) {
        // Footer art is baked into the glyph; place invisible click targets.
        if (parent() != null) {
            set(SLOT_BACK, ghost(p -> parent().open(p)));
        }
        set(SLOT_CLOSE, ghost(Player::closeInventory));
    }

    @Override
    protected void setupPagination(Player viewer) {
        // Subclasses with pages override buildFooter to add prev/next ghosts.
    }

    /** Invisible filler so the glyph background shows through cleanly. */
    @Override
    protected void fillEmpty() {
        GuiItem filler = GuiItem.of(Material.PAPER)
                .model(ForgeIcons.INVISIBLE)
                .hideTooltip()
                .name(" ");
        for (int i = 0; i < size(); i++) {
            setIfAbsent(i, filler);
        }
    }

    /**
     * Create an invisible click-target item with no tooltip.
     *
     * @param action what happens on click
     * @return the ghost item
     */
    protected static GuiItem ghost(Consumer<Player> action) {
        return GuiItem.of(Material.PAPER)
                .model(ForgeIcons.INVISIBLE)
                .hideTooltip()
                .name(" ")
                .action(action);
    }

    /**
     * Create an invisible click-target that keeps its tooltip (for buttons
     * whose baked art uses an abbreviated label).
     *
     * @param name MiniMessage display name
     * @param lore MiniMessage lore lines
     * @param action what happens on click
     * @return the ghost item with tooltip
     */
    protected static GuiItem ghostTip(String name, List<String> lore, Consumer<Player> action) {
        return GuiItem.of(Material.PAPER)
                .model(ForgeIcons.INVISIBLE)
                .name(name)
                .lore(lore)
                .action(action);
    }

    /**
     * Create a bright button-tile item for dynamic list entries.
     *
     * @param tileModel one of the ForgeIcons.TILE_* keys
     * @param name MiniMessage display name
     * @param lore MiniMessage lore lines
     * @param action what happens on click
     * @return the tile item
     */
    protected static GuiItem tile(String tileModel, String name, List<String> lore,
            Consumer<Player> action) {
        return GuiItem.of(Material.PAPER)
                .model(tileModel)
                .name(name)
                .lore(lore)
                .action(action);
    }
}
