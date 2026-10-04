package com.forge.core.guild;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /claim — claim the chunk you are standing in for your guild. */
public final class ClaimCommand extends ForgeCommand {
    public ClaimCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "claim";
    }

    @Override
    public String description() {
        return "Claim the chunk you are standing in for your guild.";
    }

    @Override
    public String usage() {
        return "/claim";
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
            Text.error(sender, "Only guild officers can claim land.");
            return;
        }
        Chunk chunk = player.getLocation().getChunk();
        Guild owner = guilds.claimOwnerGuild(chunk);
        if (owner != null) {
            if (owner.name().equalsIgnoreCase(guild.name())) {
                Text.error(sender, "Your guild already owns this chunk.");
            } else {
                Text.error(sender, "This chunk is claimed by <white>"
                        + Text.escape(owner.name()) + "</white>.");
            }
            return;
        }
        if (guilds.claimCount(guild) >= guilds.maxClaims()) {
            Text.error(sender, "Your guild has reached the claim limit (" + guilds.maxClaims() + ").");
            return;
        }
        double cost = guilds.claimCost();
        if (cost > 0) {
            if (guild.bank() < cost) {
                Text.error(sender, "The guild bank can't afford the claim cost (<white>"
                        + plugin.economy().format(cost) + "</white>).");
                return;
            }
            guild.bank(guild.bank() - cost);
        }
        guilds.claim(guild, chunk);
        guilds.save();
        Text.ok(sender, "Chunk claimed for <white>" + Text.escape(guild.name()) + "</white>"
                + (cost > 0 ? " (" + plugin.economy().format(cost) + " from the guild bank)." : "."));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
