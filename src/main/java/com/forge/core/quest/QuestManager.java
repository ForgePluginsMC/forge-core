package com.forge.core.quest;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Loads quest definitions from {@code quests.yml}, tracks per-player progress,
 * and checks objectives on a timer.
 *
 * <p>Progress model: when a player starts a quest stage, the current value of
 * each objective's statistic is snapshotted. Progress is
 * {@code current - start}; the objective completes when progress reaches the
 * target. Snapshots persist in the player's {@code UserData} under
 * {@code quests.<id>}.
 */
@NullMarked
public final class QuestManager {
    private final ForgeCore plugin;
    private final Map<String, Quest> quests = new HashMap<>();

    public QuestManager(ForgeCore plugin) {
        this.plugin = plugin;
        load();
        // Check objectives every 30 seconds for online players
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::checkAll, 600L, 600L);
    }

    /** All loaded quests. */
    public List<Quest> all() {
        return List.copyOf(quests.values());
    }

    /** Quests of a given type. */
    public List<Quest> byType(Quest.Type type) {
        List<Quest> out = new ArrayList<>();
        for (Quest quest : quests.values()) {
            if (quest.type() == type) {
                out.add(quest);
            }
        }
        return out;
    }

    /** Find a quest by id (case-insensitive). */
    public @Nullable Quest get(String id) {
        return quests.get(id.toLowerCase(Locale.ROOT));
    }

    private void load() {
        File file = new File(plugin.getDataFolder(), "quests.yml");
        if (!file.exists()) {
            plugin.saveResource("quests.yml", false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = config.getConfigurationSection("quests");
        if (root == null) {
            return;
        }
        for (String id : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            try {
                quests.put(id.toLowerCase(Locale.ROOT), parseQuest(id, section));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Skipping invalid quest '" + id + "': " + e.getMessage());
            }
        }
        plugin.getLogger().info("Loaded " + quests.size() + " quests.");
    }

    private Quest parseQuest(String id, ConfigurationSection section) {
        String name = section.getString("name", id);
        String description = section.getString("description", "");
        Quest.Type type;
        try {
            type = Quest.Type.valueOf(section.getString("type", "NORMAL").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("invalid type");
        }
        List<String> prerequisites = section.getStringList("requires");

        List<QuestStage> stages = new ArrayList<>();
        ConfigurationSection stagesSection = section.getConfigurationSection("stages");
        if (stagesSection == null) {
            throw new IllegalArgumentException("no stages defined");
        }
        for (String stageKey : stagesSection.getKeys(false)) {
            ConfigurationSection stageSection = stagesSection.getConfigurationSection(stageKey);
            if (stageSection == null) {
                continue;
            }
            stages.add(parseStage(stageSection));
        }
        if (stages.isEmpty()) {
            throw new IllegalArgumentException("no valid stages");
        }
        return new Quest(id, name, description, type, prerequisites, stages);
    }

    private QuestStage parseStage(ConfigurationSection section) {
        String name = section.getString("name", "Stage");
        List<QuestObjective> objectives = new ArrayList<>();
        for (Map<?, ?> map : section.getMapList("objectives")) {
            String statName = String.valueOf(map.get("stat"));
            Statistic stat;
            try {
                stat = Statistic.valueOf(statName.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("unknown statistic: " + statName);
            }
            if (stat.getType() != Statistic.Type.UNTYPED) {
                throw new IllegalArgumentException("statistic requires a parameter: " + statName);
            }
            int target = ((Number) map.get("target")).intValue();
            Object descObj = map.get("description");
            String desc = descObj == null ? statName : String.valueOf(descObj);
            objectives.add(new QuestObjective(stat, target, desc));
        }
        if (objectives.isEmpty()) {
            throw new IllegalArgumentException("stage has no objectives");
        }
        QuestReward reward = parseReward(section.getConfigurationSection("reward"));
        return new QuestStage(name, objectives, reward);
    }

    private QuestReward parseReward(@Nullable ConfigurationSection section) {
        if (section == null) {
            return new QuestReward(0, 0, List.of(), List.of());
        }
        double money = section.getDouble("money", 0);
        int xp = section.getInt("xp", 0);
        List<QuestReward.RewardItem> items = new ArrayList<>();
        for (Map<?, ?> map : section.getMapList("items")) {
            String matName = String.valueOf(map.get("material"));
            Material material = Material.matchMaterial(matName);
            if (material == null) {
                throw new IllegalArgumentException("unknown material: " + matName);
            }
            int amount = 1;
            Object amountObj = map.get("amount");
            if (amountObj instanceof Number number) {
                amount = number.intValue();
            }
            Object nameObj = map.get("name");
            String name = nameObj == null ? null : String.valueOf(nameObj);
            items.add(new QuestReward.RewardItem(material, amount, name));
        }
        List<String> commands = section.getStringList("commands");
        return new QuestReward(money, xp, items, commands);
    }

    // --- Progress tracking ---

    private String path(String questId) {
        return "quests." + questId.toLowerCase(Locale.ROOT);
    }

    /** Has the player completed this quest (all stages)? */
    public boolean isComplete(Player player, String questId) {
        return plugin.users().get(player).getBoolean(path(questId) + ".complete", false);
    }

    /** Current stage index (0-based), or -1 if not started. */
    public int currentStage(Player player, String questId) {
        long stage = plugin.users().get(player).getLong(path(questId) + ".stage", -1L);
        return stage < 0 || stage > Integer.MAX_VALUE ? -1 : (int) stage;
    }

    /** Has the player started this quest? */
    public boolean isStarted(Player player, String questId) {
        return currentStage(player, questId) >= 0 && !isComplete(player, questId);
    }

    /** Can the player start this quest (prerequisites met, not already done)? */
    public boolean canStart(Player player, Quest quest) {
        if (isComplete(player, quest.id()) || isStarted(player, quest.id())) {
            return false;
        }
        for (String prereq : quest.prerequisites()) {
            if (!isComplete(player, prereq)) {
                return false;
            }
        }
        // Dailies/weeklies: check reset
        if (quest.type() != Quest.Type.NORMAL && needsReset(player, quest)) {
            reset(player, quest.id());
        }
        return true;
    }

    /** Start a quest: snapshot current stats for stage 0 objectives. */
    public void start(Player player, Quest quest) {
        var data = plugin.users().get(player);
        String base = path(quest.id());
        data.set(base + ".stage", 0);
        data.set(base + ".complete", false);
        snapshotStats(player, quest, 0);
        plugin.users().save(player.getUniqueId());
    }

    /** Abandon a quest (clears progress). */
    public void abandon(Player player, String questId) {
        var data = plugin.users().get(player);
        data.set(path(questId), null);
        plugin.users().save(player.getUniqueId());
    }

    private void snapshotStats(Player player, Quest quest, int stageIndex) {
        var data = plugin.users().get(player);
        String base = path(quest.id()) + ".start";
        QuestStage stage = quest.stages().get(stageIndex);
        for (QuestObjective objective : stage.objectives()) {
            data.set(base + "." + objective.stat().name(), player.getStatistic(objective.stat()));
        }
    }

    /** Progress (0..target) for an objective. */
    public int progress(Player player, Quest quest, int stageIndex, QuestObjective objective) {
        var data = plugin.users().get(player);
        int start = (int) data.getLong(
                path(quest.id()) + ".start." + objective.stat().name(), 0L);
        int current = player.getStatistic(objective.stat());
        return Math.max(0, current - start);
    }

    /** Check all online players' active quests. Runs on a timer. */
    private void checkAll() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            checkPlayer(player);
        }
    }

    private void checkPlayer(Player player) {
        for (Quest quest : quests.values()) {
            int stageIndex = currentStage(player, quest.id());
            if (stageIndex < 0 || stageIndex >= quest.stages().size()) {
                continue;
            }
            if (isComplete(player, quest.id())) {
                continue;
            }
            QuestStage stage = quest.stages().get(stageIndex);
            boolean done = true;
            for (QuestObjective objective : stage.objectives()) {
                if (progress(player, quest, stageIndex, objective) < objective.target()) {
                    done = false;
                    break;
                }
            }
            if (done) {
                completeStage(player, quest, stageIndex);
            }
        }
    }

    private void completeStage(Player player, Quest quest, int stageIndex) {
        QuestStage stage = quest.stages().get(stageIndex);
        giveReward(player, stage.reward());
        var data = plugin.users().get(player);
        String base = path(quest.id());

        if (stageIndex + 1 >= quest.stages().size()) {
            data.set(base + ".complete", true);
            Text.send(player, "<gold><bold>Quest complete:</bold></gold> <white>"
                    + Text.escape(quest.name()) + "</white>");
        } else {
            int next = stageIndex + 1;
            data.set(base + ".stage", next);
            snapshotStats(player, quest, next);
            Text.send(player, "<gold>Stage complete:</gold> <white>"
                    + Text.escape(stage.name()) + "</white> <gray>— next:</gold> <white>"
                    + Text.escape(quest.stages().get(next).name()) + "</white>");
        }
        plugin.users().save(player.getUniqueId());
    }

    /** Give a reward to a player. */
    public void giveReward(Player player, QuestReward reward) {
        if (reward.money() > 0) {
            plugin.economy().add(player.getUniqueId(), reward.money());
            Text.send(player, "<green>+" + plugin.economy().format(reward.money()) + "</green>");
        }
        if (reward.xpLevels() > 0) {
            player.giveExpLevels(reward.xpLevels());
            Text.send(player, "<green>+" + reward.xpLevels() + " XP levels</green>");
        }
        for (QuestReward.RewardItem item : reward.items()) {
            ItemStack stack = new ItemStack(item.material(), item.amount());
            if (item.name() != null) {
                ItemMeta meta = stack.getItemMeta();
                if (meta != null) {
                    meta.displayName(Text.of(item.name()));
                    stack.setItemMeta(meta);
                }
            }
            HashMap<Integer, ItemStack> leftover =
                    player.getInventory().addItem(stack);
            for (ItemStack drop : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
            Text.send(player, "<green>+" + item.amount() + "x "
                    + Text.escape(item.material().name().toLowerCase(Locale.ROOT).replace('_', ' '))
                    + "</green>");
        }
        for (String command : reward.commands()) {
            String parsed = command.replace("%player%", player.getName());
            plugin.getServer().dispatchCommand(
                    plugin.getServer().getConsoleSender(), parsed);
        }
    }

    // --- Daily/weekly resets ---

    private boolean needsReset(Player player, Quest quest) {
        var data = plugin.users().get(player);
        long last = data.getLong(path(quest.id()) + ".last_reset", 0L);
        long now = System.currentTimeMillis();
        java.time.ZoneId zone = java.time.ZoneId.systemDefault();
        java.time.LocalDate lastDate =
                java.time.Instant.ofEpochMilli(last).atZone(zone).toLocalDate();
        java.time.LocalDate nowDate =
                java.time.Instant.ofEpochMilli(now).atZone(zone).toLocalDate();
        if (quest.type() == Quest.Type.DAILY) {
            return !lastDate.equals(nowDate);
        } else {
            // Weekly: reset on Monday
            java.time.LocalDate lastMonday =
                    lastDate.with(java.time.DayOfWeek.MONDAY);
            java.time.LocalDate nowMonday =
                    nowDate.with(java.time.DayOfWeek.MONDAY);
            return !lastMonday.equals(nowMonday);
        }
    }

    private void reset(Player player, String questId) {
        var data = plugin.users().get(player);
        data.set(path(questId), null);
        data.set(path(questId) + ".last_reset", System.currentTimeMillis());
        plugin.users().save(player.getUniqueId());
    }
}
