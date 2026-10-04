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

/** /time — set world time: day, noon, night, midnight or raw ticks. */
public final class TimeCommand extends ForgeCommand {
    private static final List<String> PRESETS = List.of("day", "noon", "night", "midnight");

    public TimeCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "time";
    }

    @Override
    public String description() {
        return "Set the time of a world.";
    }

    @Override
    public String usage() {
        return "/time <day|noon|night|midnight|ticks> [world]";
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
        long ticks;
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "day" -> ticks = 1000L;
            case "noon" -> ticks = 6000L;
            case "night" -> ticks = 13000L;
            case "midnight" -> ticks = 18000L;
            default -> {
                try {
                    ticks = Long.parseLong(args[0]);
                } catch (NumberFormatException bad) {
                    Text.error(sender, "Unknown time preset. " + usage());
                    return;
                }
            }
        }
        world.setTime(ticks);
        Text.ok(sender, "Time in <white>" + Text.escape(world.getName()) + "</white> set to <white>"
                + ticks + "</white> ticks.");
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
