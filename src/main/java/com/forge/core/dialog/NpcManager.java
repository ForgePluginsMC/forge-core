package com.forge.core.dialog;

import com.forge.core.ForgeCore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Manages NPCs: spawned villagers with attached dialogs.
 *
 * <p>NPCs persist in {@code npcs.yml} by UUID. The NPC's entity carries a
 * persistent-data tag so right-clicks can be routed even after restarts.
 */
@NullMarked
public final class NpcManager {
    private final ForgeCore plugin;
    private final NamespacedKey npcKey;
    private final Map<String, Npc> npcs = new HashMap<>();

    public NpcManager(ForgeCore plugin) {
        this.plugin = plugin;
        this.npcKey = new NamespacedKey(plugin, "npc_id");
        load();
    }

    /** Spawn an NPC villager at a location. */
    public Npc create(String id, String name, Location location) {
        String key = id.toLowerCase(Locale.ROOT);
        remove(key);

        Villager villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        villager.customName(net.kyori.adventure.text.Component.text(name));
        villager.setCustomNameVisible(true);
        villager.setAI(false);
        villager.setInvulnerable(true);
        villager.setSilent(true);
        villager.getPersistentDataContainer().set(npcKey, PersistentDataType.STRING, key);

        Npc npc = new Npc(key, name, villager.getUniqueId(), location.clone());
        npcs.put(key, npc);
        save();
        return npc;
    }

    /** Get an NPC by id. */
    public @Nullable Npc get(String id) {
        return npcs.get(id.toLowerCase(Locale.ROOT));
    }

    /** All NPC ids. */
    public List<String> ids() {
        return new ArrayList<>(npcs.keySet());
    }

    /** Remove an NPC (and its entity). */
    public boolean remove(String id) {
        String key = id.toLowerCase(Locale.ROOT);
        Npc npc = npcs.remove(key);
        if (npc == null) {
            return false;
        }
        Entity entity = plugin.getServer().getEntity(npc.entityId());
        if (entity != null) {
            entity.remove();
        }
        save();
        return true;
    }

    /** Move an NPC to a new location (respawns the entity). */
    public boolean move(String id, Location location) {
        String key = id.toLowerCase(Locale.ROOT);
        Npc npc = npcs.get(key);
        if (npc == null) {
            return false;
        }
        Entity old = plugin.getServer().getEntity(npc.entityId());
        if (old != null) {
            old.remove();
        }
        Villager villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        villager.customName(net.kyori.adventure.text.Component.text(npc.name()));
        villager.setCustomNameVisible(true);
        villager.setAI(false);
        villager.setInvulnerable(true);
        villager.setSilent(true);
        villager.getPersistentDataContainer().set(npcKey, PersistentDataType.STRING, key);
        npc.setEntityId(villager.getUniqueId());
        npc.setLocation(location.clone());
        save();
        return true;
    }

    /** Attach a dialog to an NPC. */
    public boolean setDialog(String id, String dialogId) {
        Npc npc = get(id);
        if (npc == null) {
            return false;
        }
        npc.setDialogId(dialogId.toLowerCase(Locale.ROOT));
        save();
        return true;
    }

    /** Find the NPC id for an entity (via persistent data). */
    public @Nullable String npcIdFor(Entity entity) {
        return entity.getPersistentDataContainer().get(npcKey, PersistentDataType.STRING);
    }

    private void save() {
        YamlConfiguration config = new YamlConfiguration();
        for (Npc npc : npcs.values()) {
            String path = "npcs." + npc.id();
            config.set(path + ".name", npc.name());
            config.set(path + ".entity", npc.entityId().toString());
            config.set(path + ".dialog", npc.dialogId());
            Location loc = npc.location();
            if (loc.getWorld() != null) {
                config.set(path + ".world", loc.getWorld().getName());
            }
            config.set(path + ".x", loc.getX());
            config.set(path + ".y", loc.getY());
            config.set(path + ".z", loc.getZ());
            config.set(path + ".yaw", loc.getYaw());
            config.set(path + ".pitch", loc.getPitch());
        }
        try {
            config.save(plugin.getDataFolder().toPath().resolve("npcs.yml").toFile());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save npcs.yml: " + e.getMessage());
        }
    }

    private void load() {
        java.io.File file = plugin.getDataFolder().toPath().resolve("npcs.yml").toFile();
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("npcs");
        if (section == null) {
            return;
        }
        for (String id : section.getKeys(false)) {
            try {
                String name = section.getString(id + ".name", id);
                UUID entityId = UUID.fromString(section.getString(id + ".entity", ""));
                String dialogId = section.getString(id + ".dialog");
                String worldName = section.getString(id + ".world", "");
                org.bukkit.World world = plugin.getServer().getWorld(worldName);
                if (world == null) {
                    continue;
                }
                Location loc = new Location(world,
                        section.getDouble(id + ".x"),
                        section.getDouble(id + ".y"),
                        section.getDouble(id + ".z"),
                        (float) section.getDouble(id + ".yaw"),
                        (float) section.getDouble(id + ".pitch"));
                Npc npc = new Npc(id, name, entityId, loc);
                if (dialogId != null) {
                    npc.setDialogId(dialogId);
                }
                npcs.put(id.toLowerCase(Locale.ROOT), npc);
            } catch (Exception e) {
                plugin.getLogger().warning("Skipping bad NPC entry '" + id + "': " + e.getMessage());
            }
        }
    }

    /** An NPC record. */
    @NullMarked
    public static final class Npc {
        private final String id;
        private final String name;
        private UUID entityId;
        private Location location;
        private @Nullable String dialogId;

        Npc(String id, String name, UUID entityId, Location location) {
            this.id = id;
            this.name = name;
            this.entityId = entityId;
            this.location = location;
        }

        public String id() {
            return id;
        }

        public String name() {
            return name;
        }

        public UUID entityId() {
            return entityId;
        }

        void setEntityId(UUID entityId) {
            this.entityId = entityId;
        }

        public Location location() {
            return location;
        }

        void setLocation(Location location) {
            this.location = location;
        }

        public @Nullable String dialogId() {
            return dialogId;
        }

        void setDialogId(@Nullable String dialogId) {
            this.dialogId = dialogId;
        }
    }
}
