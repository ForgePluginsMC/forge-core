package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;

/** /removewarp <name> — delete a server warp. */
public final class RemoveWarpCommand extends TeleportCommand {
    public RemoveWarpCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "removewarp";
    }

    @Override
    public List<String> aliases() {
        return List.of("delwarp");
    }

    @Override
    public String description() {
        return "Delete a server warp.";
    }

    @Override
    public String usage() {
        return "/removewarp <name>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        if (plugin.warps().remove(args[0])) {
            Text.ok(sender, "Warp <white>" + Text.escape(args[0]) + "</white> removed.");
        } else {
            throw fail("Warp <white>" + Text.escape(args[0]) + "</white> does not exist.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(plugin.warps().names(), args);
        }
        return List.of();
    }
}
