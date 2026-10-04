package com.forge.core.merge.stack;

import com.forge.core.ForgeCore;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

/**
 * Wires the stacking merge: owns keys/settings/managers, registers the
 * listener, runs the merge-scan and name-visibility tasks, and manages
 * {@code plugins/ForgeCore/stack.yml}.
 *
 * <p>Idempotent: {@link #init(ForgeCore)} runs once; the parent calls it at
 * the top of {@code StackPack.commands()}.
 */
public final class StackSetup {

    private static boolean initialized;
    private static ForgeCore plugin;
    private static StackKeys keys;
    private static StackSettings settings;
    private static StackManager stacks;
    private static SpawnerManager spawners;
    private static BukkitTask scanTask;
    private static BukkitTask nameTask;

    private StackSetup() {
    }

    public static void init(ForgeCore plugin) {
        if (initialized) {
            return;
        }
        initialized = true;
        StackSetup.plugin = plugin;
        keys = new StackKeys(plugin);
        settings = loadSettings();
        spawners = new SpawnerManager(plugin, keys);
        stacks = new StackManager(plugin, keys, spawners);
        plugin.getServer().getPluginManager().registerEvents(new StackListener(), plugin);
        startTasks();
        // Re-hologram stacked spawners across already-loaded chunks (restarts).
        plugin.getServer().getScheduler().runTask(plugin, spawners::restoreHolograms);
    }

    public static StackKeys keys() {
        return keys;
    }

    public static StackSettings settings() {
        return settings;
    }

    public static StackManager stacks() {
        return stacks;
    }

    public static SpawnerManager spawners() {
        return spawners;
    }

    /** Re-reads stack.yml and restarts tasks (intervals may have changed). */
    public static void reload() {
        settings = loadSettings();
        stopTasks();
        startTasks();
    }

    /** Flips the master stacking switch and persists it to stack.yml. */
    public static boolean toggle() {
        boolean next = !settings.enabled();
        File file = stackFile();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("enabled", next);
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("[ForgeCore] Could not save stack.yml: " + e.getMessage());
        }
        settings = new StackSettings(config, plugin.getLogger());
        return next;
    }

    private static void startTasks() {
        long interval = settings.scanIntervalTicks();
        scanTask = plugin.getServer().getScheduler().runTaskTimer(plugin,
                () -> {
                    if (settings.enabled()) {
                        stacks.scan();
                    }
                }, interval, interval);
        long nameInterval = settings.nameVisibleIntervalTicks();
        nameTask = plugin.getServer().getScheduler().runTaskTimer(plugin,
                () -> {
                    if (settings.enabled()) {
                        stacks.updateNameVisibility();
                    }
                }, nameInterval, nameInterval);
    }

    private static void stopTasks() {
        if (scanTask != null) {
            scanTask.cancel();
            scanTask = null;
        }
        if (nameTask != null) {
            nameTask.cancel();
            nameTask = null;
        }
    }

    private static File stackFile() {
        return new File(plugin.getDataFolder(), "stack.yml");
    }

    private static StackSettings loadSettings() {
        File file = stackFile();
        if (!file.exists()) {
            try {
                Files.writeString(file.toPath(), DEFAULTS, StandardCharsets.UTF_8);
            } catch (IOException e) {
                plugin.getLogger().warning("[ForgeCore] Could not write default stack.yml: " + e.getMessage());
            }
        }
        return new StackSettings(YamlConfiguration.loadConfiguration(file), plugin.getLogger());
    }

    private static final String DEFAULTS = """
            # ------------------------------------------------------------------
            # ForgeCore stacking — configuration (merged from forge-stack)
            # All user-facing text supports MiniMessage. Placeholders:
            #   entity-name-format: <count> <type>
            #   spawner hologram/item formats: <count> <type>
            # ------------------------------------------------------------------

            # Master switch for all stacking behavior.
            enabled: true

            # Radius (blocks) in which same-type mobs merge into a stack.
            merge-radius: 5.0

            # Default maximum mobs per stack.
            max-stack-size: 100

            # Per-entity-type overrides (entity type names, e.g. ZOMBIE, COW).
            per-type-max:
              ZOMBIE: 50

            # Entity types that never stack.
            no-stack-types:
              - VILLAGER
              - WANDERING_TRADER
              - ENDER_DRAGON
              - WITHER
              - ARMOR_STAND
              - PLAYER

            # Spawn reasons that never merge on spawn (the periodic scan still merges them
            # unless spawner-only-mode is on).
            skip-spawn-reasons:
              - CUSTOM
              - COMMAND
              - SPAWNER_EGG

            # When true, only spawner-born mobs stack.
            spawner-only-mode: false

            # Display name of a stacked entity.
            entity-name-format: "<gray><count>x <white><type>"

            # Multiply a killed stack's drops and EXP by its size.
            multiply-drops: true
            # Hard cap on the drop/EXP multiplier (anti-dupe safety).
            max-drop-multiplier: 64

            # Radius (blocks) in which ground items merge.
            item-merge-radius: 3.0

            # Maximum items per merged ground stack.
            max-item-stack: 500

            # Per-material overrides for ground item stacks.
            per-material-max: {}

            # Radius (blocks) in which XP orbs merge.
            xp-merge-radius: 4.0

            # How often (ticks) the merge scan runs. 200 = 10 seconds.
            scan-interval-ticks: 200

            # How far away (blocks) a player must be to see a stack's "10x Salmon"
            # nameplate. Names hide beyond this range to cut visual noise.
            name-visible-range: 24.0

            # How often (ticks) nameplate visibility is re-checked. Lower = names
            # appear/disappear more responsively as you walk around.
            name-visible-interval-ticks: 40

            spawner:
              # Extra mobs spawned per spawner cycle for a stacked spawner (beyond the vanilla one).
              per-cycle-cap: 4
              # Floating label above stacked spawners.
              hologram-format: "<gold><count>x <type> spawner"
              # Name of stacked spawner items from /stack givespawner.
              item-name-format: "<gold><count>x <type> Spawner"
              # When true, breaking a stacked spawner only drops its item with Silk Touch.
              break-requires-silk-touch: true
            """;
}
