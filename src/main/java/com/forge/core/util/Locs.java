package com.forge.core.util;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jspecify.annotations.Nullable;

/**
 * Location serialization for YAML storage.
 */
public final class Locs {
    private Locs() {
    }

    /** Serialize a location to a plain map. */
    public static Map<String, Object> serialize(Location location) {
        Map<String, Object> map = new HashMap<>();
        World world = location.getWorld();
        map.put("world", world == null ? "" : world.getUID().toString());
        map.put("x", location.getX());
        map.put("y", location.getY());
        map.put("z", location.getZ());
        map.put("yaw", location.getYaw());
        map.put("pitch", location.getPitch());
        return map;
    }

    /** Deserialize a location map; null when the world is gone or data is bad. */
    @SuppressWarnings("unchecked")
    public static @Nullable Location deserialize(@Nullable Object raw) {
        if (!(raw instanceof Map<?, ?> map)) {
            return null;
        }
        try {
            Object worldRaw = map.get("world");
            if (!(worldRaw instanceof String worldId) || worldId.isEmpty()) {
                return null;
            }
            World world = Bukkit.getWorld(java.util.UUID.fromString(worldId));
            if (world == null) {
                return null;
            }
            double x = ((Number) map.get("x")).doubleValue();
            double y = ((Number) map.get("y")).doubleValue();
            double z = ((Number) map.get("z")).doubleValue();
            float yaw = map.get("yaw") instanceof Number n ? n.floatValue() : 0f;
            float pitch = map.get("pitch") instanceof Number n ? n.floatValue() : 0f;
            return new Location(world, x, y, z, yaw, pitch);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    /** Short human-readable location, e.g. {@code world 100, 64, -200}. */
    public static String pretty(Location location) {
        World world = location.getWorld();
        String worldName = world == null ? "?" : world.getName();
        return worldName + " " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
    }
}
