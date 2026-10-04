package com.forge.core.cmd.systemsb;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.systemsb.bungee.BungeeManager;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Move between BungeeCord servers: {@code /server} shows your current
 * server, {@code /server <name>} connects you, {@code /server <name>
 * <player>} sends another player (needs {@code forgecore.server.others}).
 */
public final class ServerCommand extends ForgeCommand {
    public ServerCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "server";
    }

    @Override
    public String description() {
        return "Switch BungeeCord servers.";
    }

    @Override
    public String usage() {
        return "/server [server] [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        BungeeManager bungee = SystemsBSetup.bungee();
        if (args.length == 0) {
            Player player = asPlayer(sender);
            if (player == null) {
                Text.usage(sender, usage());
                return;
            }
            bungee.requestOwnServer(player);
            Text.send(sender, "Requesting your current server...");
            return;
        }
        if (args.length == 1) {
            Player player = asPlayer(sender);
            if (player == null) {
                throw new CommandRegistry.CommandFailure("Specify a player: /server <server> <player>.");
            }
            bungee.connect(player, args[0]);
            Text.ok(sender, "Connecting you to <white>" + Text.escape(args[0]) + "</white>...");
            return;
        }
        if (!sender.hasPermission("forgecore.server.others")) {
            throw new CommandRegistry.CommandFailure("You don't have permission to move other players.");
        }
        Player target = Players.find(sender, args[1]);
        if (target == null) {
            return;
        }
        bungee.connect(target, args[0]);
        Text.ok(sender, "Sending " + Text.escape(target.getName())
                + " to <white>" + Text.escape(args[0]) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(SystemsBSetup.bungee().cachedServers(), args);
        }
        if (args.length == 2) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
