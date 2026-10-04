package com.forge.core.guild;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /unclaim — release your guild's claim on the chunk you are standing in. */
public final class UnclaimCommand extends ForgeCommand {
    public UnclaimCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "unclaim";
    }

    @Override
    public String description() {
        return "Release your guild's claim on this chunk.";
    }

    @Override
    public String usage() {
        return "/unclaim";
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
        GuildManager guilds = plugin.guilds();
        Guild guild = guilds.guildOf(player);
        if (guild == null) {
            Text.error(sender, "You are not in a guild.");
            return;
        }
        if (!guild.isOfficer(player.getUniqueId())) {
            Text.error(sender, "Only guild officers can unclaim land.");
            return;
        }
        Chunk chunk = player.getLocation().getChunk();
        Guild owner = guilds.claimOwnerGuild(chunk);
        if (owner == null) {
            Text.error(sender, "This chunk is wilderness — nothing to unclaim.");
            return;
        }
        if (!owner.name().equalsIgnoreCase(guild.name())) {
            Text.error(sender, "This chunk belongs to <white>" + Text.escape(owner.name()) + "</white>.");
            return;
        }
        guilds.unclaim(chunk);
        guilds.save();
        Text.ok(sender, "Claim released.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
