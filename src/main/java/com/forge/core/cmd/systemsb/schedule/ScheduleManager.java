package com.forge.core.cmd.systemsb.schedule;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.systemsb.ActionRunner;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jspecify.annotations.Nullable;

/**
 * Declarative scheduled actions in {@code schedules.yml}.
 *
 * <p>Triggers: {@code interval-minutes} (repeating task, actions run once
 * console-style), {@code playtime-minutes} (once per player when their total
 * playtime passes the threshold), {@code firstJoinServer},
 * {@code joinServer}, {@code quitServer}, {@code playerDeath},
 * {@code playerRespawn}, {@code playerTeleport}.
 */
public final class ScheduleManager implements Listener {
    public enum Trigger {
        INTERVAL, PLAYTIME, FIRST_JOIN, JOIN, QUIT, DEATH, RESPAWN, TELEPORT
    }

    /** One named schedule. */
    public record ScheduleDef(String name, Trigger trigger, long minutes, List<String> actions) {
    }

    private final ForgeCore plugin;
    private final File file;
    private final Map<String, ScheduleDef> schedules = new LinkedHashMap<>();
    private final Map<String, Long> lastIntervalRun = new ConcurrentHashMap<>();

    public ScheduleManager(ForgeCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "schedules.yml");
        load();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getScheduler().runTaskTimer(plugin, task -> tick(), 1200L, 1200L);
    }

    public List<ScheduleDef> all() {
        return List.copyOf(schedules.values());
    }

    public @Nullable ScheduleDef byName(String name) {
        for (ScheduleDef def : schedules.values()) {
            if (def.name().equalsIgnoreCase(name)) {
                return def;
            }
        }
        return null;
    }

    /** Force-run a schedule's actions; player may be null for console-style runs. */
    public void run(ScheduleDef def, @Nullable Player player) {
        ActionRunner.run(plugin, player, def.actions());
        if (def.trigger() == Trigger.INTERVAL) {
            lastIntervalRun.put(def.name(), System.currentTimeMillis());
            saveLastRun();
        }
    }

    private void tick() {
        long now = System.currentTimeMillis();
        for (ScheduleDef def : schedules.values()) {
            switch (def.trigger()) {
                case INTERVAL -> {
                    long last = lastIntervalRun.getOrDefault(def.name(), 0L);
                    if (now - last >= def.minutes() * 60_000L) {
                        ActionRunner.run(plugin, null, def.actions());
                        lastIntervalRun.put(def.name(), now);
                        saveLastRun();
                    }
                }
                case PLAYTIME -> {
                    long threshold = def.minutes() * 60L;
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        String key = "schedule-done." + def.name();
                        if (plugin.users().get(player.getUniqueId()).getBoolean(key, false)) {
                            continue;
                        }
                        if (plugin.users().get(player.getUniqueId()).playtimeSeconds() >= threshold) {
                            plugin.users().get(player.getUniqueId()).setBoolean(key, true);
                            plugin.users().save(player.getUniqueId());
                            ActionRunner.run(plugin, player, def.actions());
                        }
                    }
                }
                default -> {
                    // Event-driven triggers are handled by listeners.
                }
            }
        }
    }

    private void fire(Trigger trigger, @Nullable Player player) {
        for (ScheduleDef def : schedules.values()) {
            if (def.trigger() == trigger) {
                ActionRunner.run(plugin, player, def.actions());
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            fire(Trigger.FIRST_JOIN, player);
        }
        fire(Trigger.JOIN, player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        fire(Trigger.QUIT, event.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        fire(Trigger.DEATH, event.getEntity());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        fire(Trigger.RESPAWN, event.getPlayer());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        fire(Trigger.TELEPORT, event.getPlayer());
    }

    private void load() {
        if (!file.exists()) {
            saveDefaults();
        }
        schedules.clear();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("schedules");
        if (section != null) {
            for (String name : section.getKeys(false)) {
                ConfigurationSection entry = section.getConfigurationSection(name);
                if (entry == null) {
                    continue;
                }
                try {
                    Trigger trigger = parseTrigger(entry.getString("trigger", ""));
                    schedules.put(name, new ScheduleDef(
                            name,
                            trigger,
                            Math.max(1L, entry.getLong("minutes", 60L)),
                            List.copyOf(entry.getStringList("actions"))));
                } catch (IllegalArgumentException exception) {
                    plugin.getLogger().warning("Schedule '" + name + "': " + exception.getMessage());
                }
            }
        }
        ConfigurationSection lastRun = config.getConfigurationSection("last-run");
        if (lastRun != null) {
            for (String name : lastRun.getKeys(false)) {
                lastIntervalRun.put(name, lastRun.getLong(name, 0L));
            }
        }
    }

    private Trigger parseTrigger(String raw) {
        return switch (raw.trim()) {
            case "interval-minutes" -> Trigger.INTERVAL;
            case "playtime-minutes" -> Trigger.PLAYTIME;
            case "firstJoinServer" -> Trigger.FIRST_JOIN;
            case "joinServer" -> Trigger.JOIN;
            case "quitServer" -> Trigger.QUIT;
            case "playerDeath" -> Trigger.DEATH;
            case "playerRespawn" -> Trigger.RESPAWN;
            case "playerTeleport" -> Trigger.TELEPORT;
            default -> throw new IllegalArgumentException("unknown trigger '" + raw + "'");
        };
    }

    private void saveLastRun() {
        YamlConfiguration config = file.exists()
                ? YamlConfiguration.loadConfiguration(file)
                : new YamlConfiguration();
        for (Map.Entry<String, Long> entry : lastIntervalRun.entrySet()) {
            config.set("last-run." + entry.getKey(), entry.getValue());
        }
        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not save schedules.yml: " + exception.getMessage());
        }
    }

    private void saveDefaults() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("schedules.welcome.trigger", "firstJoinServer");
        config.set("schedules.welcome.actions", List.of(
                "msg:<green>Welcome to the server, <white>%player_name%<green>!",
                "command:give %player% bread 8"));
        config.set("schedules.daily-bonus.trigger", "interval-minutes");
        config.set("schedules.daily-bonus.minutes", 1440);
        config.set("schedules.daily-bonus.actions", List.of(
                "money:100.0",
                "broadcast:<gold>Daily bonuses have been paid out!"));
        config.set("schedules.veteran-bonus.trigger", "playtime-minutes");
        config.set("schedules.veteran-bonus.minutes", 600);
        config.set("schedules.veteran-bonus.actions", List.of(
                "money:500.0",
                "msg:<green>Thanks for 10 hours of playtime! Here is a bonus."));
        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not write default schedules.yml: " + exception.getMessage());
        }
    }
}
