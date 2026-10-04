package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /warp <name> — teleport to a server warp. */
public final class WarpCommand extends TeleportCommand {
    public WarpCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "warp";
    }

    @Override
    public String description() {
        return "Teleport to a server warp.";
    }

    @Override
    public String usage() {
        return "/warp <name>";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = requirePlayer(sender);
        if (args.length == 0) {
            List<String> warps = plugin.warps().names();
            if (warps.isEmpty()) {
                Text.send(sender, "<gray>No warps exist yet.");
            } else {
                Text.send(sender, "<gray>Warps: <white>" + Text.escape(String.join("<gray>, <white>", warps)));
            }
            return;
        }
        Location warp = plugin.warps().get(args[0]);
        if (warp == null) {
            throw fail("Warp <white>" + Text.escape(args[0]) + "</white> does not exist.");
        }
        teleport(player, warp, "<gray>Teleported to warp <white>" + Text.escape(args[0]) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(plugin.warps().names(), args);
        }
        return List.of();
    }
}
