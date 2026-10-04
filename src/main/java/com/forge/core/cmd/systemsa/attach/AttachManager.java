package com.forge.core.cmd.systemsa.attach;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.Nullable;

/**
 * Attaches commands to the held item via PersistentDataContainer.
 *
 * <p>Modifiers inside the attached command string:
 * <ul>
 *   <li>{@code !left!} — only fires on left-click (default: both clicks)</li>
 *   <li>{@code !right!} — only fires on right-click</li>
 *   <li>{@code !limiteduse:[n]!} — the item breaks after n uses</li>
 *   <li>{@code !cc!} — run as console instead of the player</li>
 * </ul>
 */
public final class AttachManager implements Listener {
    private static final Pattern MODIFIERS = Pattern.compile("!(left|right|cc|limiteduse:\\d+)!");
    private static final Pattern LIMITED_USE = Pattern.compile("!limiteduse:(\\d+)!");

    private static @Nullable AttachManager instance;

    /** Global accessor. */
    public static AttachManager get() {
        if (instance == null) {
            throw new IllegalStateException("AttachManager not initialized");
        }
        return instance;
    }

    private final ForgeCore plugin;
    private final NamespacedKey key;
    private final NamespacedKey usesKey;

    public AttachManager(ForgeCore plugin) {
        instance = this;
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "forgecore-attached");
        this.usesKey = new NamespacedKey(plugin, "forgecore-attached-uses");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /** Append a command string to the held item. */
    public boolean attach(Player player, String command) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        List<String> commands = container.get(key, PersistentDataType.LIST.strings());
        List<String> updated = commands == null ? new ArrayList<>() : new ArrayList<>(commands);
        updated.add(command);
        container.set(key, PersistentDataType.LIST.strings(), updated);
        container.remove(usesKey);
        item.setItemMeta(meta);
        return true;
    }

    /** Remove all attached commands from the held item. */
    public boolean clear(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(key, PersistentDataType.LIST.strings())) {
            return false;
        }
        container.remove(key);
        container.remove(usesKey);
        item.setItemMeta(meta);
        return true;
    }

    /** Attached command strings on the held item, or null. */
    public @Nullable List<String> attached(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().get(key, PersistentDataType.LIST.strings());
    }

    private static int limitedUses(String raw) {
        Matcher matcher = LIMITED_USE.matcher(raw);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!EquipmentSlot.HAND.equals(event.getHand())) {
            return;
        }
        Action action = event.getAction();
        boolean left = action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;
        boolean right = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
        if (!left && !right) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || item.getType().isAir()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        List<String> commands = container.get(key, PersistentDataType.LIST.strings());
        if (commands == null || commands.isEmpty()) {
            return;
        }
        Player player = event.getPlayer();
        boolean fired = false;
        int uses = container.getOrDefault(usesKey, PersistentDataType.INTEGER, 0);
        int limit = 0;
        for (String raw : commands) {
            if (raw.contains("!left!") && !left) {
                continue;
            }
            if (raw.contains("!right!") && !right) {
                continue;
            }
            int commandLimit = limitedUses(raw);
            if (commandLimit > 0) {
                limit = commandLimit;
            }
            String command = MODIFIERS.matcher(raw).replaceAll("").trim()
                    .replace("%player%", player.getName());
            if (command.startsWith("/")) {
                command = command.substring(1);
            }
            if (command.isEmpty()) {
                continue;
            }
            fired = true;
            if (raw.contains("!cc!")) {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
            } else {
                player.performCommand(command);
            }
        }
        if (fired && limit > 0) {
            uses++;
            if (uses >= limit) {
                player.getInventory().setItemInMainHand(null);
                Text.send(player, "Your <white>" + Text.escape(displayName(item))
                        + "</white> broke after <white>" + limit + "</white> uses.");
            } else {
                container.set(usesKey, PersistentDataType.INTEGER, uses);
                item.setItemMeta(meta);
            }
        }
    }

    private static String displayName(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            net.kyori.adventure.text.Component name = meta.displayName();
            if (name != null) {
                return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                        .plainText().serialize(name);
            }
        }
        return item.getType().name().toLowerCase(java.util.Locale.ROOT).replace('_', ' ');
    }
}
