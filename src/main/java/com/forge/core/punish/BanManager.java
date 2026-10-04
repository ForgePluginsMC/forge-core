package com.forge.core.punish;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import com.forge.core.util.Time;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.jspecify.annotations.Nullable;

/**
 * Bans and temp-bans, persisted in {@code bans.yml}. Also handles IP locks
 * ({@code /lockip}): when set, the account only works from the locked IP.
 */
public final class BanManager implements Listener {
    /** A ban entry. {@code until} 0 = permanent. */
    public record BanInfo(UUID uuid, String name, String reason, String by, long createdAt, long until) {
        public boolean permanent() {
            return until == 0;
        }

        public boolean expired() {
            return until != 0 && until < System.currentTimeMillis();
        }
    }

    private final ForgeCore plugin;
    private final File file;
    private final Map<UUID, BanInfo> bans = new ConcurrentHashMap<>();

    public BanManager(ForgeCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "bans.yml");
        load();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (Map<?, ?> entry : config.getMapList("bans")) {
            try {
                UUID uuid = UUID.fromString(String.valueOf(entry.get("uuid")));
                BanInfo info = new BanInfo(
                        uuid,
                        stringOf(entry.get("name"), "?"),
                        stringOf(entry.get("reason"), "Banned"),
                        stringOf(entry.get("by"), "Console"),
                        longOf(entry.get("created"), 0L),
                        longOf(entry.get("until"), 0L));
                if (!info.expired()) {
                    bans.put(uuid, info);
                }
            } catch (RuntimeException exception) {
                // Skip bad rows.
            }
        }
    }

    private static String stringOf(@Nullable Object value, String def) {
        return value == null ? def : String.valueOf(value);
    }

    private static long longOf(@Nullable Object value, long def) {
        return value instanceof Number number ? number.longValue() : def;
    }

    /** Persist bans.yml. */
    public void save() {        YamlConfiguration config = new YamlConfiguration();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (BanInfo info : bans.values()) {
            if (info.expired()) {
                continue;
            }
            Map<String, Object> row = new HashMap<>();
            row.put("uuid", info.uuid().toString());
            row.put("name", info.name());
            row.put("reason", info.reason());
            row.put("by", info.by());
            row.put("created", info.createdAt());
            row.put("until", info.until());
            rows.add(row);
        }
        config.set("bans", rows);
        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not save bans.yml: " + exception.getMessage());
        }
    }

    public void ban(UUID uuid, String name, String reason, String by) {
        bans.put(uuid, new BanInfo(uuid, name, reason, by, System.currentTimeMillis(), 0));
        save();
        Player online = plugin.getServer().getPlayer(uuid);
        if (online != null) {
            online.kick(kickMessage(bans.get(uuid)));
        }
    }

    public void tempBan(UUID uuid, String name, String reason, String by, long untilMillis) {
        bans.put(uuid, new BanInfo(uuid, name, reason, by, System.currentTimeMillis(), untilMillis));
        save();
        Player online = plugin.getServer().getPlayer(uuid);
        if (online != null) {
            online.kick(kickMessage(bans.get(uuid)));
        }
    }

    public boolean unban(UUID uuid) {
        boolean removed = bans.remove(uuid) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    /** Active ban for a UUID, or null. */
    public @Nullable BanInfo get(UUID uuid) {
        BanInfo info = bans.get(uuid);
        if (info != null && info.expired()) {
            bans.remove(uuid);
            save();
            return null;
        }
        return info;
    }

    /** Find an active ban by player name (case-insensitive). */
    public @Nullable BanInfo findByName(String name) {
        for (BanInfo info : bans.values()) {
            if (!info.expired() && info.name().equalsIgnoreCase(name)) {
                return info;
            }
        }
        return null;
    }

    public List<BanInfo> all() {
        List<BanInfo> list = new ArrayList<>();
        for (BanInfo info : bans.values()) {
            if (!info.expired()) {
                list.add(info);
            }
        }
        return list;
    }

    private Component kickMessage(BanInfo info) {
        String detail = info.permanent()
                ? "This ban is permanent."
                : "Expires in " + Time.format((info.until() - System.currentTimeMillis()) / 1000) + ".";
        return Text.of("<red>You are banned from this server.\n<gray>Reason: <white>" + Text.escape(info.reason())
                + "\n<gray>" + detail);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }
        // Ban check.
        BanInfo info = get(event.getUniqueId());
        if (info != null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, kickMessage(info));
            return;
        }
        // IP lock check.
        String locked = plugin.users().get(event.getUniqueId()).getString("locked-ip", null);
        String current = event.getAddress() == null ? "" : event.getAddress().getHostAddress();
        if (locked != null && !locked.equals(current)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    Text.of("<red>This account is locked to a different IP address."));
            return;
        }
        // Remember the IP a player joined from (used by /checkaccount).
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            var data = plugin.users().get(event.getUniqueId());
            data.setString("last-ip", current);
            data.setString("last-name", event.getName());
            plugin.users().save(event.getUniqueId());
        });
    }
}
