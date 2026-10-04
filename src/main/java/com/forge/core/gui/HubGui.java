package com.forge.core.gui;

import com.forge.core.ForgeCore;
import java.util.List;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * The /menu hub: intent-based sections with web-dashboard styling.
 *
 * <p>Sections are grouped by WHAT THE PLAYER WANTS TO DO, not by pack name.
 */
@NullMarked
public final class HubGui extends WebGui {
    private final ForgeCore plugin;

    public HubGui(ForgeCore plugin) {
        this.plugin = plugin;
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
        // Welcome card in header
        set(4, GuiItem.of(org.bukkit.Material.PAPER)
                .model(ForgeIcons.ICON_TOOLS)
                .name("<gold><bold>ForgeCore Control Panel")
                .lore(
                        "<gray>Welcome, <yellow>" + viewer.getName() + "<gray>!",
                        "<gray>Select a section below to get started.",
                        "",
                        "<dark_gray>Tip: Use /resourcepack for the full UI theme"));
    }

    @Override
    protected void buildContent(Player viewer) {
        // Row 1: Main sections
        set(10, sectionCard("Teleport", ForgeIcons.ICON_TELEPORT,
                "Homes, warps, and teleport requests",
                p -> new TeleportSectionGui(plugin, this).open(p)));
        set(11, sectionCard("Moderation", ForgeIcons.ICON_MODERATION,
                "Punishments and player management",
                p -> new ModerationSectionGui(plugin, this).open(p)));
        set(12, sectionCard("Economy", ForgeIcons.ICON_ECONOMY,
                "Money, shops, and payments",
                p -> new EconomySectionGui(plugin, this).open(p)));
        set(13, sectionCard("Player Tools", ForgeIcons.ICON_TOOLS,
                "Movement, appearance, and utilities",
                p -> new PlayerSectionGui(plugin, this).open(p)));
        set(14, sectionCard("Guilds", ForgeIcons.ICON_GUILD,
                "Guilds, territory, and wars",
                p -> new GuildGui(plugin, this).open(p)));
        set(15, sectionCard("Quests", ForgeIcons.ICON_QUEST,
                "Quests, dailies, and weeklies",
                p -> openQuestGui(p)));
        set(16, sectionCard("Permissions", ForgeIcons.ICON_PERMISSION,
                "Groups, tracks, and nodes",
                p -> new PermissionGui(plugin, this).open(p)));

        // Row 2: Quick access
        set(19, quickCard("Online Players", ForgeIcons.STATUS_ONLINE,
                "View and manage players",
                p -> new PlayerListGui(plugin, this).open(p)));
        set(20, quickCard("My Homes", ForgeIcons.ICON_HOME,
                "Teleport to your homes",
                p -> new HomeGui(plugin, this).open(p)));
        set(21, quickCard("Warps", ForgeIcons.ICON_WARP,
                "Browse server warps",
                p -> new WarpGui(plugin, this).open(p)));
        set(22, quickCard("Kits", ForgeIcons.ICON_KIT,
                "Claim available kits",
                p -> new KitGui(plugin, this).open(p)));
        set(23, quickCard("Mail", ForgeIcons.ICON_MAIL,
                "Read your messages",
                p -> new MailGui(plugin, this).open(p)));
        set(24, quickCard("NPCs", ForgeIcons.ICON_NPC,
                "Manage NPCs and dialogs",
                p -> new NpcSectionGui(plugin, this).open(p)));
        set(25, quickCard("Ban List", ForgeIcons.BUTTON_DANGER,
                "View active bans",
                p -> new BanListGui(plugin, this).open(p)));

        // Row 3: System
        set(31, quickCard("Server Info", ForgeIcons.DOT_ACTIVE,
                "Version, TPS, and stats",
                p -> runCmd(p, "version")));
        set(32, quickCard("My Stats", ForgeIcons.ICON_TOOLS,
                "View your statistics",
                p -> runCmd(p, "stats")));
        set(33, quickCard("Help", ForgeIcons.BUTTON_SECONDARY,
                "Command help",
                p -> runCmd(p, "forgecore")));
    }

    private GuiItem sectionCard(String name, String icon, String description,
            java.util.function.Consumer<Player> action) {
        return GuiItem.card(icon, "<gold><bold>" + name, description).action(action);
    }

    private GuiItem quickCard(String name, String icon, String description,
            java.util.function.Consumer<Player> action) {
        return GuiItem.card(icon, "<aqua><bold>" + name, description).action(action);
    }

    private void openQuestGui(Player viewer) {
        viewer.closeInventory();
        viewer.performCommand("quest");
    }

    private void runCmd(Player player, String cmd) {
        player.closeInventory();
        player.performCommand(cmd);
    }

    /** Open the hub. */
    public static void open(ForgeCore plugin, Player player) {
        new HubGui(plugin).open(player);
    }
}
