package com.forge.core.economy;

import com.forge.core.ForgeCore;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

/**
 * Built-in economy: balances plus a configurable worth (sell-price) table.
 * Balances live in {@code economy.yml}; prices in {@code worth.yml}.
 */
public final class EconomyManager {
    private final ForgeCore plugin;
    private final File balancesFile;
    private final File worthFile;
    private final Map<UUID, Double> balances = new ConcurrentHashMap<>();
    private final Map<String, Double> worth = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private boolean dirty;

    public EconomyManager(ForgeCore plugin) {
        this.plugin = plugin;
        this.balancesFile = new File(plugin.getDataFolder(), "economy.yml");
        this.worthFile = new File(plugin.getDataFolder(), "worth.yml");
        load();
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, task -> {
            if (dirty) {
                save();
            }
        }, 6000L, 6000L);
    }

    private void load() {
        if (balancesFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(balancesFile);
            for (Map<?, ?> entry : config.getMapList("balances")) {
                try {
                    UUID uuid = UUID.fromString(String.valueOf(entry.get("uuid")));
                    double amount = ((Number) entry.get("amount")).doubleValue();
                    balances.put(uuid, amount);
                } catch (RuntimeException exception) {
                    // Skip bad rows.
                }
            }
        }
        if (worthFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(worthFile);
            for (Map<?, ?> entry : config.getMapList("worth")) {
                try {
                    worth.put(String.valueOf(entry.get("item")),
                            ((Number) entry.get("price")).doubleValue());
                } catch (RuntimeException exception) {
                    // Skip bad rows.
                }
            }
        }
    }

    /** Persist balances and worth. */
    public void save() {
        dirty = false;
        YamlConfiguration balancesConfig = new YamlConfiguration();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            Map<String, Object> row = new java.util.HashMap<>();
            row.put("uuid", entry.getKey().toString());
            row.put("amount", entry.getValue());
            rows.add(row);
        }
        balancesConfig.set("balances", rows);
        YamlConfiguration worthConfig = new YamlConfiguration();
        List<Map<String, Object>> prices = new ArrayList<>();
        for (Map.Entry<String, Double> entry : worth.entrySet()) {
            Map<String, Object> row = new java.util.HashMap<>();
            row.put("item", entry.getKey());
            row.put("price", entry.getValue());
            prices.add(row);
        }
        worthConfig.set("worth", prices);
        try {
            balancesConfig.save(balancesFile);
            worthConfig.save(worthFile);
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not save economy data: " + exception.getMessage());
        }
    }

    /** Starting balance for players never seen before. */
    public double startingBalance() {
        return plugin.getConfig().getDouble("starting-balance", 0.0);
    }

    public double get(UUID uuid) {
        return balances.getOrDefault(uuid, startingBalance());
    }

    public void set(UUID uuid, double amount) {
        balances.put(uuid, Math.max(0, amount));
        dirty = true;
    }

    public void add(UUID uuid, double amount) {
        set(uuid, get(uuid) + amount);
    }

    /** Take money; returns false when funds are insufficient. */
    public boolean take(UUID uuid, double amount) {
        double current = get(uuid);
        if (current < amount) {
            return false;
        }
        set(uuid, current - amount);
        return true;
    }

    public boolean has(UUID uuid, double amount) {
        return get(uuid) >= amount;
    }

    /** Format an amount with the configured currency symbol. */
    public String format(double amount) {
        String symbol = plugin.getConfig().getString("currency-symbol", "$");
        return symbol + String.format(Locale.ROOT, "%,.2f", amount);
    }

    /** Top balances, highest first. Each entry is {@code [uuid, amount]}. */
    public List<Map.Entry<UUID, Double>> top(int limit) {
        return balances.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .toList();
    }

    /** Sell price for one unit of a material, 0 when not priced. */
    public double worthOf(Material material) {
        return worth.getOrDefault(material.name(), 0.0);
    }

    /** Sell value of a whole stack. */
    public double worthOf(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return 0;
        }
        return worthOf(item.getType()) * item.getAmount();
    }

    public void setWorth(Material material, double price) {
        worth.put(material.name(), Math.max(0, price));
        dirty = true;
    }

    public Map<String, Double> worthTable() {
        return Map.copyOf(worth);
    }

    /**
     * Fill the worth table with sensible defaults for common materials.
     * Only fills entries that are not already set.
     */
    public void generateWorth() {
        Map<String, Double> defaults = new java.util.LinkedHashMap<>();
        defaults.put("COAL", 2.0);
        defaults.put("RAW_IRON", 4.0);
        defaults.put("IRON_INGOT", 8.0);
        defaults.put("RAW_COPPER", 3.0);
        defaults.put("COPPER_INGOT", 5.0);
        defaults.put("RAW_GOLD", 8.0);
        defaults.put("GOLD_INGOT", 15.0);
        defaults.put("DIAMOND", 50.0);
        defaults.put("EMERALD", 40.0);
        defaults.put("LAPIS_LAZULI", 4.0);
        defaults.put("REDSTONE", 2.0);
        defaults.put("QUARTZ", 3.0);
        defaults.put("AMETHYST_SHARD", 6.0);
        defaults.put("NETHERITE_SCRAP", 120.0);
        defaults.put("NETHERITE_INGOT", 500.0);
        defaults.put("COAL_ORE", 3.0);
        defaults.put("IRON_ORE", 6.0);
        defaults.put("COPPER_ORE", 4.0);
        defaults.put("GOLD_ORE", 12.0);
        defaults.put("DIAMOND_ORE", 75.0);
        defaults.put("EMERALD_ORE", 60.0);
        defaults.put("LAPIS_ORE", 6.0);
        defaults.put("REDSTONE_ORE", 3.0);
        defaults.put("NETHER_QUARTZ_ORE", 5.0);
        defaults.put("WHEAT", 1.0);
        defaults.put("CARROT", 1.0);
        defaults.put("POTATO", 1.0);
        defaults.put("BEETROOT", 1.0);
        defaults.put("BREAD", 2.0);
        defaults.put("COOKED_BEEF", 3.0);
        defaults.put("COOKED_PORKCHOP", 3.0);
        defaults.put("COOKED_CHICKEN", 2.0);
        defaults.put("COOKED_MUTTON", 2.0);
        defaults.put("COOKED_RABBIT", 2.0);
        defaults.put("BAKED_POTATO", 2.0);
        defaults.put("APPLE", 2.0);
        defaults.put("GOLDEN_APPLE", 30.0);
        defaults.put("OAK_LOG", 1.0);
        defaults.put("SPRUCE_LOG", 1.0);
        defaults.put("BIRCH_LOG", 1.0);
        defaults.put("JUNGLE_LOG", 1.0);
        defaults.put("ACACIA_LOG", 1.0);
        defaults.put("DARK_OAK_LOG", 1.0);
        defaults.put("MANGROVE_LOG", 1.0);
        defaults.put("CHERRY_LOG", 1.0);
        defaults.put("COBBLESTONE", 0.5);
        defaults.put("STONE", 0.5);
        defaults.put("DIRT", 0.25);
        defaults.put("SAND", 0.5);
        defaults.put("GRAVEL", 0.5);
        defaults.put("GLASS", 1.0);
        defaults.put("STRING", 1.0);
        defaults.put("FEATHER", 1.0);
        defaults.put("LEATHER", 2.0);
        defaults.put("ROTTEN_FLESH", 0.5);
        defaults.put("BONE", 1.0);
        defaults.put("ARROW", 1.0);
        defaults.put("ENDER_PEARL", 10.0);
        defaults.put("BLAZE_ROD", 12.0);
        defaults.put("GHAST_TEAR", 20.0);
        defaults.put("NETHER_WART", 3.0);
        defaults.put("SUGAR_CANE", 1.0);
        defaults.put("CACTUS", 1.0);
        defaults.put("VINE", 0.5);
        defaults.put("LILY_PAD", 1.0);
        defaults.put("KELP", 0.5);
        defaults.put("SEAGRASS", 0.5);
        defaults.put("TURTLE_SCUTE", 15.0);
        defaults.put("PHANTOM_MEMBRANE", 8.0);
        defaults.put("SHULKER_SHELL", 25.0);
        defaults.put("TOTEM_OF_UNDYING", 200.0);
        defaults.put("ELYTRA", 1000.0);
        defaults.put("NETHER_STAR", 500.0);
        defaults.put("DRAGON_EGG", 5000.0);
        defaults.put("BEACON", 800.0);
        defaults.put("CONDUIT", 300.0);
        defaults.put("HEART_OF_THE_SEA", 100.0);
        defaults.put("TRIDENT", 250.0);
        defaults.put("MENDING", 0.0);
        for (Map.Entry<String, Double> entry : defaults.entrySet()) {
            worth.putIfAbsent(entry.getKey(), entry.getValue());
        }
        // Enchanted books are priced by enchantment elsewhere; keep the marker out.
        worth.remove("MENDING");
        dirty = true;
    }
}
