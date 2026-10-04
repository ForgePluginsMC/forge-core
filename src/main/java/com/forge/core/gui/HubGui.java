package com.forge.core.gui;

import com.forge.core.ForgeCore;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * The /menu hub: Hypixel-style illustrated category cards.
 *
 * <p>Layout (54 slots):
 * <ul>
 *   <li>Row 0: Title tab
 *   <li>Rows 1-2: Card row 1 (3 cards, each 3 wide x 2 tall)
 *   <li>Rows 3-4: Card row 2 (3 cards, each 3 wide x 2 tall)
 *   <li>Row 5: Footer — back, utilities, pagination, close
 * </ul>
 *
 * <p>Each card is 6 slots: top row shows a 3-piece illustration,
 * bottom row shows a 3-piece label bar. Clicking any piece opens the section.
 */
@NullMarked
public final class HubGui extends WebGui {
    private final ForgeCore plugin;
    private final int page;

    /** Card categories in display order. */
    private static final List<CardDef> CARDS = List.of(
            new CardDef("teleport", "Teleport", "Homes, warps, and teleport requests"),
            new CardDef("moderation", "Moderation", "Punishments and player management"),
            new CardDef("economy", "Economy", "Money, shops, and payments"),
            new CardDef("player", "Player", "Movement, appearance, and utilities"),
            new CardDef("guilds", "Guilds", "Guilds, territory, and wars"),
            new CardDef("quests", "Quests", "Quests, dailies, and weeklies"),
            new CardDef("warps", "Warps", "Browse server warps"),
            new CardDef("homes", "Homes", "Teleport to your homes"),
            new CardDef("kits", "Kits", "Claim available kits"));

    private static final int CARDS_PER_PAGE = 6;

    /** Card slot layouts: {artL, artM, artR, labelL, labelM, labelR}. */
    private static final int[][] CARD_SLOTS = {
        {9, 10, 11, 18, 19, 20},
        {12, 13, 14, 21, 22, 23},
        {15, 16, 17, 24, 25, 26},
        {27, 28, 29, 36, 37, 38},
        {30, 31, 32, 39, 40, 41},
        {33, 34, 35, 42, 43, 44},
    };

    public HubGui(ForgeCore plugin) {
        this(plugin, 0);
    }

    public HubGui(ForgeCore plugin, int page) {
        this.plugin = plugin;
        this.page = page;
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
    protected void buildHeader(Player viewer) {
        // Title tab at slot 4
        set(4, GuiItem.of(Material.PAPER)
                .model(ForgeIcons.ICON_TOOLS)
                .name("<gold><bold>ForgeCore Server Menu")
                .lore(
                        "<gray>Welcome, <yellow>" + viewer.getName() + "<gray>!",
                        "<gray>Click a card to open that section.",
                        "",
                        "<dark_gray>Tip: Use /resourcepack for the full UI theme"));
    }

    @Override
    protected void buildContent(Player viewer) {
        int totalPages = (CARDS.size() + CARDS_PER_PAGE - 1) / CARDS_PER_PAGE;
        int safePage = Math.min(page, totalPages - 1);
        int start = safePage * CARDS_PER_PAGE;

        for (int i = 0; i < CARDS_PER_PAGE; i++) {
            int cardIndex = start + i;
            if (cardIndex >= CARDS.size()) {
                break;
            }
            CardDef card = CARDS.get(cardIndex);
            placeCard(card, CARD_SLOTS[i]);
        }
    }

    /** Place a 3x2 illustrated card at the given slots. */
    private void placeCard(CardDef card, int[] slots) {
        Consumer<Player> action = cardAction(card.id());
        String name = "<gold><bold>" + card.name();
        List<String> lore = List.of(
                "<gray>" + card.description(),
                "",
                "<yellow>Click to open");

        // Art row (3 pieces)
        ForgeIcons.CardPiece[] pieces = ForgeIcons.CardPiece.values();
        for (int i = 0; i < 3; i++) {
            set(slots[i], GuiItem.of(Material.PAPER)
                    .model(ForgeIcons.cardArt(card.id(), pieces[i]))
                    .name(name)
                    .lore(lore)
                    .action(action));
        }
        // Label row (3 pieces)
        for (int i = 0; i < 3; i++) {
            set(slots[3 + i], GuiItem.of(Material.PAPER)
                    .model(ForgeIcons.cardLabel(card.id(), pieces[i]))
                    .name(name)
                    .lore(lore)
                    .action(action));
        }
    }

    /** Get the action for a card category. */
    private Consumer<Player> cardAction(String id) {
        HubGui self = this;
        return switch (id) {
            case "teleport" -> p -> new TeleportSectionGui(plugin, self).open(p);
            case "moderation" -> p -> new ModerationSectionGui(plugin, self).open(p);
            case "economy" -> p -> new EconomySectionGui(plugin, self).open(p);
            case "player" -> p -> new PlayerSectionGui(plugin, self).open(p);
            case "guilds" -> p -> new GuildGui(plugin, self).open(p);
            case "quests" -> p -> {
                p.closeInventory();
                p.performCommand("quest");
            };
            case "warps" -> p -> new WarpGui(plugin, self).open(p);
            case "homes" -> p -> new HomeGui(plugin, self).open(p);
            case "kits" -> p -> new KitGui(plugin, self).open(p);
            default -> p -> {};
        };
    }

    @Override
    protected void buildFooter(Player viewer) {
        // Utility icons
        HubGui self = this;
        set(46, GuiItem.of(Material.PAPER)
                .model(ForgeIcons.ICON_MAIL)
                .name("<aqua><bold>Mail")
                .lore("<gray>Read your messages.")
                .action(p -> new MailGui(plugin, self).open(p)));
        set(47, GuiItem.of(Material.PAPER)
                .model(ForgeIcons.STATUS_ONLINE)
                .name("<green><bold>Online Players")
                .lore("<gray>View and manage players.")
                .action(p -> new PlayerListGui(plugin, self).open(p)));
        set(51, GuiItem.of(Material.PAPER)
                .model(ForgeIcons.ICON_NPC)
                .name("<yellow><bold>NPCs")
                .lore("<gray>Manage NPCs and dialogs.")
                .action(p -> new NpcSectionGui(plugin, self).open(p)));
        set(52, GuiItem.of(Material.PAPER)
                .model(ForgeIcons.ICON_PERMISSION)
                .name("<light_purple><bold>Permissions")
                .lore("<gray>Groups, tracks, and nodes.")
                .action(p -> new PermissionGui(plugin, self).open(p)));

        // Pagination
        int totalPages = (CARDS.size() + CARDS_PER_PAGE - 1) / CARDS_PER_PAGE;
        if (totalPages > 1) {
            if (page > 0) {
                set(SLOT_PREV, GuiItem.of(Material.PAPER)
                        .model(ForgeIcons.ARROW_LEFT)
                        .name("<yellow><bold>Previous Page")
                        .lore("<gray>Go to page " + page + " of " + totalPages)
                        .action(p -> new HubGui(plugin, page - 1).open(p)));
            }
            StringBuilder dots = new StringBuilder();
            for (int i = 0; i < totalPages; i++) {
                dots.append(i == page ? "<gold>●" : "<dark_gray>○");
            }
            set(SLOT_PAGE_INFO, GuiItem.of(Material.PAPER)
                    .model(ForgeIcons.DOT_ACTIVE)
                    .name("<yellow>Page " + (page + 1) + " <gray>of " + totalPages)
                    .lore(dots.toString()));
            if (page < totalPages - 1) {
                set(SLOT_NEXT, GuiItem.of(Material.PAPER)
                        .model(ForgeIcons.ARROW_RIGHT)
                        .name("<yellow><bold>Next Page")
                        .lore("<gray>Go to page " + (page + 2) + " of " + totalPages)
                        .action(p -> new HubGui(plugin, page + 1).open(p)));
            }
        }

        // Close button
        set(SLOT_CLOSE, GuiItem.of(Material.PAPER)
                .model(ForgeIcons.BUTTON_DANGER)
                .name("<red><bold>Close")
                .lore("<gray>Close this menu.")
                .action(Player::closeInventory));
    }

    /** Card definition: category id, display name, description. */
    private record CardDef(String id, String name, String description) {
    }

    /** Open the hub. */
    public static void open(ForgeCore plugin, Player player) {
        new HubGui(plugin).open(player);
    }
}
