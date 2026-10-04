package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /spawn [player] — teleport to the world spawn. */
public final class SpawnCommand extends TeleportCommand {
    public SpawnCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "spawn";
    }

    @Override
    public String description() {
        return "Teleport to the world spawn.";
    }

    @Override
    public String usage() {
        return "/spawn [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player target;
        if (args.length == 0) {
            target = requirePlayer(sender);
        } else {
            if (!sender.hasPermission("forgecore.spawn.others")) {
                throw fail("You don't have permission to teleport others to spawn.");
            }
            Player found = Players.find(sender, args[0]);
            if (found == null) {
                return;
            }
            target = found;
        }
        Location spawn = target.getWorld().getSpawnLocation();
        teleport(target, spawn, "<gray>Teleported to spawn.");
        if (!target.equals(sender)) {
            Text.send(sender, "<gray>Teleported <white>" + Text.escape(target.getName()) + "</white> to spawn.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission("forgecore.spawn.others")) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
