package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Jail a player. Their return location is recorded for /unjail. */
public final class JailCommand extends ForgeCommand {
    public JailCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "jail";
    }

    @Override
    public String description() {
        return "Jail a player.";
    }

    @Override
    public String usage() {
        return "/jail <player> [jail]";
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
        String jailName;
        if (args.length > 1) {
            jailName = args[1];
        } else {
            List<String> names = plugin.jails().names();
            if (names.isEmpty()) {
                throw new CommandRegistry.CommandFailure("No jails defined. Create one with <white>/jailedit create <name></white>.");
            }
            jailName = names.get(0);
        }
        Location jail = plugin.jails().get(jailName);
        if (jail == null) {
            throw new CommandRegistry.CommandFailure("Unknown jail: <white>" + Text.escape(jailName) + "</white>.");
        }
        plugin.jails().jail(target, jailName);
        Text.ok(sender, "Jailed <white>" + Text.escape(target.getName()) + "</white> in <white>" + Text.escape(jailName) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        if (args.length == 2) {
            return Players.filter(plugin.jails().names(), args);
        }
        return List.of();
    }
}
