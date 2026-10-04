package com.forge.core.guild;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /claimlist — list your guild's claimed chunks. */
public final class ClaimlistCommand extends ForgeCommand {
    public ClaimlistCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "claimlist";
    }

    @Override
    public List<String> aliases() {
        return List.of("claims");
    }

    @Override
    public String description() {
        return "List your guild's claimed chunks.";
    }

    @Override
    public String usage() {
        return "/claimlist";
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
        List<GuildManager.ClaimKey> claims = guilds.claimsOf(guild);
        if (claims.isEmpty()) {
            Text.send(sender, "Your guild owns no land. Use <white>/claim</white> to claim the chunk you're in.");
            return;
        }
        Text.send(sender, "<gold><bold>" + Text.escape(guild.name())
                + "</bold></gold> claims <white>" + claims.size() + "</white>/<white>"
                + guilds.maxClaims() + "</white>:");
        for (GuildManager.ClaimKey key : claims) {
            World world = Bukkit.getWorld(key.world());
            String worldName = world == null ? "?" : world.getName();
            Text.send(sender, " <gray>-</gray> " + Text.escape(worldName)
                    + " chunk <white>" + key.x() + ", " + key.z() + "</white>");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
