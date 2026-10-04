package com.forge.core.guild;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /guildchat — send a message to your guild members. */
public final class GuildchatCommand extends ForgeCommand {
    public GuildchatCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "guildchat";
    }

    @Override
    public List<String> aliases() {
        return List.of();
    }

    @Override
    public String description() {
        return "Send a message to your guild members.";
    }

    @Override
    public String usage() {
        return "/guildchat <message>";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return;
        }
        Guild guild = plugin.guilds().guildOf(player);
        if (guild == null) {
            Text.error(sender, "You are not in a guild.");
            return;
        }
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        String message = String.join(" ", args);
        String formatted = "<dark_green>[<green>" + Text.escape(guild.tag())
                + "</green>]</dark_green> <gray>" + Text.escape(player.getName())
                + ":</gray> <white>" + Text.escape(message) + "</white>";
        for (UUID uuid : guild.members()) {
            Player member = Bukkit.getPlayer(uuid);
            if (member != null && member.isOnline()) {
                Text.send(member, formatted);
            }
        }
    }
}
