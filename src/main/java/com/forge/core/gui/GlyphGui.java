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

    /**
     * Hotbar navigation slots (raw slots). The hotbar is the nav bar:
     * 81 back, 83 prev, 84 page info, 85 next, 88 close.
     */
    protected static final int HOTBAR_BACK = 81;
    protected static final int HOTBAR_PREV = 84;
    protected static final int HOTBAR_PAGE = 85;
    protected static final int HOTBAR_NEXT = 86;
    protected static final int HOTBAR_CLOSE = 89;

    @Override
    protected Component title() {
        return Component.text(ALIGN_LEFT + glyphChar()).font(GLYPH_FONT);
    }

    @Override
    protected void buildHeader(Player viewer) {
        // Header is baked into the title glyph.
    }

    @Override
    protected void addBackButton() {
        // No-op: back button is in the hotbar via buildFooter().
    }

    /**
     * Hotbar navigation. Chest row 5 is content space now; nav lives in
     * the hotbar as visible button items.
     */
    @Override
    protected void buildFooter(Player viewer) {
        if (parent() != null) {
            set(HOTBAR_BACK, GuiItem.of(Material.PAPER)
                    .model(ForgeIcons.BUTTON_BACK)
                    .name("<red><bold>Back")
                    .lore("<gray>Return to the previous menu.")
                    .action(p -> parent().open(p)));
        }
        setupPagination(viewer);
        set(HOTBAR_CLOSE, GuiItem.of(Material.PAPER)
                .model(ForgeIcons.BUTTON_DANGER)
                .name("<red><bold>Close")
                .lore("<gray>Close this menu.")
                .action(Player::closeInventory));
    }

    @Override
    protected void setupPagination(Player viewer) {
        // Subclasses with pages override buildFooter to add prev/next.
    }

    /**
     * Hotbar pagination buttons.
     *
     * @param viewer the viewer
     * @param page current page (0-based)
     * @param totalPages total page count
     * @param gui the paginated gui (for page navigation)
     */
    protected final void addHotbarPagination(
            Player viewer, int page, int totalPages, PaginatedGui<?> gui) {
        if (totalPages > 1) {
            if (page > 0) {
                set(HOTBAR_PREV, GuiItem.of(Material.PAPER)
                        .model(ForgeIcons.ARROW_LEFT)
                        .name("<yellow><bold>Previous Page")
                        .lore("<gray>Go to page " + page + " of " + totalPages)
                        .action(p -> gui.openPage(p, page - 1)));
            }
            StringBuilder dots = new StringBuilder();
            for (int i = 0; i < Math.min(totalPages, 10); i++) {
                dots.append(i == page ? "<gold>●" : "<dark_gray>○");
            }
            set(HOTBAR_PAGE, GuiItem.of(Material.PAPER)
                    .model(page == 0 ? ForgeIcons.DOT_ACTIVE : ForgeIcons.DOT_INACTIVE)
                    .name("<yellow>Page " + (page + 1) + " <gray>of " + totalPages)
                    .lore(dots.toString()));
            if (page < totalPages - 1) {
                set(HOTBAR_NEXT, GuiItem.of(Material.PAPER)
                        .model(ForgeIcons.ARROW_RIGHT)
                        .name("<yellow><bold>Next Page")
                        .lore("<gray>Go to page " + (page + 2) + " of " + totalPages)
                        .action(p -> gui.openPage(p, page + 1)));
            }
        }
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
