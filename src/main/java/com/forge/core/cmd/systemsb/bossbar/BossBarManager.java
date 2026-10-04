package com.forge.core.cmd.systemsb.bossbar;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * BossBar messages: timed manual messages plus automatic sleeping
 * and totem-pop bars. Bars are tracked per player and removed cleanly.
 */
public final class BossBarManager implements Listener {
    private final ForgeCore plugin;
    private final Map<UUID, Set<BossBar>> active = new ConcurrentHashMap<>();
    private final Map<UUID, BossBar> sleepBars = new ConcurrentHashMap<>();

    public BossBarManager(ForgeCore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getScheduler().runTaskTimer(plugin, task -> tickSleepBars(), 20L, 20L);
    }

    /** Show a timed boss bar to a player. */
    public void show(Player player, Component title, BossBar.Color color, int seconds) {
        BossBar bar = BossBar.bossBar(title, 1.0f, color, BossBar.Overlay.PROGRESS);
        player.showBossBar(bar);
        active.computeIfAbsent(player.getUniqueId(), key -> ConcurrentHashMap.newKeySet()).add(bar);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> hide(player, bar), seconds * 20L);
    }

    /** Hide one bar previously shown to the player. */
    public void hide(Player player, BossBar bar) {
        player.hideBossBar(bar);
        Set<BossBar> bars = active.get(player.getUniqueId());
        if (bars != null) {
            bars.remove(bar);
        }
    }

    /** Remove every bar this manager showed to the player. */
    public void clear(Player player) {
        Set<BossBar> bars = active.remove(player.getUniqueId());
        if (bars != null) {
            for (BossBar bar : bars) {
                player.hideBossBar(bar);
            }
        }
        BossBar sleep = sleepBars.remove(player.getUniqueId());
        if (sleep != null) {
            player.hideBossBar(sleep);
        }
    }

    private void tickSleepBars() {
        for (Map.Entry<UUID, BossBar> entry : sleepBars.entrySet()) {
            Player player = plugin.getServer().getPlayer(entry.getKey());
            BossBar bar = entry.getValue();
            if (player == null || !player.isSleeping()) {
                continue;
            }
            float progress = bar.progress() + 0.1f;
            bar.progress(progress > 1.0f ? 0.0f : progress);
        }
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) {
            return;
        }
        Player player = event.getPlayer();
        BossBar bar = BossBar.bossBar(
                Text.of("<aqua>Sleeping..."), 0.0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
        player.showBossBar(bar);
        sleepBars.put(player.getUniqueId(), bar);
    }

    @EventHandler
    public void onBedLeave(PlayerBedLeaveEvent event) {
        BossBar bar = sleepBars.remove(event.getPlayer().getUniqueId());
        if (bar != null) {
            event.getPlayer().hideBossBar(bar);
        }
    }

    @EventHandler
    public void onTotem(EntityResurrectEvent event) {
        if (event.getEntity() instanceof Player player) {
            show(player, Text.of("<yellow><bold>TOTEM ACTIVATED!"), BossBar.Color.YELLOW, 3);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        clear(event.getPlayer());
    }
}
