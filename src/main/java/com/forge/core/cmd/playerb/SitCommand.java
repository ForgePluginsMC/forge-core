package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/** /sit — sit on an invisible armor-stand seat; move, teleport or take damage to stand. */
public final class SitCommand extends ForgeCommand {
    public SitCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "sit";
    }

    @Override
    public String description() {
        return "Sit down; move or run /sit again to stand up.";
    }

    @Override
    public String usage() {
        return "/sit";
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
        if (PlayerBState.seats.containsKey(player.getUniqueId())) {
            standUp(player, true);
            return;
        }
        if (player.isInsideVehicle()) {
            Text.error(sender, "You are already riding something.");
            return;
        }
        ArmorStand seat = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
        seat.setVisible(false);
        seat.setMarker(true);
        seat.setSmall(true);
        seat.setGravity(false);
        seat.setInvulnerable(true);
        seat.setSilent(true);
        seat.setBasePlate(false);
        seat.setCollidable(false);
        seat.addPassenger(player);
        PlayerBState.seats.put(player.getUniqueId(), seat);
        Text.ok(sender, "Sitting. Move, take damage or run <white>/sit</white> again to stand.");
    }

    /** Remove a player's seat; silent when they were not sitting. */
    static void standUp(Player player, boolean tell) {
        ArmorStand seat = PlayerBState.seats.remove(player.getUniqueId());
        if (seat == null) {
            return;
        }
        seat.remove();
        if (tell) {
            Text.send(player, "You stand up.");
        }
    }
}
