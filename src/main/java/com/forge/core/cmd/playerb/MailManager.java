package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Offline player mail. Messages persist in {@code mail.yml}, keyed by
 * recipient UUID. Players are notified on join when they have unread mail.
 */
public final class MailManager implements Listener {
    public record Mail(String from, String message, long sentAt) {
    }

    private final ForgeCore plugin;
    private final File file;
    private final Map<UUID, List<Mail>> inbox = new ConcurrentHashMap<>();

    public MailManager(ForgeCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "mail.yml");
        load();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void send(UUID to, String from, String message) {
        inbox.computeIfAbsent(to, k -> new ArrayList<>())
                .add(new Mail(from, message, System.currentTimeMillis()));
        save();
    }

    public List<Mail> read(UUID uuid) {
        return List.copyOf(inbox.getOrDefault(uuid, List.of()));
    }

    public void clear(UUID uuid) {
        inbox.remove(uuid);
        save();
    }

    public int count(UUID uuid) {
        return inbox.getOrDefault(uuid, List.of()).size();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        int unread = count(player.getUniqueId());
        if (unread > 0) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> com.forge.core.util.Text.send(player,
                    "You have <white>" + unread + "</white> unread mail. Use <white>/mail read</white>."), 40L);
        }
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                List<Mail> mails = new ArrayList<>();
                for (Map<?, ?> entry : config.getMapList(key)) {
                    Object sentAtRaw = entry.get("sentAt");
                    long sentAt = sentAtRaw instanceof Number number ? number.longValue() : 0L;
                    mails.add(new Mail(
                            String.valueOf(entry.get("from")),
                            String.valueOf(entry.get("message")),
                            sentAt));
                }
                if (!mails.isEmpty()) {
                    inbox.put(uuid, mails);
                }
            } catch (RuntimeException ignored) {
                // Skip bad rows.
            }
        }
    }

    private void save() {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<UUID, List<Mail>> entry : inbox.entrySet()) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (Mail mail : entry.getValue()) {
                Map<String, Object> map = new java.util.LinkedHashMap<>();
                map.put("from", mail.from());
                map.put("message", mail.message());
                map.put("sentAt", mail.sentAt());
                list.add(map);
            }
            config.set(entry.getKey().toString(), list);
        }
        try {
            config.save(file);
        } catch (IOException ignored) {
            // Best effort.
        }
    }
}
