package com.forge.core.gui.dashboard;

import com.forge.core.ForgeCore;
import com.forge.core.gui.ForgeIcons;
import com.forge.core.gui.GuiItem;
import com.forge.core.gui.WebGui;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Diagnostics: 10-second lag scan and automatic issue detection.
 */
@NullMarked
public final class DashboardDiagnosticsGui extends WebGui {
    private final ForgeCore plugin;

    /** Cached scan results. */
    private static @Nullable LagScan lastScan;

    public DashboardDiagnosticsGui(ForgeCore plugin) {
        this.plugin = plugin;
    }

    @Override
    protected List<String> breadcrumb() {
        return List.of("Dashboard", "Diagnostics");
    }

    @Override
    protected @Nullable WebGui parent() {
        return new DashboardGui(plugin);
    }

    @Override
    protected void buildContent(Player viewer) {
        // Lag scan card
        set(11, GuiItem.cardWithAction(ForgeIcons.STATUS_BUSY,
                "<gold><bold>Lag Diagnosis",
                "<yellow>Click to run scan",
                "Run a 10-second scan of TPS,",
                "entities, chunks, and memory.")
                .action(p -> runLagScan(p)));

        // Show last scan results if available
        LagScan scan = lastScan;
        if (scan != null) {
            int slot = 13;
            for (String line : scan.summaryLines()) {
                if (slot > 16) {
                    break;
                }
                set(slot, GuiItem.of(Material.PAPER)
                        .model(ForgeIcons.DOT_ACTIVE)
                        .name("<white>" + line));
                slot++;
            }
        }

        // Issue detector
        set(29, GuiItem.cardWithAction(ForgeIcons.STATUS_OFFLINE,
                "<gold><bold>Issue Detector",
                "<yellow>Click to run checks",
                "Automatic checks for config",
                "errors, orphans, and conflicts.")
                .action(p -> new IssueListGui(plugin).open(p)));
    }

    private void runLagScan(Player player) {
        Text.send(player, "<yellow>Running 10-second lag scan...");
        double[] tpsBefore = Bukkit.getTPS();
        long memBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            double[] tpsAfter = Bukkit.getTPS();
            long memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            List<String> lines = new ArrayList<>();
            lines.add("TPS: " + fmt(Math.min(tpsAfter[0], 20.0))
                    + " (was " + fmt(Math.min(tpsBefore[0], 20.0)) + ")");
            lines.add("Memory: " + (memAfter / 1024 / 1024) + "MB"
                    + " (Δ " + ((memAfter - memBefore) / 1024 / 1024) + "MB)");

            for (World world : Bukkit.getWorlds()) {
                int entities = world.getEntities().size();
                int chunks = world.getLoadedChunks().length;
                int tileEntities = countTileEntities(world);
                lines.add(world.getName() + ": " + entities + " entities, "
                        + chunks + " chunks, " + tileEntities + " tile entities");
            }

            // Recommendations
            List<String> advice = new ArrayList<>();
            double tps = Math.min(tpsAfter[0], 20.0);
            if (tps < 18.0) {
                advice.add("TPS below 18 — consider reducing view distance or entity counts.");
            }
            for (World world : Bukkit.getWorlds()) {
                if (world.getEntities().size() > 500) {
                    advice.add(world.getName() + " has " + world.getEntities().size()
                            + " entities — consider /killall or groundclean.");
                }
            }
            if (advice.isEmpty()) {
                advice.add("No issues detected. Server is healthy.");
            }

            lastScan = new LagScan(lines, advice);
            Text.send(player, "<green>Scan complete. Opening results...");
            new DashboardDiagnosticsGui(plugin).open(player);

            // Show recommendations in chat too
            for (String tip : advice) {
                Text.send(player, "<yellow>» <gray>" + tip);
            }
        }, 200L); // 10 seconds
        player.closeInventory();
    }

    private static int countTileEntities(World world) {
        int count = 0;
        for (Chunk chunk : world.getLoadedChunks()) {
            count += chunk.getTileEntities().length;
        }
        return count;
    }

    private static String fmt(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    /** Immutable lag scan result. */
    private record LagScan(List<String> summaryLines, List<String> advice) {
    }

    /** GUI listing detected issues with fix buttons. */
    private static final class IssueListGui extends WebGui {
        private final ForgeCore plugin;

        IssueListGui(ForgeCore plugin) {
            this.plugin = plugin;
        }

        @Override
        protected List<String> breadcrumb() {
            return List.of("Dashboard", "Diagnostics", "Issues");
        }

        @Override
        protected @Nullable WebGui parent() {
            return new DashboardDiagnosticsGui(plugin);
        }

        @Override
        protected void buildContent(Player viewer) {
            List<Issue> issues = detectIssues();
            if (issues.isEmpty()) {
                set(22, GuiItem.of(Material.LIME_STAINED_GLASS_PANE)
                        .model(ForgeIcons.STATUS_ONLINE)
                        .name("<green><bold>No issues found")
                        .lore("<gray>All automatic checks passed."));
                return;
            }
            int slot = 10;
            for (Issue issue : issues) {
                if (slot > 43) {
                    break;
                }
                GuiItem item = GuiItem.of(Material.YELLOW_STAINED_GLASS_PANE)
                        .model(ForgeIcons.STATUS_AWAY)
                        .name("<yellow><bold>" + issue.title())
                        .lore("<gray>" + issue.detail(), "",
                                issue.fixLabel() != null
                                        ? "<green>Click to: " + issue.fixLabel()
                                        : "<dark_gray>No automatic fix");
                if (issue.fix() != null) {
                    var fix = issue.fix();
                    item.action(p -> {
                        fix.run();
                        Text.send(p, "<green>Fix applied.");
                        new IssueListGui(plugin).open(p);
                    });
                }
                set(slot, item);
                slot++;
            }
        }

        /** Run all automatic checks. */
        private List<Issue> detectIssues() {
            List<Issue> issues = new ArrayList<>();

            // Check: MOTD blank
            String motd = plugin.getConfig().getString("motd", "");
            if (motd.isBlank()) {
                issues.add(new Issue("MOTD is blank",
                        "Players see no message of the day on join.",
                        "Set default MOTD",
                        () -> plugin.getConfig().set("motd",
                                "<gold>Welcome to the server!")));
            }

            // Check: starting balance unreasonable
            double starting = plugin.getConfig().getDouble("starting-balance", 100.0);
            if (starting < 0) {
                issues.add(new Issue("Negative starting balance",
                        "New players start with " + starting + " currency.",
                        "Reset to 100",
                        () -> plugin.getConfig().set("starting-balance", 100.0)));
            }

            // Check: worlds with excessive entities
            for (World world : Bukkit.getWorlds()) {
                int entities = world.getEntities().size();
                if (entities > 1000) {
                    issues.add(new Issue("High entity count: " + world.getName(),
                            entities + " entities loaded (threshold: 1000).",
                            null, null));
                }
            }

            // Check: TPS degraded
            double tps = Math.min(Bukkit.getTPS()[0], 20.0);
            if (tps < 15.0) {
                issues.add(new Issue("Low TPS: " + fmt(tps),
                        "Server is lagging. Run lag diagnosis for details.",
                        null, null));
            }

            // Check: duplicate plugin jars (from earlier deploy issue)
            var pluginsDir = plugin.getDataFolder().getParentFile();
            if (pluginsDir != null) {
                var jars = pluginsDir.listFiles(
                        (dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".jar"));
                if (jars != null) {
                    var seen = new java.util.HashSet<String>();
                    var dupes = new java.util.HashSet<String>();
                    for (var jar : jars) {
                        String base = jar.getName().replaceAll("-[0-9].*\\.jar$", ".jar");
                        if (!seen.add(base)) {
                            dupes.add(jar.getName());
                        }
                    }
                    for (String dupe : dupes) {
                        issues.add(new Issue("Duplicate plugin jar: " + dupe,
                                "Multiple versions may cause 'ambiguous plugin name' errors.",
                                null, null));
                    }
                }
            }

            // Check: maintenance mode on
            if (plugin.getConfig().getBoolean("maintenance", false)) {
                issues.add(new Issue("Maintenance mode is ON",
                        "Non-bypassed players cannot join.",
                        "Disable maintenance",
                        () -> plugin.getConfig().set("maintenance", false)));
            }

            return issues.stream()
                    .sorted(Comparator.comparing(Issue::title))
                    .toList();
        }

        /** A detected issue with optional auto-fix. */
        private record Issue(String title, String detail,
                             @Nullable String fixLabel, @Nullable Runnable fix) {
        }
    }
}
