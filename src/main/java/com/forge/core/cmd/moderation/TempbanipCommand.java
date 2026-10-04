package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import com.forge.core.util.Time;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Temporarily ban an IP address. */
public final class TempbanipCommand extends ForgeCommand {
    public TempbanipCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "tempbanip";
    }

    @Override
    public String description() {
        return "Temporarily ban an IP address.";
    }

    @Override
    public String usage() {
        return "/tempbanip <ip|player> <duration> [reason...]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            Text.usage(sender, usage());
            return;
        }
        String ip = BanipCommand.resolveIp(args[0]);
        if (ip == null) {
            Text.error(sender, "Could not resolve <white>" + Text.escape(args[0]) + "</white> to an IP address.");
            return;
        }
        long seconds;
        try {
            seconds = Time.parseSeconds(args[1]);
        } catch (IllegalArgumentException exception) {
            throw new CommandRegistry.CommandFailure("Bad duration: <white>" + Text.escape(args[1])
                    + "</white> (try 10m, 2h, 1d).");
        }
        String reason = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "Banned";
        long until = System.currentTimeMillis() + seconds * 1000L;
        ModerationState.ipBans().tempBan(ip, reason, sender.getName(), until);
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            var address = online.getAddress();
            if (address != null && ip.equals(address.getAddress().getHostAddress())) {
                online.kick(Text.of("<red>Your IP address was temporarily banned.\n<gray>Reason: <white>"
                        + Text.escape(reason)));
            }
        }
        Text.ok(sender, "Temp-banned IP <white>" + Text.escape(ip) + "</white> for "
                + Time.format(seconds) + ".");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
