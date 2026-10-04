package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/** /launch [player] — launch into the air. */
public final class LaunchCommand extends TeleportCommand {
    public LaunchCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "launch";
    }

    @Override
    public String description() {
        return "Launch yourself (or another player) into the air.";
    }

    @Override
    public String usage() {
        return "/launch [player]";
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
        Vector forward = target.getLocation().getDirection().setY(0).normalize().multiply(1.2);
        target.setFallDistance(0);
        target.setVelocity(new Vector(0, 2.5, 0).add(forward));
        plugin.afk().setActive(target);
        Text.send(target, "<gray>Wheee!");
        if (!target.equals(sender)) {
            Text.send(sender, "<gray>Launched <white>" + Text.escape(target.getName()) + "</white>.");
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
