package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /weather — set world weather: clear, rain or thunder. */
public final class WeatherCommand extends ForgeCommand {
    private static final List<String> PRESETS = List.of("clear", "rain", "thunder");

    public WeatherCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "weather";
    }

    @Override
    public String description() {
        return "Set the weather of a world.";
    }

    @Override
    public String usage() {
        return "/weather <clear|rain|thunder> [world]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1 || args.length > 2) {
            Text.usage(sender, usage());
            return;
        }
        World world;
        if (args.length == 2) {
            world = Bukkit.getWorld(args[1]);
            if (world == null) {
                Text.error(sender, "World <white>" + Text.escape(args[1]) + "</white> not found.");
                return;
            }
        } else {
            Player player = asPlayer(sender);
            if (player == null) {
                Text.error(sender, "From console, specify a world: " + usage());
                return;
            }
            world = player.getWorld();
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "clear", "sun", "sunny" -> {
                world.setStorm(false);
                world.setThundering(false);
                world.setWeatherDuration(24000);
            }
            case "rain", "storm" -> {
                world.setStorm(true);
                world.setThundering(false);
                world.setWeatherDuration(24000);
            }
            case "thunder", "thunderstorm", "lightning" -> {
                world.setStorm(true);
                world.setThundering(true);
                world.setWeatherDuration(24000);
            }
            default -> {
                Text.error(sender, "Unknown weather. " + usage());
                return;
            }
        }
        Text.ok(sender, "Weather in <white>" + Text.escape(world.getName()) + "</white> set to <white>"
                + Text.escape(args[0].toLowerCase(Locale.ROOT)) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(PRESETS, args);
        }
        if (args.length == 2) {
            List<String> worlds = new ArrayList<>();
            for (World world : Bukkit.getWorlds()) {
                worlds.add(world.getName());
            }
            return Players.filter(worlds, args);
        }
        return List.of();
    }
}
