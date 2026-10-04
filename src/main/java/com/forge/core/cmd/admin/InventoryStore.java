package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jspecify.annotations.Nullable;

/**
 * Named inventory snapshots in {@code plugins/ForgeCore/inventories.yml},
 * backing {@code /invsave}, {@code /invload}, {@code /invlist} and
 * {@code /invremove}.
 */
public final class InventoryStore {
    private final ForgeCore plugin;
    private final File file;
    private final Map<String, SavedInventory> inventories = new LinkedHashMap<>();

    /** A saved snapshot: storage contents, armor and offhand. */
    public record SavedInventory(
            String name, UUID owner, long savedAt,
            ItemStack[] storage, ItemStack[] armor, @Nullable ItemStack offhand) {
        /** Number of non-empty item stacks in the snapshot. */
        public int itemCount() {
            int count = 0;
            for (ItemStack item : storage) {
                if (item != null && !item.getType().isAir()) {
                    count++;
                }
            }
            for (ItemStack item : armor) {
                if (item != null && !item.getType().isAir()) {
                    count++;
                }
            }
            if (offhand != null && !offhand.getType().isAir()) {
                count++;
            }
            return count;
        }
    }

    public InventoryStore(ForgeCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "inventories.yml");
        load();
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = config.getConfigurationSection("inventories");
        if (root == null) {
            return;
        }
        for (String key : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            try {
                UUID owner = UUID.fromString(section.getString("owner", ""));
                long savedAt = section.getLong("saved-at", 0);
                ItemStack[] storage = readItems(section, "storage", 36);
                ItemStack[] armor = readItems(section, "armor", 4);
                ItemStack offhand = readSingle(section, "offhand");
                inventories.put(key.toLowerCase(Locale.ROOT),
                        new SavedInventory(key, owner, savedAt, storage, armor, offhand));
            } catch (RuntimeException bad) {
                plugin.getLogger().warning("Skipping bad saved inventory '" + key + "': " + bad.getMessage());
            }
        }
    }

    /** Persist every snapshot. */
    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        for (SavedInventory saved : inventories.values()) {
            String path = "inventories." + saved.name() + ".";
            config.set(path + "owner", saved.owner().toString());
            config.set(path + "saved-at", saved.savedAt());
            writeItems(config, path + "storage", saved.storage());
            writeItems(config, path + "armor", saved.armor());
            if (saved.offhand() != null) {
                config.set(path + "offhand", serialize(saved.offhand()));
            }
        }
        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not save inventories.yml: " + exception.getMessage());
        }
    }

    /** Snapshot a player's current inventory under a name (overwrites). */
    public void put(String name, UUID owner, PlayerInventory inventory) {
        ItemStack[] storage = inventory.getStorageContents();
        ItemStack[] armor = inventory.getArmorContents();
        ItemStack offhand = inventory.getItemInOffHand();
        inventories.put(name.toLowerCase(Locale.ROOT),
                new SavedInventory(name, owner, System.currentTimeMillis(),
                        copy(storage), copy(armor), offhand.isEmpty() ? null : offhand.clone()));
        save();
    }

    public @Nullable SavedInventory get(String name) {
        return inventories.get(name.toLowerCase(Locale.ROOT));
    }

    public boolean remove(String name) {
        boolean removed = inventories.remove(name.toLowerCase(Locale.ROOT)) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public Collection<SavedInventory> all() {
        return List.copyOf(inventories.values());
    }

    /** Apply a snapshot to a live player inventory. */
    public static void apply(PlayerInventory inventory, SavedInventory saved) {
        ItemStack[] storage = new ItemStack[36];
        System.arraycopy(saved.storage(), 0, storage, 0, Math.min(saved.storage().length, 36));
        inventory.setStorageContents(storage);
        ItemStack[] armor = new ItemStack[4];
        System.arraycopy(saved.armor(), 0, armor, 0, Math.min(saved.armor().length, 4));
        inventory.setArmorContents(armor);
        inventory.setItemInOffHand(saved.offhand() == null ? ItemStack.empty() : saved.offhand().clone());
    }

    private static ItemStack[] copy(ItemStack[] items) {
        ItemStack[] copy = new ItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            copy[i] = items[i] == null ? null : items[i].clone();
        }
        return copy;
    }

    private void writeItems(YamlConfiguration config, String path, ItemStack[] items) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item == null || item.isEmpty()) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("slot", i);
            row.put("item", serialize(item));
            rows.add(row);
        }
        config.set(path, rows);
    }

    private static Map<String, Object> serialize(ItemStack item) {
        return item.serialize();
    }

    @SuppressWarnings("unchecked")
    private ItemStack[] readItems(ConfigurationSection section, String key, int size) {
        ItemStack[] items = new ItemStack[size];
        List<Map<?, ?>> rows = section.getMapList(key);
        for (Map<?, ?> row : rows) {
            try {
                int slot = ((Number) row.get("slot")).intValue();
                Object raw = row.get("item");
                if (slot >= 0 && slot < size && raw instanceof Map<?, ?> map) {
                    items[slot] = ItemStack.deserialize((Map<String, Object>) map);
                }
            } catch (RuntimeException bad) {
                // Skip bad rows.
            }
        }
        return items;
    }

    @SuppressWarnings("unchecked")
    private @Nullable ItemStack readSingle(ConfigurationSection section, String key) {
        Object raw = section.get(key);
        if (raw instanceof Map<?, ?> map) {
            try {
                return ItemStack.deserialize((Map<String, Object>) map);
            } catch (RuntimeException bad) {
                return null;
            }
        }
        return null;
    }
}
