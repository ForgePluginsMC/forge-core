package com.forge.core.merge.items;

import com.forge.core.ForgeCore;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Runtime hub for the merged forge-items system. Mirrors the original
 * plugin's surface (config, logger, managers, messaging) so the ported
 * classes work unchanged; data lives under {@code plugins/ForgeCore/items/}
 * and the module config is {@code plugins/ForgeCore/items.yml}.
 */
public final class ItemsModule {
    private final ForgeCore plugin;
    private final Logger log;
    private final File dataFolder;
    private final File configFile;
    private YamlConfiguration config;

    private final ItemRegistry registry;
    private final CooldownManager cooldowns;
    private final ManaManager mana;
    private final ActionExecutor actions;
    private final ItemLevelManager levels;
    private final ItemListener listener;
    private final SetBonusManager setBonuses;
    private final DropManager drops;
    private final ItemBrowserGui browser;
    private final ChatInput chatInput;
    private final ItemEditorGui editor;

    private int itemCount;
    private int setCount;
    private int dropCount;

    public ItemsModule(ForgeCore plugin) {
        this.plugin = plugin;
        this.log = plugin.getLogger();
        this.dataFolder = new File(plugin.getDataFolder(), "items");
        this.configFile = new File(plugin.getDataFolder(), "items.yml");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        this.config = loadConfig();
        this.registry = new ItemRegistry(this);
        this.cooldowns = new CooldownManager();
        this.mana = new ManaManager(this);
        this.actions = new ActionExecutor(this);
        this.levels = new ItemLevelManager(this, actions);
        this.listener = new ItemListener(this, registry, cooldowns, actions);
        this.setBonuses = new SetBonusManager(this, registry, actions, cooldowns);
        this.drops = new DropManager(registry);
        this.browser = new ItemBrowserGui(this);
        this.chatInput = new ChatInput(this);
        this.editor = new ItemEditorGui(this, chatInput);
    }

    /** The owning ForgeCore plugin (for scheduler and listener registration). */
    public ForgeCore plugin() {
        return plugin;
    }

    public Logger getLogger() {
        return log;
    }

    public Server getServer() {
        return plugin.getServer();
    }

    /** The items module config ({@code plugins/ForgeCore/items.yml}). */
    public YamlConfiguration getConfig() {
        return config;
    }

    /** Data folder: {@code plugins/ForgeCore/items/}. */
    public File getDataFolder() {
        return dataFolder;
    }

    public void reloadConfig() {
        this.config = loadConfig();
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            log.warning("Could not save items.yml: " + e.getMessage());
        }
    }

    private YamlConfiguration loadConfig() {
        copyDefault("config.yml", configFile);
        return YamlConfiguration.loadConfiguration(configFile);
    }

    /** Copies a bundled default from {@code merge-items/<path>} when missing. */
    private void copyDefault(String resourcePath, File dest) {
        if (dest.exists()) {
            return;
        }
        try (InputStream in = plugin.getResource("merge-items/" + resourcePath)) {
            if (in == null) {
                log.warning("Missing bundled default: merge-items/" + resourcePath);
                return;
            }
            File parent = dest.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            java.nio.file.Files.copy(in, dest.toPath());
        } catch (IOException e) {
            log.warning("Could not write default " + dest.getName() + ": " + e.getMessage());
        }
    }

    /** (Re)loads items.yml + all item definitions. Returns the item count. */
    public int reloadItems() {
        reloadConfig();
        File dir = new File(dataFolder, "items");
        if (!dir.exists()) {
            dir.mkdirs();
            copyDefault("items/thunder_hammer.yml", new File(dir, "thunder_hammer.yml"));
            copyDefault("items/frost_bow.yml", new File(dir, "frost_bow.yml"));
            copyDefault("items/guardian_chestplate.yml", new File(dir, "guardian_chestplate.yml"));
            copyDefault("items/ember_blade.yml", new File(dir, "ember_blade.yml"));
            copyDefault("items/ember_chestplate.yml", new File(dir, "ember_chestplate.yml"));
        }
        File setsFile = new File(dataFolder, "sets.yml");
        copyDefault("sets.yml", setsFile);
        File dropsFile = new File(dataFolder, "drops.yml");
        copyDefault("drops.yml", dropsFile);
        itemCount = registry.loadAll(dir);
        setCount = registry.loadSets(setsFile);
        List<DropManager.MobDrop> mobDrops = registry.loadDrops(dropsFile);
        drops.setDrops(mobDrops);
        dropCount = mobDrops.size();
        registry.registerRecipes();
        // Validate every action line so typos surface at load, not mid-fight.
        for (String id : registry.ids()) {
            CustomItem item = registry.get(id);
            for (Activator act : item.activators().values()) {
                actions.validate(item.id(), act.name(), act.actions());
            }
            if (item.levels() != null) {
                actions.validate(item.id(), "level-up", item.levels().levelUpActions());
            }
        }
        for (SetBonus bonus : registry.setBonuses().values()) {
            for (SetBonus.Tier tier : bonus.tiers()) {
                actions.validate("set:" + bonus.id(), tier.pieces() + "pc", tier.actions());
            }
        }
        return itemCount;
    }

    public ItemRegistry registry() {
        return registry;
    }

    public CooldownManager cooldowns() {
        return cooldowns;
    }

    public ManaManager mana() {
        return mana;
    }

    public ActionExecutor actions() {
        return actions;
    }

    public ItemLevelManager levels() {
        return levels;
    }

    public ItemListener listener() {
        return listener;
    }

    public SetBonusManager setBonuses() {
        return setBonuses;
    }

    public DropManager drops() {
        return drops;
    }

    public ItemBrowserGui browser() {
        return browser;
    }

    public ChatInput chatInput() {
        return chatInput;
    }

    public ItemEditorGui editor() {
        return editor;
    }

    public int itemCount() {
        return itemCount;
    }

    public int setCount() {
        return setCount;
    }

    public int dropCount() {
        return dropCount;
    }

    /** Plain-text version of an item's display name (for messages). */
    public String plainName(CustomItem item) {
        if (item.name() == null) {
            return item.id();
        }
        return PlainTextComponentSerializer.plainText().serialize(item.name());
    }

    /** A configured message with prefix and <placeholder> replacement, with a fallback. */
    public Component prefixedOr(String key, String fallback, String... pairs) {
        String prefix = getConfig().getString("settings.message-prefix", "");
        String raw = getConfig().getString(key, fallback);
        return TextUtil.parse(prefix + replacePairs(raw, pairs));
    }

    /** A configured message with prefix and <placeholder> replacement. */
    public Component prefixed(String key, String... pairs) {
        String prefix = getConfig().getString("settings.message-prefix", "");
        String raw = getConfig().getString(key, key);
        return TextUtil.parse(prefix + replacePairs(raw, pairs));
    }

    private static String replacePairs(String raw, String... pairs) {
        String resolved = raw;
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            resolved = resolved.replace("<" + pairs[i] + ">", pairs[i + 1]);
        }
        return resolved;
    }
}
