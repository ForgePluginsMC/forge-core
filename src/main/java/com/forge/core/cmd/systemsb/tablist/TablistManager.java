package com.forge.core.cmd.systemsb.tablist;

import com.forge.core.ForgeCore;
import com.forge.core.util.Placeholders;
import com.forge.core.util.Text;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

/**
 * Animated tablist header/footer. Frames from config
 * {@code tablist.header-frames} / {@code tablist.footer-frames} cycle on a
 * task ({@code tablist.interval-ticks}, default 100); placeholders are
 * expanded per player. Components go through the non-deprecated
 * {@code BaseComponent[]} setter.
 */
public final class TablistManager {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private final ForgeCore plugin;
    private int frame;

    public TablistManager(ForgeCore plugin) {
        this.plugin = plugin;
        long interval = Math.max(20L, plugin.getConfig().getLong("tablist.interval-ticks", 100L));
        plugin.getServer().getScheduler().runTaskTimer(plugin, task -> apply(), interval, interval);
    }

    /** Immediately re-apply the current frame to everyone online. */
    public void refresh() {
        apply();
    }

    private void apply() {
        List<String> headers = plugin.getConfig().getStringList("tablist.header-frames");
        List<String> footers = plugin.getConfig().getStringList("tablist.footer-frames");
        if (headers.isEmpty() && footers.isEmpty()) {
            return;
        }
        String headerRaw = headers.isEmpty() ? "" : headers.get(frame % headers.size());
        String footerRaw = footers.isEmpty() ? "" : footers.get(frame % footers.size());
        frame++;
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            Component header = Text.of(Placeholders.apply(player, headerRaw));
            Component footer = Text.of(Placeholders.apply(player, footerRaw));
            applyLegacy(player, header, footer);
        }
    }

    /**
     * Paper 26.3.build.35 deprecates every tablist header/footer setter and
     * offers no Component-based replacement; the deprecated call is contained
     * to this one method until Paper ships a replacement.
     */
    @SuppressWarnings("deprecation")
    private void applyLegacy(Player player, Component header, Component footer) {
        player.setPlayerListHeaderFooter(LEGACY.serialize(header), LEGACY.serialize(footer));
    }
}
