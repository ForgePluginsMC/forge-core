package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Locs;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /pos [player] — show coordinates. */
public final class PosCommand extends TeleportCommand {
    public PosCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "pos";
    }

    @Override
    public List<String> aliases() {
        return List.of("getpos", "coords");
    }

    @Override
    public String description() {
        return "Show your coordinates (or another player's).";
    }

    @Override
    public String usage() {
        return "/pos [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player target;
        if (args.length == 0) {
            target = requirePlayer(sender);
        } else {
            Player found = Players.find(sender, args[0]);
            if (found == null) {
                return;
            }
            target = found;
        }
        Text.send(sender, "<gray>" + Text.escape(target.getName()) + ": <white>"
                + Text.escape(Locs.pretty(target.getLocation())));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
