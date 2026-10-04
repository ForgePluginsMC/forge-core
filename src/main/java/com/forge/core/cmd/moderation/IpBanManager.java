package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

/**
 * IP-based bans, stored in {@code ipbans.yml}. Checked on pre-login before
 * the player joins.
 */
public final class IpBanManager implements Listener {
    public record IpBan(String ip, String reason, String by, long until) {
        public boolean permanent() {
            return until == 0;
        }

        public boolean expired() {
            return until != 0 && until < System.currentTimeMillis();
        }
    }

    private final ForgeCore plugin;
    private final File file;
    private final Map<String, IpBan> bans = new ConcurrentHashMap<>();

    public IpBanManager(ForgeCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "ipbans.yml");
        load();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void ban(String ip, String reason, String by) {
        bans.put(ip, new IpBan(ip, reason, by, 0L));
        save();
    }

    public void tempBan(String ip, String reason, String by, long untilMillis) {
        bans.put(ip, new IpBan(ip, reason, by, untilMillis));
        save();
    }

    /** Returns true when an entry was removed. */
    public boolean unban(String ip) {
        boolean removed = bans.remove(ip) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public IpBan get(String ip) {
        IpBan ban = bans.get(ip);
        if (ban != null && ban.expired()) {
            bans.remove(ip);
            save();
            return null;
        }
        return ban;
    }

    public Map<String, IpBan> all() {
        bans.entrySet().removeIf(entry -> entry.getValue().expired());
        return Map.copyOf(bans);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }
        String ip = event.getAddress() == null ? "" : event.getAddress().getHostAddress();
        IpBan ban = get(ip);
        if (ban != null) {
            String detail = ban.permanent()
                    ? "This IP ban is permanent."
                    : "Expires in " + com.forge.core.util.Time.format((ban.until() - System.currentTimeMillis()) / 1000) + ".";
            Component message = Text.of("<red>Your IP address is banned from this server.\n<gray>Reason: <white>"
                    + Text.escape(ban.reason()) + "\n<gray>" + detail);
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, message);
        }
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (Map<?, ?> entry : config.getMapList("ipbans")) {
            try {
                String ip = String.valueOf(entry.get("ip"));
                Object untilRaw = entry.get("until");
                long until = untilRaw instanceof Number number ? number.longValue() : 0L;
                Object reasonRaw = entry.get("reason");
                Object byRaw = entry.get("by");
                IpBan ban = new IpBan(ip,
                        reasonRaw == null ? "Banned" : String.valueOf(reasonRaw),
                        byRaw == null ? "?" : String.valueOf(byRaw), until);
                if (!ban.expired()) {
                    bans.put(ip, ban);
                }
            } catch (RuntimeException ignored) {
                // Skip bad rows.
            }
        }
    }

    private void save() {
        YamlConfiguration config = new YamlConfiguration();
        var list = new java.util.ArrayList<Map<String, Object>>();
        for (IpBan ban : bans.values()) {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("ip", ban.ip());
            map.put("reason", ban.reason());
            map.put("by", ban.by());
            map.put("until", ban.until());
            list.add(map);
        }
        config.set("ipbans", list);
        try {
            config.save(file);
        } catch (IOException ignored) {
            // Best effort.
        }
    }
}
