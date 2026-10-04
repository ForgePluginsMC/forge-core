package com.forge.core.gui;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Base class for paginated web-style GUIs.
 *
 * @param <T> the type of items being displayed
 */
@NullMarked
public abstract class PaginatedGui<T> extends WebGui {
    /** Items per page (36 content slots). */
    protected static final int PAGE_SIZE = 36;

    private final int page;

    protected PaginatedGui() {
        this(0);
    }

    protected PaginatedGui(int page) {
        this.page = page;
    }

    /** All items to display (before pagination). */
    protected abstract List<T> allItems(Player viewer);

    /** Convert an item to a clickable card. */
    protected abstract GuiItem toCard(Player viewer, T item);

    /** Create a new instance for the given page. */
    protected abstract PaginatedGui<T> withPage(int page);

    /** Open a specific page for the player. */
    final void openPage(Player player, int newPage) {
        withPage(newPage).open(player);
    }

    /** Current page (0-indexed). */
    protected final int page() {
        return page;
    }

    @Override
    protected final void buildContent(Player viewer) {
        List<T> all = new ArrayList<>(allItems(viewer));
        int totalPages = Math.max(1, (all.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int safePage = Math.min(page, totalPages - 1);
        int start = safePage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, all.size());

        List<GuiItem> cards = new ArrayList<>();
        for (int i = start; i < end; i++) {
            cards.add(toCard(viewer, all.get(i)));
        }
        setContent(cards);

        if (all.isEmpty()) {
            set(CONTENT_SLOTS[13], GuiItem.of(org.bukkit.Material.PAPER)
                    .model(ForgeIcons.DOT_INACTIVE)
                    .name("<gray>No items found")
                    .lore("<dark_gray>Nothing to display here."));
        }
    }

    @Override
    protected final void setupPagination(Player viewer) {
        List<T> all = allItems(viewer);
        int totalPages = Math.max(1, (all.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int safePage = Math.min(page, totalPages - 1);
        addPagination(viewer, safePage, totalPages, this);
    }

    @Override
    protected @Nullable WebGui parent() {
        // Subclasses override to provide parent
        return null;
    }
}
