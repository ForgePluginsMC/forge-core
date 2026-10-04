package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Permanently ban an IP address. */
public final class BanipCommand extends ForgeCommand {
    public BanipCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "banip";
    }

    @Override
    public String description() {
        return "Permanently ban an IP address or player IP.";
    }

    @Override
    public String usage() {
        return "/banip <ip|player> [reason...]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        String ip = resolveIp(args[0]);
        if (ip == null) {
            Text.error(sender, "Could not resolve <white>" + Text.escape(args[0]) + "</white> to an IP address.");
            return;
        }
        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "Banned";
        ModerationState.ipBans().ban(ip, reason, sender.getName());
        // Kick anyone currently on that IP.
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            var address = online.getAddress();
            if (address != null && ip.equals(address.getAddress().getHostAddress())) {
                online.kick(Text.of("<red>Your IP address was banned.\n<gray>Reason: <white>" + Text.escape(reason)));
            }
        }
        Text.ok(sender, "Banned IP <white>" + Text.escape(ip) + "</white>.");
        Staff.notify("<red><bold>IP ban:</bold></red> <white>" + Text.escape(ip)
                + "</white> was banned by <white>" + Text.escape(sender.getName()) + "</white>.");
    }

    /** Resolve an argument to an IP: raw IP, or the IP of an online player. */
    static String resolveIp(String arg) {
        if (arg.matches("[0-9a-fA-F:.]+")) {
            return arg;
        }
        Player online = Players.findQuiet(arg);
        if (online != null && online.getAddress() != null) {
            return online.getAddress().getAddress().getHostAddress();
        }
        return null;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
