package com.forge.core.cmd.systemsb;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.systemsb.rank.RankManager;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Show details of a rank, including which requirements you meet. */
public final class RankinfoCommand extends ForgeCommand {
    public RankinfoCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "rankinfo";
    }

    @Override
    public String description() {
        return "Show a rank's requirements and rewards.";
    }

    @Override
    public String usage() {
        return "/rankinfo [rank]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        RankManager ranks = SystemsBSetup.ranks();
        RankManager.RankDef def;
        if (args.length == 0) {
            Player player = asPlayer(sender);
            def = player == null ? ranks.ladder().get(0) : ranks.rankOf(player.getUniqueId());
        } else {
            def = ranks.byId(args[0]);
            if (def == null) {
                throw new CommandRegistry.CommandFailure("Unknown rank: " + Text.escape(args[0]) + ".");
            }
        }
        Player viewer = asPlayer(sender);
        Text.send(sender, "<gold><bold>Rank: " + def.display());
        Text.send(sender, "<gray>Requirements: <white>" + Text.escape(ranks.requirementSummary(def)));
        if (viewer != null) {
            List<String> missing = ranks.missing(viewer, def);
            if (missing.isEmpty()) {
                Text.send(sender, "<green>You meet all requirements.");
            } else {
                Text.send(sender, "<red>Still needed: <white>" + Text.escape(String.join(", ", missing)));
            }
        }
        if (!def.rewards().isEmpty()) {
            Text.send(sender, "<gray>Reward actions: <white>" + def.rewards().size());
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> ids = new ArrayList<>();
            for (RankManager.RankDef def : SystemsBSetup.ranks().ladder()) {
                ids.add(def.id());
            }
            return Players.filter(ids, args);
        }
        return List.of();
    }
}
