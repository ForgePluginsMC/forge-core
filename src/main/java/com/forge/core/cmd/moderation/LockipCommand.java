package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.data.UserData;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.net.InetSocketAddress;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Toggle an IP lock on a player's account (they can only join from the locked IP). */
public final class LockipCommand extends ForgeCommand {
    public LockipCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "lockip";
    }

    @Override
    public String description() {
        return "Toggle locking a player's account to their current IP.";
    }

    @Override
    public String usage() {
        return "/lockip <player>";
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
        UserData data = plugin.users().get(target);
        String locked = data.getString("locked-ip", null);
        if (locked != null) {
            data.setString("locked-ip", null);
            plugin.users().save(target.getUniqueId());
            Text.ok(sender, "IP lock removed for <white>" + Text.escape(target.getName()) + "</white>.");
            return;
        }
        InetSocketAddress address = target.getAddress();
        if (address == null || address.getAddress() == null) {
            throw new CommandRegistry.CommandFailure("Could not determine <white>" + Text.escape(target.getName()) + "</white>'s IP address.");
        }
        String ip = address.getAddress().getHostAddress();
        data.setString("locked-ip", ip);
        plugin.users().save(target.getUniqueId());
        Text.ok(sender, "Locked <white>" + Text.escape(target.getName()) + "</white> to IP <white>" + Text.escape(ip) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
