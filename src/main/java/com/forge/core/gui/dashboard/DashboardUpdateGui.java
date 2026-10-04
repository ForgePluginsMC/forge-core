package com.forge.core.gui.dashboard;

import com.forge.core.ForgeCore;
import com.forge.core.gui.ForgeIcons;
import com.forge.core.gui.GuiItem;
import com.forge.core.gui.WebGui;
import com.forge.core.util.Text;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Update alerts: check GitHub for newer ForgeCore releases.
 */
@NullMarked
public final class DashboardUpdateGui extends WebGui {
    private final ForgeCore plugin;

    private static @Nullable UpdateInfo cached;
    private static long cachedAt;

    public DashboardUpdateGui(ForgeCore plugin) {
        this.plugin = plugin;
    }

    @Override
    protected List<String> breadcrumb() {
        return List.of("Dashboard", "Updates");
    }

    @Override
    protected @Nullable WebGui parent() {
        return new DashboardGui(plugin);
    }

    @Override
    protected void buildContent(Player viewer) {
        UpdateInfo info = cached;
        boolean fresh = info != null && System.currentTimeMillis() - cachedAt < 300_000;

        if (!fresh) {
            set(22, GuiItem.of(Material.PAPER)
                    .model(ForgeIcons.DOT_ACTIVE)
                    .name("<yellow><bold>Checking for updates...")
                    .lore("<gray>Contacting GitHub..."));
            checkForUpdates(viewer);
            return;
        }

        String current = plugin.getPluginMeta().getVersion();
        if (info.latest().equals(current)) {
            set(22, GuiItem.of(Material.LIME_STAINED_GLASS_PANE)
                    .model(ForgeIcons.STATUS_ONLINE)
                    .name("<green><bold>UP TO DATE")
                    .lore(
                            "<gray>Running: <white>v" + current,
                            "<gray>Latest: <white>v" + info.latest(),
                            "",
                            "<dark_gray>Last checked just now"));
        } else {
            set(22, GuiItem.of(Material.YELLOW_STAINED_GLASS_PANE)
                    .model(ForgeIcons.STATUS_AWAY)
                    .name("<yellow><bold>UPDATE AVAILABLE: v" + info.latest())
                    .lore(
                            "<gray>Running: <white>v" + current,
                            "<gray>Latest: <white>v" + info.latest(),
                            "",
                            info.notes().isBlank() ? "<dark_gray>No changelog"
                                    : "<gray>" + truncate(info.notes(), 200),
                            "",
                            "<yellow>Download: <white>" + info.url()));
        }

        set(31, GuiItem.of(Material.PAPER)
                .model(ForgeIcons.BUTTON_SECONDARY)
                .name("<white><bold>Check Again")
                .lore("<gray>Force a fresh check.")
                .action(p -> {
                    cached = null;
                    new DashboardUpdateGui(plugin).open(p);
                }));
    }

    private void checkForUpdates(Player viewer) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.github.com/repos/ForgePluginsMC/forge-core/releases/latest"))
                        .header("Accept", "application/vnd.github+json")
                        .header("User-Agent", "ForgeCore-UpdateChecker")
                        .timeout(Duration.ofSeconds(15))
                        .build();
                HttpResponse<String> response = client.send(
                        request, HttpResponse.BodyHandlers.ofString());
                String body = response.body();

                String tag = extract(body, "\"tag_name\"\\s*:\\s*\"([^\"]+)\"");
                String htmlUrl = extract(body, "\"html_url\"\\s*:\\s*\"([^\"]+)\"");
                String notes = extract(body, "\"body\"\\s*:\\s*\"([^\"]*)\"");

                String version = tag.startsWith("v") ? tag.substring(1) : tag;
                cached = new UpdateInfo(version, htmlUrl, notes.replace("\\n", " ").replace("\\r", ""));
                cachedAt = System.currentTimeMillis();
            } catch (Exception e) {
                cached = new UpdateInfo(plugin.getPluginMeta().getVersion(), "", "");
                cachedAt = System.currentTimeMillis();
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (viewer.isOnline()) {
                    new DashboardUpdateGui(plugin).open(viewer);
                }
            });
        });
        viewer.closeInventory();
        Text.send(viewer, "<yellow>Checking GitHub for updates...");
    }

    private static String extract(String json, String regex) {
        Matcher m = Pattern.compile(regex).matcher(json);
        return m.find() ? m.group(1) : "";
    }

    private static String truncate(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }

    private record UpdateInfo(String latest, String url, String notes) {
    }
}
