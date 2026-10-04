package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Locs;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /point <player|x y z> — set your compass target and remember it. */
public final class PointCommand extends TeleportCommand {
    public PointCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "point";
    }

    @Override
    public String description() {
        return "Point your compass at a player or coordinates.";
    }

    @Override
    public String usage() {
        return "/point <player|x y z>";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = requirePlayer(sender);
        Location target;
        if (args.length == 1) {
            Player found = Players.find(sender, args[0]);
            if (found == null) {
                return;
            }
            target = found.getLocation();
        } else if (args.length == 3) {
            double x;
            double y;
            double z;
            try {
                x = Double.parseDouble(args[0].toLowerCase(Locale.ROOT));
                y = Double.parseDouble(args[1].toLowerCase(Locale.ROOT));
                z = Double.parseDouble(args[2].toLowerCase(Locale.ROOT));
            } catch (NumberFormatException exception) {
                throw fail("Coordinates must be numbers.");
            }
            target = new Location(player.getWorld(), x, y, z);
        } else {
            Text.usage(sender, usage());
            return;
        }
        player.setCompassTarget(target);
        plugin.users().get(player).setLocation("compass-target", target);
        plugin.users().save(player.getUniqueId());
        Text.ok(sender, "Compass now points to <white>" + Text.escape(Locs.pretty(target)) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
