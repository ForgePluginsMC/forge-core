package com.forge.core.data;

import com.forge.core.ForgeCore;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 * Loads, caches and saves per-player {@link UserData}.
 * Saves run asynchronously; a full save happens on disable.
 */
public final class UserManager {
    private final ForgeCore plugin;
    private final File folder;
    private final Map<UUID, UserData> cache = new ConcurrentHashMap<>();

    public UserManager(ForgeCore plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "userdata");
        folder.mkdirs();
    }

    /** Get (loading from disk when needed) a player's data. */
    public UserData get(UUID uuid) {
        return cache.computeIfAbsent(uuid, this::load);
    }

    /** Get a player's data. */
    public UserData get(Player player) {
        return get(player.getUniqueId());
    }

    private UserData load(UUID uuid) {
        File file = new File(folder, uuid + ".yml");
        YamlConfiguration config = file.exists() ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();
        return new UserData(uuid, config);
    }

    /** Save one player's data asynchronously when dirty. */
    public void save(UUID uuid) {
        UserData data = cache.get(uuid);
        if (data == null || !data.dirty()) {
            return;
        }
        data.clean();
        YamlConfiguration snapshot = data.raw();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> write(uuid, snapshot));
    }

    /** Save every cached entry synchronously (used on disable). */
    public void saveAll() {
        for (Map.Entry<UUID, UserData> entry : cache.entrySet()) {
            write(entry.getKey(), entry.getValue().raw());
            entry.getValue().clean();
        }
    }

    /** Drop from cache after saving. */
    public void unload(UUID uuid) {
        UserData data = cache.remove(uuid);
        if (data != null && data.dirty()) {
            data.clean();
            write(uuid, data.raw());
        }
    }

    private void write(UUID uuid, YamlConfiguration config) {
        try {
            config.save(new File(folder, uuid + ".yml"));
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not save userdata for " + uuid + ": " + exception.getMessage());
        }
    }
}
