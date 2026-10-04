package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jspecify.annotations.NullMarked;

/**
 * Player warning system, stored in {@code warnings.yml}.
 * Warnings expire after a configurable number of days.
 */
@NullMarked
public final class WarnManager {
    public record Warning(UUID target, String targetName, String reason,
                          String category, String by, long at) {
        public boolean expired(long expireMillis) {
            return expireMillis > 0 && at + expireMillis < System.currentTimeMillis();
        }
    }

    private final ForgeCore plugin;
    private final File file;
    private final Map<UUID, List<Warning>> warnings = new ConcurrentHashMap<>();

    public WarnManager(ForgeCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "warnings.yml");
        load();
    }

    public void warn(UUID target, String targetName, String reason, String category, String by) {
        warnings.computeIfAbsent(target, k -> new ArrayList<>())
                .add(new Warning(target, targetName, reason, category, by, System.currentTimeMillis()));
        save();
    }

    /** Active (non-expired) warnings for a player. */
    public List<Warning> get(UUID target) {
        List<Warning> list = warnings.getOrDefault(target, List.of());
        long expire = expireMillis();
        List<Warning> active = new ArrayList<>();
        for (Warning w : list) {
            if (!w.expired(expire)) {
                active.add(w);
            }
        }
        if (active.size() != list.size()) {
            if (active.isEmpty()) {
                warnings.remove(target);
            } else {
                warnings.put(target, active);
            }
            save();
        }
        return List.copyOf(active);
    }

    public boolean clear(UUID target) {
        boolean removed = warnings.remove(target) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public int count(UUID target) {
        return get(target).size();
    }

    private long expireMillis() {
        return plugin.getConfig().getLong("warn-expire-days", 30) * 24L * 60L * 60L * 1000L;
    }

    @SuppressWarnings("unchecked")
    private void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                List<Warning> list = new ArrayList<>();
                List<Map<?, ?>> maps = config.getMapList(key);
                for (Map<?, ?> map : maps) {
                    Object category = map.get("category");
                    list.add(new Warning(
                            uuid,
                            String.valueOf(map.get("name")),
                            String.valueOf(map.get("reason")),
                            category == null ? "general" : String.valueOf(category),
                            String.valueOf(map.get("by")),
                            ((Number) map.get("at")).longValue()));
                }
                if (!list.isEmpty()) {
                    warnings.put(uuid, list);
                }
            } catch (RuntimeException e) {
                plugin.getLogger().warning("Skipping bad warning entry: " + key);
            }
        }
    }

    private void save() {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<UUID, List<Warning>> entry : warnings.entrySet()) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (Warning w : entry.getValue()) {
                Map<String, Object> map = new java.util.LinkedHashMap<>();
                map.put("name", w.targetName());
                map.put("reason", w.reason());
                map.put("category", w.category());
                map.put("by", w.by());
                map.put("at", w.at());
                list.add(map);
            }
            config.set(entry.getKey().toString(), list);
        }
        try {
            config.save(file);
        } catch (java.io.IOException e) {
            plugin.getLogger().warning("Could not save warnings.yml: " + e.getMessage());
        }
    }
}
