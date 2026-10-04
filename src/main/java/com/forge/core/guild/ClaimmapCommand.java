package com.forge.core.guild;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * /claimmap — ASCII territory map of nearby chunks.
 *
 * <p>Legend: <green>#</green> your guild, <red>#</red> other guilds,
 * <gray>-</gray> wilderness, <yellow>X</yellow> your position.
 */
public final class ClaimmapCommand extends ForgeCommand {
    private static final int RADIUS = 4;

    public ClaimmapCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "claimmap";
    }

    @Override
    public List<String> aliases() {
        return List.of("cmap");
    }

    @Override
    public String description() {
        return "Show an ASCII map of nearby territory claims.";
    }

    @Override
    public String usage() {
        return "/claimmap";
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
        Guild own = guilds.guildOf(player);
        String ownKey = own == null ? "" : own.name().toLowerCase(Locale.ROOT);
        Chunk center = player.getLocation().getChunk();
        UUID worldId = center.getWorld().getUID();

        Text.send(sender, "<gold><bold>Territory map</bold></gold> <gray>(north up)</gray>");
        for (int dz = -RADIUS; dz <= RADIUS; dz++) {
            StringBuilder row = new StringBuilder();
            for (int dx = -RADIUS; dx <= RADIUS; dx++) {
                if (dx == 0 && dz == 0) {
                    row.append("<yellow>X</yellow>");
                    continue;
                }
                String owner = guilds.claimOwner(
                        new GuildManager.ClaimKey(worldId, center.getX() + dx, center.getZ() + dz));
                row.append(cellFor(owner, ownKey));
            }
            Text.send(sender, row.toString());
        }
        Text.send(sender, "<green>#</green> yours  <red>#</red> claimed  <gray>-</gray> wild  <yellow>X</yellow> you");
    }

    private static String cellFor(@Nullable String owner, String ownKey) {
        if (owner == null) {
            return "<gray>-</gray>";
        }
        if (owner.equals(ownKey)) {
            return "<green>#</green>";
        }
        return "<red>#</red>";
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
