package com.forge.core.cmd.systemsb;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.systemsb.counter.CounterManager;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Show (or reset) your session counters: kills, deaths, joins, blocks. */
public final class CounterCommand extends ForgeCommand {
    public CounterCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "counter";
    }

    @Override
    public String description() {
        return "Show your session counters.";
    }

    @Override
    public String usage() {
        return "/counter [reset]";
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
        CounterManager counter = SystemsBSetup.counter();
        if (args.length > 0 && args[0].equalsIgnoreCase("reset")) {
            if (!sender.hasPermission("forgecore.counter.reset")) {
                throw new CommandRegistry.CommandFailure("You don't have permission to reset counters.");
            }
            counter.reset(player.getUniqueId());
            Text.ok(sender, "Session counters reset.");
            return;
        }
        CounterManager.Counters stats = counter.get(player.getUniqueId());
        Text.send(sender, "<gold><bold>Session counters:");
        Text.send(sender, "<gray>Kills: <white>" + stats.kills);
        Text.send(sender, "<gray>Deaths: <white>" + stats.deaths);
        Text.send(sender, "<gray>Joins: <white>" + stats.joins);
        Text.send(sender, "<gray>Blocks broken: <white>" + stats.broken);
        Text.send(sender, "<gray>Blocks placed: <white>" + stats.placed);
    }
}
