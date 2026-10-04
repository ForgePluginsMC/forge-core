package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.data.UserData;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * Tracks private-message partners (for /reply) and per-player ignore lists.
 * Ignore lists persist in userdata under {@code ignores} as comma-joined UUIDs.
 */
public final class MsgManager {
    /** Stand-in UUID for console / non-player senders. */
    public static final UUID CONSOLE_UUID = new UUID(0L, 0L);

    private static @Nullable MsgManager instance;

    private final ForgeCore plugin;
    private final Map<UUID, UUID> lastMessager = new ConcurrentHashMap<>();

    private MsgManager(ForgeCore plugin) {
        this.plugin = plugin;
    }

    static void init(ForgeCore plugin) {
        instance = new MsgManager(plugin);
    }

    public static MsgManager get() {
        MsgManager manager = instance;
        if (manager == null) {
            throw new IllegalStateException("MsgManager not initialized");
        }
        return manager;
    }

    /** UUID to use for a sender; console maps to {@link #CONSOLE_UUID}. */
    public static UUID uuidOf(@Nullable Player player) {
        return player == null ? CONSOLE_UUID : player.getUniqueId();
    }

    /** Record that {@code a} and {@code b} last messaged each other. */
    public void setLast(UUID a, UUID b) {
        lastMessager.put(a, b);
        lastMessager.put(b, a);
    }

    /** Who {@code uuid} last exchanged a private message with, or null. */
    public @Nullable UUID getLast(UUID uuid) {
        return lastMessager.get(uuid);
    }

    /** UUIDs ignored by {@code uuid}. */
    public Set<UUID> ignores(UUID uuid) {
        Set<UUID> out = new LinkedHashSet<>();
        String raw = plugin.users().get(uuid).getString("ignores", "");
        if (!raw.isEmpty()) {
            for (String part : raw.split(",")) {
                try {
                    out.add(UUID.fromString(part.trim()));
                } catch (IllegalArgumentException ignored) {
                    // Skip malformed entries.
                }
            }
        }
        return out;
    }

    /** Toggle ignoring {@code target}; returns true when now ignoring. */
    public boolean toggleIgnore(UUID uuid, UUID target) {
        Set<UUID> current = ignores(uuid);
        boolean nowIgnoring;
        if (current.contains(target)) {
            current.remove(target);
            nowIgnoring = false;
        } else {
            current.add(target);
            nowIgnoring = true;
        }
        StringBuilder joined = new StringBuilder();
        for (UUID id : current) {
            if (!joined.isEmpty()) {
                joined.append(',');
            }
            joined.append(id);
        }
        UserData data = plugin.users().get(uuid);
        data.setString("ignores", joined.toString());
        plugin.users().save(uuid);
        return nowIgnoring;
    }

    public boolean isIgnoring(UUID uuid, UUID target) {
        return ignores(uuid).contains(target);
    }
}
