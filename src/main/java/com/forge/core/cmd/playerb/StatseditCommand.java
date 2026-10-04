package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /statsedit — set an untyped vanilla statistic for a player. */
public final class StatseditCommand extends ForgeCommand {
    private static final List<String> UNTYPED = Arrays.stream(Statistic.values())
            .filter(stat -> stat.getType() == Statistic.Type.UNTYPED)
            .map(stat -> stat.name().toLowerCase(Locale.ROOT))
            .sorted()
            .collect(Collectors.toList());

    public StatseditCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "statsedit";
    }

    @Override
    public String description() {
        return "Set an untyped vanilla statistic for a player.";
    }

    @Override
    public String usage() {
        return "/statsedit <player> <stat> <value>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length != 3) {
            Text.usage(sender, usage());
            return;
        }
        Player target = Players.find(sender, args[0]);
        if (target == null) {
            return;
        }
        Statistic stat;
        try {
            stat = Statistic.valueOf(args[1].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException bad) {
            Text.error(sender, "Unknown statistic <white>" + Text.escape(args[1]) + "</white>.");
            return;
        }
        if (stat.getType() != Statistic.Type.UNTYPED) {
            Text.error(sender, "Only untyped statistics can be set directly.");
            return;
        }
        int value;
        try {
            value = Integer.parseInt(args[2]);
        } catch (NumberFormatException bad) {
            Text.error(sender, "<white>" + Text.escape(args[2]) + "</white> is not a number.");
            return;
        }
        if (value < 0) {
            Text.error(sender, "Value cannot be negative.");
            return;
        }
        target.setStatistic(stat, value);
        Text.ok(sender, "Set <white>" + Text.escape(stat.name().toLowerCase(Locale.ROOT)) + "</white> to <white>"
                + value + "</white> for <white>" + Text.escape(target.getName()) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        if (args.length == 2) {
            return Players.filter(UNTYPED, args);
        }
        return List.of();
    }
}
