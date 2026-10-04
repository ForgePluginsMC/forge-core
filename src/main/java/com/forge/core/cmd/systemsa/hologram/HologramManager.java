package com.forge.core.cmd.systemsa.hologram;

import com.forge.core.ForgeCore;
import com.forge.core.util.Locs;
import com.forge.core.util.Placeholders;
import com.forge.core.util.Text;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.jspecify.annotations.Nullable;

/**
 * Floating text holograms built from {@link TextDisplay} entities, one per
 * line. Lines support MiniMessage and placeholders; text refreshes every 5
 * seconds, resolving placeholders against the nearest viewer. Stored in
 * {@code holograms.yml}.
 */
public final class HologramManager {
    private static final double LINE_SPACING = 0.3;
    private static final double VIEW_DISTANCE_SQUARED = 48.0 * 48.0;

    private static @Nullable HologramManager instance;

    /** Global accessor. */
    public static HologramManager get() {
        if (instance == null) {
            throw new IllegalStateException("HologramManager not initialized");
        }
        return instance;
    }

    /** One hologram: a location plus stacked text lines. */
    public static final class Hologram {
        public final String name;
        public Location location;
        public final List<String> lines = new ArrayList<>();
        final List<UUID> entities = new ArrayList<>();

        Hologram(String name, Location location) {
            this.name = name;
            this.location = location;
        }
    }

    private final ForgeCore plugin;
    private final File file;
    private final Map<String, Hologram> holograms = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public HologramManager(ForgeCore plugin) {
        instance = this;
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "holograms.yml");
        load();
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::refreshAll, 100L, 100L);
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (Map<?, ?> entry : config.getMapList("holograms")) {
            Object nameRaw = entry.get("name");
            Location location = Locs.deserialize(entry.get("location"));
            if (!(nameRaw instanceof String name) || location == null) {
                continue;
            }
            Hologram hologram = new Hologram(name, location);
            Object linesRaw = entry.get("lines");
            if (linesRaw instanceof List<?> rawLines) {
                for (Object line : rawLines) {
                    if (line instanceof String text) {
                        hologram.lines.add(text);
                    }
                }
            }
            Object entitiesRaw = entry.get("entities");
            List<String> entityIds = new ArrayList<>();
            if (entitiesRaw instanceof List<?> rawEntities) {
                for (Object id : rawEntities) {
                    if (id instanceof String text) {
                        entityIds.add(text);
                    }
                }
            }
            holograms.put(name, hologram);
            respawn(hologram, entityIds);
        }
    }

    /** Persist holograms.yml. */
    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Hologram hologram : holograms.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", hologram.name);
            map.put("location", Locs.serialize(hologram.location));
            map.put("lines", new ArrayList<>(hologram.lines));
            List<String> ids = new ArrayList<>();
            for (UUID id : hologram.entities) {
                ids.add(id.toString());
            }
            map.put("entities", ids);
            list.add(map);
        }
        config.set("holograms", list);
        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not save holograms.yml: " + exception.getMessage());
        }
    }

    public boolean create(String name, Location location, @Nullable String firstLine) {
        String key = name.toLowerCase(Locale.ROOT);
        if (holograms.containsKey(key)) {
            return false;
        }
        Hologram hologram = new Hologram(name, location);
        if (firstLine != null && !firstLine.isBlank()) {
            hologram.lines.add(firstLine);
        }
        holograms.put(key, hologram);
        respawn(hologram, List.of());
        save();
        return true;
    }

    public boolean delete(String name) {
        Hologram hologram = holograms.remove(name.toLowerCase(Locale.ROOT));
        if (hologram == null) {
            return false;
        }
        removeEntities(hologram);
        save();
        return true;
    }

    public @Nullable Hologram get(String name) {
        return holograms.get(name.toLowerCase(Locale.ROOT));
    }

    public List<String> names() {
        return new ArrayList<>(holograms.keySet());
    }

    public void moveHere(Hologram hologram, Location location) {
        hologram.location = location;
        respawn(hologram, List.of());
        save();
    }

    public void addLine(Hologram hologram, String text) {
        hologram.lines.add(text);
        respawn(hologram, List.of());
        save();
    }

    public boolean setLine(Hologram hologram, int index, String text) {
        if (index < 0 || index >= hologram.lines.size()) {
            return false;
        }
        hologram.lines.set(index, text);
        respawn(hologram, List.of());
        save();
        return true;
    }

    public boolean removeLine(Hologram hologram, int index) {
        if (index < 0 || index >= hologram.lines.size()) {
            return false;
        }
        hologram.lines.remove(index);
        respawn(hologram, List.of());
        save();
        return true;
    }

    private void removeEntities(Hologram hologram) {
        for (UUID id : hologram.entities) {
            Entity entity = Bukkit.getEntity(id);
            if (entity != null) {
                entity.remove();
            }
        }
        hologram.entities.clear();
    }

    /** Rebuild the display entities, reusing surviving ones by UUID. */
    private void respawn(Hologram hologram, List<String> knownIds) {
        List<UUID> fresh = new ArrayList<>();
        List<UUID> known = new ArrayList<>();
        for (String id : knownIds) {
            try {
                known.add(UUID.fromString(id));
            } catch (IllegalArgumentException ignored) {
                // Bad UUID in file; spawn a fresh entity instead.
            }
        }
        for (int i = 0; i < hologram.lines.size(); i++) {
            TextDisplay display = null;
            if (i < known.size()) {
                Entity entity = Bukkit.getEntity(known.get(i));
                if (entity instanceof TextDisplay textDisplay) {
                    display = textDisplay;
                }
            }
            if (display == null) {
                Location at = hologram.location.clone().add(0.0, -i * LINE_SPACING, 0.0);
                if (at.getWorld() == null) {
                    continue;
                }
                display = at.getWorld().spawn(at, TextDisplay.class, spawned -> {
                    spawned.setBillboard(Display.Billboard.CENTER);
                    spawned.setAlignment(TextDisplay.TextAlignment.CENTER);
                });
            } else {
                display.teleport(hologram.location.clone().add(0.0, -i * LINE_SPACING, 0.0));
            }
            fresh.add(display.getUniqueId());
        }
        // Remove stale entities that no longer map to a line.
        for (UUID id : hologram.entities) {
            if (!fresh.contains(id)) {
                Entity entity = Bukkit.getEntity(id);
                if (entity != null) {
                    entity.remove();
                }
            }
        }
        hologram.entities.clear();
        hologram.entities.addAll(fresh);
        refresh(hologram);
    }

    private @Nullable Player viewerFor(Hologram hologram) {
        Location location = hologram.location;
        if (location.getWorld() == null) {
            return null;
        }
        Player best = null;
        double bestDistance = VIEW_DISTANCE_SQUARED;
        for (Player player : location.getWorld().getPlayers()) {
            double distance = player.getLocation().distanceSquared(location);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = player;
            }
        }
        return best;
    }

    private void refresh(Hologram hologram) {
        Player viewer = viewerFor(hologram);
        for (int i = 0; i < hologram.entities.size() && i < hologram.lines.size(); i++) {
            Entity entity = Bukkit.getEntity(hologram.entities.get(i));
            if (entity instanceof TextDisplay display) {
                display.text(Text.of(Placeholders.apply(viewer, hologram.lines.get(i))));
            }
        }
    }

    private void refreshAll() {
        for (Hologram hologram : holograms.values()) {
            refresh(hologram);
        }
    }
}
