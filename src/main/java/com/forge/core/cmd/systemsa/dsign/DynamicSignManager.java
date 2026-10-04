package com.forge.core.cmd.systemsa.dsign;

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
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * Dynamic signs: targeted signs whose four lines support MiniMessage and
 * placeholders, refreshed on a timer. Broken signs drop their entry.
 * Stored in {@code dsigns.yml}.
 */
public final class DynamicSignManager {
    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private static @Nullable DynamicSignManager instance;

    /** Global accessor. */
    public static DynamicSignManager get() {
        if (instance == null) {
            throw new IllegalStateException("DynamicSignManager not initialized");
        }
        return instance;
    }

    /** One dynamic sign: a block location plus four raw template lines. */
    public static final class DynamicSign {
        public final String name;
        public Location location;
        public final String[] lines = new String[4];

        DynamicSign(String name, Location location, String[] lines) {
            this.name = name;
            this.location = location;
            System.arraycopy(lines, 0, this.lines, 0, 4);
        }
    }

    private final ForgeCore plugin;
    private final File file;
    private final Map<String, DynamicSign> signs = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public DynamicSignManager(ForgeCore plugin) {
        instance = this;
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "dsigns.yml");
        load();
        int seconds = Math.max(5, plugin.getConfig().getInt("dsign-interval-seconds", 30));
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::refreshAll, seconds * 20L, seconds * 20L);
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (Map<?, ?> entry : config.getMapList("dsigns")) {
            Object nameRaw = entry.get("name");
            Location location = Locs.deserialize(entry.get("location"));
            if (!(nameRaw instanceof String name) || location == null) {
                continue;
            }
            String[] lines = new String[]{"", "", "", ""};
            Object linesRaw = entry.get("lines");
            if (linesRaw instanceof List<?> rawLines) {
                for (int i = 0; i < 4 && i < rawLines.size(); i++) {
                    if (rawLines.get(i) instanceof String text) {
                        lines[i] = text;
                    }
                }
            }
            signs.put(name, new DynamicSign(name, location, lines));
        }
    }

    /** Persist dsigns.yml. */
    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        List<Map<String, Object>> list = new ArrayList<>();
        for (DynamicSign sign : signs.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", sign.name);
            map.put("location", Locs.serialize(sign.location));
            map.put("lines", List.of(sign.lines));
            list.add(map);
        }
        config.set("dsigns", list);
        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not save dsigns.yml: " + exception.getMessage());
        }
    }

    public boolean create(String name, Block block) {
        String key = name.toLowerCase(Locale.ROOT);
        if (signs.containsKey(key)) {
            return false;
        }
        if (!(block.getState() instanceof Sign sign)) {
            return false;
        }
        String[] lines = new String[4];
        SignSide front = sign.getSide(Side.FRONT);
        for (int i = 0; i < 4; i++) {
            lines[i] = MINI.serialize(front.line(i));
        }
        signs.put(key, new DynamicSign(name, block.getLocation(), lines));
        save();
        return true;
    }

    public boolean delete(String name) {
        boolean removed = signs.remove(name.toLowerCase(Locale.ROOT)) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public @Nullable DynamicSign get(String name) {
        return signs.get(name.toLowerCase(Locale.ROOT));
    }

    public List<String> names() {
        return new ArrayList<>(signs.keySet());
    }

    private @Nullable Player viewerFor(Location location) {
        if (location.getWorld() == null) {
            return null;
        }
        Player best = null;
        double bestDistance = 32.0 * 32.0;
        for (Player player : location.getWorld().getPlayers()) {
            double distance = player.getLocation().distanceSquared(location);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = player;
            }
        }
        return best;
    }

    private void refreshAll() {
        List<String> dead = new ArrayList<>();
        for (DynamicSign entry : signs.values()) {
            Location location = entry.location;
            if (location.getWorld() == null) {
                continue;
            }
            Block block = location.getBlock();
            if (!(block.getState() instanceof Sign sign)) {
                dead.add(entry.name);
                continue;
            }
            Player viewer = viewerFor(location);
            SignSide front = sign.getSide(Side.FRONT);
            for (int i = 0; i < 4; i++) {
                front.line(i, Text.of(Placeholders.apply(viewer, entry.lines[i])));
            }
            sign.update();
        }
        for (String name : dead) {
            signs.remove(name.toLowerCase(Locale.ROOT));
            plugin.getLogger().info("Dynamic sign '" + name + "' was broken; entry removed.");
        }
        if (!dead.isEmpty()) {
            save();
        }
    }
}
