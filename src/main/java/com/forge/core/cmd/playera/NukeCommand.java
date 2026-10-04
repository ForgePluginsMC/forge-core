package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/** Drop a nuke: big explosion plus a lightning storm at a target. */
public final class NukeCommand extends PlayerACommand {
    public NukeCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "nuke";
    }

    @Override
    public String description() {
        return "Drop a nuke on a player or where you look.";
    }

    @Override
    public String usage() {
        return "/nuke [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        Location target;
        String targetName;
        if (args.length == 0) {
            var block = player.getTargetBlockExact(100);
            if (block == null) {
                Text.error(sender, "No block in sight within 100 blocks.");
                return;
            }
            target = block.getLocation();
            targetName = "your target";
        } else {
            Player found = Players.find(sender, args[0]);
            if (found == null) {
                return;
            }
            target = found.getLocation();
            targetName = found.getName();
        }
        target.getWorld().createExplosion(target, 8.0f, true, true);
        for (int i = 0; i < 6; i++) {
            Location strike = target.clone().add(
                    (Math.random() - 0.5) * 12, 0, (Math.random() - 0.5) * 12);
            target.getWorld().strikeLightning(strike);
        }
        Text.ok(sender, "Nuked <white>" + Text.escape(targetName) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return playerNames(args);
    }
}
