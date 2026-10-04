package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import com.forge.core.vanish.VanishApi;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /near [radius] — list nearby players with distances. */
public final class NearCommand extends TeleportCommand {
    public NearCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "near";
    }

    @Override
    public String description() {
        return "List players near you with distances.";
    }

    @Override
    public String usage() {
        return "/near [radius]";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = requirePlayer(sender);
        double radius = 100;
        if (args.length > 0) {
            try {
                radius = Double.parseDouble(args[0]);
            } catch (NumberFormatException exception) {
                throw fail("Radius must be a number.");
            }
            if (radius <= 0) {
                throw fail("Radius must be positive.");
            }
        }
        boolean seeVanished = sender.hasPermission("forgecore.vanish.see");
        List<Nearby> nearby = new ArrayList<>();
        for (Player other : plugin.getServer().getOnlinePlayers()) {
            if (other.equals(player) || !other.getWorld().equals(player.getWorld())) {
                continue;
            }
            if (VanishApi.isVanished(other) && !seeVanished) {
                continue;
            }
            double distance = player.getLocation().distance(other.getLocation());
            if (distance <= radius) {
                nearby.add(new Nearby(other.getName(), distance));
            }
        }
        nearby.sort(Comparator.comparingDouble(Nearby::distance));
        if (nearby.isEmpty()) {
            Text.send(sender, "<gray>No players within <white>" + radius + "</white> blocks.");
            return;
        }
        List<String> parts = new ArrayList<>();
        for (Nearby entry : nearby) {
            parts.add("<white>" + Text.escape(entry.name()) + "</white> <gray>("
                    + Math.round(entry.distance()) + "m)");
        }
        Text.send(sender, "<gray>Nearby players (" + nearby.size() + "): " + String.join("<gray>, ", parts));
    }

    private record Nearby(String name, double distance) {
    }
}
