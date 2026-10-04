package com.forge.core.gui;

import com.forge.core.util.Text;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Base class for web-dashboard-style GUIs.
 *
 * <p>Layout (54 slots):
 * <ul>
 *   <li>Row 0 (0-8): Header — breadcrumb navigation
 *   <li>Rows 1-4 (9-44): Content area — cards/features
 *   <li>Row 5 (45-53): Footer — back button, pagination, page indicator
 * </ul>
 */
@NullMarked
public abstract class WebGui extends ForgeGui {
    protected static final MiniMessage MM = MiniMessage.miniMessage();

    /** Content slots: rows 1-4 (36 slots). */
    protected static final int[] CONTENT_SLOTS = {
        9, 10, 11, 12, 13, 14, 15, 16, 17,
        18, 19, 20, 21, 22, 23, 24, 25, 26,
        27, 28, 29, 30, 31, 32, 33, 34, 35,
        36, 37, 38, 39, 40, 41, 42, 43, 44
    };

    /** Footer slots. */
    protected static final int SLOT_BACK = 45;
    protected static final int SLOT_PREV = 48;
    protected static final int SLOT_PAGE_INFO = 49;
    protected static final int SLOT_NEXT = 50;
    protected static final int SLOT_CLOSE = 53;

    @Override
    protected final int size() {
        return 54;
    }

    /** Breadcrumb path, e.g. ["Menu", "Teleport", "Homes"]. */
    protected abstract List<String> breadcrumb();

    @Override
    protected final Component title() {
        List<String> crumbs = breadcrumb();
        StringBuilder sb = new StringBuilder("<dark_gray>ForgeCore <gray>» ");
        for (int i = 0; i < crumbs.size(); i++) {
            if (i > 0) {
                sb.append("<dark_gray> » ");
            }
            if (i == crumbs.size() - 1) {
                sb.append("<gold><bold>").append(crumbs.get(i));
            } else {
                sb.append("<gray>").append(crumbs.get(i));
            }
        }
        return MM.deserialize(sb.toString());
    }

    /** Build the content area. Subclasses populate CONTENT_SLOTS. */
    protected abstract void buildContent(Player viewer);

    @Override
    protected final void build(Player viewer) {
        buildHeader(viewer);
        buildContent(viewer);
        buildFooter(viewer);
    }

    /** Header row: breadcrumb items (clickable for navigation). */
    protected void buildHeader(Player viewer) {
        // Header is rendered via the title; slots 0-8 can hold quick nav
        // Default: empty (title shows breadcrumb)
    }

    /** Footer row: back, pagination, close. */
    protected void buildFooter(Player viewer) {
        ForgeGui parent = parent();
        if (parent != null) {
            set(SLOT_BACK, GuiItem.of(Material.PAPER)
                    .model(ForgeIcons.BUTTON_BACK)
                    .name("<red><bold>Back")
                    .lore("<gray>Return to the previous menu.")
                    .action(p -> parent.open(p)));
        }

        // Pagination (overridden by PaginatedGui)
        setupPagination(viewer);

        set(SLOT_CLOSE, GuiItem.of(Material.PAPER)
                .model(ForgeIcons.BUTTON_DANGER)
                .name("<red><bold>Close")
                .lore("<gray>Close this menu.")
                .action(Player::closeInventory));
    }

    /** Setup pagination controls. Default: no pagination. */
    protected void setupPagination(Player viewer) {
        // Overridden by PaginatedGui
    }

    /** Add pagination buttons for the given page state. */
    protected final void addPagination(
            Player viewer, int page, int totalPages, PaginatedGui<?> gui) {
        if (totalPages <= 1) {
            // Show single page indicator
            set(SLOT_PAGE_INFO, GuiItem.of(Material.PAPER)
                    .model(ForgeIcons.DOT_ACTIVE)
                    .name("<gray>Page 1 of 1")
                    .lore("<dark_gray>Single page"));
            return;
        }

        // Previous button
        if (page > 0) {
            set(SLOT_PREV, GuiItem.of(Material.PAPER)
                    .model(ForgeIcons.ARROW_LEFT)
                    .name("<yellow><bold>Previous Page")
                    .lore("<gray>Go to page " + page + " of " + totalPages)
                    .action(p -> gui.openPage(p, page - 1)));
        } else {
            set(SLOT_PREV, GuiItem.of(Material.PAPER)
                    .model(ForgeIcons.DOT_INACTIVE)
                    .name("<dark_gray>No previous page")
                    .lore("<dark_gray>You're on the first page"));
        }

        // Page indicator with dots
        StringBuilder dots = new StringBuilder();
        for (int i = 0; i < Math.min(totalPages, 10); i++) {
            dots.append(i == page ? "<gold>●" : "<dark_gray>○");
        }
        set(SLOT_PAGE_INFO, GuiItem.of(Material.PAPER)
                .model(page == 0 ? ForgeIcons.DOT_ACTIVE : ForgeIcons.DOT_INACTIVE)
                .name("<yellow>Page " + (page + 1) + " <gray>of " + totalPages)
                .lore(dots.toString(), "<dark_gray>Use arrows to navigate"));

        // Next button
        if (page < totalPages - 1) {
            set(SLOT_NEXT, GuiItem.of(Material.PAPER)
                    .model(ForgeIcons.ARROW_RIGHT)
                    .name("<yellow><bold>Next Page")
                    .lore("<gray>Go to page " + (page + 2) + " of " + totalPages)
                    .action(p -> gui.openPage(p, page + 1)));
        } else {
            set(SLOT_NEXT, GuiItem.of(Material.PAPER)
                    .model(ForgeIcons.DOT_INACTIVE)
                    .name("<dark_gray>No next page")
                    .lore("<dark_gray>You're on the last page"));
        }
    }

    /** Fill content slots with items, starting from slot 9. */
    protected final void setContent(List<GuiItem> items) {
        for (int i = 0; i < items.size() && i < CONTENT_SLOTS.length; i++) {
            set(CONTENT_SLOTS[i], items.get(i));
        }
    }

    /** Send a toast-style message to the player. */
    protected final void notify(Player player, String message) {
        Text.send(player, message);
    }
}
