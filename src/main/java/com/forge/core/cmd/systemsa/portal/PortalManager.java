package com.forge.core.cmd.systemsa.portal;

import com.forge.core.ForgeCore;
import com.forge.core.util.Locs;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jspecify.annotations.Nullable;

/**
 * Portal pads: walk onto a 3x3 pad to teleport, run console commands and/or
 * hop to another BungeeCord server. Stored in {@code portals.yml}.
 */
public final class PortalManager implements Listener {
    private static @Nullable PortalManager instance;

    /** Global accessor. */
    public static PortalManager get() {
        if (instance == null) {
            throw new IllegalStateException("PortalManager not initialized");
        }
        return instance;
    }

    /** One portal pad. */
    public static final class Portal {
        public final String name;
        public Location center;
        public int radius = 2;
        public @Nullable Location destination;
        public final List<String> commands = new ArrayList<>();
        public @Nullable String server;
        public String particleName = "PORTAL";
        public Particle particle = Particle.PORTAL;

        Portal(String name, Location center) {
            this.name = name;
            this.center = center;
        }

        public void resolveParticle() {
            try {
                particle = Particle.valueOf(particleName.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                particle = Particle.PORTAL;
            }
        }
    }

    private final ForgeCore plugin;
    private final File file;
    private final Map<String, Portal> portals = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public PortalManager(ForgeCore plugin) {
        instance = this;
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "portals.yml");
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
        load();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::spawnParticles, 20L, 10L);
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (Map<?, ?> entry : config.getMapList("portals")) {
            Object nameRaw = entry.get("name");
            Location center = Locs.deserialize(entry.get("center"));
            if (!(nameRaw instanceof String name) || center == null) {
                continue;
            }
            Portal portal = new Portal(name, center);
            Object radiusRaw = entry.get("radius");
            if (radiusRaw instanceof Number number) {
                portal.radius = Math.max(1, number.intValue());
            }
            portal.destination = Locs.deserialize(entry.get("destination"));
            Object commandsRaw = entry.get("commands");
            if (commandsRaw instanceof List<?> list) {
                for (Object item : list) {
                    if (item instanceof String command) {
                        portal.commands.add(command);
                    }
                }
            }
            Object serverRaw = entry.get("server");
            if (serverRaw instanceof String server && !server.isBlank()) {
                portal.server = server;
            }
            Object particleRaw = entry.get("particle");
            if (particleRaw instanceof String particle && !particle.isBlank()) {
                portal.particleName = particle;
            }
            portal.resolveParticle();
            portals.put(name, portal);
        }
    }

    /** Persist portals.yml. */
    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Portal portal : portals.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", portal.name);
            map.put("center", Locs.serialize(portal.center));
            map.put("radius", portal.radius);
            if (portal.destination != null) {
                map.put("destination", Locs.serialize(portal.destination));
            }
            map.put("commands", new ArrayList<>(portal.commands));
            if (portal.server != null) {
                map.put("server", portal.server);
            }
            map.put("particle", portal.particleName);
            list.add(map);
        }
        config.set("portals", list);
        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not save portals.yml: " + exception.getMessage());
        }
    }

    public boolean create(String name, Location center) {
        String key = name.toLowerCase(Locale.ROOT);
        if (portals.containsKey(key)) {
            return false;
        }
        portals.put(key, new Portal(name, center));
        save();
        return true;
    }

    public boolean delete(String name) {
        boolean removed = portals.remove(name.toLowerCase(Locale.ROOT)) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public @Nullable Portal get(String name) {
        return portals.get(name.toLowerCase(Locale.ROOT));
    }

    public List<String> names() {
        return new ArrayList<>(portals.keySet());
    }

    private boolean inside(Portal portal, Location location) {
        Location center = portal.center;
        if (center.getWorld() == null || !center.getWorld().equals(location.getWorld())) {
            return false;
        }
        int radius = portal.radius;
        return Math.abs(location.getBlockX() - center.getBlockX()) <= radius
                && Math.abs(location.getBlockZ() - center.getBlockZ()) <= radius
                && Math.abs(location.getBlockY() - center.getBlockY()) <= 2;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Location to = event.getTo();
        Location from = event.getFrom();
        if (to.getBlockX() == from.getBlockX()
                && to.getBlockY() == from.getBlockY()
                && to.getBlockZ() == from.getBlockZ()) {
            return;
        }
        Player player = event.getPlayer();
        long now = System.currentTimeMillis();
        Long last = cooldowns.get(player.getUniqueId());
        if (last != null && now - last < 3000L) {
            return;
        }
        for (Portal portal : portals.values()) {
            if (inside(portal, to)) {
                cooldowns.put(player.getUniqueId(), now);
                trigger(player, portal);
                return;
            }
        }
    }

    private void trigger(Player player, Portal portal) {
        if (portal.destination != null) {
            player.teleport(portal.destination);
        }
        for (String raw : portal.commands) {
            String command = raw.replace("%player%", player.getName());
            if (command.startsWith("/")) {
                command = command.substring(1);
            }
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
        }
        if (portal.server != null) {
            sendToServer(player, portal.server);
        }
    }

    private void sendToServer(Player player, String server) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);
            out.writeUTF("Connect");
            out.writeUTF(server);
            player.sendPluginMessage(plugin, "BungeeCord", bytes.toByteArray());
        } catch (IOException exception) {
            plugin.getLogger().warning("BungeeCord send failed: " + exception.getMessage());
        }
    }

    private void spawnParticles() {
        for (Portal portal : portals.values()) {
            Location center = portal.center;
            if (center.getWorld() == null) {
                continue;
            }
            try {
                center.getWorld().spawnParticle(
                        portal.particle,
                        center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
                        10, 1.0, 0.6, 1.0, 0.02);
            } catch (IllegalArgumentException ignored) {
                // Particle needs extra data (e.g. DUST); skip silently.
            }
        }
    }

}
