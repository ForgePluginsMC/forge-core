package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.data.UserData;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import com.forge.core.util.Time;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Show what is known about a player: UUID, joins, playtime, online state. */
public final class SeenCommand extends ForgeCommand {
    public SeenCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "seen";
    }

    @Override
    public String description() {
        return "Show information about a player.";
    }

    @Override
    public String usage() {
        return "/seen <player>";
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
            uuid = online.getUniqueId();
            name = online.getName();
        } else {
            OfflinePlayer offline = Players.offline(args[0]);
            if (offline == null || offline.getName() == null) {
                throw new CommandRegistry.CommandFailure("Player <white>" + Text.escape(args[0]) + "</white> has never joined.");
            }
            uuid = offline.getUniqueId();
            name = offline.getName();
        }
        boolean isOnline = Bukkit.getPlayer(uuid) != null;
        UserData data = plugin.users().get(uuid);
        long firstSeen = data.getLong("first-seen", 0L);
        long lastSeen = data.getLong("last-seen", 0L);
        Text.send(sender, "<gold><bold>Seen:</bold></gold> <white>" + Text.escape(name));
        Text.send(sender, "<gray>UUID: <white>" + uuid);
        Text.send(sender, "<gray>Status: " + (isOnline ? "<green>online now" : "<red>offline"));
        Text.send(sender, "<gray>First joined: <white>" + (firstSeen == 0 ? "unknown" : Time.formatDate(firstSeen)));
        Text.send(sender, "<gray>Last seen: <white>" + (isOnline ? "now" : lastSeen == 0 ? "unknown" : Time.formatDate(lastSeen)));
        Text.send(sender, "<gray>Total playtime: <white>" + Time.format(data.playtimeSeconds()));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
