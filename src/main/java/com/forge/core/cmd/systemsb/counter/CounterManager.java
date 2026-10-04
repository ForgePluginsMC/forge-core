package com.forge.core.cmd.systemsb.counter;

import com.forge.core.ForgeCore;
import com.forge.core.data.UserData;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Session counters (kills, deaths, joins, blocks broken/placed) plus
 * persistent totals in userdata ({@code stats.kills} etc., read by the
 * rank system).
 */
public final class CounterManager implements Listener {
    /** Mutable session counters for one player. */
    public static final class Counters {
        public long kills;
        public long deaths;
        public long joins;
        public long broken;
        public long placed;
    }

    private final ForgeCore plugin;
    private final Map<UUID, Counters> sessions = new ConcurrentHashMap<>();

    public CounterManager(ForgeCore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /** Session counters for a player (created on demand). */
    public Counters get(UUID uuid) {
        return sessions.computeIfAbsent(uuid, key -> new Counters());
    }

    /** Clear one player's session counters. */
    public void reset(UUID uuid) {
        sessions.remove(uuid);
    }

    private void add(UUID uuid, String persistentKey, Consumer<Counters> increment) {
        Counters counters = get(uuid);
        increment.accept(counters);
        UserData data = plugin.users().get(uuid);
        data.setLong(persistentKey, data.getLong(persistentKey, 0) + 1);
        plugin.users().save(uuid);
    }

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            add(killer.getUniqueId(), "stats.kills", counters -> counters.kills++);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        add(event.getEntity().getUniqueId(), "stats.deaths", counters -> counters.deaths++);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        add(event.getPlayer().getUniqueId(), "stats.joins", counters -> counters.joins++);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        add(event.getPlayer().getUniqueId(), "stats.blocks-broken", counters -> counters.broken++);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        add(event.getPlayer().getUniqueId(), "stats.blocks-placed", counters -> counters.placed++);
    }
}
