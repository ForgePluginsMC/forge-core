package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /tpa <player> — request to teleport to another player. */
public final class TpaCommand extends TeleportCommand {
    public TpaCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "tpa";
    }

    @Override
    public String description() {
        return "Request to teleport to another player.";
    }

    @Override
    public String usage() {
        return "/tpa <player>";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player from = requirePlayer(sender);
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        Player to = Players.find(sender, args[0]);
        if (to == null) {
            return;
        }
        if (from.equals(to)) {
            throw fail("You cannot send a teleport request to yourself.");
        }
        if (!plugin.tpa().request(from, to, false)) {
            throw fail("<white>" + Text.escape(to.getName()) + "</white> is not accepting teleport requests.");
        }
        Text.send(from, "<gray>Teleport request sent to <white>" + Text.escape(to.getName()) + "</white>.");
        Text.send(to, "<gold><white>" + Text.escape(from.getName()) + "</white> wants to teleport to you. "
                + "<gray>Type <white>/tpaccept</white> to accept or <white>/tpdeny</white> to deny.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
