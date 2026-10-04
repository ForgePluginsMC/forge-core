package com.forge.core.cmd.permission;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import java.util.UUID;
import org.bukkit.command.CommandSender;

/** Remove a directly-granted permission node from a player. */
public final class PermUnsetCommand extends ForgeCommand {
    public PermUnsetCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "permunset";
    }

    @Override
    public String description() {
        return "Remove a permission node from a player.";
    }

    @Override
    public String usage() {
        return "/permunset <player> <node>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length != 2) {
            Text.usage(sender, usage());
            return;
        }
        UUID uuid = PermUtil.uuidOf(sender, args[0]);
        if (uuid == null) {
            return;
        }
        if (plugin.permissions().unsetPermission(uuid, args[1])) {
            Text.ok(sender, "Removed <white>" + Text.escape(args[1]) + "</white> from <white>"
                    + Text.escape(PermUtil.displayName(uuid)) + "</white>.");
        } else {
            Text.error(sender, "No such grant: <white>" + Text.escape(args[1]) + "</white>.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
