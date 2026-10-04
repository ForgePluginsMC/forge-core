package com.forge.core.cmd.systemsa;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * In-game custom crafting recipe creator.
 * Place ingredients in the 3x3 grid, put the result in the result slot,
 * click the save button. Recipes persist and are registered server-wide.
 */
@NullMarked
public final class CustomRecipeGui implements Listener {
    // 3x3 grid slots (centered in a 54-slot inventory)
    private static final int[] GRID = {10, 11, 12, 19, 20, 21, 28, 29, 30};
    private static final int RESULT = 24;
    private static final int SAVE = 49;

    private final ForgeCore plugin;
    private final File file;
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();
    private final Map<String, NamespacedKey> registered = new ConcurrentHashMap<>();

    public CustomRecipeGui(ForgeCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "customrecipes.yml");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        loadAndRegister();
    }

    public void open(Player player) {
        Session session = new Session();
        Inventory inv = Bukkit.createInventory(session, 54, Text.of("<gold>Custom Recipe Creator</gold>"));
        session.inventory = inv;
        // Decorative glass around grid
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta gm = glass.getItemMeta();
        gm.displayName(Text.of("<dark_gray>·</dark_gray>"));
        glass.setItemMeta(gm);
        for (int i = 0; i < 54; i++) {
            boolean isGrid = false;
            for (int g : GRID) {
                if (g == i) {
                    isGrid = true;
                    break;
                }
            }
            if (!isGrid && i != RESULT && i != SAVE) {
                inv.setItem(i, glass);
            }
        }
        // Result placeholder
        ItemStack resultPane = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta rm = resultPane.getItemMeta();
        rm.displayName(Text.of("<aqua>Place result here</aqua>"));
        resultPane.setItemMeta(rm);
        inv.setItem(RESULT, resultPane);
        // Save button
        ItemStack saveBtn = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta sm = saveBtn.getItemMeta();
        sm.displayName(Text.of("<green><bold>Save Recipe</bold></green>"));
        sm.lore(List.of(Text.of("<gray>Click to register this recipe</gray>")));
        saveBtn.setItemMeta(sm);
        inv.setItem(SAVE, saveBtn);

        sessions.put(player.getUniqueId(), session);
        player.openInventory(inv);
    }

    private static final class Session implements InventoryHolder {
        @Nullable Inventory inventory;

        @Override
        public Inventory getInventory() {
            if (inventory == null) {
                throw new IllegalStateException("No inventory");
            }
            return inventory;
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        Session session = sessions.get(player.getUniqueId());
        if (session == null || session.inventory == null
                || !event.getInventory().equals(session.inventory)) {
            return;
        }
        int slot = event.getRawSlot();
        boolean isGrid = false;
        for (int g : GRID) {
            if (g == slot) {
                isGrid = true;
                break;
            }
        }
        if (slot == SAVE) {
            event.setCancelled(true);
            saveRecipe(player, session);
            return;
        }
        if (slot == RESULT) {
            // Allow placing/removing result, but not the placeholder
            ItemStack current = event.getCurrentItem();
            if (current != null && current.getType() == Material.LIGHT_BLUE_STAINED_GLASS_PANE) {
                // Let them place an item (don't cancel), clear placeholder next tick
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    ItemStack now = session.inventory.getItem(RESULT);
                    if (now != null && now.getType() == Material.LIGHT_BLUE_STAINED_GLASS_PANE) {
                        session.inventory.setItem(RESULT, null);
                    }
                });
            }
            return;
        }
        if (!isGrid) {
            event.setCancelled(true);
        }
        // Grid slots: allow normal interaction
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        Session session = sessions.remove(player.getUniqueId());
        if (session == null || session.inventory == null) {
            return;
        }
        // Return grid items to player
        Inventory inv = session.inventory;
        for (int g : GRID) {
            ItemStack item = inv.getItem(g);
            if (item != null && !item.getType().isAir()) {
                player.getInventory().addItem(item).values().forEach(
                        leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
            }
        }
        ItemStack result = inv.getItem(RESULT);
        if (result != null && !result.getType().isAir()
                && result.getType() != Material.LIGHT_BLUE_STAINED_GLASS_PANE) {
            player.getInventory().addItem(result).values().forEach(
                    leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
        }
    }

    private void saveRecipe(Player player, Session session) {
        if (session.inventory == null) {
            return;
        }
        Inventory inv = session.inventory;
        ItemStack result = inv.getItem(RESULT);
        if (result == null || result.getType().isAir()
                || result.getType() == Material.LIGHT_BLUE_STAINED_GLASS_PANE) {
            Text.error(player, "Place a result item first.");
            return;
        }
        // Build shape from grid
        Map<Character, Material> matMap = new HashMap<>();
        String[] shape = new String[3];
        char nextChar = 'a';
        Map<Material, Character> charFor = new HashMap<>();
        boolean empty = true;
        for (int row = 0; row < 3; row++) {
            StringBuilder sb = new StringBuilder();
            for (int col = 0; col < 3; col++) {
                ItemStack item = inv.getItem(GRID[row * 3 + col]);
                if (item == null || item.getType().isAir()) {
                    sb.append(' ');
                } else {
                    empty = false;
                    Material mat = item.getType();
                    Character c = charFor.get(mat);
                    if (c == null) {
                        c = nextChar++;
                        charFor.put(mat, c);
                        matMap.put(c, mat);
                    }
                    sb.append(c);
                }
            }
            shape[row] = sb.toString();
        }
        if (empty) {
            Text.error(player, "Place ingredients in the grid first.");
            return;
        }
        String keyName = "custom_" + UUID.randomUUID().toString().substring(0, 8);
        NamespacedKey key = new NamespacedKey(plugin, keyName);
        ShapedRecipe recipe = new ShapedRecipe(key, result.clone());
        recipe.shape(shape[0], shape[1], shape[2]);
        for (Map.Entry<Character, Material> e : matMap.entrySet()) {
            recipe.setIngredient(e.getKey(), e.getValue());
        }
        if (!Bukkit.addRecipe(recipe)) {
            Text.error(player, "Could not register recipe (duplicate?).");
            return;
        }
        registered.put(keyName, key);
        persistRecipe(keyName, shape, matMap, result);
        Text.ok(player, "Recipe <green>saved</green> and registered.");
        player.closeInventory();
    }

    private void persistRecipe(String keyName, String[] shape,
                               Map<Character, Material> matMap, ItemStack result) {
        YamlConfiguration config = file.exists()
                ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();
        config.set(keyName + ".shape", List.of(shape));
        for (Map.Entry<Character, Material> e : matMap.entrySet()) {
            config.set(keyName + ".ingredients." + e.getKey(), e.getValue().name());
        }
        config.set(keyName + ".result", result);
        try {
            config.save(file);
        } catch (java.io.IOException e) {
            plugin.getLogger().warning("Could not save customrecipes.yml: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadAndRegister() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String keyName : config.getKeys(false)) {
            try {
                List<String> shape = (List<String>) config.getStringList(keyName + ".shape");
                if (shape.size() != 3) {
                    continue;
                }
                ItemStack result = config.getItemStack(keyName + ".result");
                if (result == null) {
                    continue;
                }
                NamespacedKey key = new NamespacedKey(plugin, keyName);
                ShapedRecipe recipe = new ShapedRecipe(key, result);
                recipe.shape(shape.get(0), shape.get(1), shape.get(2));
                var ingSec = config.getConfigurationSection(keyName + ".ingredients");
                if (ingSec != null) {
                    for (String ch : ingSec.getKeys(false)) {
                        Material mat = Material.valueOf(ingSec.getString(ch, "AIR"));
                        recipe.setIngredient(ch.charAt(0), mat);
                    }
                }
                if (Bukkit.addRecipe(recipe)) {
                    registered.put(keyName, key);
                }
            } catch (RuntimeException e) {
                plugin.getLogger().warning("Skipping bad custom recipe: " + keyName);
            }
        }
        if (!registered.isEmpty()) {
            plugin.getLogger().info("Loaded " + registered.size() + " custom recipes.");
        }
    }

    /** Remove a registered recipe (for future use). */
    public boolean removeRecipe(String keyName) {
        NamespacedKey key = registered.remove(keyName);
        if (key == null) {
            return false;
        }
        Bukkit.removeRecipe(key);
        YamlConfiguration config = file.exists()
                ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();
        config.set(keyName, null);
        try {
            config.save(file);
        } catch (java.io.IOException e) {
            plugin.getLogger().warning("Could not save customrecipes.yml: " + e.getMessage());
        }
        return true;
    }
}
