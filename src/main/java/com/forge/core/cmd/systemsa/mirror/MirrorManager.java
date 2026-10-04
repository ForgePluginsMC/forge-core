package com.forge.core.cmd.systemsa.mirror;

import com.forge.core.ForgeCore;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jspecify.annotations.Nullable;

/**
 * Mirrors block placements across 11 symmetry modes, centered on the
 * player's position when placement happens. Session-only.
 */
public final class MirrorManager implements Listener {
    /** The 11 mirror modes. */
    public static final List<String> MODES = List.of(
            "x", "z", "y", "point", "quad",
            "x-offset", "z-offset", "y-offset",
            "xy-diag", "xz-diag", "yz-diag");

    /** Short explanation per mode, shown by {@code /mirror mode}. */
    public static final Map<String, String> DESCRIPTIONS = Map.ofEntries(
            Map.entry("x", "mirror east-west across your position"),
            Map.entry("z", "mirror north-south across your position"),
            Map.entry("y", "mirror up-down across your position"),
            Map.entry("point", "180-degree rotation about your position"),
            Map.entry("quad", "four-way horizontal mirror"),
            Map.entry("x-offset", "mirror across a plane 5 blocks east of you"),
            Map.entry("z-offset", "mirror across a plane 5 blocks south of you"),
            Map.entry("y-offset", "mirror across a plane 5 blocks above you"),
            Map.entry("xy-diag", "diagonal mirror, swaps X and Y offsets"),
            Map.entry("xz-diag", "diagonal mirror, swaps X and Z offsets"),
            Map.entry("yz-diag", "diagonal mirror, swaps Y and Z offsets"));

    private static @Nullable MirrorManager instance;

    /** Global accessor. */
    public static MirrorManager get() {
        if (instance == null) {
            throw new IllegalStateException("MirrorManager not initialized");
        }
        return instance;
    }

    private final Map<UUID, String> active = new HashMap<>();

    public MirrorManager(ForgeCore plugin) {
        instance = this;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void start(Player player, String mode) {
        active.put(player.getUniqueId(), mode.toLowerCase(Locale.ROOT));
    }

    public void stop(Player player) {
        active.remove(player.getUniqueId());
    }

    public @Nullable String modeOf(Player player) {
        return active.get(player.getUniqueId());
    }

    public boolean isValidMode(String mode) {
        return MODES.contains(mode.toLowerCase(Locale.ROOT));
    }

    private static List<int[]> mirrors(String mode, int dx, int dy, int dz) {
        return switch (mode) {
            case "x" -> List.of(new int[]{-dx, dy, dz});
            case "z" -> List.of(new int[]{dx, dy, -dz});
            case "y" -> List.of(new int[]{dx, -dy, dz});
            case "point" -> List.of(new int[]{-dx, dy, -dz});
            case "quad" -> List.of(
                    new int[]{-dx, dy, dz},
                    new int[]{dx, dy, -dz},
                    new int[]{-dx, dy, -dz});
            case "x-offset" -> List.of(new int[]{-dx + 10, dy, dz});
            case "z-offset" -> List.of(new int[]{dx, dy, -dz + 10});
            case "y-offset" -> List.of(new int[]{dx, -dy + 10, dz});
            case "xy-diag" -> List.of(new int[]{dy, dx, dz});
            case "xz-diag" -> List.of(new int[]{dz, dy, dx});
            case "yz-diag" -> List.of(new int[]{dx, dz, dy});
            default -> List.of();
        };
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        String mode = active.get(event.getPlayer().getUniqueId());
        if (mode == null) {
            return;
        }
        Block placed = event.getBlockPlaced();
        Location origin = event.getPlayer().getLocation();
        int ox = origin.getBlockX();
        int oy = origin.getBlockY();
        int oz = origin.getBlockZ();
        int dx = placed.getX() - ox;
        int dy = placed.getY() - oy;
        int dz = placed.getZ() - oz;
        BlockData data = placed.getBlockData();
        for (int[] offset : mirrors(mode, dx, dy, dz)) {
            int tx = ox + offset[0];
            int ty = oy + offset[1];
            int tz = oz + offset[2];
            if (tx == placed.getX() && ty == placed.getY() && tz == placed.getZ()) {
                continue;
            }
            Block target = placed.getWorld().getBlockAt(tx, ty, tz);
            if (!target.isReplaceable()) {
                continue;
            }
            target.setBlockData(data.clone(), false);
        }
    }
}
