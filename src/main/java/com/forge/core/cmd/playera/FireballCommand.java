package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;

/** Launch a fireball from your position. */
public final class FireballCommand extends PlayerACommand {
    public FireballCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "fireball";
    }

    @Override
    public String description() {
        return "Launch a fireball.";
    }

    @Override
    public String usage() {
        return "/fireball";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        player.launchProjectile(Fireball.class);
        Text.ok(sender, "Fireball launched.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
