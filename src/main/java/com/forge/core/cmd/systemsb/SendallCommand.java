package com.forge.core.cmd.systemsb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;

/** Send every online player to another BungeeCord server. */
public final class SendallCommand extends ForgeCommand {
    public SendallCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "sendall";
    }

    @Override
    public String description() {
        return "Send all online players to another server.";
    }

    @Override
    public String usage() {
        return "/sendall <server>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        SystemsBSetup.bungee().sendAll(args[0]);
        Text.ok(sender, "Sending everyone to <white>" + Text.escape(args[0]) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(SystemsBSetup.bungee().cachedServers(), args);
        }
        return List.of();
    }
}
