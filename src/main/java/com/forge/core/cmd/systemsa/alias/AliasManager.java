package com.forge.core.cmd.systemsa.alias;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jspecify.annotations.Nullable;

/**
 * Custom command aliases. {@code /alias args...} rewrites to the mapped
 * command plus args and re-dispatches. Alias chains are capped at depth 5
 * to stop loops. Stored in {@code aliases.yml}.
 */
public final class AliasManager implements Listener {
    private static final int MAX_DEPTH = 5;

    private static @Nullable AliasManager instance;

    /** Global accessor. */
    public static AliasManager get() {
        if (instance == null) {
            throw new IllegalStateException("AliasManager not initialized");
        }
        return instance;
    }

    private final ForgeCore plugin;
    private final File file;
    private final Map<String, String> aliases = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<UUID, Integer> depth = new HashMap<>();

    public AliasManager(ForgeCore plugin) {
        instance = this;
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "aliases.yml");
        load();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (Map<?, ?> entry : config.getMapList("aliases")) {
            Object aliasRaw = entry.get("alias");
            Object commandRaw = entry.get("command");
            if (aliasRaw instanceof String alias && commandRaw instanceof String command
                    && !alias.isBlank() && !command.isBlank()) {
                aliases.put(alias.toLowerCase(Locale.ROOT), command);
            }
        }
    }

    /** Persist aliases.yml. */
    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : aliases.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("alias", entry.getKey());
            map.put("command", entry.getValue());
            list.add(map);
        }
        config.set("aliases", list);
        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not save aliases.yml: " + exception.getMessage());
        }
    }

    /** Normalize an alias label: no leading slash, lowercase. */
    public static String normalize(String alias) {
        String label = alias.startsWith("/") ? alias.substring(1) : alias;
        return label.toLowerCase(Locale.ROOT);
    }

    public boolean create(String alias, String command) {
        String key = normalize(alias);
        if (key.isEmpty() || aliases.containsKey(key)) {
            return false;
        }
        String template = command.startsWith("/") ? command.substring(1) : command;
        if (template.isBlank()) {
            return false;
        }
        aliases.put(key, template);
        save();
        return true;
    }

    public boolean delete(String alias) {
        boolean removed = aliases.remove(normalize(alias)) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public @Nullable String get(String alias) {
        return aliases.get(normalize(alias));
    }

    public List<String> names() {
        return new ArrayList<>(aliases.keySet());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPreprocess(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if (!message.startsWith("/")) {
            return;
        }
        String body = message.substring(1);
        String label;
        String args;
        int space = body.indexOf(' ');
        if (space < 0) {
            label = body;
            args = "";
        } else {
            label = body.substring(0, space);
            args = body.substring(space + 1);
        }
        String mapped = aliases.get(label.toLowerCase(Locale.ROOT));
        if (mapped == null) {
            return;
        }
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        int current = depth.getOrDefault(uuid, 0);
        if (current >= MAX_DEPTH) {
            event.setCancelled(true);
            Text.error(player, "Alias loop detected; stopping.");
            return;
        }
        depth.put(uuid, current + 1);
        try {
            event.setCancelled(true);
            player.performCommand(args.isEmpty() ? mapped : mapped + " " + args);
        } finally {
            depth.put(uuid, current);
        }
    }
}
