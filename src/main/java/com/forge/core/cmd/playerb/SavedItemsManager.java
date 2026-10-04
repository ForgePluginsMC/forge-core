package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Server-side named item storage, persisted to {@code saveditems.yml}. */
@NullMarked
public final class SavedItemsManager {
    private final ForgeCore plugin;
    private final File file;
    private final Map<String, ItemStack> items = new ConcurrentHashMap<>();

    public SavedItemsManager(ForgeCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "saveditems.yml");
        load();
    }

    public void save(String name, ItemStack item) {
        items.put(name.toLowerCase(Locale.ROOT), item.clone());
        saveFile();
    }

    public @Nullable ItemStack get(String name) {
        ItemStack item = items.get(name.toLowerCase(Locale.ROOT));
        return item == null ? null : item.clone();
    }

    public boolean delete(String name) {
        boolean removed = items.remove(name.toLowerCase(Locale.ROOT)) != null;
        if (removed) {
            saveFile();
        }
        return removed;
    }

    public List<String> names() {
        List<String> names = new ArrayList<>(items.keySet());
        names.sort(String::compareTo);
        return names;
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            try {
                ItemStack item = config.getItemStack(key);
                if (item != null) {
                    items.put(key.toLowerCase(Locale.ROOT), item);
                }
            } catch (RuntimeException e) {
                plugin.getLogger().warning("Skipping bad saved item: " + key);
            }
        }
    }

    private void saveFile() {
        YamlConfiguration config = new YamlConfiguration();
        Map<String, ItemStack> sorted = new LinkedHashMap<>();
        List<String> names = new ArrayList<>(items.keySet());
        names.sort(String::compareTo);
        for (String name : names) {
            sorted.put(name, items.get(name));
        }
        for (Map.Entry<String, ItemStack> entry : sorted.entrySet()) {
            config.set(entry.getKey(), entry.getValue());
        }
        try {
            config.save(file);
        } catch (java.io.IOException e) {
            plugin.getLogger().warning("Could not save saveditems.yml: " + e.getMessage());
        }
    }
}
