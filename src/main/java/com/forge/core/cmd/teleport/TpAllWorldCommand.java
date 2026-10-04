package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /tpallworld <world> — teleport every online player to a world's spawn. */
public final class TpAllWorldCommand extends TeleportCommand {
    public TpAllWorldCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "tpallworld";
    }

    @Override
    public String description() {
        return "Teleport every online player to a world's spawn.";
    }

    @Override
    public String usage() {
        return "/tpallworld <world>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        World world = Bukkit.getWorld(args[0]);
        if (world == null) {
            throw fail("World <white>" + Text.escape(args[0]) + "</white> does not exist.");
        }
        Location spawn = world.getSpawnLocation();
        int moved = 0;
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            teleport(player, spawn,
                    "<gray>You were teleported to the spawn of <white>" + Text.escape(world.getName()) + "</white>.");
            moved++;
        }
        Text.ok(sender, "Teleported <white>" + moved + "</white> player" + (moved == 1 ? "" : "s")
                + " to <white>" + Text.escape(world.getName()) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> worlds = new ArrayList<>();
            for (World world : Bukkit.getWorlds()) {
                worlds.add(world.getName());
            }
            return com.forge.core.util.Players.filter(worlds, args);
        }
        return List.of();
    }
}
