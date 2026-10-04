package com.forge.core.cmd.systemsb;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/** Show or set a world's view distance (2-32 chunks). */
public final class ViewrangeCommand extends ForgeCommand {
    public ViewrangeCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "viewrange";
    }

    @Override
    public String description() {
        return "Show or set a world's view distance.";
    }

    @Override
    public String usage() {
        return "/viewrange [range] [world]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        World world;
        if (args.length >= 2) {
            world = Bukkit.getWorld(args[1]);
            if (world == null) {
                throw new CommandRegistry.CommandFailure("Unknown world: " + Text.escape(args[1]) + ".");
            }
        } else {
            Player player = asPlayer(sender);
            if (player == null) {
                Text.usage(sender, usage());
                return;
            }
            world = player.getWorld();
        }
        if (args.length == 0) {
            Text.send(sender, "View distance of <white>" + Text.escape(world.getName())
                    + "</white>: <white>" + world.getViewDistance() + "</white> chunks.");
            return;
        }
        int range;
        try {
            range = Integer.parseInt(args[0]);
        } catch (NumberFormatException exception) {
            throw new CommandRegistry.CommandFailure("Range must be a number.");
        }
        if (range < 2 || range > 32) {
            throw new CommandRegistry.CommandFailure("Range must be between 2 and 32.");
        }
        world.setViewDistance(range);
        Text.ok(sender, "View distance of <white>" + Text.escape(world.getName())
                + "</white> set to <white>" + range + "</white> chunks.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            List<String> names = new java.util.ArrayList<>();
            for (World world : Bukkit.getWorlds()) {
                names.add(world.getName());
            }
            return Players.filter(names, args);
        }
        return List.of();
    }
}
