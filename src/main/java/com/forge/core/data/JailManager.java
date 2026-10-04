package com.forge.core.data;

import com.forge.core.ForgeCore;
import com.forge.core.util.Locs;
import com.forge.core.util.Text;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * Jails: named cells. Jailed players are teleported in and held there;
 * unjailing returns them to where they were taken from.
 */
public final class JailManager {
    private final ForgeCore plugin;
    private final File file;
    private final Map<String, Location> jails = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public JailManager(ForgeCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "jails.yml");
        load();
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (Map<?, ?> entry : config.getMapList("jails")) {
            Object name = entry.get("name");
            Location location = Locs.deserialize(entry.get("location"));
            if (name instanceof String jailName && location != null) {
                jails.put(jailName, location);
            }
        }
    }

    /** Persist jails.yml. */
    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<String, Location> entry : jails.entrySet()) {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("name", entry.getKey());
            map.put("location", Locs.serialize(entry.getValue()));
            list.add(map);
        }
        config.set("jails", list);
        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not save jails.yml: " + exception.getMessage());
        }
    }

    public void set(String name, Location location) {
        jails.put(name.toLowerCase(Locale.ROOT), location);
        save();
    }

    public boolean remove(String name) {
        boolean removed = jails.remove(name.toLowerCase(Locale.ROOT)) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public @Nullable Location get(String name) {
        return jails.get(name.toLowerCase(Locale.ROOT));
    }

    public List<String> names() {
        return new ArrayList<>(jails.keySet());
    }

    /** True when the player is currently jailed. */
    public boolean isJailed(Player player) {
        return plugin.users().get(player).getString("jail.name", null) != null;
    }

    /** Jail a player in the named jail. */
    public void jail(Player player, String jailName) {
        Location jail = get(jailName);
        if (jail == null) {
            return;
        }
        UserData data = plugin.users().get(player);
        if (!isJailed(player)) {
            data.setLocation("jail.return", player.getLocation());
        }
        data.setString("jail.name", jailName.toLowerCase(Locale.ROOT));
        plugin.users().save(player.getUniqueId());
        player.teleport(jail);
        Text.error(player, "You have been jailed.");
    }

    /** Release a player, returning them to their pre-jail location. */
    public void unjail(Player player) {
        UserData data = plugin.users().get(player);
        Location back = data.getLocation("jail.return");
        data.set("jail.name", null);
        data.set("jail.return", null);
        plugin.users().save(player.getUniqueId());
        if (back != null) {
            player.teleport(back);
        }
        Text.ok(player, "You have been released from jail.");
    }
}
