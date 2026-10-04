package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/** /ride — mount the entity you are looking at (within 5 blocks); run again to dismount. */
public final class RideCommand extends ForgeCommand {
    public RideCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "ride";
    }

    @Override
    public String description() {
        return "Ride the entity you are looking at; run again to dismount.";
    }

    @Override
    public String usage() {
        return "/ride";
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
        if (player.isInsideVehicle()) {
            player.leaveVehicle();
            Text.ok(sender, "Dismounted.");
            return;
        }
        Entity target = player.getTargetEntity(5);
        if (target == null) {
            Text.error(sender, "Look at an entity within 5 blocks.");
            return;
        }
        if (!target.addPassenger(player)) {
            Text.error(sender, "You cannot ride that.");
            return;
        }
        Text.ok(sender, "Riding <white>" + Text.escape(target.getType().name().toLowerCase().replace('_', ' '))
                + "</white>. Run <white>/ride</white> again to dismount.");
    }
}
