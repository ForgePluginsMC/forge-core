package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import com.forge.core.util.Time;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Show when a player was last online. */
public final class LastonlineCommand extends ForgeCommand {
    public LastonlineCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "lastonline";
    }

    @Override
    public String description() {
        return "Show when a player was last online.";
    }

    @Override
    public String usage() {
        return "/lastonline <player>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        UUID uuid;
        String name;
        Player online = Players.findQuiet(args[0]);
        if (online != null) {
            Text.send(sender, "<white>" + Text.escape(online.getName()) + "</white> is <green>online now</green>.");
            return;
        }
        OfflinePlayer offline = Players.offline(args[0]);
        if (offline == null || offline.getName() == null) {
            throw new CommandRegistry.CommandFailure("Player <white>" + Text.escape(args[0]) + "</white> has never joined.");
        }
        uuid = offline.getUniqueId();
        name = offline.getName();
        long lastSeen = plugin.users().get(uuid).getLong("last-seen", 0L);
        if (lastSeen == 0L) {
            Text.send(sender, "<white>" + Text.escape(name) + "</white> was last online at an <gray>unknown time</gray>.");
        } else {
            Text.send(sender, "<white>" + Text.escape(name) + "</white> was last online <white>"
                    + Time.formatDate(lastSeen) + "</white> <gray>(" + Time.format((System.currentTimeMillis() - lastSeen) / 1000) + " ago)</gray>.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
