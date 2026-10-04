package com.forge.core.cmd.quest;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.quest.Quest;
import com.forge.core.quest.QuestManager;
import com.forge.core.quest.QuestObjective;
import com.forge.core.quest.QuestStage;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

/**
 * /quest — quest system root command.
 *
 * <p>Subcommands: list, info, start, abandon. With no args, opens the quest GUI.
 */
@NullMarked
public final class QuestCommand extends ForgeCommand {
    public QuestCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "quest";
    }

    @Override
    public List<String> aliases() {
        return List.of("quests");
    }

    @Override
    public String description() {
        return "View and manage your quests.";
    }

    @Override
    public String usage() {
        return "/quest [list|info|start|abandon]";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return;
        }
        QuestManager quests = plugin.quests();
        if (args.length == 0) {
            plugin.questGui().open(player);
            return;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "list" -> list(player, quests, args);
            case "info" -> info(player, quests, args);
            case "start" -> start(player, quests, args);
            case "abandon" -> abandon(player, quests, args);
            default -> Text.usage(sender, usage());
        }
    }

    private void list(Player player, QuestManager quests, String[] args) {
        Quest.Type filter = null;
        if (args.length > 1) {
            try {
                filter = Quest.Type.valueOf(args[1].toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                Text.error(player, "Invalid type. Use: normal, daily, weekly.");
                return;
            }
        }
        List<Quest> list = filter == null ? quests.all() : quests.byType(filter);
        if (list.isEmpty()) {
            Text.send(player, "<gray>No quests found.");
            return;
        }
        Text.send(player, "<gold><bold>Quests:</bold></gold>");
        for (Quest quest : list) {
            String status;
            if (quests.isComplete(player, quest.id())) {
                status = "<green>[Done]";
            } else if (quests.isStarted(player, quest.id())) {
                status = "<gold>[Active]";
            } else if (quests.canStart(player, quest)) {
                status = "<yellow>[Available]";
            } else {
                status = "<gray>[Locked]";
            }
            Text.send(player, status + " <white>" + Text.escape(quest.name())
                    + " <gray>(" + Text.escape(quest.id()) + ")");
        }
    }

    private void info(Player player, QuestManager quests, String[] args) {
        if (args.length < 2) {
            Text.usage(player, "/quest info <id>");
            return;
        }
        Quest quest = quests.get(args[1]);
        if (quest == null) {
            Text.error(player, "Quest not found: " + Text.escape(args[1]));
            return;
        }
        Text.send(player, "<gold><bold>" + Text.escape(quest.name()) + "</bold></gold>");
        Text.send(player, "<gray>" + Text.escape(quest.description()));
        Text.send(player, "<gray>Type: <white>" + quest.type().name().toLowerCase());
        if (!quest.prerequisites().isEmpty()) {
            Text.send(player, "<gray>Requires: <white>"
                    + Text.escape(String.join(", ", quest.prerequisites())));
        }
        int stageNum = 1;
        for (QuestStage stage : quest.stages()) {
            Text.send(player, "<yellow>Stage " + stageNum + ": <white>"
                    + Text.escape(stage.name()));
            int current = quests.currentStage(player, quest.id());
            for (QuestObjective objective : stage.objectives()) {
                String prog = "";
                if (current == stageNum - 1 && quests.isStarted(player, quest.id())) {
                    int p = quests.progress(player, quest, current, objective);
                    prog = " <gray>(" + p + "/" + objective.target() + ")";
                }
                Text.send(player, "  <gray>- " + Text.escape(objective.description()) + prog);
            }
            stageNum++;
        }
    }

    private void start(Player player, QuestManager quests, String[] args) {
        if (args.length < 2) {
            Text.usage(player, "/quest start <id>");
            return;
        }
        Quest quest = quests.get(args[1]);
        if (quest == null) {
            Text.error(player, "Quest not found: " + Text.escape(args[1]));
            return;
        }
        if (!quests.canStart(player, quest)) {
            if (quests.isComplete(player, quest.id())) {
                Text.error(player, "You've already completed that quest.");
            } else if (quests.isStarted(player, quest.id())) {
                Text.error(player, "You've already started that quest.");
            } else {
                Text.error(player, "You don't meet the prerequisites for that quest.");
            }
            return;
        }
        quests.start(player, quest);
        Text.ok(player, "Quest started: " + Text.escape(quest.name()));
    }

    private void abandon(Player player, QuestManager quests, String[] args) {
        if (args.length < 2) {
            Text.usage(player, "/quest abandon <id>");
            return;
        }
        Quest quest = quests.get(args[1]);
        if (quest == null) {
            Text.error(player, "Quest not found: " + Text.escape(args[1]));
            return;
        }
        if (!quests.isStarted(player, quest.id())) {
            Text.error(player, "You haven't started that quest.");
            return;
        }
        quests.abandon(player, quest.id());
        Text.ok(player, "Quest abandoned: " + Text.escape(quest.name()));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(List.of("list", "info", "start", "abandon"), args);
        }
        if (args.length == 2
                && (args[0].equalsIgnoreCase("info")
                        || args[0].equalsIgnoreCase("start")
                        || args[0].equalsIgnoreCase("abandon"))) {
            List<String> ids = new ArrayList<>();
            for (Quest quest : plugin.quests().all()) {
                ids.add(quest.id());
            }
            return Players.filter(ids, args);
        }
        return List.of();
    }
}
