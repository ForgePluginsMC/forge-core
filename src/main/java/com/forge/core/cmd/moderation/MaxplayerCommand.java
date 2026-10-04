package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

/** Set the server's max player count. */
public final class MaxplayerCommand extends ForgeCommand {
    public MaxplayerCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "maxplayer";
    }

    @Override
    public String description() {
        return "Set the maximum player count.";
    }

    @Override
    public String usage() {
        return "/maxplayer <amount>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.send(sender, "Max players: <white>" + Bukkit.getMaxPlayers() + "</white>.");
            Text.usage(sender, usage());
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException exception) {
            throw new CommandRegistry.CommandFailure("Amount must be a number.");
        }
        if (amount < 1) {
            throw new CommandRegistry.CommandFailure("Amount must be at least 1.");
        }
        Bukkit.setMaxPlayers(amount);
        Text.ok(sender, "Max players set to <white>" + amount + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("20", "50", "100");
        }
        return List.of();
    }
}
