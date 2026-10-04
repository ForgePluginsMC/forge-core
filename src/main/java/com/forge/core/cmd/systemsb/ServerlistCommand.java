package com.forge.core.cmd.systemsb;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.systemsb.bungee.BungeeManager;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/** Query the BungeeCord proxy for its server list. */
public final class ServerlistCommand extends ForgeCommand {
    public ServerlistCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "serverlist";
    }

    @Override
    public String description() {
        return "List the servers on the BungeeCord network.";
    }

    @Override
    public String usage() {
        return "/serverlist";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        BungeeManager bungee = SystemsBSetup.bungee();
        @Nullable UUID waiter = sender instanceof Player player ? player.getUniqueId() : null;
        List<String> cached = bungee.cachedServers();
        bungee.requestServerList(waiter);
        if (!cached.isEmpty()) {
            Text.send(sender, "Servers (cached): <white>" + Text.escape(String.join(", ", cached))
                    + "</white> <gray>— fresh list incoming.");
        } else {
            Text.send(sender, "Requesting the server list from the proxy...");
        }
    }
}
