package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import com.forge.core.util.Time;
import java.util.List;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /stats — vanilla statistics summary for a player. */
public final class StatsCommand extends ForgeCommand {
    public StatsCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "stats";
    }

    @Override
    public String description() {
        return "Show a player's vanilla statistics summary.";
    }

    @Override
    public String usage() {
        return "/stats [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length > 1) {
            Text.usage(sender, usage());
            return;
        }
        Player target;
        if (args.length == 1) {
            if (!sender.hasPermission("forgecore.stats.others")) {
                Text.error(sender, "You don't have permission to do that.");
                return;
            }
            target = Players.find(sender, args[0]);
            if (target == null) {
                return;
            }
        } else {
            target = asPlayer(sender);
            if (target == null) {
                Text.error(sender, "Only players can use that command.");
                return;
            }
        }
        String name = plugin.users().get(target).nickOrName(target);
        Text.send(sender, "<white>Statistics for " + Text.escape(name) + ":</white>");
        stat(sender, "Play time", Time.format(target.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20L));
        stat(sender, "Time since death", Time.format(target.getStatistic(Statistic.TIME_SINCE_DEATH) / 20L));
        stat(sender, "Deaths", String.valueOf(target.getStatistic(Statistic.DEATHS)));
        stat(sender, "Jumps", String.valueOf(target.getStatistic(Statistic.JUMP)));
        stat(sender, "Mob kills", String.valueOf(target.getStatistic(Statistic.MOB_KILLS)));
        stat(sender, "Player kills", String.valueOf(target.getStatistic(Statistic.PLAYER_KILLS)));
        stat(sender, "Damage dealt", String.format("%.1f", target.getStatistic(Statistic.DAMAGE_DEALT) / 10.0));
        stat(sender, "Damage taken", String.format("%.1f", target.getStatistic(Statistic.DAMAGE_TAKEN) / 10.0));
        stat(sender, "Distance walked", String.format("%.1f km",
                target.getStatistic(Statistic.WALK_ONE_CM) / 100000.0));
        stat(sender, "Distance flown", String.format("%.1f km",
                target.getStatistic(Statistic.FLY_ONE_CM) / 100000.0));
        stat(sender, "Items crafted", String.valueOf(target.getStatistic(Statistic.CRAFT_ITEM)));
    }

    private static void stat(CommandSender sender, String label, String value) {
        Text.send(sender, "<gray>" + label + ":</gray> <white>" + Text.escape(value) + "</white>");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission("forgecore.stats.others")) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
