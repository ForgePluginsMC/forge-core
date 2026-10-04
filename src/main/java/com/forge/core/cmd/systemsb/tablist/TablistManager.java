package com.forge.core.cmd.systemsb.tablist;

import com.forge.core.ForgeCore;
import com.forge.core.util.Placeholders;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Animated tablist header/footer, per-group tab names and animated
 * server-list MOTD — merged from forge-tablist, keeping its more advanced
 * features (multi-line frames, group prefix/suffix, wall-clock MOTD).
 *
 * <p>Frames are MiniMessage with {@code %player%}, {@code %online%},
 * {@code %max%}, {@code %tps%} plus ForgeCore's {@code %forgecore_*} and
 * {@code %player_name%} placeholders. Header/footer go through Adventure's
 * non-deprecated {@code Audience#sendPlayerListHeaderAndFooter}.
 */
public final class TablistManager {
    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private final ForgeCore plugin;
    private int frame;

    public TablistManager(ForgeCore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(new TablistListener(this), plugin);
        long interval = Math.max(20L, plugin.getConfig().getLong("tablist.interval-ticks", 100L));
        plugin.getServer().getScheduler().runTaskTimer(plugin, task -> apply(), interval, interval);
    }

    /** Immediately re-apply the current frame and tab names to everyone online. */
    public void refresh() {
        apply();
    }

    /** Push the current frame to one player (used on join). */
    public void pushCurrentFrame(Player player) {
        int online = plugin.getServer().getOnlinePlayers().size();
        int max = plugin.getServer().getMaxPlayers();
        player.sendPlayerListHeaderAndFooter(
                frameComponent(currentLines("tablist.header-frames"), player, online, max),
                frameComponent(currentLines("tablist.footer-frames"), player, online, max));
    }

    /**
     * Set the player's tab-list entry: the first matching group's
     * prefix/suffix wrapped around their nick (or name). Parsed as one
     * MiniMessage string so paired tags wrap the name correctly.
     */
    public void applyTablistName(Player player) {
        String nick = plugin.users().get(player).getString("nick", null);
        String base = nick == null ? player.getName() : Text.strip(nick);
        if (base.length() > 16) {
            base = base.substring(0, 16);
        }
        for (TabGroup group : groups()) {
            if (group.permission().isEmpty() || player.hasPermission(group.permission())) {
                player.playerListName(safe(group.prefix() + Text.escape(base) + group.suffix()));
                return;
            }
        }
        player.playerListName(Component.text(base));
    }

    /** Current MOTD frame. Wall-clock derived (no task); safe off the main thread. */
    public Component currentMotd(int online, int max) {
        List<List<String>> frames = readFrames("tablist.motd-frames");
        if (frames.isEmpty()) {
            return Component.empty();
        }
        long seconds = Math.max(1L, plugin.getConfig().getLong("tablist.motd-interval-seconds", 5L));
        long slot = System.currentTimeMillis() / (seconds * 1000L);
        List<String> lines = frames.get((int) (slot % frames.size()));
        List<Component> parts = new ArrayList<>(lines.size());
        for (String line : lines) {
            parts.add(MINI.deserialize(applyVars(line, "", online, max)));
        }
        return Component.join(JoinConfiguration.newlines(), parts);
    }

    /** Configured MOTD max-players override, or -1 for none. */
    public int motdMaxPlayers() {
        return plugin.getConfig().getInt("tablist.motd-max-players", -1);
    }

    private void apply() {
        List<String> headerLines = currentLines("tablist.header-frames");
        List<String> footerLines = currentLines("tablist.footer-frames");
        if (!headerLines.isEmpty() || !footerLines.isEmpty()) {
            int online = plugin.getServer().getOnlinePlayers().size();
            int max = plugin.getServer().getMaxPlayers();
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.sendPlayerListHeaderAndFooter(
                        frameComponent(headerLines, player, online, max),
                        frameComponent(footerLines, player, online, max));
            }
            frame++;
        }
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            applyTablistName(player);
        }
    }

    private List<String> currentLines(String path) {
        List<List<String>> frames = readFrames(path);
        if (frames.isEmpty()) {
            return List.of();
        }
        return frames.get(Math.floorMod(frame, frames.size()));
    }

    private Component frameComponent(List<String> lines, Player player, int online, int max) {
        if (lines.isEmpty()) {
            return Component.empty();
        }
        List<Component> parts = new ArrayList<>(lines.size());
        for (String line : lines) {
            parts.add(Text.of(Placeholders.apply(player, applyVars(line, player.getName(), online, max))));
        }
        return Component.join(JoinConfiguration.newlines(), parts);
    }

    private static String applyVars(String raw, String playerName, int online, int max) {
        double tps = 20.0;
        try {
            double[] values = Bukkit.getTPS();
            if (values.length > 0) {
                tps = values[0];
            }
        } catch (Exception ignored) {
            tps = 20.0;
        }
        return raw.replace("%player%", playerName == null ? "" : playerName)
                .replace("%online%", String.valueOf(online))
                .replace("%max%", String.valueOf(max))
                .replace("%tps%", String.format(Locale.ROOT, "%.1f", tps));
    }

    /** Parse MiniMessage, falling back to literal text on malformed tags. */
    private static Component safe(String raw) {
        try {
            return MINI.deserialize(raw);
        } catch (Exception e) {
            return Component.text(raw);
        }
    }

    private List<List<String>> readFrames(String path) {
        List<List<String>> frames = new ArrayList<>();
        for (Object frame : plugin.getConfig().getList(path, List.of())) {
            if (frame instanceof List<?> lines) {
                List<String> clean = new ArrayList<>();
                for (Object line : lines) {
                    if (line instanceof String text) {
                        clean.add(text);
                    }
                }
                if (!clean.isEmpty()) {
                    frames.add(List.copyOf(clean));
                }
            } else if (frame instanceof String single) {
                // Backwards compatible: a plain string is a one-line frame.
                frames.add(List.of(single));
            }
        }
        return frames;
    }

    private List<TabGroup> groups() {
        List<TabGroup> groups = new ArrayList<>();
        for (Map<?, ?> entry : plugin.getConfig().getMapList("tablist.groups")) {
            Object permission = entry.get("permission");
            Object prefix = entry.get("prefix");
            Object suffix = entry.get("suffix");
            groups.add(new TabGroup(
                    permission == null ? "" : String.valueOf(permission),
                    prefix == null ? "" : String.valueOf(prefix),
                    suffix == null ? "" : String.valueOf(suffix)));
        }
        return groups;
    }

    /** A tab-name style. Empty permission matches every player (fallback group). */
    public record TabGroup(String permission, String prefix, String suffix) {
    }
}
