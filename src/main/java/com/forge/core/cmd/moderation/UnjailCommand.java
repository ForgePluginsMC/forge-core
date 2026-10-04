package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Release a jailed player, returning them to their pre-jail location. */
public final class UnjailCommand extends ForgeCommand {
    public UnjailCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "unjail";
    }

    @Override
    public String description() {
        return "Release a player from jail.";
    }

    @Override
    public String usage() {
        return "/unjail <player>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        Player target = Players.find(sender, args[0]);
        if (target == null) {
            return;
        }
        if (!plugin.jails().isJailed(target)) {
            throw new CommandRegistry.CommandFailure("<white>" + Text.escape(target.getName()) + "</white> is not jailed.");
        }
        plugin.jails().unjail(target);
        Text.ok(sender, "Released <white>" + Text.escape(target.getName()) + "</white> from jail.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
