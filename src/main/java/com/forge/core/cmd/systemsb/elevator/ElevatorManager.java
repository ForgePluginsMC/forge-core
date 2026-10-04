package com.forge.core.cmd.systemsb.elevator;

import com.forge.core.ForgeCore;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.jspecify.annotations.Nullable;

/**
 * Sneak elevators (no command): sneak while standing on a sign block whose
 * first line is {@code [elevator]} and second line is {@code up} or
 * {@code down} to ride to the nearest elevator sign in that direction
 * (up to 20 blocks away). The player keeps their x/z and look direction.
 */
public final class ElevatorManager implements Listener {
    private static final int RANGE = 20;
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private final ForgeCore plugin;
    private final Map<UUID, Long> cooldown = new ConcurrentHashMap<>();

    public ElevatorManager(ForgeCore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) {
            return;
        }
        Player player = event.getPlayer();
        long now = System.currentTimeMillis();
        if (now - cooldown.getOrDefault(player.getUniqueId(), 0L) < 1000L) {
            return;
        }
        Block feet = player.getLocation().getBlock();
        Sign sign = signAt(feet);
        String direction = direction(sign);
        if (direction == null) {
            sign = signAt(feet.getRelative(BlockFace.DOWN));
            direction = direction(sign);
        }
        if (sign == null || direction == null) {
            return;
        }
        Block target = "up".equals(direction) ? find(sign.getBlock(), 1) : find(sign.getBlock(), -1);
        if (target == null) {
            return;
        }
        cooldown.put(player.getUniqueId(), now);
        Location from = player.getLocation();
        Location dest = target.getLocation().add(0.5, 1.0, 0.5);
        dest.setYaw(from.getYaw());
        dest.setPitch(from.getPitch());
        player.teleport(dest);
    }

    private @Nullable Sign signAt(Block block) {
        return block.getState() instanceof Sign sign ? sign : null;
    }

    /** The elevator direction of a sign's front face, or null when it is not one. */
    private @Nullable String direction(@Nullable Sign sign) {
        if (sign == null) {
            return null;
        }
        List<Component> lines = sign.getSide(Side.FRONT).lines();
        if (lines.size() < 2) {
            return null;
        }
        String line1 = PLAIN.serialize(lines.get(0)).trim();
        if (!"[elevator]".equalsIgnoreCase(line1)) {
            return null;
        }
        String line2 = PLAIN.serialize(lines.get(1)).trim().toLowerCase(Locale.ROOT);
        return line2.equals("up") || line2.equals("down") ? line2 : null;
    }

    /** Nearest elevator sign from {@code from} in a vertical direction. */
    private @Nullable Block find(Block from, int step) {
        for (int i = 1; i <= RANGE; i++) {
            Block candidate = from.getRelative(0, step * i, 0);
            if (direction(signAt(candidate)) != null) {
                return candidate;
            }
        }
        return null;
    }
}
