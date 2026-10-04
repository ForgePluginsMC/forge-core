package com.forge.core.cmd.quest;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.quest.Quest;
import com.forge.core.quest.QuestManager;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

/** /dailies — list and manage daily quests. */
@NullMarked
public final class DailiesCommand extends ForgeCommand {
    public DailiesCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "dailies";
    }

    @Override
    public String description() {
        return "View your daily quests (reset at midnight).";
    }

    @Override
    public String usage() {
        return "/dailies";
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
        List<Quest> dailies = quests.byType(Quest.Type.DAILY);
        if (dailies.isEmpty()) {
            Text.send(player, "<gray>No daily quests available.");
            return;
        }
        Text.send(player, "<gold><bold>Daily Quests</bold></gold> <gray>(reset at midnight)</gray>");
        for (Quest quest : dailies) {
            String status;
            if (quests.isComplete(player, quest.id())) {
                status = "<green>[Done]";
            } else if (quests.isStarted(player, quest.id())) {
                status = "<gold>[Active]";
            } else {
                status = "<yellow>[Available]";
            }
            Text.send(player, status + " <white>" + Text.escape(quest.name())
                    + " <gray>— /quest info " + Text.escape(quest.id()));
        }
    }
}
