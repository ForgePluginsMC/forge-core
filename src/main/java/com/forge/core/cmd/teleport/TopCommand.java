package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /top — teleport to the highest safe block above your position. */
public final class TopCommand extends TeleportCommand {
    public TopCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "top";
    }

    @Override
    public String description() {
        return "Teleport to the highest block at your position.";
    }

    @Override
    public String usage() {
        return "/top";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = requirePlayer(sender);
        Location current = player.getLocation();
        World world = player.getWorld();
        int x = current.getBlockX();
        int z = current.getBlockZ();
        int y = world.getHighestBlockYAt(x, z);
        Location top = new Location(world, x + 0.5, y + 1, z + 0.5, current.getYaw(), current.getPitch());
        teleport(player, top, "<gray>Teleported to the top.");
    }
}
