package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Show a player's last IP and any online players sharing it. */
public final class CheckaccountCommand extends ForgeCommand {
    public CheckaccountCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "checkaccount";
    }

    @Override
    public String description() {
        return "Show a player's last IP and accounts sharing it.";
    }

    @Override
    public String usage() {
        return "/checkaccount <player>";
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
        String ip = plugin.users().get(uuid).getString("last-ip", null);
        if (ip == null || ip.isEmpty()) {
            throw new CommandRegistry.CommandFailure("No recorded IP for <white>" + Text.escape(name) + "</white>.");
        }
        List<String> shared = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getUniqueId().equals(uuid)) {
                continue;
            }
            String other = plugin.users().get(player).getString("last-ip", "");
            if (ip.equals(other)) {
                shared.add(player.getName());
            }
        }
        Text.send(sender, "<gold><bold>Account:</bold></gold> <white>" + Text.escape(name));
        Text.send(sender, "<gray>Last IP: <white>" + Text.escape(ip));
        if (shared.isEmpty()) {
            Text.send(sender, "<gray>No other online players share this IP.");
        } else {
            Text.send(sender, "<gray>Sharing this IP: <white>" + Text.escape(String.join("<gray>, </gray><white>", shared)));
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
