package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /jump — teleport to the block you are looking at (up to 200 blocks). */
public final class JumpCommand extends TeleportCommand {
    public JumpCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "jump";
    }

    @Override
    public java.util.List<String> aliases() {
        return java.util.List.of("j");
    }

    @Override
    public String description() {
        return "Teleport to the block you are looking at.";
    }

    @Override
    public String usage() {
        return "/jump";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = requirePlayer(sender);
        Block target = player.getTargetBlockExact(200);
        if (target == null) {
            throw fail("No block in sight within 200 blocks.");
        }
        org.bukkit.Location location = target.getLocation().add(0.5, 1.0, 0.5);
        location.setYaw(player.getLocation().getYaw());
        location.setPitch(player.getLocation().getPitch());
        teleport(player, location, "<gray>Jumped.");
    }
}
