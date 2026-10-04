package com.forge.core.data;

import com.forge.core.ForgeCore;
import com.forge.core.util.Locs;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jspecify.annotations.Nullable;

/**
 * Named server warps, stored in {@code warps.yml}. Names are case-insensitive.
 */
public final class WarpManager {
    private final ForgeCore plugin;
    private final File file;
    private final Map<String, Location> warps = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public WarpManager(ForgeCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "warps.yml");
        load();
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (Map<?, ?> entry : config.getMapList("warps")) {
            Object name = entry.get("name");
            Location location = Locs.deserialize(entry.get("location"));
            if (name instanceof String warpName && location != null) {
                warps.put(warpName, location);
            }
        }
    }

    /** Persist warps.yml. */
    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<String, Location> entry : warps.entrySet()) {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("name", entry.getKey());
            map.put("location", Locs.serialize(entry.getValue()));
            list.add(map);
        }
        config.set("warps", list);
        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not save warps.yml: " + exception.getMessage());
        }
    }

    public void set(String name, Location location) {
        warps.put(name.toLowerCase(Locale.ROOT), location);
        save();
    }

    public boolean remove(String name) {
        boolean removed = warps.remove(name.toLowerCase(Locale.ROOT)) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public @Nullable Location get(String name) {
        return warps.get(name.toLowerCase(Locale.ROOT));
    }

    public boolean exists(String name) {
        return warps.containsKey(name.toLowerCase(Locale.ROOT));
    }

    /** Warp names in alphabetical order. */
    public List<String> names() {
        return new ArrayList<>(warps.keySet());
    }

    public int size() {
        return warps.size();
    }
}
