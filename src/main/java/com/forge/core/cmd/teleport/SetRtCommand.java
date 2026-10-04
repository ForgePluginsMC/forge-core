package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Locs;
import com.forge.core.util.Text;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /setrt [radius] — set this world's random-teleport center to your location. */
public final class SetRtCommand extends TeleportCommand {
    public SetRtCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "setrt";
    }

    @Override
    public String description() {
        return "Set this world's random-teleport center and radius.";
    }

    @Override
    public String usage() {
        return "/setrt [radius]";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = requirePlayer(sender);
        World world = player.getWorld();
        String base = "rtp." + world.getName();
        if (args.length > 0) {
            long radius;
            try {
                radius = Long.parseLong(args[0]);
            } catch (NumberFormatException exception) {
                throw fail("Radius must be a whole number of blocks.");
            }
            if (radius < 16) {
                throw fail("Radius must be at least 16 blocks.");
            }
            plugin.getConfig().set(base + ".radius", radius);
        }
        plugin.getConfig().set(base + ".center", Locs.serialize(player.getLocation()));
        plugin.saveConfig();
        long radius = plugin.getConfig().getLong(base + ".radius", 5000);
        Text.ok(sender, "RTP center for <white>" + Text.escape(world.getName())
                + "</white> set (radius <white>" + radius + "</white>).");
    }
}
