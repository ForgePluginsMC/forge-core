package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /servertime — show the world clock (ticks + formatted) and the real server time. */
public final class ServertimeCommand extends ForgeCommand {
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");

    public ServertimeCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "servertime";
    }

    @Override
    public String description() {
        return "Show the world time and the real server time.";
    }

    @Override
    public String usage() {
        return "/servertime";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        World world;
        Player player = asPlayer(sender);
        if (player != null) {
            world = player.getWorld();
        } else if (!Bukkit.getWorlds().isEmpty()) {
            world = Bukkit.getWorlds().get(0);
        } else {
            Text.error(sender, "No worlds loaded.");
            return;
        }
        long ticks = world.getTime();
        long hours = (ticks / 1000 + 6) % 24;
        long minutes = (ticks % 1000) * 60 / 1000;
        String gameClock = String.format("%02d:%02d", hours, minutes);
        String realClock = LocalTime.now().format(CLOCK);
        Text.send(sender, "World <white>" + Text.escape(world.getName()) + "</white>: <white>" + gameClock
                + "</white> <gray>(" + ticks + " ticks)</gray> — real time <white>" + realClock + "</white>.");
    }
}
