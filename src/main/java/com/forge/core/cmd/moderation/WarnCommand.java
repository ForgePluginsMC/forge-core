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

/** Warn a player. Warnings are logged with staff name, category, and timestamp. */
public final class WarnCommand extends ForgeCommand {
    public WarnCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "warn";
    }

    @Override
    public String description() {
        return "Warn a player with a reason.";
    }

    @Override
    public String usage() {
        return "/warn <player> [category] <reason...>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
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
        String category = "general";
        String reason;
        if (args.length >= 3) {
            category = args[1].toLowerCase(java.util.Locale.ROOT);
            reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        } else {
            reason = args[1];
        }
        ModerationState.warns().warn(uuid, targetName, reason, category, sender.getName());
        int count = ModerationState.warns().count(uuid);
        Text.ok(sender, "Warned <white>" + Text.escape(targetName) + "</white> (" + count + " active warning"
                + (count == 1 ? "" : "s") + ").");
        if (online != null) {
            Text.send(online, "<red>You have been warned by <white>" + Text.escape(sender.getName())
                    + "</white>:</red> " + Text.escape(reason));
        }
        Staff.notify("<yellow><bold>Warn:</bold></yellow> <white>" + Text.escape(targetName)
                + "</white> warned by <white>" + Text.escape(sender.getName())
                + "</white> [" + Text.escape(category) + "]: " + Text.escape(reason));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
