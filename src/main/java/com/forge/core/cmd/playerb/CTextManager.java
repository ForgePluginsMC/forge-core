package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jspecify.annotations.Nullable;

/**
 * Named custom texts displayed by /ctext and managed by /editctext.
 * Stored in {@code ctexts.yml}: name -> text, creator, created timestamp.
 */
public final class CTextManager {
    private static @Nullable CTextManager instance;

    private final ForgeCore plugin;
    private final File file;
    private final Map<String, CText> texts = new LinkedHashMap<>();

    /** A single custom text entry. */
    public record CText(String name, String text, String creator, long created) {
    }

    private CTextManager(ForgeCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "ctexts.yml");
        load();
    }

    static void init(ForgeCore plugin) {
        instance = new CTextManager(plugin);
    }

    public static CTextManager get() {
        CTextManager manager = instance;
        if (manager == null) {
            throw new IllegalStateException("CTextManager not initialized");
        }
        return manager;
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = config.getConfigurationSection("ctexts");
        if (root == null) {
            return;
        }
        for (String key : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            texts.put(key.toLowerCase(Locale.ROOT), new CText(
                    key,
                    section.getString("text", ""),
                    section.getString("creator", "?"),
                    section.getLong("created", 0L)));
        }
    }

    private void save() {
        YamlConfiguration config = new YamlConfiguration();
        for (CText text : texts.values()) {
            String base = "ctexts." + text.name();
            config.set(base + ".text", text.text());
            config.set(base + ".creator", text.creator());
            config.set(base + ".created", text.created());
        }
        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not save ctexts.yml: " + exception.getMessage());
        }
    }

    public boolean exists(String name) {
        return texts.containsKey(name.toLowerCase(Locale.ROOT));
    }

    public @Nullable CText get(String name) {
        return texts.get(name.toLowerCase(Locale.ROOT));
    }

    /** Create or overwrite a custom text. */
    public void set(String name, String text, String creator) {
        texts.put(name.toLowerCase(Locale.ROOT),
                new CText(name, text, creator, System.currentTimeMillis()));
        save();
    }

    /** Delete a custom text; true when one existed. */
    public boolean delete(String name) {
        boolean removed = texts.remove(name.toLowerCase(Locale.ROOT)) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    /** Display names of all custom texts. */
    public List<String> names() {
        List<String> names = new ArrayList<>();
        for (CText text : texts.values()) {
            names.add(text.name());
        }
        return names;
    }
}
