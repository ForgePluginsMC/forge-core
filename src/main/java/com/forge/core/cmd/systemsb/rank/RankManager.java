package com.forge.core.cmd.systemsb.rank;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.systemsb.ActionRunner;
import com.forge.core.data.UserData;
import com.forge.core.util.Text;
import com.forge.core.util.Time;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * Ordered rank ladder stored in {@code ranks.yml}. Players climb with
 * {@code /rankup} when they meet the next rank's requirements; an optional
 * auto rank-up task (config {@code ranks.auto}, default false) promotes
 * eligible players every 5 minutes.
 */
public final class RankManager {
    /** One rung of the ladder. */
    public record RankDef(
            String id,
            String display,
            long playtimeSeconds,
            double money,
            long kills,
            List<String> rewards) {
    }

    private final ForgeCore plugin;
    private final File file;
    private final List<RankDef> ladder = new ArrayList<>();

    public RankManager(ForgeCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "ranks.yml");
        load();
        plugin.getServer().getScheduler().runTaskTimer(plugin, task -> autoRankUp(), 6000L, 6000L);
    }

    /** Ordered ladder, lowest rank first. Never empty. */
    public List<RankDef> ladder() {
        return List.copyOf(ladder);
    }

    public @Nullable RankDef byId(String id) {
        for (RankDef def : ladder) {
            if (def.id().equalsIgnoreCase(id)) {
                return def;
            }
        }
        return null;
    }

    /** A player's current rank; defaults to the first rung. */
    public RankDef rankOf(UUID uuid) {
        RankDef def = byId(plugin.users().get(uuid).getString("rank", ""));
        return def == null ? ladder.get(0) : def;
    }

    public @Nullable RankDef nextOf(Player player) {
        int index = ladder.indexOf(rankOf(player.getUniqueId()));
        return index + 1 < ladder.size() ? ladder.get(index + 1) : null;
    }

    public @Nullable RankDef prevOf(Player player) {
        int index = ladder.indexOf(rankOf(player.getUniqueId()));
        return index - 1 >= 0 ? ladder.get(index - 1) : null;
    }

    /** True when the player meets every requirement of the rank. */
    public boolean meets(Player player, RankDef def) {
        return missing(player, def).isEmpty();
    }

    /** Human-readable unmet requirements; empty when all are met. */
    public List<String> missing(Player player, RankDef def) {
        List<String> missing = new ArrayList<>();
        UserData data = plugin.users().get(player.getUniqueId());
        if (data.playtimeSeconds() < def.playtimeSeconds()) {
            missing.add(Time.format(def.playtimeSeconds()) + " playtime");
        }
        if (def.money() > 0 && !plugin.economy().has(player.getUniqueId(), def.money())) {
            missing.add(plugin.economy().format(def.money()));
        }
        if (data.getLong("stats.kills", 0) < def.kills()) {
            missing.add(def.kills() + " kills");
        }
        return missing;
    }

    /** Compact requirement summary, e.g. {@code "1h playtime, $500.00, 10 kills"}. */
    public String requirementSummary(RankDef def) {
        List<String> parts = new ArrayList<>();
        if (def.playtimeSeconds() > 0) {
            parts.add(Time.format(def.playtimeSeconds()) + " playtime");
        }
        if (def.money() > 0) {
            parts.add(plugin.economy().format(def.money()));
        }
        if (def.kills() > 0) {
            parts.add(def.kills() + " kills");
        }
        return parts.isEmpty() ? "no requirements" : String.join(", ", parts);
    }

    /** Promote a player, charging the money cost and running reward actions. */
    public void promote(Player player, RankDef next) {
        UUID uuid = player.getUniqueId();
        if (next.money() > 0) {
            plugin.economy().take(uuid, next.money());
        }
        UserData data = plugin.users().get(uuid);
        data.setString("rank", next.id());
        plugin.users().save(uuid);
        ActionRunner.run(plugin, player, next.rewards());
    }

    /** Demote a player one rung (no cost, no rewards). */
    public void demote(Player player, RankDef prev) {
        UserData data = plugin.users().get(player.getUniqueId());
        data.setString("rank", prev.id());
        plugin.users().save(player.getUniqueId());
    }

    /** Admin override, works for offline players. */
    public void setRank(OfflinePlayer target, RankDef def) {
        UserData data = plugin.users().get(target.getUniqueId());
        data.setString("rank", def.id());
        plugin.users().save(target.getUniqueId());
    }

    private void autoRankUp() {
        if (!plugin.getConfig().getBoolean("ranks.auto", false)) {
            return;
        }
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            RankDef next = nextOf(player);
            if (next != null && meets(player, next)) {
                promote(player, next);
                Text.ok(player, "You ranked up to " + next.display() + "!");
            }
        }
    }

    private void load() {
        if (!file.exists()) {
            saveDefaults();
        }
        ladder.clear();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("ranks");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                ConfigurationSection rank = section.getConfigurationSection(id);
                if (rank == null) {
                    continue;
                }
                ladder.add(new RankDef(
                        id.toLowerCase(Locale.ROOT),
                        rank.getString("display", id),
                        rank.getLong("playtime-seconds", 0),
                        rank.getDouble("money", 0.0),
                        rank.getLong("kills", 0),
                        List.copyOf(rank.getStringList("rewards"))));
            }
        }
        if (ladder.isEmpty()) {
            ladder.add(new RankDef("default", "<gray>Default", 0, 0, 0, List.of()));
        }
    }

    private void saveDefaults() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("ranks.novice.display", "<gray>Novice");
        config.set("ranks.novice.playtime-seconds", 0);
        config.set("ranks.novice.money", 0.0);
        config.set("ranks.novice.kills", 0);
        config.set("ranks.novice.rewards", List.of());
        config.set("ranks.member.display", "<green>Member");
        config.set("ranks.member.playtime-seconds", 3600);
        config.set("ranks.member.money", 500.0);
        config.set("ranks.member.kills", 10);
        config.set("ranks.member.rewards", List.of(
                "money:250.0",
                "broadcast:<gold>%player_name% <green>ranked up to <white>Member<green>!"));
        config.set("ranks.veteran.display", "<gold>Veteran");
        config.set("ranks.veteran.playtime-seconds", 36000);
        config.set("ranks.veteran.money", 5000.0);
        config.set("ranks.veteran.kills", 100);
        config.set("ranks.veteran.rewards", List.of(
                "money:2500.0",
                "broadcast:<gold>%player_name% <yellow>ranked up to <white>Veteran<yellow>!"));
        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not write default ranks.yml: " + exception.getMessage());
        }
    }
}
