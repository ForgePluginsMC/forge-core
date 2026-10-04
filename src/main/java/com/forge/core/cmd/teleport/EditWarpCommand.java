package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /editwarp <name> — move a warp to your current location. */
public final class EditWarpCommand extends TeleportCommand {
    public EditWarpCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "editwarp";
    }

    @Override
    public String description() {
        return "Move a warp to your current location.";
    }

    @Override
    public String usage() {
        return "/editwarp <name>";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = requirePlayer(sender);
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        if (!plugin.warps().exists(args[0])) {
            throw fail("Warp <white>" + Text.escape(args[0]) + "</white> does not exist.");
        }
        plugin.warps().set(args[0], player.getLocation());
        Text.ok(sender, "Warp <white>" + Text.escape(args[0]) + "</white> moved to your location.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(plugin.warps().names(), args);
        }
        return List.of();
    }
}
