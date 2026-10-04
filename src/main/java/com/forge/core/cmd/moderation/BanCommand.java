package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Permanently ban a player (works offline). */
public final class BanCommand extends ForgeCommand {
    public BanCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "ban";
    }

    @Override
    public String description() {
        return "Permanently ban a player.";
    }

    @Override
    public String usage() {
        return "/ban <player> [reason...]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
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
        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "Banned";
        plugin.bans().ban(uuid, targetName, reason, sender.getName());
        Text.ok(sender, "Banned <white>" + Text.escape(targetName) + "</white>.");
        Staff.notify("<red><bold>Ban:</bold></red> <white>" + Text.escape(targetName)
                + "</white> was banned by <white>" + Text.escape(sender.getName())
                + "</white>: " + Text.escape(reason));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
