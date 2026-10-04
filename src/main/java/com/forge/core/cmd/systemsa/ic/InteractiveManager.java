package com.forge.core.cmd.systemsa.ic;

import com.forge.core.ForgeCore;
import com.forge.core.util.Locs;
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
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jspecify.annotations.Nullable;

/**
 * Interactive commands: right-clicking a bound block or entity runs its
 * command list. Commands run as the player unless prefixed with
 * {@code [console]}. {@code %player%} is replaced. Stored in
 * {@code ics.yml}.
 */
public final class InteractiveManager implements Listener {
    private static @Nullable InteractiveManager instance;

    /** Global accessor. */
    public static InteractiveManager get() {
        if (instance == null) {
            throw new IllegalStateException("InteractiveManager not initialized");
        }
        return instance;
    }

    /** One interactive binding. */
    public static final class Interactive {
        public final String name;
        public final boolean block;
        public @Nullable Location blockLocation;
        public @Nullable UUID entityId;
        public final List<String> commands = new ArrayList<>();

        Interactive(String name, Location blockLocation) {
            this.name = name;
            this.block = true;
            this.blockLocation = blockLocation;
        }

        Interactive(String name, UUID entityId) {
            this.name = name;
            this.block = false;
            this.entityId = entityId;
        }
    }

    private final ForgeCore plugin;
    private final File file;
    private final Map<String, Interactive> bindings = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public InteractiveManager(ForgeCore plugin) {
        instance = this;
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "ics.yml");
        load();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (Map<?, ?> entry : config.getMapList("ics")) {
            Object nameRaw = entry.get("name");
            if (!(nameRaw instanceof String name)) {
                continue;
            }
            Object typeRaw = entry.get("type");
            Interactive interactive = null;
            if ("block".equals(typeRaw)) {
                Location location = Locs.deserialize(entry.get("location"));
                if (location != null) {
                    interactive = new Interactive(name, location);
                }
            } else if ("entity".equals(typeRaw) && entry.get("entity") instanceof String id) {
                try {
                    interactive = new Interactive(name, UUID.fromString(id));
                } catch (IllegalArgumentException ignored) {
                    // Bad UUID in file; skip this binding.
                }
            }
            if (interactive == null) {
                continue;
            }
            Object commandsRaw = entry.get("commands");
            if (commandsRaw instanceof List<?> list) {
                for (Object item : list) {
                    if (item instanceof String command) {
                        interactive.commands.add(command);
                    }
                }
            }
            bindings.put(name, interactive);
        }
    }

    /** Persist ics.yml. */
    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Interactive interactive : bindings.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", interactive.name);
            map.put("type", interactive.block ? "block" : "entity");
            if (interactive.block && interactive.blockLocation != null) {
                map.put("location", Locs.serialize(interactive.blockLocation));
            }
            if (!interactive.block && interactive.entityId != null) {
                map.put("entity", interactive.entityId.toString());
            }
            map.put("commands", new ArrayList<>(interactive.commands));
            list.add(map);
        }
        config.set("ics", list);
        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not save ics.yml: " + exception.getMessage());
        }
    }

    public boolean createBlock(String name, Location location) {
        String key = name.toLowerCase(Locale.ROOT);
        if (bindings.containsKey(key)) {
            return false;
        }
        bindings.put(key, new Interactive(name, location.getBlock().getLocation()));
        save();
        return true;
    }

    public boolean createEntity(String name, UUID entityId) {
        String key = name.toLowerCase(Locale.ROOT);
        if (bindings.containsKey(key)) {
            return false;
        }
        bindings.put(key, new Interactive(name, entityId));
        save();
        return true;
    }

    public boolean delete(String name) {
        boolean removed = bindings.remove(name.toLowerCase(Locale.ROOT)) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public @Nullable Interactive get(String name) {
        return bindings.get(name.toLowerCase(Locale.ROOT));
    }

    public List<String> names() {
        return new ArrayList<>(bindings.keySet());
    }

    private @Nullable Interactive byBlock(Location location) {
        for (Interactive interactive : bindings.values()) {
            Location blockLocation = interactive.blockLocation;
            if (interactive.block && blockLocation != null
                    && blockLocation.getWorld() != null
                    && blockLocation.getWorld().equals(location.getWorld())
                    && blockLocation.getBlockX() == location.getBlockX()
                    && blockLocation.getBlockY() == location.getBlockY()
                    && blockLocation.getBlockZ() == location.getBlockZ()) {
                return interactive;
            }
        }
        return null;
    }

    private @Nullable Interactive byEntity(UUID entityId) {
        for (Interactive interactive : bindings.values()) {
            if (!interactive.block && entityId.equals(interactive.entityId)) {
                return interactive;
            }
        }
        return null;
    }

    private void runCommands(Player player, Interactive interactive) {
        for (String raw : interactive.commands) {
            String command = raw.replace("%player%", player.getName());
            if (command.startsWith("[console]")) {
                String consoleCommand = command.substring("[console]".length()).trim();
                if (consoleCommand.startsWith("/")) {
                    consoleCommand = consoleCommand.substring(1);
                }
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), consoleCommand);
            } else {
                if (command.startsWith("/")) {
                    command = command.substring(1);
                }
                player.performCommand(command);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!EquipmentSlot.HAND.equals(event.getHand())) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        Interactive interactive = byBlock(block.getLocation());
        if (interactive == null || interactive.commands.isEmpty()) {
            return;
        }
        event.setCancelled(true);
        runCommands(event.getPlayer(), interactive);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Entity clicked = event.getRightClicked();
        if (clicked instanceof Player) {
            return;
        }
        Interactive interactive = byEntity(clicked.getUniqueId());
        if (interactive == null || interactive.commands.isEmpty()) {
            return;
        }
        event.setCancelled(true);
        runCommands(event.getPlayer(), interactive);
    }
}
