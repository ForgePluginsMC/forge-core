package com.forge.core.cmd.systemsb;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.systemsb.rank.RankManager;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import org.bukkit.command.CommandSender;

/** Show the rank ladder with each rank's requirements. */
public final class RanklistCommand extends ForgeCommand {
    public RanklistCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "ranklist";
    }

    @Override
    public String description() {
        return "Show the rank ladder and requirements.";
    }

    @Override
    public String usage() {
        return "/ranklist";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        RankManager ranks = SystemsBSetup.ranks();
        Text.send(sender, "<gold><bold>Rank ladder:");
        int position = 1;
        for (RankManager.RankDef def : ranks.ladder()) {
            Text.send(sender, "<gray>" + position + ". " + def.display()
                    + " <gray>- " + Text.escape(ranks.requirementSummary(def)));
            position++;
        }
    }
}
