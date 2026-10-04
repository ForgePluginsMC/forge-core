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

/** /weeklies — list and manage weekly quests. */
@NullMarked
public final class WeekliesCommand extends ForgeCommand {
    public WeekliesCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "weeklies";
    }

    @Override
    public String description() {
        return "View your weekly quests (reset Monday).";
    }

    @Override
    public String usage() {
        return "/weeklies";
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
        List<Quest> weeklies = quests.byType(Quest.Type.WEEKLY);
        if (weeklies.isEmpty()) {
            Text.send(player, "<gray>No weekly quests available.");
            return;
        }
        Text.send(player, "<gold><bold>Weekly Quests</bold></gold> <gray>(reset Monday)</gray>");
        for (Quest quest : weeklies) {
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
