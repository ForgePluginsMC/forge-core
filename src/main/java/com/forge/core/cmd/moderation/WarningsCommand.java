package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** List a player's warnings. */
public final class WarningsCommand extends ForgeCommand {
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    public WarningsCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "warnings";
    }

    @Override
    public List<String> aliases() {
        return List.of("warns");
    }

    @Override
    public String description() {
        return "List warnings for a player.";
    }

    @Override
    public String usage() {
        return "/warnings [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        UUID uuid;
        String targetName;
        if (args.length >= 1) {
            if (!sender.hasPermission("forgecore.warnings.others")) {
                Text.error(sender, "You don't have permission to check others' warnings.");
                return;
            }
            Player online = Players.findQuiet(args[0]);
            if (online != null) {
                uuid = online.getUniqueId();
                targetName = online.getName();
            } else {
                OfflinePlayer offline = Players.offline(args[0]);
                if (offline == null || offline.getName() == null) {
                    Text.error(sender, "Player <white>" + Text.escape(args[0]) + "</white> has never joined.");
                    return;
                }
                uuid = offline.getUniqueId();
                targetName = offline.getName();
            }
        } else {
            Player player = asPlayer(sender);
            if (player == null) {
                Text.error(sender, "Only players can check their own warnings.");
                return;
            }
            uuid = player.getUniqueId();
            targetName = player.getName();
        }
        List<WarnManager.Warning> warnings = ModerationState.warns().get(uuid);
        if (warnings.isEmpty()) {
            Text.ok(sender, "<white>" + Text.escape(targetName) + "</white> has no active warnings.");
            return;
        }
        Text.send(sender, "<yellow>Warnings for <white>" + Text.escape(targetName) + "</white> ("
                + warnings.size() + "):</yellow>");
        int i = 1;
        for (WarnManager.Warning w : warnings) {
            Text.send(sender, "<gray>" + i++ + ".</gray> <white>" + Text.escape(w.reason()) + "</white>"
                    + " <gray>[" + Text.escape(w.category()) + "] by " + Text.escape(w.by())
                    + " on " + FMT.format(Instant.ofEpochMilli(w.at())) + "</gray>");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1 && sender.hasPermission("forgecore.warnings.others")) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
