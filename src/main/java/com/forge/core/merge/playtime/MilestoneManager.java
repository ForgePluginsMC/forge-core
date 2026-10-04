package com.forge.core.merge.playtime;

import com.forge.core.ForgeCore;
import com.forge.core.data.UserData;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 * Loads milestone definitions from {@code plugins/ForgeCore/playtime.yml} and
 * tracks claim-once state per player (keyed by milestone time, so reordering
 * the config never corrupts claims). Playtime itself comes from
 * {@link UserData#playtimeSeconds()} — no second tracker.
 */
public final class MilestoneManager {
    private final ForgeCore plugin;
    private final File file;
    private final List<Milestone> milestones = new ArrayList<>();
    private final MilestoneGui gui;

    public MilestoneManager(ForgeCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "playtime.yml");
        this.gui = new MilestoneGui(this);
        writeDefaults();
        load();
        plugin.getServer().getPluginManager().registerEvents(gui, plugin);
    }

    /** (Re)load milestones from disk. */
    public void reload() {
        load();
    }

    /** Open the milestone GUI for a player. */
    public void openGui(Player player) {
        gui.open(player);
    }

    /** Writes the default playtime.yml when none exists (no jar resource needed). */
    private void writeDefaults() {
        if (file.exists()) {
            return;
        }
        YamlConfiguration yaml = new YamlConfiguration();
        List<Map<String, Object>> defaults = new ArrayList<>();
        defaults.add(defaultMilestone("30m", "give %player% diamond 5", "DIAMOND",
                "<aqua>30 Minutes", List.of("<gray>Thanks for sticking around!", "<gray>Reward: <white>5 diamonds")));
        defaults.add(defaultMilestone("2h", "give %player% netherite_ingot 2", "NETHERITE_INGOT",
                "<light_purple>2 Hours",
                List.of("<gray>A true grinder.", "<gray>Reward: <white>2 netherite ingots")));
        defaults.add(defaultMilestone("1d", "give %player% elytra 1", "ELYTRA",
                "<gold>1 Day",
                List.of("<gray>An entire day of playtime. Legendary.", "<gray>Reward: <white>1 elytra")));
        yaml.set("milestones", defaults);
        try {
            yaml.save(file);
        } catch (java.io.IOException ex) {
            plugin.getLogger().log(Level.WARNING, "Could not write default playtime.yml", ex);
        }
    }

    private static Map<String, Object> defaultMilestone(String time, String reward, String item,
            String name, List<String> lore) {
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        map.put("time", time);
        map.put("rewards", List.of(reward));
        map.put("gui-item", item);
        map.put("gui-name", name);
        map.put("gui-lore", new ArrayList<>(lore));
        return map;
    }

    public List<Milestone> milestones() {
        return List.copyOf(milestones);
    }

    ForgeCore plugin() {
        return plugin;
    }

    private void load() {
        milestones.clear();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (Map<?, ?> entry : yaml.getMapList("milestones")) {
            try {
                milestones.add(Milestone.parse(entry));
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().log(Level.WARNING, "Skipping invalid playtime milestone: " + ex.getMessage());
            }
        }
        milestones.sort((a, b) -> Long.compare(a.seconds(), b.seconds()));
    }

    private Set<Long> claimedTimes(Player player) {
        Set<Long> claimed = new HashSet<>();
        String raw = plugin.users().get(player).getString("playtime-claimed", "");
        for (String part : raw.split(",")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                claimed.add(Long.parseLong(trimmed));
            } catch (NumberFormatException ignored) {
                // Corrupt entry — dropped on next save.
            }
        }
        return claimed;
    }

    /** Has this player claimed the milestone with the given time threshold? */
    public boolean isClaimed(Player player, long seconds) {
        return claimedTimes(player).contains(seconds);
    }

    /** Count of milestones this player has claimed. */
    public int claimedCount(Player player) {
        Set<Long> claimed = claimedTimes(player);
        int count = 0;
        for (Milestone milestone : milestones) {
            if (claimed.contains(milestone.seconds())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Claim a milestone: marks it claimed (persisted) and runs its reward
     * commands from console with {@code %player%} replaced.
     */
    public void claim(Player player, Milestone milestone) {
        Set<Long> claimed = claimedTimes(player);
        claimed.add(milestone.seconds());
        List<Long> sorted = new ArrayList<>(claimed);
        sorted.sort(Long::compare);
        StringBuilder joined = new StringBuilder();
        for (long time : sorted) {
            if (!joined.isEmpty()) {
                joined.append(',');
            }
            joined.append(time);
        }
        UserData data = plugin.users().get(player);
        data.setString("playtime-claimed", joined.toString());
        plugin.users().save(player.getUniqueId());
        for (String command : milestone.rewards()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
        }
    }
}
