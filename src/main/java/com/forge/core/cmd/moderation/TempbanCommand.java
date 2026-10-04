package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import com.forge.core.util.Time;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Temporarily ban a player (works offline). */
public final class TempbanCommand extends ForgeCommand {
    public TempbanCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "tempban";
    }

    @Override
    public String description() {
        return "Temporarily ban a player.";
    }

    @Override
    public String usage() {
        return "/tempban <player> <duration> [reason...]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            Text.usage(sender, usage());
            return;
        }
        long seconds;
        try {
            seconds = Time.parseSeconds(args[1]);
        } catch (IllegalArgumentException exception) {
            throw new CommandRegistry.CommandFailure("Bad duration: <white>" + Text.escape(args[1]) + "</white> (try 10m, 2h, 1d).");
        }
        UUID uuid;
        String targetName;
        Player online = Players.findQuiet(args[0]);
        if (online != null) {
            uuid = online.getUniqueId();
            targetName = online.getName();
        } else {
            OfflinePlayer offline = Players.offline(args[0]);
            if (offline == null || offline.getName() == null) {
                throw new CommandRegistry.CommandFailure("Player <white>" + Text.escape(args[0]) + "</white> has never joined.");
            }
            uuid = offline.getUniqueId();
            targetName = offline.getName();
        }
        String reason = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "Banned";
        long until = System.currentTimeMillis() + seconds * 1000L;
        plugin.bans().tempBan(uuid, targetName, reason, sender.getName(), until);
        Text.ok(sender, "Temp-banned <white>" + Text.escape(targetName) + "</white> for <white>" + Time.format(seconds) + "</white>.");
        Staff.notify("<red><bold>Tempban:</bold></red> <white>" + Text.escape(targetName)
                + "</white> was temp-banned by <white>" + Text.escape(sender.getName())
                + "</white> for <white>" + Time.format(seconds) + "</white>: " + Text.escape(reason));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        if (args.length == 2) {
            return Players.filter(List.of("10m", "1h", "1d", "7d", "30d"), args);
        }
        return List.of();
    }
}
