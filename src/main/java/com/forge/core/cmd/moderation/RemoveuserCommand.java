package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.io.File;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

/** Delete a player's userdata file. They must be offline. */
public final class RemoveuserCommand extends ForgeCommand {
    public RemoveuserCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "removeuser";
    }

    @Override
    public String description() {
        return "Delete a player's userdata (must be offline).";
    }

    @Override
    public String usage() {
        return "/removeuser <player>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        OfflinePlayer offline = Players.offline(args[0]);
        if (offline == null) {
            throw new CommandRegistry.CommandFailure("Player <white>" + Text.escape(args[0]) + "</white> has never joined.");
        }
        UUID uuid = offline.getUniqueId();
        if (Bukkit.getPlayer(uuid) != null) {
            throw new CommandRegistry.CommandFailure("<white>" + Text.escape(args[0]) + "</white> must be offline to remove their data.");
        }
        plugin.users().unload(uuid);
        File file = new File(plugin.getDataFolder(), "userdata" + File.separator + uuid + ".yml");
        if (file.exists() && !file.delete()) {
            throw new CommandRegistry.CommandFailure("Could not delete the userdata file.");
        }
        Text.ok(sender, "Removed userdata for <white>" + Text.escape(args[0]) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
