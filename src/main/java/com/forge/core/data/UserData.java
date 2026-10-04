package com.forge.core.data;

import com.forge.core.util.Locs;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * Per-player persistent data, stored in {@code userdata/<uuid>.yml}.
 * All writes mark the data dirty; {@link UserManager} saves it.
 */
public final class UserData {
    private final UUID uuid;
    private final YamlConfiguration config;
    private boolean dirty;

    UserData(UUID uuid, YamlConfiguration config) {
        this.uuid = uuid;
        this.config = config;
    }

    public UUID uuid() {
        return uuid;
    }

    public boolean dirty() {
        return dirty;
    }

    void clean() {
        dirty = false;
    }

    public String getString(String path, String def) {
        return config.getString(path, def);
    }

    public void setString(String path, @Nullable String value) {
        config.set(path, value);
        dirty = true;
    }

    public long getLong(String path, long def) {
        return config.getLong(path, def);
    }

    public void setLong(String path, long value) {
        config.set(path, value);
        dirty = true;
    }

    public double getDouble(String path, double def) {
        return config.getDouble(path, def);
    }

    public void setDouble(String path, double value) {
        config.set(path, value);
        dirty = true;
    }

    public boolean getBoolean(String path, boolean def) {
        return config.getBoolean(path, def);
    }

    public void setBoolean(String path, boolean value) {
        config.set(path, value);
        dirty = true;
    }

    public void set(String path, @Nullable Object value) {
        config.set(path, value);
        dirty = true;
    }

    public @Nullable Location getLocation(String path) {
        return Locs.deserialize(config.get(path));
    }

    public void setLocation(String path, @Nullable Location location) {
        config.set(path, location == null ? null : Locs.serialize(location));
        dirty = true;
    }

    /** All named homes, keyed lower-case. */
    public Map<String, Location> homes() {
        Map<String, Location> homes = new LinkedHashMap<>();
        ConfigurationSection section = config.getConfigurationSection("homes");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                Location location = Locs.deserialize(section.get(key));
                if (location != null) {
                    homes.put(key.toLowerCase(java.util.Locale.ROOT), location);
                }
            }
        }
        return homes;
    }

    public void setHome(String name, Location location) {
        config.set("homes." + name.toLowerCase(java.util.Locale.ROOT), Locs.serialize(location));
        dirty = true;
    }

    public boolean removeHome(String name) {
        String key = "homes." + name.toLowerCase(java.util.Locale.ROOT);
        if (!config.contains(key)) {
            return false;
        }
        config.set(key, null);
        dirty = true;
        return true;
    }

    /** Total playtime in seconds (updated by the playtime ticker). */
    public long playtimeSeconds() {
        return config.getLong("playtime-seconds", 0);
    }

    public void addPlaytime(long seconds) {
        config.set("playtime-seconds", playtimeSeconds() + seconds);
        dirty = true;
    }

    public @Nullable String nick() {
        return config.getString("nick", null);
    }

    public void setNick(@Nullable String nick) {
        config.set("nick", nick);
        dirty = true;
    }

    /** Nickname if set, otherwise the player's real name. */
    public String nickOrName(Player player) {
        String nick = nick();
        return nick == null ? player.getName() : nick;
    }

    public boolean god() {
        return config.getBoolean("god", false);
    }

    public void setGod(boolean god) {
        config.set("god", god);
        dirty = true;
    }

    YamlConfiguration raw() {
        return config;
    }
}
