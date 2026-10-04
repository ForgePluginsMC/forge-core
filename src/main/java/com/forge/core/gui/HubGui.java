package com.forge.core.gui;

import com.forge.core.ForgeCore;
import java.util.List;
import java.util.function.Consumer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * The /menu hub: glyph-rendered card menu.
 *
 * <p>The entire menu visual (header, card buttons, footer) is drawn by a
 * single wide bitmap glyph in the inventory title
 * ({@code minecraft:forge_cards} font). Slots hold invisible items that
 * provide click targets; tooltips are hidden so only the button art shows.
 *
 * <p>Player-facing categories only. Admin tools live in /dashboard.
 *
 * <p>Layout (54 slots):
 * <ul>
 *   <li>Rows 0-1 (slots 0-17): card row 1 (3 cards)
 *   <li>Rows 2-3 (slots 18-35): card row 2 (3 cards)
 *   <li>Row 5 (slots 45-53): footer — mail, players, pagination, close
 * </ul>
 */
@NullMarked
public final class HubGui extends WebGui {
    /** Glyph font for menu pages. */
    private static final Key GLYPH_FONT = Key.key("minecraft:forge_cards");

    /** Page 1 glyph (6 cards), page 2 glyph (2 cards). */
    private static final String[] PAGE_GLYPHS = {"\uE100", "\uE101"};

    /**
     * Negative-space prefix: the inventory title renders at x=8, so shift
     * left 8px to align the glyph image with the container edge (x=0).
     */
    private static final String ALIGN_LEFT = "\uF803";

    /** Player-facing cards per page. */
    private static final List<List<String>> PAGES = List.of(
            List.of("teleport", "homes", "warps", "kits", "economy", "guilds"),
            List.of("quests", "player"));

    /** Card slot groups: 6 slots each (3 wide x 2 tall). */
    private static final int[][] CARD_SLOTS = {
        {0, 1, 2, 9, 10, 11},
        {3, 4, 5, 12, 13, 14},
        {6, 7, 8, 15, 16, 17},
        {18, 19, 20, 27, 28, 29},
        {21, 22, 23, 30, 31, 32},
        {24, 25, 26, 33, 34, 35},
    };

    /** Hotbar nav slots (raw). */
    private static final int SLOT_MAIL = 82;
    private static final int SLOT_PLAYERS = 83;
    private static final int SLOT_PREV = 84;
    private static final int SLOT_NEXT = 86;
    private static final int SLOT_CLOSE = 88;

    private final ForgeCore plugin;
    private final int page;

    public HubGui(ForgeCore plugin) {
        this(plugin, 0);
    }

    public HubGui(ForgeCore plugin, int page) {
        this.plugin = plugin;
        this.page = Math.max(0, Math.min(page, PAGES.size() - 1));
    }

    /** Open the hub for a player (page 1). */
    public static void open(ForgeCore plugin, Player player) {
        new HubGui(plugin).open(player);
    }

    @Override
    protected List<String> breadcrumb() {
        return List.of("Menu");
    }

    @Override
    protected @Nullable WebGui parent() {
        return null;
    }

    @Override
    protected Component title() {
        // Single wide glyph draws the entire menu UI, shifted to x=0.
        return Component.text(ALIGN_LEFT + PAGE_GLYPHS[page]).font(GLYPH_FONT);
    }

    @Override
    protected void buildHeader(Player viewer) {
        // Header is baked into the title glyph.
    }

    /** Create an invisible click-target item with no tooltip. */
    private static GuiItem ghost(Consumer<Player> action) {
        return GuiItem.of(Material.PAPER)
                .model(ForgeIcons.INVISIBLE)
                .hideTooltip()
                .name(" ")
                .action(action);
    }

    @Override
    protected void buildContent(Player viewer) {
        List<String> cards = PAGES.get(page);
        for (int i = 0; i < cards.size(); i++) {
            Consumer<Player> action = cardAction(cards.get(i));
            for (int slot : CARD_SLOTS[i]) {
                set(slot, ghost(action));
            }
        }
    }

    /** Get the action for a card category. */
    private Consumer<Player> cardAction(String id) {
        HubGui self = this;
        return switch (id) {
            case "teleport" -> p -> new TeleportSectionGui(plugin, self).open(p);
            case "homes" -> p -> new HomeGui(plugin, self).open(p);
            case "warps" -> p -> new WarpGui(plugin, self).open(p);
            case "kits" -> p -> new KitGui(plugin, self).open(p);
            case "economy" -> p -> new EconomySectionGui(plugin, self).open(p);
            case "guilds" -> p -> new GuildGui(plugin, self).open(p);
            case "quests" -> p -> {
                p.closeInventory();
                p.performCommand("quest");
            };
            case "player" -> p -> new PlayerSectionGui(plugin, self).open(p);
            default -> p -> {};
        };
    }

    @Override
    protected void buildFooter(Player viewer) {
        HubGui self = this;
        // Mail (tooltip kept so players know what it is)
        set(SLOT_MAIL, GuiItem.of(Material.PAPER)
                .model(ForgeIcons.INVISIBLE)
                .name("<aqua><bold>Mail")
                .lore("<gray>Read your messages.")
                .action(p -> new MailGui(plugin, self).open(p)));
        // Online players
        set(SLOT_PLAYERS, GuiItem.of(Material.PAPER)
                .model(ForgeIcons.INVISIBLE)
                .name("<green><bold>Online Players")
                .lore("<gray>View online players.")
                .action(p -> new PlayerListGui(plugin, self).open(p)));
        // Pagination (no tooltip — arrows are baked into the glyph)
        if (page > 0) {
            set(SLOT_PREV, GuiItem.of(Material.PAPER).model(ForgeIcons.ARROW_LEFT).name("<yellow><bold>Previous Page").lore("<gray>Go back one page").action(p -> new HubGui(plugin, page - 1).open(p)));
        }
        if (page < PAGES.size() - 1) {
            set(SLOT_NEXT, GuiItem.of(Material.PAPER).model(ForgeIcons.ARROW_RIGHT).name("<yellow><bold>Next Page").lore("<gray>Go forward one page").action(p -> new HubGui(plugin, page + 1).open(p)));
        }
        // Close (tooltip kept)
        set(SLOT_CLOSE, GuiItem.of(Material.PAPER)
                .model(ForgeIcons.INVISIBLE)
                .name("<red><bold>Close")
                .lore("<gray>Close this menu.")
                .action(Player::closeInventory));
    }
}
