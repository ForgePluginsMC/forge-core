package com.forge.core.gui.dashboard;

import com.forge.core.ForgeCore;
import com.forge.core.gui.ForgeIcons;
import com.forge.core.gui.GuiItem;
import com.forge.core.gui.WebGui;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Main admin dashboard: live server health plus navigation to
 * world management, feature toggles, diagnostics, updates, and setup.
 */
@NullMarked
public final class DashboardGui extends WebGui {
    private final ForgeCore plugin;

    public DashboardGui(ForgeCore plugin) {
        this.plugin = plugin;
    }

    @Override
    protected List<String> breadcrumb() {
        return List.of("Dashboard");
    }

    @Override
    protected @Nullable WebGui parent() {
        return null;
    }

    @Override
    protected void buildContent(Player viewer) {
        // Status row
        set(10, dependenciesCard());
        set(13, performanceCard());
        set(16, foregcoreCard());

        // Navigation cards
        set(20, GuiItem.card(ForgeIcons.ICON_WARP,
                "<gold><bold>World Management",
                "<gray>Pre-generate chunks, view",
                "<gray>world info and statistics.",
                "",
                "<yellow>Click to open")
                .action(p -> new DashboardWorldGui(plugin).open(p)));

        set(22, GuiItem.card(ForgeIcons.ICON_TOOLS,
                "<gold><bold>Feature Toggles",
                "<gray>Enable or disable ForgeCore",
                "<gray>features with one click.",
                "",
                "<yellow>Click to open")
                .action(p -> new DashboardFeaturesGui(plugin).open(p)));

        set(24, GuiItem.card(ForgeIcons.STATUS_BUSY,
                "<gold><bold>Diagnostics",
                "<gray>Lag diagnosis and automatic",
                "<gray>issue detection.",
                "",
                "<yellow>Click to open")
                .action(p -> new DashboardDiagnosticsGui(plugin).open(p)));

        set(29, GuiItem.card(ForgeIcons.ICON_MAIL,
                "<gold><bold>Update Alerts",
                "<gray>Check for newer ForgeCore",
                "<gray>releases on GitHub.",
                "",
                "<yellow>Click to check")
                .action(p -> new DashboardUpdateGui(plugin).open(p)));

        set(31, GuiItem.card(ForgeIcons.ICON_QUEST,
                "<gold><bold>Setup Assistant",
                "<gray>First-run wizard: spawn, motd,",
                "<gray>economy, and default rank.",
                "",
                "<yellow>Click to start")
                .action(p -> new DashboardSetupGui(plugin).open(p)));
    }

    private GuiItem dependenciesCard() {
        boolean vault = Bukkit.getServicesManager()
                .getRegistration(net.milkbowl.vault.economy.Economy.class) != null;
        boolean papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        boolean healthy = vault; // Vault is required, PAPI is optional

        String statusColor = healthy ? "<green>" : "<red>";
        String statusText = healthy ? "OPERATIONAL" : "DEGRADED";
        Material icon = healthy ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;

        return GuiItem.of(icon)
                .model(healthy ? ForgeIcons.STATUS_ONLINE : ForgeIcons.STATUS_OFFLINE)
                .name(statusColor + "<bold>Dependencies: " + statusText)
                .lore(
                        "<gray>Vault economy: " + (vault ? "<green>hooked" : "<red>missing"),
                        "<gray>PlaceholderAPI: " + (papi ? "<green>hooked" : "<yellow>optional"),
                        "",
                        "<dark_gray>Live status");
    }

    private GuiItem performanceCard() {
        double tps = Math.min(Bukkit.getTPS()[0], 20.0);
        String color;
        String label;
        Material icon;
        String iconKey;
        if (tps >= 19.0) {
            color = "<green>";
            label = "FLAWLESS";
            icon = Material.LIME_STAINED_GLASS_PANE;
            iconKey = ForgeIcons.STATUS_ONLINE;
        } else if (tps >= 15.0) {
            color = "<yellow>";
            label = "GOOD";
            icon = Material.YELLOW_STAINED_GLASS_PANE;
            iconKey = ForgeIcons.STATUS_AWAY;
        } else {
            color = "<red>";
            label = "DEGRADED";
            icon = Material.RED_STAINED_GLASS_PANE;
            iconKey = ForgeIcons.STATUS_BUSY;
        }

        Runtime rt = Runtime.getRuntime();
        long usedMb = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
        long maxMb = rt.maxMemory() / 1024 / 1024;

        return GuiItem.of(icon)
                .model(iconKey)
                .name(color + "<bold>Performance: " + label)
                .lore(
                        "<gray>TPS: <white>" + String.format("%.1f", tps),
                        "<gray>Memory: <white>" + usedMb + "MB <gray>/ " + maxMb + "MB",
                        "<gray>Players: <white>" + Bukkit.getOnlinePlayers().size(),
                        "",
                        "<dark_gray>Live status");
    }

    private GuiItem foregcoreCard() {
        String version = plugin.getPluginMeta().getVersion();
        return GuiItem.of(Material.LIME_STAINED_GLASS_PANE)
                .model(ForgeIcons.STATUS_ONLINE)
                .name("<green><bold>ForgeCore: ACTIVE")
                .lore(
                        "<gray>Version: <white>v" + version,
                        "<gray>Authors: <white>ForgePlugins",
                        "",
                        "<dark_gray>Live status");
    }
}
