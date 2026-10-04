package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /tp <player> [target] — teleport to a player, or move one player to another. */
public final class TpCommand extends TeleportCommand {
    public TpCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "tp";
    }

    @Override
    public List<String> aliases() {
        return List.of("teleport");
    }

    @Override
    public String description() {
        return "Teleport to a player, or teleport one player to another.";
    }

    @Override
    public String usage() {
        return "/tp <player> [target]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        Player first;
        Player second;
        if (args.length == 1) {
            first = requirePlayer(sender);
            Player found = Players.find(sender, args[0]);
            if (found == null) {
                return;
            }
            second = found;
        } else {
            if (!sender.hasPermission("forgecore.tp.others")) {
                throw fail("You don't have permission to teleport other players.");
            }
            Player from = Players.find(sender, args[0]);
            Player to = Players.find(sender, args[1]);
            if (from == null || to == null) {
                return;
            }
            first = from;
            second = to;
        }
        if (first.equals(second)) {
            throw fail("You cannot teleport someone to themselves.");
        }
        teleport(first, second.getLocation(), "<gray>Teleported to <white>" + Text.escape(second.getName()) + "</white>.");
        if (!first.equals(sender)) {
            Text.send(sender, "<gray>Teleported <white>" + Text.escape(first.getName()) + "</white> to <white>"
                    + Text.escape(second.getName()) + "</white>.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 2) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
