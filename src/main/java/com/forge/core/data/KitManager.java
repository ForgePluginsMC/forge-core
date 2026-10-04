package com.forge.core.data;

import com.forge.core.ForgeCore;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Kits: named item bundles with cooldowns and optional costs.
 * Stored in {@code kits.yml}; per-player cooldowns live in userdata.
 */
public final class KitManager {
    /** A kit definition. */
    public record Kit(String name, List<ItemStack> items, long cooldownSeconds, double cost) {
    }

    private final ForgeCore plugin;
    private final File file;
    private final Map<String, Kit> kits = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public KitManager(ForgeCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "kits.yml");
        load();
    }

    @SuppressWarnings("unchecked")
    private void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("kits");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            List<ItemStack> items = new ArrayList<>();
            for (Map<?, ?> raw : section.getMapList(key + ".items")) {
                try {
                    items.add(ItemStack.deserialize((Map<String, Object>) raw));
                } catch (RuntimeException exception) {
                    plugin.getLogger().warning("Skipping bad kit item in kit " + key);
                }
            }
            kits.put(key, new Kit(
                    key,
                    List.copyOf(items),
                    section.getLong(key + ".cooldown-seconds", 0),
                    section.getDouble(key + ".cost", 0)));
        }
    }

    /** Persist kits.yml. */
    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        for (Kit kit : kits.values()) {
            String base = "kits." + kit.name();
            List<Map<String, Object>> items = new ArrayList<>();
            for (ItemStack item : kit.items()) {
                items.add(item.serialize());
            }
            config.set(base + ".items", items);
            config.set(base + ".cooldown-seconds", kit.cooldownSeconds());
            config.set(base + ".cost", kit.cost());
        }
        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not save kits.yml: " + exception.getMessage());
        }
    }

    public void create(String name, List<ItemStack> items, long cooldownSeconds, double cost) {
        kits.put(name.toLowerCase(Locale.ROOT), new Kit(name.toLowerCase(Locale.ROOT), List.copyOf(items), cooldownSeconds, cost));
        save();
    }

    public boolean delete(String name) {
        boolean removed = kits.remove(name.toLowerCase(Locale.ROOT)) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public @Nullable Kit get(String name) {
        return kits.get(name.toLowerCase(Locale.ROOT));
    }

    public List<String> names() {
        return new ArrayList<>(kits.keySet());
    }

    /** Seconds remaining on this player's cooldown for the kit, 0 when ready. */
    public long cooldownRemaining(Player player, Kit kit) {
        if (kit.cooldownSeconds() <= 0) {
            return 0;
        }
        long last = plugin.users().get(player).getLong("kit-cooldown." + kit.name(), 0);
        long remaining = kit.cooldownSeconds() - (System.currentTimeMillis() / 1000 - last);
        return Math.max(0, remaining);
    }

    /** Give the kit to a player, dropping overflow at their feet. */
    public void give(Player player, Kit kit) {
        Map<Integer, ItemStack> overflow = new HashMap<>();
        for (ItemStack item : kit.items()) {
            overflow.putAll(player.getInventory().addItem(item.clone()));
        }
        for (ItemStack item : overflow.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
        if (kit.cooldownSeconds() > 0) {
            UserData data = plugin.users().get(player);
            data.setLong("kit-cooldown." + kit.name(), System.currentTimeMillis() / 1000);
            plugin.users().save(player.getUniqueId());
        }
    }
}
