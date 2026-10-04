package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Locs;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /compass — no argument: report the stored compass target (set by /point, else
 * world spawn). With a player: track them — the compass points at them and an
 * action bar shows live distance.
 */
public final class CompassCommand extends ForgeCommand {
    public CompassCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "compass";
    }

    @Override
    public String description() {
        return "Show your compass target, or track a player with it.";
    }

    @Override
    public String usage() {
        return "/compass [player]";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return;
        }
        if (args.length == 0) {
            Location stored = plugin.users().get(player).getLocation("compass-target");
            if (stored != null) {
                Text.send(sender, "Compass target: <white>" + Text.escape(Locs.pretty(stored)) + "</white>.");
            } else {
                Location spawn = player.getWorld().getSpawnLocation();
                Text.send(sender, "Compass target: <white>spawn</white> ("
                        + Text.escape(Locs.pretty(spawn)) + ").");
            }
            java.util.UUID trackedId = PlayerBState.compassTracking.get(player.getUniqueId());
            if (trackedId != null) {
                Player tracked = plugin.getServer().getPlayer(trackedId);
                Text.send(sender, "<gray>Currently tracking "
                        + (tracked == null ? "?" : Text.escape(tracked.getName())) + ".</gray>");
            }
            return;
        }
        if (args.length != 1) {
            Text.usage(sender, usage());
            return;
        }
        Player target = Players.find(sender, args[0]);
        if (target == null) {
            return;
        }
        if (target.getUniqueId().equals(player.getUniqueId())) {
            Text.error(sender, "You cannot track yourself.");
            return;
        }
        PlayerBState.compassTracking.put(player.getUniqueId(), target.getUniqueId());
        player.setCompassTarget(target.getLocation());
        Text.ok(sender, "Tracking <white>" + Text.escape(target.getName())
                + "</white> — watch the action bar for distance.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
